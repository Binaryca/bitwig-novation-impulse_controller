package net.tlipinski.bitwig.controller.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataKnobCommandTest {

    @Test
    void computesRelativeDelta() {
        assertEquals(1, DataKnobCommand.delta(10, 11));
        assertEquals(-1, DataKnobCommand.delta(10, 9));
        assertEquals(0, DataKnobCommand.delta(5, 5));
        assertEquals(5, DataKnobCommand.delta(10, 15));
    }

    @Test
    void wrapsAcrossTheProgramNumberRange() {
        // 127 -> 0 is one step up
        assertEquals(1, DataKnobCommand.delta(127, 0));
        // 0 -> 127 is one step down
        assertEquals(-1, DataKnobCommand.delta(0, 127));
        // 126 -> 0 is two steps up
        assertEquals(2, DataKnobCommand.delta(126, 0));
        // 1 -> 127 is two steps down
        assertEquals(-2, DataKnobCommand.delta(1, 127));
    }

    @Test
    void pluginDevicePrefersThePluginPresetTab() {
        String[] names = {"Bitwig Presets", "Plug-in Presets", "Samples"};
        assertEquals(1, DataKnobCommand.targetIndex(names, true));
    }

    @Test
    void pluginDeviceFallsBackToAnyPresetTab() {
        String[] names = {"Bitwig Presets", "Samples"};
        assertEquals(0, DataKnobCommand.targetIndex(names, true));
    }

    @Test
    void nonPluginDeviceSkipsThePluginPresetTab() {
        String[] names = {"Bitwig Presets", "Plug-in Presets", "Samples"};
        assertEquals(0, DataKnobCommand.targetIndex(names, false));
    }

    @Test
    void nonPluginDeviceSkipsThePluginPresetTabEvenWhenItComesFirst() {
        String[] names = {"Plug-in Presets", "Bitwig Presets", "Samples"};
        assertEquals(1, DataKnobCommand.targetIndex(names, false));
    }

    @Test
    void nonPluginDeviceFallsBackToThePluginTabWhenItIsTheOnlyOne() {
        String[] names = {"Plug-in Presets", "Samples"};
        assertEquals(0, DataKnobCommand.targetIndex(names, false));
    }

    @Test
    void withoutPresetTabsNothingIsSelected() {
        assertEquals(-1, DataKnobCommand.targetIndex(new String[]{"Samples"}, true));
        assertEquals(-1, DataKnobCommand.targetIndex(new String[]{}, false));
    }
}
