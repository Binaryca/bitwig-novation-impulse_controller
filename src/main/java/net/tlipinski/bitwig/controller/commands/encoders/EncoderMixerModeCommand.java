package net.tlipinski.bitwig.controller.commands.encoders;

import net.tlipinski.bitwig.controller.Controller;
import net.tlipinski.bitwig.controller.EncoderMode;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiCommand;
import net.tlipinski.bitwig.controller.MidiSend;
import net.tlipinski.bitwig.controller.SysexSend;

public class EncoderMixerModeCommand implements MidiCommand {

    public EncoderMixerModeCommand(Controller controller, MidiSend midiSend, SysexSend sysexSend) {
        this.controller = controller;
        this.sysexSend = sysexSend;

        midiSend.resetEncoderMode();
    }

    @Override
    public boolean triggersWhen(int statusByte, int data1, int data2) {
        // The zone mode buttons report a state value (0) instead of a press
        // velocity, so any data2 matches; handle() is idempotent.
        return statusByte == Midi.CH2_CC
                && data1 == Midi.ENCODER_MIXER_MODE;
    }

    @Override
    public void handle(int data1, int data2) {
        controller.changeEncoderMode(EncoderMode.MIXER);
        sysexSend.displayText("Mixer");
    }

    private final Controller controller;
    private final SysexSend sysexSend;
}
