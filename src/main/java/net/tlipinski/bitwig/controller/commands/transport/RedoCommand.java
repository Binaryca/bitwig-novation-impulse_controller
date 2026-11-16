package net.tlipinski.bitwig.controller.commands.transport;

import com.bitwig.extension.controller.api.Application;
import net.tlipinski.bitwig.controller.Controller;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiCommand;

public class RedoCommand implements MidiCommand {

    public RedoCommand(Controller controller) {
        this.controller = controller;
        this.application = controller.getHost().createApplication();
    }

    @Override
    public boolean triggersWhen(int statusByte, int data1, int data2) {
        return controller.isShiftPressed()
                && statusByte == Midi.CH1_CC
                && data1 == Midi.TRANSPORT_FORWARD
                && Midi.isPressed(data2);
    }

    @Override
    public void handle(int data1, int data2) {
        application.redo();
        controller.getSysexSend().displayText("Redo");
    }

    private final Controller controller;
    private final Application application;
}
