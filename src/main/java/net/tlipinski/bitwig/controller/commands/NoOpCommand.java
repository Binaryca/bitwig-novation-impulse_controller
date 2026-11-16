package net.tlipinski.bitwig.controller.commands;

import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiCommand;

public class NoOpCommand implements MidiCommand {

    @Override
    public boolean triggersWhen(int statusByte, int data1, int data2) {
        return statusByte == Midi.CH1_CC && data1 == Midi.NO_OP;
    }

    @Override
    public void handle(int data1, int data2) {
        // Reserved button; intentionally ignored.
    }
}
