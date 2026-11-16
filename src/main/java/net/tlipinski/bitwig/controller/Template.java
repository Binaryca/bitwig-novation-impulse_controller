package net.tlipinski.bitwig.controller;

/**
 * SysEx template sent to the Impulse from the extension preferences.
 *
 * <p>Field order is significant: it is the order expected by the Impulse
 * template dump format. Keep the constants in this order.</p>
 */
public class Template {
    private static final String header     = "f0002029430000";

    private static final String name       = "4269747769672020";
    private static final String channel    = "00";
    private static final String port       = "03";
    private static final String velocity   = "02";
    private static final String aftertouch = "01";
    private static final String octave     = "40";
    private static final String transpose  = "0b";
    private static final String zones      = "00";

    private static final String zone1      = "243b400000";
    private static final String zone2      = "3c60400100";
    private static final String zone3      = "2454401004";
    private static final String zone4      = "2454401004";

    private static final String encoder1   = "09357f00100801";
    private static final String encoder2   = "09367f00100801";
    private static final String encoder3   = "09377f00100801";
    private static final String encoder4   = "09387f00100801";
    private static final String encoder5   = "09397f00100801";
    private static final String encoder6   = "093a7f00100801";
    private static final String encoder7   = "093b7f00100801";
    private static final String encoder8   = "093c7f00100801";

    private static final String drumPad1   = "08287f00100801";
    private static final String drumPad2   = "08297f00100801";
    private static final String drumPad3   = "082a7f00100801";
    private static final String drumPad4   = "082b7f00100801";
    private static final String drumPad5   = "08247f00100801";
    private static final String drumPad6   = "08257f00100801";
    private static final String drumPad7   = "08267f00100801";
    private static final String drumPad8   = "08277f00100801";

    private static final String fader1     = "092a7f00100801";
    private static final String fader2     = "092b7f00100801";
    private static final String fader3     = "092c7f00100801";
    private static final String fader4     = "092d7f00100801";
    private static final String fader5     = "092e7f00100801";
    private static final String fader6     = "092f7f00100801";
    private static final String fader7     = "09307f00100801";
    private static final String fader8     = "09317f00100801";
    private static final String faderM     = "09327f00100801";

    private static final String button1    = "11337f00100801";
    private static final String button2    = "11347f00100801";
    private static final String button3    = "11357f00100801";
    private static final String button4    = "11367f00100801";
    private static final String button5    = "11377f00100801";
    private static final String button6    = "11387f00100801";
    private static final String button7    = "11397f00100801";
    private static final String button8    = "113a7f00100801";
    private static final String buttonM    = "113b7f00100801";

    private static final String bank       = "09017f00100801";

    private static final String end        = "f7";

    String toSysexString() {
        return String.join("",
                header,
                name,
                channel,
                port,
                velocity,
                aftertouch,
                octave,
                transpose,
                zones,
                zone1,
                zone2,
                zone3,
                zone4,
                encoder1, encoder2, encoder3, encoder4,
                encoder5, encoder6, encoder7, encoder8,
                drumPad1, drumPad2, drumPad3, drumPad4,
                drumPad5, drumPad6, drumPad7, drumPad8,
                fader1, fader2, fader3, fader4,
                fader5, fader6, fader7, fader8, faderM,
                button1, button2, button3, button4,
                button5, button6, button7, button8, buttonM,
                bank,
                end);
    }
}
