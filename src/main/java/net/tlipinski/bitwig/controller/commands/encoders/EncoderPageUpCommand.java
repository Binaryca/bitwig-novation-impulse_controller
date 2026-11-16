package net.tlipinski.bitwig.controller.commands.encoders;

import net.tlipinski.bitwig.controller.Controller;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiCommand;

public class EncoderPageUpCommand implements MidiCommand {

    public EncoderPageUpCommand(Controller controller) {
        this.controller = controller;
    }

    @Override
    public boolean triggersWhen(int statusByte, int data1, int data2) {
        return statusByte == Midi.CH2_CC
                && data1 == Midi.ENCODER_PAGE_UP
                && Midi.isPressed(data2);
    }

    @Override
    public void handle(int data1, int data2) {
        controller.getTracks().getCursorRemoteControlsPage().selectPreviousPage(false);
    }

    private final Controller controller;
}
