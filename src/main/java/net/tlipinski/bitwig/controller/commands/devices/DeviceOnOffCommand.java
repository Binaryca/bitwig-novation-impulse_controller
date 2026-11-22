package net.tlipinski.bitwig.controller.commands.devices;

import com.bitwig.extension.controller.api.Device;
import com.bitwig.extension.controller.api.DeviceBank;
import net.tlipinski.bitwig.controller.Controller;
import net.tlipinski.bitwig.controller.EncoderMode;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiCommand;
import net.tlipinski.bitwig.controller.SysexSend;

/**
 * In the plugin encoder mode the track buttons (CC 9-16) toggle the enabled
 * state of the first eight devices of the cursor track chain; Shift + button
 * selects the device (the official Nektar scripts use their instrument mode
 * the same way). The master button (CC 17) has no device and is swallowed,
 * so it cannot mute the master track by surprise in this mode.
 *
 * <p>Toggling uses the {@code isEnabled().toggle()} value API: the classic
 * {@code toggleEnabledState()} method is deprecated since API version 2 and
 * throws with the required API 25.</p>
 *
 * <p>The device name for the display is announced through value observers
 * because the API values update asynchronously after the action. The
 * enabled state is visible on the button LED (see DeviceOnOffObserver).</p>
 */
public class DeviceOnOffCommand implements MidiCommand {

    public DeviceOnOffCommand(Controller controller, SysexSend sysexSend) {
        this.controller = controller;
        this.sysexSend = sysexSend;

        // A selection changes the name of the cursor device: announce it.
        controller.getTracks().getCursorDevice().name().addValueObserver((String name) -> {
            if (announceSelection) {
                announceSelection = false;
                sysexSend.displayText(name);
            }
        });

        // A toggle changes the enabled state of the pressed bank device.
        DeviceBank bank = controller.getTracks().getDeviceBank();
        for (int i = 0; i < Midi.BUTTON_LAST - Midi.BUTTON_FIRST; i++) {
            Device device = bank.getDevice(i);
            device.isEnabled().addValueObserver((boolean enabled) -> {
                if (announceToggle) {
                    announceToggle = false;
                    sysexSend.displayText(device.name().get());
                }
            });
        }
    }

    @Override
    public boolean triggersWhen(int statusByte, int data1, int data2) {
        // The buttons send 127 on press and 0 on release; only the press may
        // act (the release would otherwise toggle or select a second time).
        // The master button (CC 17) is not included: in the plugin mode it
        // toggles the master fader target instead (MasterFaderToggleCommand).
        return controller.getEncoderMode() == EncoderMode.PLUGIN
                && statusByte == Midi.CH1_CC
                && data1 >= Midi.BUTTON_FIRST
                && data1 < Midi.BUTTON_LAST
                && Midi.isPressed(data2);
    }

    @Override
    public void handle(int data1, int data2) {
        int index = deviceIndex(data1);
        if (index < 0) {
            return;
        }

        Device device = controller.getTracks().getDeviceBank().getDevice(index);
        if (!device.exists().get()) {
            sysexSend.displayText("Empty");
            return;
        }

        if (controller.isShiftPressed()) {
            announceSelection = true;
            controller.getTracks().getCursorDevice().selectDevice(device);
        } else {
            announceToggle = true;
            // toggleEnabledState() is deprecated since API version 2 and
            // throws with the required API 25, so the value API is used (the
            // same reason the metronome uses isMetronomeEnabled().toggle()).
            device.isEnabled().toggle();
        }
    }

    /**
     * Maps a track button CC number to a device index in the bank: buttons
     * CC 9-16 are devices 0-7, the master button (CC 17) has no device.
     */
    static int deviceIndex(int data1) {
        int index = data1 - Midi.BUTTON_FIRST;
        if (index >= 0 && index < Midi.BUTTON_LAST - Midi.BUTTON_FIRST) {
            return index;
        }
        return -1;
    }

    private final Controller controller;
    private final SysexSend sysexSend;
    private boolean announceSelection = false;
    private boolean announceToggle = false;
}
