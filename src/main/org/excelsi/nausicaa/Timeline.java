package org.excelsi.nausicaa;


import java.util.ArrayList;
import java.util.List;


public final class Timeline {
    private final List<TimelineListener> _listeners = new ArrayList<>();


    public Timeline() {
    }

    public void addTimelineListener(TimelineListener listener) {
        _listeners.add(listener);
    }

    public void removeTimelineListener(TimelineListener listener) {
        _listeners.remove(listener);
    }

    public void notifyListeners(TimelineEvent e) {
        for(TimelineListener l:new ArrayList<>(_listeners)) {
            l.timelineChanged(e);
        }
    }
}
