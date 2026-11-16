package net.tlipinski.bitwig.controller;

/**
 * Default persistent control labels written to the Impulse with the Novation
 * command 67 05 (see docs/HARDWARE.md). Each zone holds nine controls with
 * eight characters each; the Impulse pads shorter labels with spaces.
 */
public final class ControlLabels {

    private ControlLabels() {
    }

    public static final String[] FADERS = {
            "Vol 1", "Vol 2", "Vol 3", "Vol 4", "Vol 5", "Vol 6", "Vol 7", "Vol 8", "Master"
    };

    public static final String[] BUTTONS = {
            "Mute 1", "Mute 2", "Mute 3", "Mute 4", "Mute 5", "Mute 6", "Mute 7", "Mute 8", "Mute M"
    };

    public static final String[] KNOBS = {
            "Knob 1", "Knob 2", "Knob 3", "Knob 4", "Knob 5", "Knob 6", "Knob 7", "Knob 8", ""
    };
}
