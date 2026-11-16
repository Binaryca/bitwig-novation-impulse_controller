package net.tlipinski.bitwig.controller;

import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.CursorRemoteControlsPage;
import com.bitwig.extension.controller.api.CursorTrack;
import com.bitwig.extension.controller.api.MasterTrack;
import com.bitwig.extension.controller.api.PinnableCursorDevice;
import com.bitwig.extension.controller.api.SceneBank;
import com.bitwig.extension.controller.api.Track;
import com.bitwig.extension.controller.api.TrackBank;

import java.util.ArrayList;
import java.util.List;

public class Tracks {

    public Tracks(ControllerHost host) {
        // One scene is enough: the Impulse has a single row of clip pads and
        // the scene bank is used to move that row.
        this.trackBank = host.createTrackBank(Midi.TRACK_COUNT, Midi.SEND_COUNT, 1);
        this.masterTrack = host.createMasterTrack(8);

        this.cursorTrack = host.createCursorTrack(2, 0);
        this.cursorDevice = cursorTrack.createCursorDevice();
        this.cursorRemoteControlsPage = cursorDevice.createCursorRemoteControlsPage(8);

        masterTrack.name().markInterested();
        for (int i = 0; i < trackBank.getSizeOfBank(); i++) {
            Track channel = trackBank.getItemAt(i);
            channel.name().markInterested();
            channel.trackType().markInterested();
        }
    }

    public CursorRemoteControlsPage getCursorRemoteControlsPage() {
        return cursorRemoteControlsPage;
    }

    public TrackBank getTrackBank() {
        return trackBank;
    }

    public Track get(int index) {
        if (index == Midi.MASTER_TRACK_INDEX) {
            return masterTrack;
        }

        Track track = trackBank.getItemAt(index);
        if (isEmpty(track)) {
            return null;
        }
        return track;
    }

    public boolean isNotEmpty(int index) {
        return !isEmpty(index);
    }

    public boolean isEmpty(int index) {
        if (index == Midi.MASTER_TRACK_INDEX) {
            return false;
        }
        return isEmpty(trackBank.getItemAt(index));
    }

    public List<Track> getAll() {
        List<Track> result = new ArrayList<>();
        for (int i = 0; i < trackBank.getSizeOfBank(); i++) {
            result.add(trackBank.getItemAt(i));
        }
        result.add(masterTrack);
        return result;
    }

    public SceneBank getSceneBank() {
        return trackBank.sceneBank();
    }

    public CursorTrack getCursorTrack() {
        return cursorTrack;
    }

    public PinnableCursorDevice getCursorDevice() {
        return cursorDevice;
    }

    private boolean isEmpty(Track track) {
        String type = track.trackType().get();
        return type.isEmpty() || "Master".equals(type);
    }

    private final TrackBank trackBank;
    private final MasterTrack masterTrack;
    private final CursorRemoteControlsPage cursorRemoteControlsPage;
    private final CursorTrack cursorTrack;
    private final PinnableCursorDevice cursorDevice;
}
