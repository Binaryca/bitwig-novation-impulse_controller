package net.tlipinski.bitwig.controller.commands.mixer;

import net.tlipinski.bitwig.controller.Controller;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiCommand;

public class TrackBankDownCommand implements MidiCommand {

    public TrackBankDownCommand(Controller controller) {
        this.controller = controller;
    }

    @Override
    public boolean triggersWhen(int statusByte, int data1, int data2) {
        return statusByte == Midi.CH1_CC
                && data1 == Midi.TRACK_BANK_DOWN
                && Midi.isPressed(data2);
    }

    @Override
    public void handle(int data1, int data2) {
        controller.getTracks().getTrackBank().scrollPageForwards();
    }

    private final Controller controller;
}
