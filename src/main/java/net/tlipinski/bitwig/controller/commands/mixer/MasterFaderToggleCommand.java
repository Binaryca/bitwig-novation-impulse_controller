package net.tlipinski.bitwig.controller.commands.mixer;

import net.tlipinski.bitwig.controller.Controller;
import net.tlipinski.bitwig.controller.EncoderMode;
import net.tlipinski.bitwig.controller.FaderSoftTakeover;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiCommand;
import net.tlipinski.bitwig.controller.SysexSend;

/**
 * Toggles whether the master fader (CC 8) controls the master track or the
 * cursor track. In the plugin encoder mode a plain press of the master
 * button does it (the button has no other function there); in the mixer
 * mode it is Shift + the master button, because a plain press keeps the
 * master mute. The takeover state of the fader is reset on every target
 * change, so the fader re-engages for the value of the new target; while
 * the fader follows the cursor track, selecting another track resets it as
 * well.
 */
public class MasterFaderToggleCommand implements MidiCommand {

    public MasterFaderToggleCommand(Controller controller, FaderSoftTakeover takeover, SysexSend sysexSend) {
        this.controller = controller;
        this.takeover = takeover;
        this.sysexSend = sysexSend;

        controller.getTracks().getCursorTrack().name().addValueObserver((String name) -> {
            if (controller.isMasterFaderFollowsCursor()) {
                takeover.reset(Midi.FADER_MASTER);
            }
        });
    }

    @Override
    public boolean triggersWhen(int statusByte, int data1, int data2) {
        return statusByte == Midi.CH1_CC
                && data1 == Midi.BUTTON_LAST
                && Midi.isPressed(data2)
                && (controller.getEncoderMode() == EncoderMode.PLUGIN || controller.isShiftPressed());
    }

    @Override
    public void handle(int data1, int data2) {
        boolean follows = controller.toggleMasterFaderFollowsCursor();
        takeover.reset(Midi.FADER_MASTER);
        sysexSend.displayText(follows ? "Cursor" : "Master");
    }

    private final Controller controller;
    private final FaderSoftTakeover takeover;
    private final SysexSend sysexSend;
}
