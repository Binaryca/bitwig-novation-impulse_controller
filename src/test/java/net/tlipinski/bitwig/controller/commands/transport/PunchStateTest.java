package net.tlipinski.bitwig.controller.commands.transport;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PunchStateTest {

    private static final long MS = 1;

    @Test
    void firstPressIsAPunchIn() {
        PunchState state = new PunchState();
        PunchState.Punch punch = state.press(1000 * MS, 32.0);
        assertEquals(PunchState.Phase.IN, punch.phase);
        assertEquals(32.0, punch.position);
    }

    @Test
    void secondPressWithinTheTimeoutIsAPunchOut() {
        PunchState state = new PunchState();
        state.press(1000 * MS, 32.0);
        PunchState.Punch punch = state.press(5000 * MS, 36.0);
        assertEquals(PunchState.Phase.OUT, punch.phase);
        assertEquals(32.0, punch.position);
        assertEquals(4.0, punch.duration);
    }

    @Test
    void punchOutClampsToTheMinimumLoopLength() {
        PunchState state = new PunchState();
        state.press(1000 * MS, 32.0);
        // Out at or before the start: the loop keeps a minimal length.
        PunchState.Punch atStart = state.press(2000 * MS, 32.0);
        assertEquals(1.0, atStart.duration);
    }

    @Test
    void punchOutBeforeTheStartKeepsTheMinimumLoopLength() {
        PunchState state = new PunchState();
        state.press(1000 * MS, 32.0);
        PunchState.Punch before = state.press(2000 * MS, 20.0);
        assertEquals(1.0, before.duration);
    }

    @Test
    void timeoutStartsANewPunchIn() {
        PunchState state = new PunchState();
        state.press(1000 * MS, 32.0);
        // 30 s timeout passed: this is a fresh punch in, not a punch out.
        PunchState.Punch punch = state.press(40_000 * MS, 64.0);
        assertEquals(PunchState.Phase.IN, punch.phase);
        assertEquals(64.0, punch.position);
    }

    @Test
    void timeoutBoundaryStillCountsAsAPunchOut() {
        PunchState state = new PunchState();
        state.press(1000 * MS, 32.0);
        // Exactly at the timeout boundary the cycle still completes.
        PunchState.Punch punch = state.press(31_000 * MS, 36.0);
        assertEquals(PunchState.Phase.OUT, punch.phase);
        assertEquals(4.0, punch.duration);
    }

    @Test
    void afterAPunchOutTheNextPressIsAPunchInAgain() {
        PunchState state = new PunchState();
        state.press(1000 * MS, 32.0);
        state.press(2000 * MS, 36.0);
        PunchState.Punch punch = state.press(3000 * MS, 40.0);
        assertEquals(PunchState.Phase.IN, punch.phase);
        assertEquals(40.0, punch.position);
    }
}
