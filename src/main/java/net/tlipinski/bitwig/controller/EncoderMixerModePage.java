package net.tlipinski.bitwig.controller;

public class EncoderMixerModePage {
    public enum Page {
        PAN("Pan"),
        SEND1("Send 1"),
        SEND2("Send 2"),
        SEND3("Send 3"),
        SEND4("Send 4"),
        SEND5("Send 5"),
        SEND6("Send 6");

        Page(String displayName) {
            this.displayName = displayName;
        }

        private final String displayName;
    }

    public void selectPreviousPage(boolean shouldCycle) {
        int ord = page.ordinal() - 1;
        page = Page.values()[Math.max(ord, 0)];
    }

    public void selectNextPage(boolean shouldCycle) {
        int ord = page.ordinal() + 1;
        page = Page.values()[Math.min(ord, Page.values().length - 1)];
    }

    public String name() {
        return page.displayName;
    }

    public Page getPage() {
        return page;
    }

    private Page page = Page.PAN;
}
