package net.tlipinski.bitwig.controller;

import com.bitwig.extension.callback.SysexMidiDataReceivedCallback;
import com.bitwig.extension.controller.api.ControllerHost;
import net.tlipinski.bitwig.controller.observers.ImpulseModel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SysexCallback implements SysexMidiDataReceivedCallback {

    public SysexCallback(ControllerHost host, Controller controller, Preferences prefs) {
        this.host = host;
        this.controller = controller;
        this.prefs = prefs;
    }

    @Override
    public void sysexDataReceived(String data) {
        if (prefs.debug()) {
            host.println("Received sysex: " + data);
        }

        Matcher matcher = IMPULSE_WELCOME.matcher(data);
        if (matcher.matches()) {
            int model = Integer.valueOf(matcher.group(1), 16);
            try {
                controller.setModel(ImpulseModel.get(model));
                host.showPopupNotification("Novation Impulse " + model + " Initialized");
                controller.getSysexSend().displayText("Ready");
                controller.getSysexSend().displayNumber(model);
            } catch (IllegalArgumentException e) {
                host.errorln("Unsupported Novation Impulse model: " + model);
                controller.getSysexSend().displayText("Unknown");
            }
        }
    }

    private static final Pattern IMPULSE_WELCOME = Pattern.compile("f00020296707(..)f7");
    private final ControllerHost host;
    private final Controller controller;
    private final Preferences prefs;
}
