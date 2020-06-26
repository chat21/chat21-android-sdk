package org.chat21.android.utils.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.TransformationUtils;

import java.security.MessageDigest;

import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.PAINT_FLAGS;

public class PositionedCropTransformation extends BitmapTransformation {

    private static final String ID = PositionedCropTransformation.class.getName();
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

    private float xPercentage = 0.5f;
    private float yPercentage = 0.5f;

    public PositionedCropTransformation(Context context) {
        super();
    }

    public PositionedCropTransformation(Context context, @FloatRange(from = 0.0, to = 1.0) float xPercentage, @FloatRange(from = 0.0, to = 1.0) float yPercentage) {
        super();
        this.xPercentage = xPercentage;
        this.yPercentage = yPercentage;
    }

    // Bitmap doesn't implement equals, so == and .equals are equivalent here.
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        final Bitmap toReuse = pool.get(outWidth, outHeight, toTransform.getConfig() != null
                ? toTransform.getConfig() : Bitmap.Config.ARGB_8888);
        Bitmap transformed = crop(toReuse, toTransform, outWidth, outHeight, xPercentage, yPercentage);
        if (toReuse != transformed) {
            pool.put(toReuse);
            //toReuse.recycle();
        }
        return transformed;
    }

    /**
     * A potentially expensive operation to crop the given Bitmap so that it fills the given dimensions. This operation
     * is significantly less expensive in terms of memory if a mutable Bitmap with the given dimensions is passed in
     * as well.
     *
     * @param recycled    A mutable Bitmap with dimensions width and height that we can load the cropped portion of toCrop
     *                    into.
     * @param toCrop      The Bitmap to resize.
     * @param width       The width in pixels of the final Bitmap.
     * @param height      The height in pixels of the final Bitmap.
     * @param xPercentage The horizontal percentage of the crop. 0.0f => left, 0.5f => center, 1.0f => right or anything in between 0 and 1
     * @param yPercentage The vertical percentage of the crop. 0.0f => top, 0.5f => center, 1.0f => bottom or anything in between 0 and 1
     * @return The resized Bitmap (will be recycled if recycled is not null).
     */
    private static Bitmap crop(Bitmap recycled, Bitmap toCrop, int width, int height, float xPercentage, float yPercentage) {
        if (toCrop == null) {
            return null;
        } else if (toCrop.getWidth() == width && toCrop.getHeight() == height) {
            return toCrop;
        }
        // From ImageView/Bitmap.createScaledBitmap.
        final float scale;
        float dx = 0, dy = 0;
        Matrix m = new Matrix();
        if (toCrop.getWidth() * height > width * toCrop.getHeight()) {
            scale = (float) height / (float) toCrop.getHeight();
            dx = (width - toCrop.getWidth() * scale);
            dx *= xPercentage;
        } else {
            scale = (float) width / (float) toCrop.getWidth();
            dy = (height - toCrop.getHeight() * scale);
            dy *= yPercentage;
        }

        m.setScale(scale, scale);
        m.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
        final Bitmap result;
        if (recycled != null) {
            result = recycled;
        } else {
            result = Bitmap.createBitmap(width, height, getSafeConfig(toCrop));
        }

        // We don't add or remove alpha, so keep the alpha setting of the Bitmap we were given.
        TransformationUtils.setAlpha(toCrop, result);

        Canvas canvas = new Canvas(result);
        Paint paint = new Paint(PAINT_FLAGS);
        canvas.drawBitmap(toCrop, m, paint);
        return result;
    }

    private static Bitmap.Config getSafeConfig(Bitmap bitmap) {
        return bitmap.getConfig() != null ? bitmap.getConfig() : Bitmap.Config.ARGB_8888;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PositionedCropTransformation;
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
    }
}