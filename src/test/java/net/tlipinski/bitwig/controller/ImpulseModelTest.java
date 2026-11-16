package net.tlipinski.bitwig.controller;

import net.tlipinski.bitwig.controller.observers.ImpulseModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImpulseModelTest {

    @Test
    void mapsKeyCountsToModels() {
        assertSame(ImpulseModel.IMPULSE_25, ImpulseModel.get(25));
        assertSame(ImpulseModel.IMPULSE_49, ImpulseModel.get(49));
        assertSame(ImpulseModel.IMPULSE_61, ImpulseModel.get(61));
        assertThrows(IllegalArgumentException.class, () -> ImpulseModel.get(88));
    }

    @Test
    void reportsModelCapabilities() {
        assertTrue(ImpulseModel.IMPULSE_25.isImpulse25());
        assertFalse(ImpulseModel.IMPULSE_25.isImpulse49or61());
        assertTrue(ImpulseModel.IMPULSE_61.isImpulse49or61());
    }
}
