package org.chat21.android.utils.image;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by frontiere21 on 30/09/16.
 */

public class ImageUtils {
//    private static final String TAG = ImageUtils.class.getName();

    /**
     * returns a drawable with the new color
     *
     * @param context
     * @param colorToId          the id of the new color to change
     * @param drawableToChangeId the id of the drawable to color
     * @return the colored drawable
     */
    public static Drawable changeDrawableColor(Context context, @ColorRes int colorToId,
                                               @DrawableRes int drawableToChangeId) {
//        Log.v(TAG, "changeDrawableColor");
        int color = context.getResources().getColor(colorToId);
        Drawable drawable = context.getResources().getDrawable(drawableToChangeId);
        drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);

        return drawable;
    }

    // source :
    // https://stackoverflow.com/questions/30527045/choosing-photo-using-new-google-photos-app-is-broken
    public static Uri getImageUrlWithAuthority(Context context, Uri uri) {
        InputStream is = null;
        if (uri.getAuthority() != null) {
            try {
                is = context.getContentResolver().openInputStream(uri);
                Bitmap bmp = BitmapFactory.decodeStream(is);
                return writeToTempImageAndGetPathUri(context, bmp);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static Uri writeToTempImageAndGetPathUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public static String getRealPathFromURI(ContentResolver contentResolver, Uri contentUri) {
        Cursor cursor = contentResolver.query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index);
        }
    }
}
