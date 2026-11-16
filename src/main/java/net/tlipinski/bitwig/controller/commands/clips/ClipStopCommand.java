package net.tlipinski.bitwig.controller.commands.clips;

import com.bitwig.extension.controller.api.Track;
import net.tlipinski.bitwig.controller.Controller;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiCommand;

public class ClipStopCommand implements MidiCommand {

    public ClipStopCommand(Controller controller) {
        this.controller = controller;
    }

    @Override
    public boolean triggersWhen(int statusByte, int data1, int data2) {
        return controller.isShiftPressed()
                && statusByte == Midi.CH1_CC
                && data1 >= Midi.CLIP_FIRST
                && data1 <= Midi.CLIP_LAST
                && Midi.isPressed(data2);
    }

    @Override
    public void handle(int data1, int data2) {
        int trackIndex = data1 - Midi.CLIP_FIRST;
        Track channel = controller.getTracks().getTrackBank().getItemAt(trackIndex);
        channel.stop();
    }

    private final Controller controller;
}
