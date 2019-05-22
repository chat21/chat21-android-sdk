package org.chat21.android.utils.image;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;

import java.security.MessageDigest;

/**
 * Created by stefano on 19/04/2016.
 */
public class CropCircleTransformation implements Transformation<Bitmap> {

    private BitmapPool mBitmapPool;

    public CropCircleTransformation(Context context) {
        this(Glide.get(context).getBitmapPool());
    }

    public CropCircleTransformation(BitmapPool pool) {
        this.mBitmapPool = pool;
    }

    // TODO:
//    @Override
//    public Resource<Bitmap> transform(Resource<Bitmap> resource, int outWidth, int outHeight) {
//        Bitmap source = resource.get();
//        int size = Math.min(source.getWidth(), source.getHeight());
//
//        int width = (source.getWidth() - size) / 2;
//        int height = (source.getHeight() - size) / 2;
//
//        Bitmap bitmap = mBitmapPool.get(size, size, Bitmap.Config.ARGB_8888);
//        if (bitmap == null) {
//            bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
//        }
//
//        Canvas canvas = new Canvas(bitmap);
//        Paint paint = new Paint();
//        BitmapShader shader =
//                new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
//        if (width != 0 || height != 0) {
//            // source isn't square, move viewport to center
//            Matrix matrix = new Matrix();
//            matrix.setTranslate(-width, -height);
//            shader.setLocalMatrix(matrix);
//        }
//        paint.setShader(shader);
//        paint.setAntiAlias(true);
//
//        float r = size / 2f;
//        canvas.drawCircle(r, r, r, paint);
//
//        return BitmapResource.obtain(bitmap, mBitmapPool);
//    }
//
//    @Override public String getId() {
//        return "CropCircleTransformation()";
//    }

    @NonNull
    @Override
    public Resource<Bitmap> transform(@NonNull Context context, @NonNull Resource<Bitmap> resource, int outWidth, int outHeight) {
        return null;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {

    }
}