package net.tlipinski.bitwig.controller.commands;

import com.bitwig.extension.controller.api.CursorBrowserResultItem;
import com.bitwig.extension.controller.api.CursorDevice;
import com.bitwig.extension.controller.api.PopupBrowser;
import net.tlipinski.bitwig.controller.Controller;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiCommand;

/**
 * The data knob transmits Program Change messages on channel 1 while the
 * Program Change mode (Shift + MIDI Chan) is active on the Impulse.
 *
 * <p>The knob drives the modern popup browser; the deprecated browsing
 * sessions and the old preset API (Device.loadPreset, switchToNextPreset,
 * ...) are not available with the required API version 25. The browser is
 * opened at the cursor device's replace insertion point (which opens the
 * preset selection for that device), the preset content type is selected -
 * the "Plug-in Presets" tab for VST/AU/CLAP devices (the indexed preset
 * FILES of the plug-in; the presets stored inside the plug-in's own GUI
 * are not reachable through Bitwig) and the Bitwig presets for native
 * devices - and the highlighted preset is loaded with
 * {@code commit()}.</p>
 *
 * <p>The pending knob steps are drained with the browser's own file
 * selection actions ({@code selectNextFile()}/{@code selectPreviousFile()}).
 * These move the visible highlight directly and the browser clamps them to
 * the results list at its boundaries (verified on hardware); the results
 * cursor of the API does not move the visible highlight, the keyboard arrow
 * keys leave the results list at its boundaries, and the hasNext/
 * hasPrevious values of the results cursor are dead in the new browser.
 * The selection actions are only
 * sent when {@code browser.exists()} reports an open browser, after a short
 * settle delay, and while no commit/cancel is closing the browser (the
 * exists state lags behind, so leftover steps sent during closing would
 * reach the main window).</p>
 *
 * <p>The preset name for the display is read from the results column cursor
 * item, which trails the visible selection by a step or two and is
 * therefore shown through a value observer when it settles.</p>
 *
 * <p>The browser auditions every preset the selection moves to; during a
 * fast sweep that loads device state per step and grows the Bitwig JVM
 * heap, which crashed once with an out-of-memory error when the operating
 * system could not commit the additional memory. So the audition is
 * disabled while the knob drives the browser and restored afterwards.</p>
 *
 * <p>{@code scheduleTask} calls made from a MIDI callback never fire, so a
 * self-rescheduling ticker started at init performs the open/move/commit
 * steps. Turning the knob while no browser is open arms an open request;
 * the request is fulfilled by exactly one {@code browse()} call and is
 * cleared by the exists() observer when the browser actually opens (a
 * second browse() call while the browser is still opening makes Bitwig
 * close and reopen it, which showed up as the browser flashing open and
 * closed). If the open is not confirmed within a timeout, it is retried up
 * to three times.</p>
 */
public class DataKnobCommand implements MidiCommand {

    public DataKnobCommand(Controller controller) {
        this.controller = controller;

        this.browser = controller.getHost().createPopupBrowser();
        this.cursorDevice = controller.getTracks().getCursorDevice();

        // exists() reports whether the popup browser is currently open. The
        // observer owns the lifecycle of the open request: a confirmed open
        // clears it (instead of the ticker, which would leave a window for a
        // second browse() call to race in while the browser is still
        // opening), and a confirmed close resets the cycle.
        browser.exists().addValueObserver((boolean open) -> {
            this.browsing = open;
            this.closing = false;
            this.browseSentAt = 0;
            if (open) {
                this.pendingOpen = false;
                this.browseAttempts = 0;
            } else {
                this.contentTypeSet = false;
                this.contentTypeAttempts = 0;
                this.ready = false;
                this.openedAt = 0;
                if (this.auditionDisabled) {
                    browser.shouldAudition().set(this.auditionWasOn);
                    this.auditionDisabled = false;
                }
            }
        });

        browser.shouldAudition().markInterested();

        this.cursorResult = (CursorBrowserResultItem) browser.resultsColumn().createCursorItem();
        cursorResult.name().markInterested();
        // The API cursor of the results column trails the visible selection
        // by one or two steps right after the selection steps are sent, so
        // the display is updated when the name value settles instead of
        // right after the steps.
        cursorResult.name().addValueObserver((String name) -> {
            if (browsing && !closing) {
                controller.getSysexSend().displayText(name);
            }
        });

        browser.selectedContentTypeIndex().markInterested();
        browser.contentTypeNames().markInterested();
        cursorDevice.deviceType().markInterested();

        controller.getHost().scheduleTask(this::tick, TICK_MS);
    }

