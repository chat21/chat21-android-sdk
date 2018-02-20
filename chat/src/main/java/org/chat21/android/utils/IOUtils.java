package org.chat21.android.utils;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
//        Log.d(TAG, "saveObjectToFile");
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
//        Log.d(TAG, "getObjectFromFile");
//        Log.d(TAG, "context: " + context);

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
//        Log.d(TAG, "deleteObject");
        boolean success = context.deleteFile(filename);

        if (success) {
            Log.d(TAG, filename + " deleted from disk with success");
        } else {
            Log.e(TAG, "cannot delete object " + filename + " from disk");
        }
        return success;
    }
}