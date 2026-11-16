package net.tlipinski.bitwig.controller.commands.encoders;

import net.tlipinski.bitwig.controller.Controller;
import net.tlipinski.bitwig.controller.EncoderMode;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiCommand;
import net.tlipinski.bitwig.controller.SysexSend;

public class EncoderPluginModeCommand implements MidiCommand {

    public EncoderPluginModeCommand(Controller controller, SysexSend sysexSend) {
        this.controller = controller;
        this.sysexSend = sysexSend;
    }

    @Override
    public boolean triggersWhen(int statusByte, int data1, int data2) {
        return statusByte == Midi.CH2_CC
                && data1 == Midi.ENCODER_PLUGIN_MODE
                && Midi.isPressed(data2);
    }

    @Override
    public void handle(int data1, int data2) {
        controller.changeEncoderMode(EncoderMode.PLUGIN);
        sysexSend.displayText("Plugin");
    }

    private final Controller controller;
    private final SysexSend sysexSend;
}
