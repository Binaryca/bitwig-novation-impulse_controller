package net.tlipinski.bitwig.controller;

/**
 * MIDI mapping used by the Novation Impulse template sent from this extension.
 *
 * <p>The Impulse sends most controls as Control Change messages on channel 1
 * ({@link #CH1_CC}); encoders, Shift and the encoder-mode buttons use channel 2
 * ({@link #CH2_CC}). Transport buttons are reused for scene navigation while the
 * Mix panel is open.</p>
 */
public final class Midi {

    private Midi() {
    }

    public static final int CH1_CC = 0xB0;
    public static final int CH2_CC = 0xB1;
    /**
     * Program Change on channel 1: the data knob sends these while the
     * Program Change mode (Shift + MIDI Chan) is active.
     */
    public static final int CH1_PC = 0xC0;

    public static final int TRACK_COUNT = 8;
    public static final int MASTER_TRACK_INDEX = TRACK_COUNT;
    public static final int SEND_COUNT = 6;

    public static final int FADER_FIRST = 0;
    public static final int FADER_LAST = 8;
    public static final int FADER_MASTER = 8;

    public static final int BUTTON_FIRST = 9;
    public static final int BUTTON_LAST = 17;
    public static final int BUTTON_MASTER = 17;

    public static final int CLIP_FIRST = 60;
    public static final int CLIP_LAST = 67;

    public static final int ENCODER_FIRST = 0;
    public static final int ENCODER_LAST = 7;
    public static final int ENCODER_MIXER_MODE = 9;
    public static final int ENCODER_PLUGIN_MODE = 10;
    public static final int ENCODER_PAGE_UP = 11;
    public static final int ENCODER_PAGE_DOWN = 12;
    public static final int SHIFT = 13;

    public static final int BUTTONS_MODE = 34;
    public static final int TRACK_BANK_UP = 35;
    public static final int TRACK_BANK_DOWN = 36;
    public static final int TRACK_NEXT = 37;
    public static final int TRACK_PREVIOUS = 38;
    public static final int NO_OP = 39;

    public static final int TRANSPORT_REWIND = 27;
    public static final int TRANSPORT_FORWARD = 28;
    public static final int TRANSPORT_STOP = 29;
    public static final int TRANSPORT_PLAY = 30;
    public static final int TRANSPORT_LOOP = 31;
    public static final int TRANSPORT_RECORD = 32;

    public static final int SCENE_UP = TRANSPORT_REWIND;
    public static final int SCENE_DOWN = TRANSPORT_FORWARD;
    public static final int SCENE_LAUNCH = TRANSPORT_LOOP;

    /**
     * Button and pad messages send 1 (or 127) when pressed and 0 when released.
     */
    public static boolean isPressed(int data2) {
        return data2 > 0;
    }
}
