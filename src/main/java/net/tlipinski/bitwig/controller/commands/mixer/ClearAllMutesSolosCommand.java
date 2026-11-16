package net.tlipinski.bitwig.controller.commands.mixer;

import com.bitwig.extension.controller.api.Track;
import net.tlipinski.bitwig.controller.ButtonsMode;
import net.tlipinski.bitwig.controller.Controller;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiCommand;
import net.tlipinski.bitwig.controller.MidiSend;
import net.tlipinski.bitwig.controller.SysexSend;
import net.tlipinski.bitwig.controller.observers.callbacks.RefreshAllLightsCallback;

import java.util.List;

/**
 * Shift + the mute/solo mode button clears every mute (in mute mode) or every
 * solo (in solo mode) of the eight visible tracks and the master track.
 */
public class ClearAllMutesSolosCommand implements MidiCommand {

    public ClearAllMutesSolosCommand(Controller controller, MidiSend midiSend, SysexSend sysexSend) {
        this.controller = controller;
        this.midiSend = midiSend;
        this.sysexSend = sysexSend;
    }

    @Override
    public boolean triggersWhen(int statusByte, int data1, int data2) {
        return controller.isShiftPressed()
                && statusByte == Midi.CH1_CC
                && data1 == Midi.BUTTONS_MODE;
    }

    @Override
    public void handle(int data1, int data2) {
        // The mode button is a hardware toggle: it flips its internal state on
        // every press and sends 1 for mutes or 0 for solos. Keep the plugin
        // mode in sync with it so the LEDs do not get out of step.
        ButtonsMode newMode = Midi.isPressed(data2) ? ButtonsMode.MUTE : ButtonsMode.SOLO;

        ButtonsMode previous = controller.getButtonsMode();
        List<Track> all = controller.getTracks().getAll();
        if (previous == ButtonsMode.MUTE) {
            for (Track track : all) {
                track.mute().set(false);
            }
            sysexSend.displayText("ClrMutes");
        } else {
            for (Track track : all) {
                track.solo().set(false);
            }
            sysexSend.displayText("ClrSolos");
        }

        controller.changeButtonsMode(newMode);
        new RefreshAllLightsCallback(controller, midiSend).valueChanged(newMode);
    }

    private final Controller controller;
    private final MidiSend midiSend;
    private final SysexSend sysexSend;
}
