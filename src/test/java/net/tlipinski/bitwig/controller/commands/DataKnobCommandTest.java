package net.tlipinski.bitwig.controller.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataKnobCommandTest {

    @Test
    void computesRelativeDelta() {
        assertEquals(1, DataKnobCommand.delta(10, 11));
        assertEquals(-1, DataKnobCommand.delta(10, 9));
        assertEquals(0, DataKnobCommand.delta(5, 5));
        assertEquals(5, DataKnobCommand.delta(10, 15));
    }

    @Test
    void wrapsAcrossTheProgramNumberRange() {
        // 127 -> 0 is one step up
        assertEquals(1, DataKnobCommand.delta(127, 0));
        // 0 -> 127 is one step down
        assertEquals(-1, DataKnobCommand.delta(0, 127));
        // 126 -> 0 is two steps up
        assertEquals(2, DataKnobCommand.delta(126, 0));
        // 1 -> 127 is two steps down
        assertEquals(-2, DataKnobCommand.delta(1, 127));
    }
}
