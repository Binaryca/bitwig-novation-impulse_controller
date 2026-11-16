package net.tlipinski.bitwig.controller;

import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.SettableEnumValue;
import com.bitwig.extension.controller.api.Signal;

public class Preferences {

    public Preferences(ControllerHost host, SysexSend sysexSend) {
        SettableEnumValue debugLogging = host.getPreferences().getEnumSetting("Debug logging", "Debug", new String[]{"on", "off"}, "off");
        debugLogging.addValueObserver((String value) -> {
            this.debug = value.equals("on");
            host.println("Debug logging: " + value);
        });

        SettableEnumValue controlLabels = host.getPreferences().getEnumSetting("Control labels", "Display", new String[]{"on", "off"}, "off");
        this.controlLabels = "on".equals(controlLabels.get());
        controlLabels.addValueObserver((String value) -> {
            this.controlLabels = value.equals("on");
            host.println("Control labels: " + value);
            if (this.controlLabels) {
                sysexSend.sendControlLabels(ControlLabels.FADERS, ControlLabels.BUTTONS, ControlLabels.KNOBS);
            }
        });

        SettableEnumValue sendTemplateAtStartup = host.getPreferences().getEnumSetting("Send template at startup", "MIDI", new String[]{"on", "off"}, "on");
        this.sendTemplateAtStartup = !"off".equals(sendTemplateAtStartup.get());
        sendTemplateAtStartup.addValueObserver((String value) -> {
            this.sendTemplateAtStartup = value.equals("on");
            host.println("Send template at startup: " + value);
        });

        Signal sendTemplate = host.getPreferences().getSignalSetting("Template", "MIDI", "Send template");
        sendTemplate.addSignalObserver(new SendTemplateCallback(host, sysexSend));
    }

    public boolean debug() {
        return debug;
    }

    public boolean controlLabels() {
        return controlLabels;
    }

    public boolean sendTemplateAtStartup() {
        return sendTemplateAtStartup;
    }

    private boolean debug = false;
    private boolean controlLabels = false;
    private boolean sendTemplateAtStartup = true;
}
