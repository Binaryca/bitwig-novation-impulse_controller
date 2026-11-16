package net.tlipinski.bitwig.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemplateTest {

    @Test
    void sendsExpectedTemplateDump() {
        String sysex = new Template().toSysexString();

        assertEquals(576, sysex.length());
        assertTrue(sysex.startsWith("f0002029430000"));
        assertTrue(sysex.endsWith("f7"));
        assertTrue(sysex.contains("09357f00100801"), "encoder 1");
        assertTrue(sysex.contains("08287f00100801"), "drum pad 1");
        assertTrue(sysex.contains("092a7f00100801"), "fader 1");
        assertTrue(sysex.contains("11337f00100801"), "button 1");
    }
}
