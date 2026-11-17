package net.tlipinski.bitwig.controller.commands.transport;

import com.bitwig.extension.controller.api.Action;
import com.bitwig.extension.controller.api.Application;
import net.tlipinski.bitwig.controller.Controller;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiCommand;

/**
 * Shift + Stop saves the project.
 *
 * <p>Bitwig exposes project saving only through its action system; the
 * action identifiers changed between Bitwig versions (the shipped Nektar
 * scripts still use {@code getAction("Undo")}, while newer actions are
 * named like {@code slice_to_multi_sampler_track}). So the save action is
 * found once at init by its name instead of a hard-coded identifier.</p>
 */
public class SaveCommand implements MidiCommand {

    public SaveCommand(Controller controller) {
        this.controller = controller;
        this.application = controller.getHost().createApplication();
        for (Action action : this.application.getActions()) {
            if ("save".equalsIgnoreCase(action.getId())
                    || "Save".equals(action.getName())
                    || "Save".equals(action.getMenuItemText())) {
                this.saveAction = action;
                break;
            }
        }
    }

    @Override
    public boolean triggersWhen(int statusByte, int data1, int data2) {
        return controller.isShiftPressed()
                && statusByte == Midi.CH1_CC
                && data1 == Midi.TRANSPORT_STOP
                && Midi.isPressed(data2);
    }

    @Override
    public void handle(int data1, int data2) {
        if (saveAction == null) {
            // The action list may not have been populated at init; retry once.
            for (Action action : this.application.getActions()) {
                if ("save".equalsIgnoreCase(action.getId())
                        || "Save".equals(action.getName())
                        || "Save".equals(action.getMenuItemText())) {
                    this.saveAction = action;
                    break;
                }
            }
        }
        if (saveAction != null) {
            saveAction.invoke();
            controller.getSysexSend().displayText("Save");
        } else {
            controller.getSysexSend().displayText("No Save");
        }
    }

    private final Controller controller;
    private final Application application;
    private Action saveAction;
}
