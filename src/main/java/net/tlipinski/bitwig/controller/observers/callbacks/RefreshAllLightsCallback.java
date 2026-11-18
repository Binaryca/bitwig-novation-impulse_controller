package net.tlipinski.bitwig.controller.observers.callbacks;

import com.bitwig.extension.callback.ObjectValueChangedCallback;
import com.bitwig.extension.controller.api.Device;
import com.bitwig.extension.controller.api.DeviceBank;
import com.bitwig.extension.controller.api.Track;
import net.tlipinski.bitwig.controller.ButtonsMode;
import net.tlipinski.bitwig.controller.EncoderMode;
import net.tlipinski.bitwig.controller.Midi;
import net.tlipinski.bitwig.controller.MidiSend;
import net.tlipinski.bitwig.controller.Controller;

import java.util.List;

public class RefreshAllLightsCallback implements ObjectValueChangedCallback<ButtonsMode> {
    private final Controller controller;
    private final MidiSend midiSend;

    public RefreshAllLightsCallback(Controller controller, MidiSend midiSend) {
        this.controller = controller;
        this.midiSend = midiSend;
    }

    @Override
    public void valueChanged(ButtonsMode newValue) {
        // In the plugin encoder mode the buttons address devices, so their
        // LEDs show the device enabled states regardless of the mute/solo
        // button mode.
        if (controller.getEncoderMode() == EncoderMode.PLUGIN) {
            refreshDeviceLights();
            return;
        }

        List<Track> all = controller.getTracks().getAll();
        for (int i = 0; i < all.size(); i++) {
            Track track = all.get(i);

            if (newValue == ButtonsMode.MUTE) {
                handleMute(i, track.mute().get());
            }
            if (newValue == ButtonsMode.SOLO) {
                handleSolo(i, track.solo().get());
            }
        }
    }

    private void refreshDeviceLights() {
        DeviceBank bank = controller.getTracks().getDeviceBank();
        int count = Midi.BUTTON_LAST - Midi.BUTTON_FIRST;
        for (int i = 0; i < count; i++) {
            Device device = bank.getDevice(i);
            midiSend.light(i, device.exists().get() && device.isEnabled().get());
        }
        // The master button has no device: its LED goes off in this mode.
        midiSend.light(count, false);
    }

    private void handleMute(int idx, boolean mute) {
        if (controller.getButtonsMode() == ButtonsMode.MUTE) {
            if (controller.getTracks().isNotEmpty(idx)) {
                midiSend.light(idx, !mute);
            } else {
                midiSend.light(idx, false);
            }
        }
    }

    private void handleSolo(int idx, boolean solo) {
        if (controller.getButtonsMode() == ButtonsMode.SOLO) {
            if (controller.getTracks().isNotEmpty(idx)) {
                midiSend.light(idx, solo);
            } else {
                midiSend.light(idx, false);
            }
        }
    }
}
