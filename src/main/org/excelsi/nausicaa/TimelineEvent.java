package org.excelsi.nausicaa;


public final class TimelineEvent {
    private final String _type;


    public TimelineEvent(String type) {
        _type = type;
    }

    public String getType() {
        return _type;
    }
}
