package net.tlipinski.bitwig.controller;

import com.bitwig.extension.controller.api.ClipLauncherSlot;

/**
 * Keeps the eight clip-launch pad LEDs in sync with the current scene.
 */
public class ClipLauncher {

    public ClipLauncher(Controller controller) {
        this.controller = controller;
        controller.setClipLauncher(this);

        for (int i = 0; i < Midi.TRACK_COUNT; i++) {
            final int trackIndex = i;
            ClipLauncherSlot clip = clip(trackIndex);
            clip.hasContent().markInterested();
            clip.isPlaying().markInterested();
            clip.isPlaybackQueued().markInterested();
            clip.isStopQueued().markInterested();

            clip.isPlaying().addValueObserver(isPlaying -> refreshTrack(trackIndex));
            clip.isPlaybackQueued().addValueObserver(isQueued -> refreshTrack(trackIndex));
            clip.isStopQueued().addValueObserver(isQueued -> refreshTrack(trackIndex));
            clip.hasContent().addValueObserver(hasContent -> refreshTrack(trackIndex));
        }

        controller.getTracks().getSceneBank().scrollPosition().addValueObserver(position -> {
            controller.getSysexSend().displayText("Scene " + (position + 1));
            refreshAll();
        });
        refreshAll();
    }

    public void refreshAll() {
        for (int trackIndex = 0; trackIndex < Midi.TRACK_COUNT; trackIndex++) {
            refreshTrack(trackIndex);
        }
    }

    public void refreshTrack(int trackIndex) {
        ClipLauncherSlot clip = clip(trackIndex);
        ClipLight light;

        if (clip.isPlaying().get()) {
            light = ClipLight.PLAYING;
        } else if (clip.isPlaybackQueued().get()) {
            light = ClipLight.PLAYBACK_QUEUED;
        } else if (clip.isStopQueued().get()) {
            light = ClipLight.STOP_QUEUED;
        } else if (clip.hasContent().get()) {
            light = ClipLight.STOPPED;
        } else {
            light = ClipLight.EMPTY;
        }

        controller.getMidiSend().clipLight(trackIndex, light);
    }

    private ClipLauncherSlot clip(int trackIndex) {
        return controller.getTracks().getTrackBank()
                .getItemAt(trackIndex)
                .clipLauncherSlotBank()
                .getItemAt(0);
    }

    private final Controller controller;
}
