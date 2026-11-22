package net.tlipinski.bitwig.controller.commands.transport;

/**
 * The state of the punch in/out cycle: the first press of the punch button
 * sets the loop start (and enables the loop), the second press sets the
 * loop end as the current position. The pending punch-out survives only
 * for a short time - after the timeout the next press starts a new punch
 * in, so a forgotten cycle cannot jump the loop end minutes later.
 */
public class PunchState {

    /** What the caller should apply to the transport. */
    public enum Phase {
        /** Set the loop start to {@link Punch#position} and enable the loop. */
        IN,
        /** Keep the loop start and set the duration to {@link Punch#duration}. */
        OUT
    }

    /** The result of a punch button press. */
    public static final class Punch {
        public final Phase phase;
        /** The new loop start (IN) or the unchanged one (OUT). */
        public final double position;
        /** The new loop duration (OUT); meaningless for IN. */
        public final double duration;

        Punch(Phase phase, double position, double duration) {
            this.phase = phase;
            this.position = position;
            this.duration = duration;
        }
    }

    /**
     * Advances the punch cycle with a press of the punch button at the
     * given transport position.
     */
    public Punch press(long now, double position) {
        if (pendingPunchOut && now - pendingAt <= TIMEOUT_MS) {
            pendingPunchOut = false;
            double duration = Math.max(MIN_LOOP_BEATS, position - loopStart);
            return new Punch(Phase.OUT, loopStart, duration);
        }
        pendingPunchOut = true;
        pendingAt = now;
        loopStart = position;
        return new Punch(Phase.IN, position, 0);
    }

    private boolean pendingPunchOut = false;
    private long pendingAt = 0;
    private double loopStart = 0;

    private static final long TIMEOUT_MS = 30_000;
    private static final double MIN_LOOP_BEATS = 1.0;
}
