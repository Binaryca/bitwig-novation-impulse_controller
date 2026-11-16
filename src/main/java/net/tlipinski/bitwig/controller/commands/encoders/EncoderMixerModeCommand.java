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
        return statusByte == Midi.CH2_CC
                && data1 == Midi.ENCODER_MIXER_MODE
                && Midi.isPressed(data2);
    }

    @Override
    public void handle(int data1, int data2) {
        controller.changeEncoderMode(EncoderMode.MIXER);
        sysexSend.displayText("Mixer");
    }

    private final Controller controller;
    private final SysexSend sysexSend;
}
