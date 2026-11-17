package net.tlipinski.bitwig.controller.commands.mixer;

import com.bitwig.extension.controller.api.Track;
import net.tlipinski.bitwig.controller.Controller;
import net.tlipinski.bitwig.controller.FaderSoftTakeover;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiCommand;
import net.tlipinski.bitwig.controller.MidiSend;
import net.tlipinski.bitwig.controller.SysexSend;

public class FaderCursorCommand implements MidiCommand {

    public FaderCursorCommand(Controller controller, MidiSend midiSend, SysexSend sysexSend, FaderSoftTakeover takeover) {
        this.controller = controller;
        this.midiSend = midiSend;
        this.sysexSend = sysexSend;
        this.takeover = takeover;

        controller.getTracks().getCursorTrack().name().markInterested();
        controller.getTracks().getCursorTrack().volume().value().markInterested();
        // The fader follows the cursor track: a track change invalidates the
        // takeover state (the name changes on every selection in practice).
        controller.getTracks().getCursorTrack().name().addValueObserver(name -> takeover.reset(0));
    }

    @Override
    public boolean triggersWhen(int statusByte, int data1, int data2) {
        return controller.getModel().isImpulse25()
                && statusByte == Midi.CH1_CC
                && data1 == Midi.FADER_MASTER;
    }

    @Override
    public void handle(int data1, int data2) {
        Track track = controller.getTracks().getCursorTrack();

        // Soft takeover: see FaderCommand.
        int current = (int) Math.round(track.volume().value().get() * 128.0);
        switch (takeover.accept(0, data2, current)) {
            case IGNORE:
                return;
            case REJECT:
                sysexSend.displayText(track.name().get());
                sysexSend.displayNumber(current);
                return;
            case ENGAGE:
                track.volume().set(data2, 128);
                sysexSend.displayText(track.name().get());

                // Echo the value back so the Impulse displays the volume level.
                midiSend.send(Midi.CH1_CC, data1, data2);
                sysexSend.displayNumber(data2);
                return;
        }
    }

    private final Controller controller;
    private final MidiSend midiSend;
    private final SysexSend sysexSend;
    private final FaderSoftTakeover takeover;
}
