package org.chat21.android.storage;

import android.content.Context;

import androidx.annotation.NonNull;

import org.chat21.android.core.users.models.IChatUser;

import java.io.File;

public interface IStorageHandler {
    void uploadFile(@NonNull final Context context, @NonNull String mediaType, @NonNull final File fileToUpload,
                    @NonNull final IChatUser recipient, @NonNull final OnUploadedCallback callback);
}