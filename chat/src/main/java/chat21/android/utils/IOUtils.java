package chat21.android.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IOUtils {
    private static final String TAG = IOUtils.class.getName();

    /**
     * Serializes to the sdcard an object
     *
     * @param context
     * @param filename the object unique filename
     * @param object   the objectì
     * @return true if success, false otherwise
     */
    public static boolean saveObjectToFile(Context context, String filename, Object object) {
        Log.d(TAG, "saveObjectToFile");
        try {
            //Open a private file associated with this Context's application package
            //for writing. Creates the file if it doesn't already exist.
            FileOutputStream fStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            //Select where you wish to save the file...
            ObjectOutputStream oStream = new ObjectOutputStream(fStream);

            oStream.writeObject(object);
            oStream.flush();
            oStream.close();

            Log.d(TAG, filename + " serialized to disk with success");
        } catch (Exception e) {
            Log.e(TAG, "cannot serialize " + filename + " to disk. " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Funzione che permette di recuperare un oggetto dal file salvato
     * in modo persistente su disco.
     */

    /**
     * Retrieves a serialized obejct from the disk
     *
     * @param context
     * @param filename the object unique filename
     * @return the serialized object
     */
    public static Object getObjectFromFile(Context context, String filename) {
        Log.d(TAG, "getObjectFromFile");
        Log.d(TAG, "context: " + context);

        try {
            FileInputStream inputStream = context.openFileInput(filename);
            ObjectInputStream ois = new ObjectInputStream(inputStream);
            Object object = ois.readObject();
            ois.close();
            Log.d(TAG, filename + " retrieved from disk with success");
            return object;
        } catch (Exception e) {
            Log.e(TAG, "cannot retrieve the serialize " + filename + " from disk. " + e.getMessage());
        }
        return null;
    }


    /**
     * Funzione che permette di cancellare un oggetto precedentemente salvato in un file su disco.
     *
     * @param context
     * @param filename the object unique filename
     * @return true if success, false otherwise
     */
    public static boolean deleteObject(Context context, String filename) {
        Log.d(TAG, "deleteObject");
        boolean success = context.deleteFile(filename);

        if (success) {
            Log.d(TAG, filename + " deleted from disk with success");
        } else {
            Log.e(TAG, "cannot delete object " + filename + " from disk");
        }
        return success;
    }

    /**
     * Return the app version label
     *
     * @param context
     * @return the app version
     */
    public static String getAppVersion(Context context) {
        PackageInfo pInfo;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Returns the file type
     *
     * @param file    the file which wants to get the type
     * @return Type.Image if the file extensions if one between (jpg, jpeg, gif, png) - Type.File otherwise
     */
    public static Type getType(File file) {

        // retrieve the extension from the file uri
        String extension = getExtensionFromUri(file);
        Log.d(TAG, "extension == " + extension);

        if (isImage(extension)) {
            return Type.Image;
        } else {
            return Type.File;
        }
    }

    /**
     * Returns the mime type from a file
     * <p>
     * source :
     * http://www.edumobile.org/android/get-file-extension-and-mime-type-in-android-development/
     *
     * @param file    the file which wants to get the mime type
     * @return the mime type
     */
    private static String getExtensionFromUri(File file) {
        Uri fileUri = Uri.fromFile(file);

        String fileExtension
                = MimeTypeMap.getFileExtensionFromUrl(fileUri.toString());

       return  fileExtension;
    }


    private static boolean isImage(String extension) {
        boolean isImage = false;

        // image extensions
        for (String currentExtension : getImageExtensions()) {
            if (extension.equalsIgnoreCase(currentExtension)) {
                isImage = true;
                break;
            }
        }

        return isImage;
    }

    private static List<String> getImageExtensions() {
        List<String> list = new ArrayList<>();
        list.add("jpg");
        list.add("jpeg");
        list.add("gif");
        list.add("png");
        return list;
    }


    public enum Type {
        Image,
        File
    }
}