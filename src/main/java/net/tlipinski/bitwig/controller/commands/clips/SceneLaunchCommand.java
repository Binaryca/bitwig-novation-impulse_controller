package net.tlipinski.bitwig.controller.commands.clips;

import com.bitwig.extension.controller.api.Application;
import com.bitwig.extension.controller.api.Track;
import net.tlipinski.bitwig.controller.Controller;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiCommand;

public class SceneLaunchCommand implements MidiCommand {

    private final Controller controller;
    private final Application application;

    public SceneLaunchCommand(Controller controller) {
        this.controller = controller;
        this.application = controller.getHost().createApplication();
        application.panelLayout().markInterested();
    }

    @Override
    public boolean triggersWhen(int statusByte, int data1, int data2) {
        return application.panelLayout().get().equals(Application.PANEL_LAYOUT_MIX)
                && statusByte == Midi.CH1_CC
                && data1 == Midi.SCENE_LAUNCH;
    }

    @Override
    public void handle(int data1, int data2) {
        if (!Midi.isPressed(data2)) {
            return;
        }

        if (controller.isShiftPressed()) {
            stopAllClips();
        } else {
            controller.getTracks().getSceneBank().launchScene(0);
        }
    }

    private void stopAllClips() {
        for (int i = 0; i < Midi.TRACK_COUNT; i++) {
            Track track = controller.getTracks().getTrackBank().getItemAt(i);
            track.stop();
        }
    }
}
