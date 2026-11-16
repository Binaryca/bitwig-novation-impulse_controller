package net.tlipinski.bitwig.controller;

import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.MidiOut;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SysexSend {

    public SysexSend(ControllerHost host) {
        this.midiOut = host.getMidiOutPort(0);
    }

    public void initController() {
        midiOut.sendSysex(sysexHeader + " 06 01 01 01 F7");
    }

    public void sendTemplate(String template) {
        midiOut.sendSysex(template);
    }

    /**
     * Sends persistent control labels to the Impulse (Novation command 67 05).
     * The Impulse stores one eight-character label per control; the hardware
     * shows these labels when it displays control labels (Mode C).
     */
    public void sendControlLabels(String[] faders, String[] buttons, String[] knobs) {
        midiOut.sendSysex(controlLabelsSysex(FADER_ZONE, faders));
        midiOut.sendSysex(controlLabelsSysex(BUTTON_ZONE, buttons));
        midiOut.sendSysex(controlLabelsSysex(KNOB_ZONE, knobs));
    }

    static String controlLabelsSysex(int zone, String... labels) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < CONTROLS_PER_ZONE; i++) {
            String label = i < labels.length ? labels[i] : "";
            text.append(toDisplayText(label));
        }
        return sysexHeader
                + " 05 " + String.format("%02X", zone)
                + " 00 00 " + hex(text.toString()) + " F7";
    }

    /**
     * Writes a text to the eight-character text area of the Impulse display.
     * Shorter texts are padded so old characters do not remain visible.
     */
    public void displayText(String text) {
        midiOut.sendSysex(sysexHeader + "08 " + hex(toDisplayText(text)) + " F7");
    }

    /**
     * Writes a number to the three-character numeric area of the Impulse
     * display, next to the text area.
     */
    public void displayNumber(int number) {
        midiOut.sendSysex(sysexHeader + "09 " + hex(String.format("%3d", number)) + " F7");
    }

    public void displayNumber(double number, int scale) {
        displayNumber((int) (number * scale));
    }

    static String toDisplayText(String text) {
        String value = text == null ? "" : text;
        if (value.length() > maxLength) {
            value = value.substring(0, maxLength);
        }
        return String.format("%-" + maxLength + "s", value);
    }

    static String hex(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.US_ASCII);

        List<String> hexes = new ArrayList<>();
        for (byte b : bytes) {
            hexes.add(String.format("%02X", b & 0xFF));
        }

        return String.join(" ", hexes);
    }

    private final MidiOut midiOut;

    private static final String sysexHeader = "F0 00 20 29 67";
    private static final int maxLength = 8;
    private static final int CONTROLS_PER_ZONE = 9;
    private static final int FADER_ZONE = 0;
    private static final int BUTTON_ZONE = 1;
    private static final int KNOB_ZONE = 2;
}
