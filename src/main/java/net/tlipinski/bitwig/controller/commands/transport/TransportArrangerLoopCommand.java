package net.tlipinski.bitwig.controller.commands.transport;

import com.bitwig.extension.controller.api.Transport;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiCommand;

public class TransportArrangerLoopCommand implements MidiCommand {

    public TransportArrangerLoopCommand(Transport transport) {
        this.transport = transport;
    }

    @Override
    public boolean triggersWhen(int statusByte, int data1, int data2) {
        return statusByte == Midi.CH1_CC
                && data1 == Midi.TRANSPORT_LOOP
                && Midi.isPressed(data2);
    }

    @Override
    public void handle(int data1, int data2) {
        transport.isArrangerLoopEnabled().toggle();
    }

    private final Transport transport;
}
