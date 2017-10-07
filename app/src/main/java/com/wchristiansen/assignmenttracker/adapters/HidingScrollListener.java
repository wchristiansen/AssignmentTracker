package com.wchristiansen.assignmenttracker.adapters;

import android.support.v7.widget.RecyclerView;

/**
 * @author will
 * @version 10/6/17
 */
public abstract class HidingScrollListener extends RecyclerView.OnScrollListener {

    private final static int HIDE_THRESHOLD = 15;

    private int scrolledDistance = 0;
    private boolean controlsVisible = true;

    public abstract void onHide();
    public abstract void onShow();

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
            onHide();
            controlsVisible = false;
            scrolledDistance = 0;
        } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
            onShow();
            controlsVisible = true;
            scrolledDistance = 0;
        }

        if((controlsVisible && dy > 0) || (!controlsVisible && dy < 0)) {
            scrolledDistance += dy;
        }
    }
}