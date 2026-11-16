package net.tlipinski.bitwig.controller.commands.encoders;

import com.bitwig.extension.controller.api.Parameter;
import com.bitwig.extension.controller.api.Track;
import net.tlipinski.bitwig.controller.Controller;
import net.tlipinski.bitwig.controller.EncoderMixerModePage;
import net.tlipinski.bitwig.controller.EncoderMode;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiCommand;
import net.tlipinski.bitwig.controller.MidiSend;
import net.tlipinski.bitwig.controller.SysexSend;

public class EncoderMixerCommand implements MidiCommand {

    public EncoderMixerCommand(Controller controller, MidiSend midiSend, SysexSend sysexSend) {
        this.controller = controller;
        this.midiSend = midiSend;
        this.sysexSend = sysexSend;

        for (int i = 0; i < Midi.TRACK_COUNT; i++) {
            Track track = controller.getTracks().getTrackBank().getItemAt(i);
            track.pan().markInterested();
            for (int s = 0; s < Midi.SEND_COUNT; s++) {
                track.sendBank().getItemAt(s).markInterested();
            }
        }
    }

    @Override
    public boolean triggersWhen(int statusByte, int data1, int data2) {
        return controller.getEncoderMode() == EncoderMode.MIXER
                && statusByte == Midi.CH2_CC
                && data1 >= Midi.ENCODER_FIRST
                && data1 <= Midi.ENCODER_LAST;
    }

    @Override
    public void handle(int data1, int data2) {
        Track track = controller.getTracks().get(data1);
        if (track == null) {
            sysexSend.displayText("<empty>");
            return;
        }

        Parameter parameter = getParameter(track);
        double change = (data2 - 64) * 0.01;
        parameter.inc(change);

        sysexSend.displayText(track.name().get());
        int value = (int) (parameter.get() * 100);
        midiSend.send(Midi.CH2_CC, data1, value);
        sysexSend.displayNumber(value);
    }

    private Parameter getParameter(Track track) {
        // PAN is ordinal 0, SEND1..SEND6 are ordinals 1..6.
        EncoderMixerModePage.Page page = controller.getEncoderMixerPage().getPage();
        if (page == EncoderMixerModePage.Page.PAN) {
            return track.pan();
        }
        return track.sendBank().getItemAt(page.ordinal() - 1);
    }

    private final Controller controller;
    private final MidiSend midiSend;
    private final SysexSend sysexSend;
}
