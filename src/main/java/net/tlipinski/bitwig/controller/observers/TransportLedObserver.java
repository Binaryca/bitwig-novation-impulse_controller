package net.tlipinski.bitwig.controller.observers;

import com.bitwig.extension.controller.api.Transport;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiSend;

/**
 * Lights the transport buttons Play, Record and Loop according to the Bitwig
 * transport state. The Impulse does not document LED feedback for the
 * non-assignable transport buttons, so this may have no visible effect on the
 * hardware; sending the button CCs is harmless either way.
 */
public class TransportLedObserver {

    public TransportLedObserver(Transport transport, MidiSend midiSend) {
        transport.isPlaying().addValueObserver((boolean playing) -> {
            midiSend.send(Midi.CH1_CC, Midi.TRANSPORT_PLAY, playing ? 1 : 0);
        });

        transport.isArrangerRecordEnabled().addValueObserver((boolean recording) -> {
            midiSend.send(Midi.CH1_CC, Midi.TRANSPORT_RECORD, recording ? 1 : 0);
        });

        transport.isArrangerLoopEnabled().addValueObserver((boolean loop) -> {
            midiSend.send(Midi.CH1_CC, Midi.TRANSPORT_LOOP, loop ? 1 : 0);
        });
    }
}
