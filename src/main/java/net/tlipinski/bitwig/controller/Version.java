package net.tlipinski.bitwig.controller;

import com.bitwig.extension.controller.api.ControllerHost;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Version {
    public static String get() {
        String pomProperties = "/META-INF/maven/net.tlipinski/bitwig-novation-impulse/pom.properties";
        try (InputStream stream = Version.class.getResourceAsStream(pomProperties)) {
            if (stream == null) {
                return "???";
            }
            Properties props = new Properties();
            props.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
            return props.getProperty("version", "???");
        } catch (IOException e) {
            return "???";
        }
    }

    public static String info(ControllerHost host) {
        return "Version: "
                + "script=" + Version.get()
                + ",host=" + host.getHostVersion()
                + ",api=" + host.getHostApiVersion()
                + ",platform=" + host.getPlatformType().name();
    }
}
