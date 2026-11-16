package net.tlipinski.bitwig.controller.commands.encoders;

import com.bitwig.extension.controller.api.CursorRemoteControlsPage;
import com.bitwig.extension.controller.api.RemoteControl;
import net.tlipinski.bitwig.controller.Controller;
import net.tlipinski.bitwig.controller.EncoderMode;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiCommand;
import net.tlipinski.bitwig.controller.MidiSend;
import net.tlipinski.bitwig.controller.SysexSend;

public class EncoderPluginCommand implements MidiCommand {

    public EncoderPluginCommand(Controller controller, MidiSend midiSend, SysexSend sysexSend) {
        this.controller = controller;
        this.midiSend = midiSend;
        this.sysexSend = sysexSend;

        CursorRemoteControlsPage page = controller.getTracks().getCursorRemoteControlsPage();
        for (int i = 0; i < 8; i++) {
            page.getParameter(i).name().markInterested();
            page.getParameter(i).markInterested();
        }
    }

    @Override
    public boolean triggersWhen(int statusByte, int data1, int data2) {
        return controller.getEncoderMode() == EncoderMode.PLUGIN
                && statusByte == Midi.CH2_CC
                && data1 >= Midi.ENCODER_FIRST
                && data1 <= Midi.ENCODER_LAST;
    }

    @Override
    public void handle(int data1, int data2) {
        RemoteControl parameter = controller.getTracks().getCursorRemoteControlsPage().getParameter(data1);

        double change = (data2 - 64) * 0.01;
        parameter.inc(change);

        sysexSend.displayText(parameter.name().get());
        int value = (int) (parameter.get() * 100);
        midiSend.send(Midi.CH2_CC, data1, value);
        sysexSend.displayNumber(value);
    }

    private final Controller controller;
    private final MidiSend midiSend;
    private final SysexSend sysexSend;
}
