package net.tlipinski.bitwig.controller.observers;

import com.bitwig.extension.controller.api.Device;
import com.bitwig.extension.controller.api.DeviceBank;
import net.tlipinski.bitwig.controller.Controller;
import net.tlipinski.bitwig.controller.EncoderMode;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiSend;

/**
 * Lights the track buttons according to the enabled state of the first
 * eight devices of the cursor track chain while the plugin encoder mode is
 * active (the buttons toggle the devices there). In the mixer mode the
 * buttons show mute/solo again (see MuteCallback and SoloCallback).
 */
public class DeviceOnOffObserver {

    public DeviceOnOffObserver(Controller controller, MidiSend midiSend) {
        DeviceBank bank = controller.getTracks().getDeviceBank();
        for (int i = 0; i < Midi.BUTTON_LAST - Midi.BUTTON_FIRST; i++) {
            Device device = bank.getDevice(i);
            int idx = i;
            device.isEnabled().addValueObserver((boolean enabled) -> {
                if (controller.getEncoderMode() == EncoderMode.PLUGIN) {
                    midiSend.light(idx, enabled);
                }
            });
        }
    }
}