    @Override
    public boolean triggersWhen(int statusByte, int data1, int data2) {
        return statusByte == Midi.CH1_PC;
    }

    @Override
    public void handle(int data1, int data2) {
        if (!browsing && !pendingOpen) {
            pendingOpen = true;
        }

        int change;
        if (previous < 0) {
            // The first program number only teaches the knob position; the
            // delta against the -1 sentinel would be one step too many (and
            // zero for 127 because of the wrap).
            previous = data1;
            change = 0;
        } else {
            change = delta(previous, data1);
            previous = data1;
        }
        if (change != 0) {
            pendingSteps = Math.max(-64, Math.min(pendingSteps + change, 64));
        }
        lastPcAt = System.currentTimeMillis();
    }

    private void tick() {
        try {
            long now = System.currentTimeMillis();

            if (pendingOpen) {
                if (browseSentAt == 0) {
                    if (browseAttempts < MAX_BROWSE_ATTEMPTS) {
                        cursorDevice.replaceDeviceInsertionPoint().browse();
                        browseSentAt = now;
                        browseAttempts++;
                        contentTypeSet = false;
                        contentTypeAttempts = 0;
                        // The browser previews every preset the selection
                        // moves to; during a fast sweep that loads device
                        // state per step and grows the Bitwig JVM heap,
                        // which crashed once with an out-of-memory error. So
                        // the audition is disabled while the knob drives the
                        // browser.
                        auditionWasOn = browser.shouldAudition().get();
                        auditionDisabled = true;
                        browser.shouldAudition().set(false);
                    } else {
                        // The browser did not open after several attempts;
                        // give up so the retries do not keep firing. The
                        // next turn of the knob starts a new attempt cycle.
                        pendingOpen = false;
                        browseAttempts = 0;
                    }
                } else if (now - browseSentAt > OPEN_TIMEOUT_MS) {
                    // The open was not confirmed in time; try again.
                    browseSentAt = 0;
                }
            }

            if (browsing) {
                if (openedAt == 0) {
                    openedAt = now;
                }

                if (!contentTypeSet) {
                    contentTypeSet = selectPresetContentType();
                }

                // The browser is considered settled after a short delay; the
                // selection steps sent earlier would be swallowed.
                if (!ready && now - openedAt >= READY_DELAY_MS) {
                    ready = true;
                }

                // Drain pending steps through the browser's own file
                // selection; only while the browser is really open and not in
                // the middle of closing (commit/cancel) or reopening.
                if (!pendingOpen && !closing && ready && pendingSteps != 0) {
                    int maxSteps = Math.abs(pendingSteps) >= FAST_BACKLOG ? 2 : 1;
                    int steps = Math.min(Math.abs(pendingSteps), maxSteps)
                            * (int) Math.signum(pendingSteps);
                    for (int i = 0; i < Math.abs(steps); i++) {
                        if (steps > 0) {
                            browser.selectNextFile();
                        } else {
                            browser.selectPreviousFile();
                        }
                    }
                    pendingSteps -= steps;
                    lastPcAt = now;
                }
            }

            // Load the highlighted preset after the knob has been idle; never
            // commit while a close/reopen cycle is in progress.
            if (browsing && !pendingOpen && !closing && ready
                    && pendingSteps == 0 && lastPcAt > 0
                    && now - lastPcAt >= COMMIT_DELAY_MS) {
                browser.commit();
                // The browser closes asynchronously; suppress selection steps
                // until exists() reports the closed state, otherwise leftover
                // steps would reach the main window.
                closing = true;
                lastPcAt = 0;
            }
        } finally {
            controller.getHost().scheduleTask(this::tick, TICK_MS);
        }
    }

