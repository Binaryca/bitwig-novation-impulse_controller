package net.tlipinski.bitwig.controller.observers;

import net.tlipinski.bitwig.controller.Controller;
import net.tlipinski.bitwig.controller.FaderSoftTakeover;
import net.tlipinski.bitwig.controller.SysexSend;
import net.tlipinski.bitwig.controller.observers.callbacks.DisplayTrackBankRangeCallback;

public class TrackBankIndexObserver {
    public TrackBankIndexObserver(Controller controller, SysexSend sysexSend, FaderSoftTakeover faderSoftTakeover) {
        // track.scrollPosition().get() was showing stale values
        controller.getTracks().getTrackBank().scrollPosition().addValueObserver(position -> {
            // The faders control different tracks after a bank change: their
            // takeover states have to re-engage for the new values.
            faderSoftTakeover.resetAll();
            new DisplayTrackBankRangeCallback(sysexSend).valueChanged(position);
        });
    }
}
