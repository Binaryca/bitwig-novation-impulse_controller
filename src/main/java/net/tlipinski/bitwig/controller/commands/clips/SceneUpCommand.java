package net.tlipinski.bitwig.controller.commands.clips;

import com.bitwig.extension.controller.api.Application;
import net.tlipinski.bitwig.controller.Controller;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiCommand;

public class SceneUpCommand implements MidiCommand {

    private final Controller controller;
    private final Application application;

    public SceneUpCommand(Controller controller) {
        this.controller = controller;
        this.application = controller.getHost().createApplication();
        application.panelLayout().markInterested();
    }

    @Override
    public boolean triggersWhen(int statusByte, int data1, int data2) {
        return application.panelLayout().get().equals(Application.PANEL_LAYOUT_MIX)
                && statusByte == Midi.CH1_CC
                && data1 == Midi.SCENE_UP;
    }

    @Override
    public void handle(int data1, int data2) {
        if (Midi.isPressed(data2)) {
            controller.getTracks().getSceneBank().scrollBackwards();
            // The scene bank contains one scene, so index 0 is the current scene.
            controller.getTracks().getSceneBank().getScene(0).selectInEditor();
            controller.getClipLauncher().refreshAll();
        }
    }
}
