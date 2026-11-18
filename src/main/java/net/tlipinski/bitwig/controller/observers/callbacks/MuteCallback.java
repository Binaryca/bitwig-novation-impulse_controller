package net.tlipinski.bitwig.controller.observers.callbacks;

import com.bitwig.extension.callback.BooleanValueChangedCallback;
import net.tlipinski.bitwig.controller.ButtonsMode;
import net.tlipinski.bitwig.controller.EncoderMode;
import net.tlipinski.bitwig.controller.MidiSend;
import net.tlipinski.bitwig.controller.Controller;

public class MuteCallback implements BooleanValueChangedCallback {
    private final int idx;
    private final Controller controller;
    private final MidiSend midiSend;

    public MuteCallback(int idx, Controller controller, MidiSend midiSend) {
        this.idx = idx;
        this.controller = controller;
        this.midiSend = midiSend;
    }

    @Override
    public void valueChanged(boolean mute) {
        // In the plugin encoder mode the buttons address devices, so their
        // LEDs show the device enabled states (see DeviceOnOffObserver).
        if (controller.getButtonsMode() == ButtonsMode.MUTE
                && controller.getEncoderMode() != EncoderMode.PLUGIN) {
            if (controller.getTracks().isNotEmpty(idx)) {
                midiSend.light(idx, !mute);
            } else {
                midiSend.light(idx, false);
            }
        }
    }
}
