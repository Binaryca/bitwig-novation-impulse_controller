package net.tlipinski.bitwig.controller.commands.mixer;

import com.bitwig.extension.controller.api.Track;
import net.tlipinski.bitwig.controller.Controller;
import net.tlipinski.bitwig.controller.FaderSoftTakeover;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiCommand;
import net.tlipinski.bitwig.controller.MidiSend;
import net.tlipinski.bitwig.controller.SysexSend;

public class FaderCommand implements MidiCommand {

    public FaderCommand(Controller controller, MidiSend midiSend, SysexSend sysexSend, FaderSoftTakeover takeover) {
        this.controller = controller;
        this.midiSend = midiSend;
        this.sysexSend = sysexSend;
        this.takeover = takeover;
    }

    @Override
    public boolean triggersWhen(int statusByte, int data1, int data2) {
        return controller.getModel().isImpulse49or61()
                && statusByte == Midi.CH1_CC
                && data1 >= Midi.FADER_FIRST
                && data1 <= Midi.FADER_LAST;
    }

    @Override
    public void handle(int data1, int data2) {
        Track track = controller.getTracks().get(data1);
        if (track == null) {
            sysexSend.displayText("<empty>");
            return;
        }

        // Soft takeover: the faders are not motorised, so their physical
        // position may differ from the value of the track they control.
        int current = (int) Math.round(track.volume().value().get() * 128.0);
        switch (takeover.accept(data1, data2, current)) {
            case IGNORE:
                // Hardware cross-talk from a resting fader: no display, no
                // state change, nothing.
                return;
            case REJECT:
                // Show where the value actually is, so the fader can be
                // swept to it.
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
