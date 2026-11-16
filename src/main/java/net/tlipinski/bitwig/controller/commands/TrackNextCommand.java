package net.tlipinski.bitwig.controller.commands;

import com.bitwig.extension.controller.api.CursorTrack;
import com.bitwig.extension.controller.api.Track;
import net.tlipinski.bitwig.controller.Controller;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiCommand;
import net.tlipinski.bitwig.controller.SysexSend;

public class TrackNextCommand implements MidiCommand {

    public TrackNextCommand(Controller controller, SysexSend sysexSend) {
        this.controller = controller;
        this.sysexSend = sysexSend;

        controller.getTracks().getCursorTrack().hasNext().markInterested();
        controller.getTracks().getCursorTrack().position().markInterested();
        for (Track track : controller.getTracks().getAll()) {
            track.position().markInterested();
        }
    }

    @Override
    public boolean triggersWhen(int statusByte, int data1, int data2) {
        return statusByte == Midi.CH1_CC
                && data1 == Midi.TRACK_NEXT
                && Midi.isPressed(data2);
    }

    @Override
    public void handle(int data1, int data2) {
        CursorTrack cursorTrack = controller.getTracks().getCursorTrack();
        cursorTrack.selectNext();
        controller.getHost().scheduleTask(() -> sysexSend.displayText(cursorTrack.name().get()), 200);
    }

    private final Controller controller;
    private final SysexSend sysexSend;
}
