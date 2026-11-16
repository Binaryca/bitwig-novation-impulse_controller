package net.tlipinski.bitwig.controller.commands.transport;

import com.bitwig.extension.controller.api.Application;
import net.tlipinski.bitwig.controller.Controller;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiCommand;

public class UndoCommand implements MidiCommand {

    public UndoCommand(Controller controller) {
        this.controller = controller;
        this.application = controller.getHost().createApplication();
    }

    @Override
    public boolean triggersWhen(int statusByte, int data1, int data2) {
        return controller.isShiftPressed()
                && statusByte == Midi.CH1_CC
                && data1 == Midi.TRANSPORT_REWIND
                && Midi.isPressed(data2);
    }

    @Override
    public void handle(int data1, int data2) {
        application.undo();
        controller.getSysexSend().displayText("Undo");
    }

    private final Controller controller;
    private final Application application;
}
