package net.tlipinski.bitwig.controller;

import java.util.Arrays;
import java.util.function.LongSupplier;

/**
 * Fader soft takeover: the Impulse faders are not motorised, so their
 * physical position can differ from the value they control (after a track
 * bank page change, for example). A fader only takes over the value when
 * its physical position reaches it; until then the movements are rejected
 * and the display shows the value the fader has to be swept to.
 *
 * <p>The states follow the proven algorithm of the official Nektar scripts:
 * MUTED (disengaged), LOW (fader below the value), HIGH (fader above the
 * value) and ACTIVE (full control). Moving from far away engages the fader
 * one step past the value (the engagement at the exact value happens only
 * from the disengaged state, when the difference is within the window); a
 * pause longer than the timeout disengages a far-away fader again. External
 * value changes correct themselves, because every message is evaluated
 * against the current value.</p>
 *
 * <p>On top of that, the Impulse hardware sporadically re-transmits the
 * position of a resting fader (with +-2 steps of ADC jitter, most likely
 * touch sensor cross-talk) while another fader is being moved. For a fader
 * resting near its value such a message looks exactly like a fresh grab and
 * would make the volume wiggle. Those messages are recognised by their
 * timing - they arrive right after a message from the fader that is really
 * being moved - and dropped silently.</p>
 */
public class FaderSoftTakeover {

    /** What the caller should do with a fader message. */
    public enum Decision {
        /** Apply the value to the track. */
        ENGAGE,
        /** Rejected: the display should show the value to sweep to. */
        REJECT,
        /** Hardware cross-talk from a resting fader: drop without a trace. */
        IGNORE
    }

    public FaderSoftTakeover() {
        this(System::nanoTime);
    }

    public FaderSoftTakeover(Controller controller) {
        this(System::nanoTime);
        this.controller = controller;

        for (int i = 0; i < Midi.FADER_MASTER; i++) {
            controller.getTracks().getTrackBank().getItemAt(i).volume().value().markInterested();
        }
        controller.getTracks().get(Midi.FADER_MASTER).volume().value().markInterested();

        controller.getHost().scheduleTask(this::tick, TICK_MS);
    }

    FaderSoftTakeover(LongSupplier clock) {
        long start = clock.getAsLong();
        this.clock = clock;
        Arrays.fill(states, State.MUTED);
        // Before the first real message every fader counts as idle for much
        // longer than the cross-talk window.
        Arrays.fill(lastRealTime, start - IDLE_NANOS);
    }

    /**
     * Classifies the movement of the given fader to the incoming value.
     */
    public Decision accept(int fader, int incoming, int current) {
        long now = clock.getAsLong();

        // Hardware cross-talk: a resting fader reporting its position (with
        // jitter) right after another fader sent a message. Near the value it
        // would engage the fader or re-apply its resting position - drop it.
        if (Math.abs(incoming - current) <= CROSS_TALK_WINDOW
                && isAnotherFaderStreaming(fader, now)) {
            return Decision.IGNORE;
        }

        boolean close = Math.abs(incoming - current) <= WINDOW;
        switch (states[fader]) {
            case MUTED:
                states[fader] = close ? State.ACTIVE : (incoming < current ? State.LOW : State.HIGH);
                break;
            case LOW:
                if (incoming > current || (current == 127 && incoming >= current)) {
                    states[fader] = State.ACTIVE;
                }
                break;
            case HIGH:
                if (incoming < current || (current == 0 && incoming <= current)) {
                    states[fader] = State.ACTIVE;
                }
                break;
            case ACTIVE:
                if (timers[fader] <= 0 && !close) {
                    states[fader] = State.MUTED;
                }
                break;
        }
        if (states[fader] == State.ACTIVE) {
            timers[fader] = TIMER_TICKS;
        }
        lastRealTime[fader] = now;
        return states[fader] == State.ACTIVE ? Decision.ENGAGE : Decision.REJECT;
    }

    /** Disengages the given fader (after a track bank page change). */
    public void reset(int fader) {
        states[fader] = State.MUTED;
        timers[fader] = 0;
    }

    /** Disengages all faders. */
    public void resetAll() {
        for (int i = 0; i < states.length; i++) {
            reset(i);
        }
    }

    private boolean isAnotherFaderStreaming(int fader, long now) {
        for (int i = 0; i < states.length; i++) {
            if (i != fader && now - lastRealTime[i] <= OTHER_STREAM_NANOS) {
                return true;
            }
        }
        return false;
    }

    private void tick() {
        try {
            decrementTimers();
        } finally {
            if (controller != null) {
                controller.getHost().scheduleTask(this::tick, TICK_MS);
            }
        }
    }

    void decrementTimers() {
        for (int i = 0; i < timers.length; i++) {
            if (timers[i] > 0) {
                timers[i]--;
            }
        }
    }

    private enum State { ACTIVE, MUTED, HIGH, LOW }

    private final LongSupplier clock;
    private final State[] states = new State[Midi.FADER_MASTER + 1];
    private final int[] timers = new int[Midi.FADER_MASTER + 1];
    private final long[] lastRealTime = new long[Midi.FADER_MASTER + 1];

    private Controller controller;

    /** Engagement window: a fader this close to the value takes over at once. */
    private static final int WINDOW = 3;
    /** Cross-talk sits this close to the value (observed: 0-3, worst 4). */
    private static final int CROSS_TALK_WINDOW = 4;
    /** Cross-talk arrives within this time after a real message (observed: 0-18 ms). */
    private static final long OTHER_STREAM_NANOS = 150_000_000L;
    private static final long IDLE_NANOS = 1_000_000_000L;
    private static final long TICK_MS = 250;
    private static final int TIMER_TICKS = 3;
}
