package net.tlipinski.bitwig.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SysexSendTest {

    @Test
    void buildsLabelSysexForNineEightCharacterControls() {
        String sysex = SysexSend.controlLabelsSysex(2, "Knob 1", "", "Longer label text");

        String[] tokens = sysex.split(" ");
        // F0 00 20 29 67 05 <zone> 00 00 + 72 data bytes + F7
        assertEquals(82, tokens.length);
        assertEquals("05", tokens[5]);
        assertEquals("02", tokens[6]);
        assertEquals("00", tokens[7]);
        assertEquals("00", tokens[8]);
        assertEquals("F7", tokens[81]);

        assertTrue(sysex.startsWith("F0 00 20 29 67 05 02 00 00 "));
        assertTrue(sysex.endsWith(" F7"));
        // "Knob 1" padded to eight characters
        assertTrue(sysex.contains("4B 6E 6F 62 20 31 20 20"));
        // Empty label becomes eight spaces
        assertTrue(sysex.contains("20 20 20 20 20 20 20 20"));
        // Labels longer than eight characters are truncated
        assertTrue(sysex.contains("4C 6F 6E 67 65 72 20 6C"));
    }

    @Test
    void padsAndTruncatesDisplayText() {
        assertEquals("Vol 1   ", SysexSend.toDisplayText("Vol 1"));
        assertEquals("12345678", SysexSend.toDisplayText("1234567890"));
        assertEquals("        ", SysexSend.toDisplayText(null));
    }
}
