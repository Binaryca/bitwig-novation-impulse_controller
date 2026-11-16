package net.tlipinski.bitwig.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MidiTest {

    @Test
    void recognisesButtonPresses() {
        assertFalse(Midi.isPressed(0));
        assertTrue(Midi.isPressed(1));
        assertTrue(Midi.isPressed(127));
    }
}
