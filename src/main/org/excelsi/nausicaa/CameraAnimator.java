package org.excelsi.nausicaa;


import javafx.animation.*;


public class CameraAnimator extends AnimationTimer {
    private final Runnable _r;
    private long _lastExecution;


    public CameraAnimator(Runnable r) {
        _r = r;
    }

    @Override public void handle(long now) {
        if(_lastExecution==0||_lastExecution+100<now) {
            _r.run();
            _lastExecution = now;
        }
    }
}
