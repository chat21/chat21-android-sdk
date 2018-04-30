package org.chat21.android.ui.decorations;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * Created by stefanodp91 on 27/02/18.
 */

public class ItemDecoration extends DividerItemDecoration {

    private Context mContext;
    private Drawable mDivider;
    private final Rect mBounds = new Rect();

    /**
     * Creates a divider {@link RecyclerView.ItemDecoration} that can be used with a
     * {@link android.support.v7.widget.LinearLayoutManager}.
     *
     * @param context     Current context, it will be used to access resources.
     * @param orientation Divider orientation. Should be {@link #HORIZONTAL} or {@link #VERTICAL}.
     */
    public ItemDecoration(Context context, int orientation, Drawable divider) {
        super(context, orientation);
        mContext = context;
        mDivider = divider;
    }

    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        canvas.save();
        final int leftWithMargin = convertDpToPx(56);
        final int right = parent.getWidth();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            parent.getDecoratedBoundsWithMargins(child, mBounds);
            final int bottom = mBounds.bottom + Math.round(ViewCompat.getTranslationY(child));
            final int top = bottom - mDivider.getIntrinsicHeight();
            mDivider.setBounds(leftWithMargin, top, right, bottom);
            mDivider.draw(canvas);
        }
        canvas.restore();
    }

    private int convertDpToPx(int dp) {
        return Math.round(dp *
                (mContext.getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
