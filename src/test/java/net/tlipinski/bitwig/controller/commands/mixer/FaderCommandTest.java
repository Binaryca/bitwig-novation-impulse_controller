package net.tlipinski.bitwig.controller.commands.mixer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FaderCommandTest {

    @Test
    void onlyTheMasterFaderCanFollowTheCursor() {
        assertTrue(FaderCommand.usesCursorTrack(8, true));
    }

    @Test
    void trackFadersNeverFollowTheCursor() {
        assertFalse(FaderCommand.usesCursorTrack(0, true));
        assertFalse(FaderCommand.usesCursorTrack(7, true));
    }

    @Test
    void theMasterFaderTargetsTheMasterByDefault() {
        assertFalse(FaderCommand.usesCursorTrack(8, false));
    }
}
