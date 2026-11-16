package net.tlipinski.bitwig.controller.commands;

import net.tlipinski.bitwig.controller.Controller;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiCommand;

public class ShiftReleasedCommand implements MidiCommand {

    public ShiftReleasedCommand(Controller controller) {
        this.controller = controller;
    }

    @Override
    public boolean triggersWhen(int statusByte, int data1, int data2) {
        return statusByte == Midi.CH2_CC
                && data1 == Midi.SHIFT
                && !Midi.isPressed(data2);
    }

    @Override
    public void handle(int data1, int data2) {
        controller.shiftReleased();
    }

    private final Controller controller;
}
