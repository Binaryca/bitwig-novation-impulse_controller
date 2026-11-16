package net.tlipinski.bitwig.controller.commands.mixer;

import net.tlipinski.bitwig.controller.Controller;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiCommand;

public class TrackBankUpCommand implements MidiCommand {

    public TrackBankUpCommand(Controller controller) {
        this.controller = controller;
    }

    @Override
    public boolean triggersWhen(int statusByte, int data1, int data2) {
        return statusByte == Midi.CH1_CC
                && data1 == Midi.TRACK_BANK_UP
                && Midi.isPressed(data2);
    }

    @Override
    public void handle(int data1, int data2) {
        controller.getTracks().getTrackBank().scrollPageBackwards();
    }

    private final Controller controller;
}
