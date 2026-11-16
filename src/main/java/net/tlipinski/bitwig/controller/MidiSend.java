package net.tlipinski.bitwig.controller;

import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.MidiOut;

public class MidiSend {

    public MidiSend(ControllerHost host) {
        this.midiOut = host.getMidiOutPort(0);
    }

    public void light(int index, boolean on) {
        if (on) {
            lightOn(index);
        } else {
            lightOff(index);
        }
    }

    public void clipLight(int index, ClipLight light) {
        midiOut.sendMidi(Midi.CH1_CC, Midi.CLIP_FIRST + index, light.light);
    }

    public void send(int status, int data1, int data2) {
        midiOut.sendMidi(status, data1, data2);
    }

    public void resetEncoderMode() {
        midiOut.sendMidi(Midi.CH2_CC, Midi.ENCODER_PLUGIN_MODE, 1);
    }

    public void resetButtonsMode() {
        // TODO: the hardware does not confirm this state; find the correct
        // feedback message for the mute/solo mode button.
        midiOut.sendMidi(Midi.CH1_CC, Midi.BUTTONS_MODE, 1);
    }

    private void lightOn(int index) {
        midiOut.sendMidi(Midi.CH1_CC, Midi.BUTTON_FIRST + index, 1);
    }

    private void lightOff(int index) {
        midiOut.sendMidi(Midi.CH1_CC, Midi.BUTTON_FIRST + index, 0);
    }

    private final MidiOut midiOut;
}