    /**
     * Selects the preset content type in the popup browser when it is not
     * already selected; returns true when done (or when the content types
     * are not reported, in which case the type the insertion point opened
     * with is kept), false when the change should be retried.
     */
    private boolean selectPresetContentType() {
        String[] names = browser.contentTypeNames().get();
        int target = targetIndex(names, isPluginDevice());
        if (target < 0) {
            // Content type names may not be reported in newer Bitwig
            // versions; keep the type the insertion point opened with.
            return true;
        }
        if (browser.selectedContentTypeIndex().get() == target) {
            return true;
        }
        browser.selectedContentTypeIndex().set(target);
        return ++contentTypeAttempts >= 5;
    }

    /**
     * Whether the cursor device is a plug-in (VST, AU or CLAP): those browse
     * the plug-in preset files when the browser offers the tab.
     */
    private boolean isPluginDevice() {
        String type = cursorDevice.deviceType().get().toLowerCase();
        boolean plugin = type.startsWith("vst") || type.startsWith("au")
                || type.startsWith("clap") || type.startsWith("plugin");
        // Diagnostic: remove after the content type selection is verified.
        if (controller.getPreferences().debug()) {
            controller.getHost().errorln("DataKnob: type='" + type + "' plugin=" + plugin
                    + " names=" + String.join(" | ", browser.contentTypeNames().get()));
        }
        return plugin;
    }

    /**
     * Resolves the content type index to browse presets in: plug-in devices
     * prefer the "Plug-in Presets" tab (the indexed preset FILES of the
     * plug-in - H2P, FXP, FXB, VSTPRESET -; the presets kept inside the
     * plug-in's own GUI are not reachable through Bitwig at all), other
     * devices browse the Bitwig presets and skip a plug-in tab. Returns -1
     * when nothing matches (the caller then keeps the type the insertion
     * point opened with).
     */
    static int targetIndex(String[] names, boolean pluginDevice) {
        int firstPreset = -1;
        int pluginPreset = -1;
        for (int i = 0; i < names.length; i++) {
            // Normalized so the "Plug-in" naming variant matches too.
            String name = names[i].toLowerCase().replaceAll("[^a-z]", "");
            if (!name.contains("preset")) {
                continue;
            }
            if (firstPreset < 0) {
                firstPreset = i;
            }
            if (pluginPreset < 0 && name.contains("plugin")) {
                pluginPreset = i;
            }
        }
        if (pluginDevice) {
            return pluginPreset >= 0 ? pluginPreset : firstPreset;
        }
        for (int i = 0; i < names.length; i++) {
            String name = names[i].toLowerCase().replaceAll("[^a-z]", "");
            if (name.contains("preset") && !name.contains("plugin")) {
                return i;
            }
        }
        return firstPreset;
    }

    /**
     * Relative change between two program numbers; the program number wraps
     * between 0 and 127, so a jump across the wrap counts as a small step.
     */
    static int delta(int previous, int program) {
        int delta = program - previous;
        if (delta > 64) {
            delta -= 128;
        } else if (delta < -64) {
            delta += 128;
        }
        return delta;
    }

    private long lastPcAt = 0;
    private long openedAt = 0;
    private long browseSentAt = 0;
    private int previous = -1;
    private int pendingSteps = 0;
    private int contentTypeAttempts = 0;
    private int browseAttempts = 0;
    private boolean browsing = false;
    private boolean pendingOpen = false;
    private boolean closing = false;
    private boolean contentTypeSet = false;
    private boolean ready = false;
    private boolean auditionWasOn = false;
    private boolean auditionDisabled = false;

    private static final long TICK_MS = 50;
    private static final long READY_DELAY_MS = 150;
    private static final long COMMIT_DELAY_MS = 1200;
    private static final long OPEN_TIMEOUT_MS = 1500;
    private static final int MAX_BROWSE_ATTEMPTS = 3;
    private static final int FAST_BACKLOG = 6;

    private final Controller controller;
    private final PopupBrowser browser;
    private final CursorDevice cursorDevice;
    private final CursorBrowserResultItem cursorResult;
}
