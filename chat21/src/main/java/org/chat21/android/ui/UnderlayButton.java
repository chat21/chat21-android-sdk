package org.chat21.android.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.TypedValue;

public class UnderlayButton {

    private Context context;
    private String text;
    private int imageResId;
    private int color;
    private int pos;
    private RectF clickRegion;
    private UnderlayButtonClickListener clickListener;

    public UnderlayButton(Context context, String text, int imageResId, int color, UnderlayButtonClickListener clickListener) {
        this.context = context;
        this.text = text;
        this.imageResId = imageResId;
        this.color = color;
        this.clickListener = clickListener;
    }

    public UnderlayButton(Context context, String text, int imageResId, int color) {
        this.context = context;
        this.text = text;
        this.imageResId = imageResId;
        this.color = color;
    }

    public void setClickListener(UnderlayButtonClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public boolean onClick(float x, float y){
        if (clickRegion != null && clickRegion.contains(x, y)){
            clickListener.onClick(pos);
            return true;
        }

        return false;
    }

    public void onDraw(Canvas canvas, RectF rect, int pos){
        Paint p = new Paint();

        // Draw background
        p.setColor(color);
        canvas.drawRect(rect, p);

        // Draw Text
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(convertDpToPx(15, context));
        textPaint.setColor(Color.WHITE);
        StaticLayout sl = new StaticLayout(text, textPaint, (int)rect.width(),
                Layout.Alignment.ALIGN_CENTER, 1, 1, false);

        canvas.save();
        Rect r = new Rect();
        float y = (rect.height() / 2f) + (r.height() / 2f) - r.bottom - (sl.getHeight() /2);
        canvas.translate(rect.left, rect.top + y);
        sl.draw(canvas);
        canvas.restore();

        clickRegion = rect;
        this.pos = pos;
    }

    private int convertDpToPx(float dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public interface UnderlayButtonClickListener {
        void onClick(int pos);
    }
}