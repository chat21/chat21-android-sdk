package chat21.android.core;

import android.util.Log;

/**
 * Created by stefanodp91 on 27/11/17.
 */

public final class ChatConfiguration {
    private static final String TAG = ChatConfiguration.class.getName();

    public String appId;
    public String firebaseUrl;
    public String storageBucket;

    public ChatConfiguration(Builder builder) {
        Log.d(TAG, ">>>>>> Configuration <<<<<<");

        this.appId = builder.mAppId;
        this.firebaseUrl = builder.mFirebaseUrl;
        this.storageBucket = builder.mStorageBucket;
    }

    /**
     * Creates a configuration object
     */
    public static final class Builder {
        private static final String TAG = Builder.class.getName();

        private String mAppId;
        private String mFirebaseUrl;
        private String mStorageBucket;

        public Builder(String appId) {
            Log.d(TAG, "Configuration.Builder: appId = " + appId);

            mAppId = appId;
        }

        public Builder firebaseUrl(String firebaseUrl) {
            Log.d(TAG, "Configuration.Builder.firebaseUrl: firebaseUrl = " + firebaseUrl);

            mFirebaseUrl = firebaseUrl;

            return this;
        }

        public Builder storageBucket(String storageBucket) {
            Log.d(TAG, "Configuration.Builder.storageReference: storageBucket = " + storageBucket);

            mStorageBucket = storageBucket;

            return this;
        }

        public ChatConfiguration build() {
            Log.d(TAG, "Configuration.build");

            return new ChatConfiguration(this);
        }
    }
}
