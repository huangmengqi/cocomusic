package com.vpapps.utils;

import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;

public class PausableRotateAnimation extends RotateAnimation {

    private long mElapsedAtPause=0;
    private boolean mPaused=false;

    public PausableRotateAnimation(float fromAlpha, float toAlpha, int pivotXType, float pivotXValue, int pivotYType, float pivotYValue) {
        super(fromAlpha, toAlpha,pivotXType,pivotXValue,pivotYType,pivotYValue);
    }

    @Override
    public boolean getTransformation(long currentTime, Transformation outTransformation) {
        if(mPaused && mElapsedAtPause==0) {
            mElapsedAtPause=currentTime-getStartTime();
        }
        if(mPaused)
            setStartTime(currentTime-mElapsedAtPause);
        return super.getTransformation(currentTime, outTransformation);
    }

    public void pause() {
        mElapsedAtPause=0;
        mPaused=true;
    }

    public void resume() {
        mPaused=false;
    }
}
