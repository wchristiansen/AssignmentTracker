package com.wchristiansen.assignmenttracker.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.wchristiansen.assignmenttracker.R;
import com.wchristiansen.assignmenttracker.adapters.AssignmentViewHolder;

/**
 * @author will
 * @version 9/27/17
 */
public class SwipeToDeleteHelper extends ItemTouchHelper.SimpleCallback {

    public static final String SWIPING_DISABLED = "disable_swiping";

    private int iconPadding;
    private Paint paint;
    private Bitmap swipeIcon;
    private Callback callback;

    public interface Callback {
        void onItemRemoved(RecyclerView.ViewHolder viewHolder, int swipeDir);
    }

    public SwipeToDeleteHelper(@NonNull Context context,
                               int dragDirection,
                               int swipeDirection,
                               @ColorRes int backgroundColor,
                               @DrawableRes int iconId,
                               Callback listener) {
        super(dragDirection, swipeDirection);
        init(context, backgroundColor, iconId, listener);
    }

    private void init(@NonNull Context context,
                      @ColorRes int backgroundColor,
                      @DrawableRes int iconId,
                      Callback listener) {
        callback = listener;

        Resources res = context.getResources();
        paint = new Paint();
        paint.setColor(res.getColor(backgroundColor));

        swipeIcon = getBitmapFromVectorDrawable(context, iconId);
        iconPadding = res.getDimensionPixelSize(R.dimen.padding_x_small);
    }

    private static Bitmap getBitmapFromVectorDrawable(@NonNull Context context,
                                                      @DrawableRes int drawableId) {
        Drawable drawable = context.getDrawable(drawableId);
        if(drawable != null) {
            Bitmap bitmap = Bitmap.createBitmap(
                    drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888
            );
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }
        return null;
    }

    @Override
    public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        // Disable swiping based on the RecyclerView's tag values
        if(recyclerView != null) {
            Object tag = recyclerView.getTag();
            if(tag != null && tag instanceof Boolean && (Boolean)tag) {
                return 0;
            }
        }
        // Disable swiping on specific elements if the flag is set
        if(viewHolder instanceof AssignmentViewHolder) {
            if(!((AssignmentViewHolder) viewHolder).canSwipeToDismiss()) {
                return 0;
            }
        }
        return super.getSwipeDirs(recyclerView, viewHolder);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView,
                          RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
        if(callback != null) {
            callback.onItemRemoved(viewHolder, swipeDir);
        }
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        // Only allow left swipe to delete. Other swipes will be ignored
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX < 0) {
            View view = viewHolder.itemView;

            float top = view.getTop();
            float right = view.getRight();
            float bottom = view.getBottom();

            // Draw Rect with varying left side, equal to the item's right side plus negative
            // displacement dX
            c.drawRect(right + dX, top, right, bottom, paint);

            // Only draw the bitmap if the icon would actually be visible "beneath" the view
            if (iconPadding < Math.abs(dX)) {
                float iconLeft = right - iconPadding - swipeIcon.getWidth();
                float iconTop = top + (bottom - top - swipeIcon.getHeight()) / 2;

                //Set the image icon for Left swipe
                c.drawBitmap(swipeIcon, iconLeft, iconTop, paint);
            }

            viewHolder.itemView.setTranslationX(dX);
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }
}