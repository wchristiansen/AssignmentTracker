package com.wchristiansen.assignmenttracker.adapters;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @author will
 * @version 10/9/17
 */
public class VerticalPaddingItemDecorator extends RecyclerView.ItemDecoration {

    private int topPadding;
    private int bottomPadding;

    public VerticalPaddingItemDecorator(int topPadding, int bottomPadding) {
        this.topPadding = topPadding;
        this.bottomPadding = bottomPadding;
    }

    @Override
    public void getItemOffsets(Rect outRect,
                               View view,
                               RecyclerView parent,
                               RecyclerView.State state) {
        // Add bottom padding if the view is the last child
        if(parent.getChildAdapterPosition(view) == state.getItemCount() - 1) {
            outRect.bottom = bottomPadding;
            outRect.top = 0;
        }

        // Add top padding if the view is the first child
        if(parent.getChildAdapterPosition(view) == 0) {
            outRect.top = topPadding;
            outRect.bottom = 0;
        }
    }
}
