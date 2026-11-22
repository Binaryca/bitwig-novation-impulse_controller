package net.tlipinski.bitwig.controller.commands.transport;

import com.bitwig.extension.controller.api.Transport;
import net.tlipinski.bitwig.controller.Controller;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiCommand;
import net.tlipinski.bitwig.controller.SysexSend;

/**
 * Shift + Loop sets the arranger loop range around the current position:
 * the first press is the punch in (loop start, the loop is enabled), the
 * second press is the punch out (loop end). Works with the Mix panel
 * closed; with it open, Shift + Loop keeps stopping all clips (the scene
 * commands run first). A forgotten punch out times out (see PunchState).
 *
 * <p>The loop API values are per-proxy: this command marks its own values,
 * the ones of the other Transport proxies stay unmarked.</p>
 */
public class PunchCommand implements MidiCommand {

    public PunchCommand(Controller controller, Transport transport, SysexSend sysexSend) {
        this.controller = controller;
        this.transport = transport;
        this.sysexSend = sysexSend;

        transport.getPosition().markInterested();
        transport.arrangerLoopStart().markInterested();
        transport.arrangerLoopDuration().markInterested();
        transport.isArrangerLoopEnabled().markInterested();
    }

    @Override
    public boolean triggersWhen(int statusByte, int data1, int data2) {
        return controller.isShiftPressed()
                && statusByte == Midi.CH1_CC
                && data1 == Midi.TRANSPORT_LOOP
                && Midi.isPressed(data2);
    }

    @Override
    public void handle(int data1, int data2) {
        PunchState.Punch punch = state.press(
                System.currentTimeMillis(),
                transport.getPosition().get());
        if (punch.phase == PunchState.Phase.IN) {
            transport.arrangerLoopStart().set(punch.position);
            transport.isArrangerLoopEnabled().set(true);
            sysexSend.displayText("Punch In");
        } else {
            transport.arrangerLoopDuration().set(punch.duration);
            sysexSend.displayText("Punch Out");
        }
    }

    private final Controller controller;
    private final Transport transport;
    private final SysexSend sysexSend;
    private final PunchState state = new PunchState();
}
