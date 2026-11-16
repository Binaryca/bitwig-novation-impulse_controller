package net.tlipinski.bitwig.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EncoderMixerModePageTest {

    @Test
    void pagesDoNotWrapAround() {
        EncoderMixerModePage page = new EncoderMixerModePage();

        assertEquals(EncoderMixerModePage.Page.PAN, page.getPage());
        assertEquals("Pan", page.name());

        page.selectNextPage(false);
        assertEquals(EncoderMixerModePage.Page.SEND1, page.getPage());

        page.selectNextPage(false);
        assertEquals(EncoderMixerModePage.Page.SEND2, page.getPage());

        page.selectNextPage(false);
        assertEquals(EncoderMixerModePage.Page.SEND3, page.getPage());

        page.selectNextPage(false);
        page.selectNextPage(false);
        page.selectNextPage(false);
        assertEquals(EncoderMixerModePage.Page.SEND6, page.getPage());

        page.selectNextPage(false);
        assertEquals(EncoderMixerModePage.Page.SEND6, page.getPage());

        page.selectPreviousPage(false);
        assertEquals(EncoderMixerModePage.Page.SEND5, page.getPage());

        page.selectPreviousPage(false);
        assertEquals(EncoderMixerModePage.Page.SEND4, page.getPage());

        page.selectPreviousPage(false);
        page.selectPreviousPage(false);
        page.selectPreviousPage(false);
        page.selectPreviousPage(false);
        assertEquals(EncoderMixerModePage.Page.PAN, page.getPage());

        page.selectPreviousPage(false);
        assertEquals(EncoderMixerModePage.Page.PAN, page.getPage());
    }
}
