package net.tlipinski.bitwig.controller.commands.encoders;

import net.tlipinski.bitwig.controller.Controller;
import net.tlipinski.bitwig.controller.EncoderMode;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiCommand;
import net.tlipinski.bitwig.controller.SysexSend;

public class EncoderMixerPageUpCommand implements MidiCommand {

    public EncoderMixerPageUpCommand(Controller controller, SysexSend sysexSend) {
        this.controller = controller;
        this.sysexSend = sysexSend;
    }

    @Override
    public boolean triggersWhen(int statusByte, int data1, int data2) {
        return controller.getEncoderMode() == EncoderMode.MIXER
                && statusByte == Midi.CH2_CC
                && data1 == Midi.ENCODER_PAGE_UP
                && Midi.isPressed(data2);
    }

    @Override
    public void handle(int data1, int data2) {
        controller.getEncoderMixerPage().selectPreviousPage(false);
        sysexSend.displayText(controller.getEncoderMixerPage().name());
    }

    private final Controller controller;
    private final SysexSend sysexSend;
}
