package net.tlipinski.bitwig.controller.commands.transport;

import com.bitwig.extension.controller.api.CursorDevice;
import net.tlipinski.bitwig.controller.Controller;
import net.tlipinski.bitwig.controller.EncoderMode;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiCommand;

/**
 * In the plugin encoder mode the Rewind / Forward buttons select the
 * previous / next device in the chain of the cursor track (the official
 * Nektar scripts use their instrument mode the same way). In the open mix
 * panel the buttons are consumed by the scene navigation commands, which
 * are registered earlier, so no additional panel check is needed here.
 *
 * <p>The buttons send 127 on press and 0 on release (and nothing while
 * held), so only presses trigger a step - the release would otherwise
 * step a second time. The device name for the display is announced
 * through a value observer because the API value updates asynchronously
 * after the selection.</p>
 */
public class DeviceNavigateCommand implements MidiCommand {

    public DeviceNavigateCommand(Controller controller) {
        this.controller = controller;
        this.cursorDevice = controller.getTracks().getCursorDevice();
        cursorDevice.name().markInterested();
        cursorDevice.name().addValueObserver((String name) -> {
            if (announce) {
                announce = false;
                controller.getSysexSend().displayText(name);
            }
        });
    }

    @Override
    public boolean triggersWhen(int statusByte, int data1, int data2) {
        // The buttons send 127 on press and 0 on release; only the press may
        // step, otherwise the release would select the next device again.
        return controller.getEncoderMode() == EncoderMode.PLUGIN
                && statusByte == Midi.CH1_CC
                && Midi.isPressed(data2)
                && (data1 == Midi.TRANSPORT_REWIND || data1 == Midi.TRANSPORT_FORWARD);
    }

    @Override
    public void handle(int data1, int data2) {
        announce = true;
        if (data1 == Midi.TRANSPORT_FORWARD) {
            cursorDevice.selectNext();
        } else {
            cursorDevice.selectPrevious();
        }
    }

    private final Controller controller;
    private final CursorDevice cursorDevice;
    private boolean announce = false;
}
