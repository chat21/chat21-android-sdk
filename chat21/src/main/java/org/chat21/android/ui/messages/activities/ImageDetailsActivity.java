package org.chat21.android.ui.messages.activities;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import org.chat21.android.R;
import org.chat21.android.core.messages.models.Message;
import org.chat21.android.ui.ChatUI;
import org.chat21.android.utils.StringUtils;
import org.chat21.android.utils.TimeUtils;

import java.io.File;

/**
 * Created by stefanodp91 on 25/11/2016.
 * <p>
 * Resolve Issue #32
 */
public class ImageDetailsActivity extends AppCompatActivity {
    private static final String TAG = ImageDetailsActivity.class.getName();

    private Message message;

    private static final int REQUEST_PHONESTORAGE = 10000;

//    private FloatingActionButton mBtnShare;
//    private FloatingActionButton mBtnDownload;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_details);

        message = (Message) getIntent().getExtras().getSerializable(ChatUI.BUNDLE_MESSAGE);

        // ### begin toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // ### end toolbar

        registerViews();

        // ### begin image
        String imgUrl = message.getImageSrc();
        setImage(imgUrl);
        // ### end image

        // ### begin title
        String title = message.getActualText();
        if (StringUtils.isValid(title)) {
            TextView mTitle = findViewById(R.id.image_title);
            mTitle.setText(title);
        }
        // ### end title

        // ### begin sender
        String sender = message.getSenderFullname();
        if (StringUtils.isValid(sender)) {
            TextView mSender = findViewById(R.id.sender);
            mSender.setText(sender);
        }
        // ### end sender

        // ### begin timestamp
        TextView mTimestamp = findViewById(R.id.timestamp);
        try {
            long timestamp = message.getTimestamp();
            String formattedTimestamp = TimeUtils.getFormattedTimestamp(this, timestamp);
            mTimestamp.setText(formattedTimestamp);
        } catch (Exception e) {
            Log.e(TAG, "cannot retrieve the timestamp. " + e.getMessage());
        }
        // ### end timestamp


//        // change the statusbar color
//        ThemeUtils.changeStatusBarColor(this, getResources().getColor(R.color.black));

//        initListeners();

        ImageView mIvDownload = findViewById(R.id.iv_download);
        mIvDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_PHONESTORAGE);
            }
        });
    }

    private void loadPermissions(String perm, int requestCode) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), perm) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, perm)) {
                ActivityCompat.requestPermissions(this, new String[]{perm}, requestCode);
            }
        } else {
            String imgUrl = message.getImageSrc();
            downloadImageNew(message.getId() ,imgUrl);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PHONESTORAGE) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String imgUrl = message.getImageSrc();
                    downloadImageNew(message.getId() ,imgUrl);
                }
            }
        }
    }

    private void downloadImageNew(String filename, String downloadUrlOfImage){
        try{
            DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            Uri downloadUri = Uri.parse(downloadUrlOfImage);
            DownloadManager.Request request = new DownloadManager.Request(downloadUri);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle(filename)
                    .setMimeType("image/jpeg")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, File.separator + filename + ".jpg");
            dm.enqueue(request);
            Toast.makeText(this, "Image download started.", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Toast.makeText(this, "Image download failed.", Toast.LENGTH_SHORT).show();
        }
    }


    private void registerViews() {
        Log.i(TAG, "registerViews");


//        mBtnShare = (FloatingActionButton) findViewById(R.id.share);
//        mBtnDownload = (FloatingActionButton) findViewById(R.id.download);
    }


//    private void initListeners() {
//        mBtnShare.setOnClickListener(onShareClickListener);
//        mBtnDownload.setOnClickListener(onDownloadClickListener);
//    }
//
//    private View.OnClickListener onShareClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            Snackbar.make(findViewById(R.id.coordinator), "share pressed", Snackbar.LENGTH_LONG).show();
//        }
//    };
//
//    private View.OnClickListener onDownloadClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            Snackbar.make(findViewById(R.id.coordinator), "download pressed", Snackbar.LENGTH_LONG).show();
//        }
//    };


    private void setImage(String imgUrl) {
        Log.i(TAG, "setImage");

        final ImageView mImage = findViewById(R.id.image);

//        mImage.setOnTouchImageViewListener(new TouchImageView.OnTouchImageViewListener() {
//            @Override
//            public void onMove() {
////                RectF rect = mImage.getZoomedRect();
////                float currentZoom = mImage.getCurrentZoom();
////                boolean isZoomed = mImage.isZoomed();
//            }
//        });


        Glide.with(getApplicationContext())
                .load(imgUrl)
                .into(mImage);

        // TODO:
        // https://github.com/MikeOrtiz/TouchImageView/issues/135
//        Glide.with(this)
//                .load(imgUrl)
//                .asBitmap()
//                .into(new SimpleTarget<Bitmap>() {
//                    @Override
//                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> glideAnimation) {
//                        mImage.setImageBitmap(resource);
//                    }
//                });

//                // make the imageview zoomable
//                // source : https://github.com/chrisbanes/PhotoView
//                PhotoViewAttacher mAttacher = new PhotoViewAttacher(mImage);
//                mAttacher.update();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}