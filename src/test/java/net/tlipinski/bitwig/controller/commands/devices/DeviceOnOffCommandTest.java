package net.tlipinski.bitwig.controller.commands.devices;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeviceOnOffCommandTest {

    @Test
    void mapsTrackButtonsToDeviceIndices() {
        assertEquals(0, DeviceOnOffCommand.deviceIndex(9));
        assertEquals(3, DeviceOnOffCommand.deviceIndex(12));
        assertEquals(7, DeviceOnOffCommand.deviceIndex(16));
    }

    @Test
    void masterButtonHasNoDevice() {
        assertEquals(-1, DeviceOnOffCommand.deviceIndex(17));
    }

    @Test
    void foreignCcNumbersHaveNoDevice() {
        assertEquals(-1, DeviceOnOffCommand.deviceIndex(8));
        assertEquals(-1, DeviceOnOffCommand.deviceIndex(18));
    }
}
