package net.tlipinski.bitwig.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ControlLabelsTest {

    @Test
    void zonesHaveNineLabelsOfAtMostEightCharacters() {
        String[][] zones = {ControlLabels.FADERS, ControlLabels.BUTTONS, ControlLabels.KNOBS};

        for (String[] zone : zones) {
            assertEquals(9, zone.length);
            for (String label : zone) {
                assertTrue(label.length() <= 8, "Label too long: " + label);
            }
        }
    }
}
