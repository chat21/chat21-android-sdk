package org.chat21.android.ui.messages.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

import org.chat21.android.R;
import org.chat21.android.core.messages.models.Message;
import org.chat21.android.ui.ChatUI;
import org.chat21.android.utils.StringUtils;
import org.chat21.android.utils.TimeUtils;
import org.chat21.android.utils.views.TouchImageView;

import java.util.Map;

/**
 * Created by stefanodp91 on 25/11/2016.
 * <p>
 * Resolve Issue #32
 */
public class ImageDetailsActivity extends AppCompatActivity {
    private static final String TAG = ImageDetailsActivity.class.getName();

    private Message message;

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