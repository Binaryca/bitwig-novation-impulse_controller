package net.tlipinski.bitwig.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FaderSoftTakeoverTest {

    private static final long MS = 1_000_000;

    @Test
    void engagesImmediatelyWhenClose() {
        FaderSoftTakeover takeover = new FaderSoftTakeover();
        assertEquals(FaderSoftTakeover.Decision.ENGAGE, takeover.accept(0, 50, 48));
        assertEquals(FaderSoftTakeover.Decision.ENGAGE, takeover.accept(0, 52, 50));
    }

    @Test
    void doesNotJumpWhenFarAboveAndEngagesOneStepPastTheValue() {
        FaderSoftTakeover takeover = new FaderSoftTakeover();
        assertEquals(FaderSoftTakeover.Decision.REJECT, takeover.accept(0, 100, 50));
        assertEquals(FaderSoftTakeover.Decision.REJECT, takeover.accept(0, 80, 50));
        assertEquals(FaderSoftTakeover.Decision.REJECT, takeover.accept(0, 50, 50));
        assertEquals(FaderSoftTakeover.Decision.ENGAGE, takeover.accept(0, 49, 50));
    }

    @Test
    void doesNotJumpWhenFarBelowAndEngagesOneStepPastTheValue() {
        FaderSoftTakeover takeover = new FaderSoftTakeover();
        assertEquals(FaderSoftTakeover.Decision.REJECT, takeover.accept(0, 10, 50));
        assertEquals(FaderSoftTakeover.Decision.REJECT, takeover.accept(0, 50, 50));
        assertEquals(FaderSoftTakeover.Decision.ENGAGE, takeover.accept(0, 51, 50));
    }

    @Test
    void engagesAtTheTopEnd() {
        FaderSoftTakeover takeover = new FaderSoftTakeover();
        assertEquals(FaderSoftTakeover.Decision.REJECT, takeover.accept(0, 100, 127));
        assertEquals(FaderSoftTakeover.Decision.ENGAGE, takeover.accept(0, 127, 127));
    }

    @Test
    void engagesAtTheBottomEnd() {
        FaderSoftTakeover takeover = new FaderSoftTakeover();
        assertEquals(FaderSoftTakeover.Decision.REJECT, takeover.accept(0, 10, 0));
        assertEquals(FaderSoftTakeover.Decision.ENGAGE, takeover.accept(0, 0, 0));
    }

    @Test
    void disengagesAfterATimeoutWhenFar() {
        FaderSoftTakeover takeover = new FaderSoftTakeover();
        assertEquals(FaderSoftTakeover.Decision.ENGAGE, takeover.accept(0, 50, 49));
        for (int i = 0; i < 4; i++) {
            takeover.decrementTimers();
        }
        assertEquals(FaderSoftTakeover.Decision.REJECT, takeover.accept(0, 100, 49));
    }

    @Test
    void staysActiveThroughATimeoutWhenClose() {
        FaderSoftTakeover takeover = new FaderSoftTakeover();
        assertEquals(FaderSoftTakeover.Decision.ENGAGE, takeover.accept(0, 50, 49));
        for (int i = 0; i < 4; i++) {
            takeover.decrementTimers();
        }
        assertEquals(FaderSoftTakeover.Decision.ENGAGE, takeover.accept(0, 52, 50));
    }

    @Test
    void resetDisengages() {
        FaderSoftTakeover takeover = new FaderSoftTakeover();
        assertEquals(FaderSoftTakeover.Decision.ENGAGE, takeover.accept(0, 50, 49));
        takeover.resetAll();
        assertEquals(FaderSoftTakeover.Decision.REJECT, takeover.accept(0, 100, 49));
    }

    @Test
    void fadersAreIndependent() {
        FaderSoftTakeover takeover = new FaderSoftTakeover();
        assertEquals(FaderSoftTakeover.Decision.ENGAGE, takeover.accept(0, 50, 49));
        assertEquals(FaderSoftTakeover.Decision.REJECT, takeover.accept(1, 100, 50));
    }

    @Test
    void dropsCrossTalkFromARestingFaderWhileAnotherOneStreams() {
        long[] t = {0};
        FaderSoftTakeover takeover = new FaderSoftTakeover(() -> t[0]);
        // Fader 1 is grabbed and moved.
        t[0] = 1000 * MS;
        assertEquals(FaderSoftTakeover.Decision.ENGAGE, takeover.accept(1, 50, 49));
        t[0] = 1040 * MS;
        assertEquals(FaderSoftTakeover.Decision.ENGAGE, takeover.accept(1, 52, 51));
        // 5 ms later the hardware re-transmits the resting fader 0 position
        // (with jitter) while fader 1 streams: it must not engage the fader.
        t[0] = 1045 * MS;
        assertEquals(FaderSoftTakeover.Decision.IGNORE, takeover.accept(0, 49, 50));
        // Once the other fader goes quiet, a real grab still works.
        t[0] = 1500 * MS;
        assertEquals(FaderSoftTakeover.Decision.ENGAGE, takeover.accept(0, 49, 50));
    }

    @Test
    void crossTalkFromAnEngagedFaderDoesNotWiggleTheValue() {
        long[] t = {0};
        FaderSoftTakeover takeover = new FaderSoftTakeover(() -> t[0]);
        // Fader 0 engaged at its value.
        t[0] = 1000 * MS;
        assertEquals(FaderSoftTakeover.Decision.ENGAGE, takeover.accept(0, 50, 49));
        // Half a second later fader 1 is grabbed (fader 0 is quiet, so its
        // grab is not mistaken for cross-talk) and then streams.
        t[0] = 1500 * MS;
        assertEquals(FaderSoftTakeover.Decision.ENGAGE, takeover.accept(1, 60, 59));
        t[0] = 1510 * MS;
        assertEquals(FaderSoftTakeover.Decision.ENGAGE, takeover.accept(1, 62, 61));
        // Fader 0 "reports" its resting position with +-2 jitter right after
        // fader 1's message: dropped instead of re-applying it (the wiggle).
        t[0] = 1520 * MS;
        assertEquals(FaderSoftTakeover.Decision.IGNORE, takeover.accept(0, 51, 51));
        t[0] = 1525 * MS;
        assertEquals(FaderSoftTakeover.Decision.IGNORE, takeover.accept(0, 49, 51));
    }

    @Test
    void crossTalkGateDoesNotBlockTheStreamingFaderItself() {
        long[] t = {0};
        FaderSoftTakeover takeover = new FaderSoftTakeover(() -> t[0]);
        t[0] = 1000 * MS;
        assertEquals(FaderSoftTakeover.Decision.ENGAGE, takeover.accept(0, 50, 49));
        // The gate only looks at OTHER faders: the streaming fader's own
        // messages are processed even though they are close to the value.
        t[0] = 1005 * MS;
        assertEquals(FaderSoftTakeover.Decision.ENGAGE, takeover.accept(0, 51, 50));
        t[0] = 1010 * MS;
        assertEquals(FaderSoftTakeover.Decision.ENGAGE, takeover.accept(0, 52, 51));
    }

    @Test
    void crossTalkGateLetsFarMovementsThrough() {
        long[] t = {0};
        FaderSoftTakeover takeover = new FaderSoftTakeover(() -> t[0]);
        t[0] = 1000 * MS;
        assertEquals(FaderSoftTakeover.Decision.ENGAGE, takeover.accept(1, 60, 59));
        // A movement far from the value is a real sweep even while another
        // fader streams: the state machine has to see it.
        t[0] = 1010 * MS;
        assertEquals(FaderSoftTakeover.Decision.REJECT, takeover.accept(0, 100, 50));
    }

    @Test
    void crossTalkGateDefersTheCatchWhileAnotherFaderStreams() {
        long[] t = {0};
        FaderSoftTakeover takeover = new FaderSoftTakeover(() -> t[0]);
        t[0] = 1000 * MS;
        assertEquals(FaderSoftTakeover.Decision.ENGAGE, takeover.accept(1, 60, 59));
        t[0] = 1010 * MS;
        assertEquals(FaderSoftTakeover.Decision.REJECT, takeover.accept(0, 100, 50));
        // The catch at the value is deferred until the other fader goes
        // quiet (within 150 ms of it, it is indistinguishable from jitter).
        t[0] = 1015 * MS;
        assertEquals(FaderSoftTakeover.Decision.IGNORE, takeover.accept(0, 49, 50));
        t[0] = 1300 * MS;
        assertEquals(FaderSoftTakeover.Decision.ENGAGE, takeover.accept(0, 49, 50));
    }
}
