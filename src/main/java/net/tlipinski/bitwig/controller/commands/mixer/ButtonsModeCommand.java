package net.tlipinski.bitwig.controller.commands.mixer;

import net.tlipinski.bitwig.controller.ButtonsMode;
import net.tlipinski.bitwig.controller.Controller;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiCommand;
import net.tlipinski.bitwig.controller.MidiSend;
import net.tlipinski.bitwig.controller.SysexSend;
import net.tlipinski.bitwig.controller.observers.callbacks.RefreshAllLightsCallback;

public class ButtonsModeCommand implements MidiCommand {

    public ButtonsModeCommand(Controller controller, MidiSend midiSend, SysexSend sysexSend) {
        this.controller = controller;
        this.midiSend = midiSend;
        this.sysexSend = sysexSend;

        midiSend.resetButtonsMode();
    }

    @Override
    public boolean triggersWhen(int statusByte, int data1, int data2) {
        return statusByte == Midi.CH1_CC && data1 == Midi.BUTTONS_MODE;
    }

    @Override
    public void handle(int data1, int data2) {
        // The mode button is a hardware toggle: 1 selects mutes, 0 selects solos.
        ButtonsMode mode = Midi.isPressed(data2) ? ButtonsMode.MUTE : ButtonsMode.SOLO;
        controller.changeButtonsMode(mode);
        new RefreshAllLightsCallback(controller, midiSend).valueChanged(mode);
        sysexSend.displayText(mode == ButtonsMode.MUTE ? "Mutes" : "Solos");
    }

    private final Controller controller;
    private final MidiSend midiSend;
    private final SysexSend sysexSend;
}
