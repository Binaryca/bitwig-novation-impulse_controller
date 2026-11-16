package net.tlipinski.bitwig.controller.commands.mixer;

import com.bitwig.extension.controller.api.Track;
import net.tlipinski.bitwig.controller.ButtonsMode;
import net.tlipinski.bitwig.controller.Controller;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiCommand;
import net.tlipinski.bitwig.controller.MidiSend;
import net.tlipinski.bitwig.controller.SysexSend;

public class SoloCommand implements MidiCommand {

    public SoloCommand(Controller controller, MidiSend midiSend, SysexSend sysexSend) {
        this.controller = controller;
        this.midiSend = midiSend;
        this.sysexSend = sysexSend;
    }

    @Override
    public boolean triggersWhen(int statusByte, int data1, int data2) {
        return controller.getButtonsMode() == ButtonsMode.SOLO
                && statusByte == Midi.CH1_CC
                && data1 >= Midi.BUTTON_FIRST
                && data1 <= Midi.BUTTON_LAST;
    }

    @Override
    public void handle(int data1, int data2) {
        if (!Midi.isPressed(data2)) {
            return;
        }

        int buttonIndex = data1 - Midi.BUTTON_FIRST;
        Track track = controller.getTracks().get(buttonIndex);
        if (track == null) {
            sysexSend.displayText("<empty>");
            return;
        }

        track.solo().toggle();
        midiSend.light(buttonIndex, track.solo().get());
        sysexSend.displayText(track.name().get());
    }

    private final Controller controller;
    private final MidiSend midiSend;
    private final SysexSend sysexSend;
}
