package net.tlipinski.bitwig.controller.commands.transport;

import com.bitwig.extension.controller.api.Transport;
import net.tlipinski.bitwig.controller.Controller;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiCommand;

/**
 * Shift + Play toggles the metronome click.
 *
 * <p>Uses the {@code isMetronomeEnabled()} value: the classic
 * {@code toggleClick()}/{@code addClickObserver()} methods are deprecated
 * since API version 2 and throw with the required API 25 (they crashed the
 * extension at startup until this was switched to the value API).</p>
 */
public class MetronomeCommand implements MidiCommand {

    public MetronomeCommand(Controller controller) {
        this.controller = controller;
        this.transport = controller.getHost().createTransport();
        transport.isMetronomeEnabled().addValueObserver((boolean isOn) -> {
            if (announce) {
                announce = false;
                controller.getSysexSend().displayText(isOn ? "Click On" : "Click Off");
            }
        });
    }

    @Override
    public boolean triggersWhen(int statusByte, int data1, int data2) {
        return controller.isShiftPressed()
                && statusByte == Midi.CH1_CC
                && data1 == Midi.TRANSPORT_PLAY
                && Midi.isPressed(data2);
    }

    @Override
    public void handle(int data1, int data2) {
        announce = true;
        transport.isMetronomeEnabled().toggle();
    }

    private final Controller controller;
    private final Transport transport;
    private boolean announce = false;
}
