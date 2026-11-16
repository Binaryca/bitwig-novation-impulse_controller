package net.tlipinski.bitwig.controller.commands.transport;

import com.bitwig.extension.controller.api.Transport;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiCommand;

public class TransportForwardCommand implements MidiCommand {

    public TransportForwardCommand(Transport transport) {
        this.transport = transport;
    }

    @Override
    public boolean triggersWhen(int statusByte, int data1, int data2) {
        return statusByte == Midi.CH1_CC
                && data1 == Midi.TRANSPORT_FORWARD
                && Midi.isPressed(data2);
    }

    @Override
    public void handle(int data1, int data2) {
        transport.fastForward();
    }

    private final Transport transport;
}
