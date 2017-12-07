package chat21.android.messages.activites;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import chat21.android.R;
import chat21.android.core.messages.models.Message;
import chat21.android.ui.ChatUI;
import chat21.android.utils.StringUtils;
import chat21.android.utils.TimeUtils;
import chat21.android.utils.views.TouchImageView;

/**
 * Created by stefanodp91 on 25/11/2016.
 * <p>
 * Resolve Issue #32
 */
public class ImageDetailsActivity extends AppCompatActivity {
    private static final String TAG = ImageDetailsActivity.class.getName();

    private Toolbar toolbar;
    private TouchImageView mImage;
    private TextView mTitle;
    private TextView mSender;
    private TextView mTimestamp;

//    private FloatingActionButton mBtnShare;
//    private FloatingActionButton mBtnDownload;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_details);

//        // change the statusbar color
//        ThemeUtils.changeStatusBarColor(this, getResources().getColor(R.color.black));

        registerViews();
        initViews();
//        initListeners();
    }

    private Message getMessage() {
        Log.i(TAG, "getMessage");
        return (Message) getIntent().getExtras().getSerializable(ChatUI._INTENT_EXTRAS_MESSAGE);
    }

    private void registerViews() {
        Log.i(TAG, "registerViews");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mImage = (TouchImageView) findViewById(R.id.image);
        mTitle = (TextView) findViewById(R.id.image_title);
        mSender = (TextView) findViewById(R.id.sender);
        mTimestamp = (TextView) findViewById(R.id.timestamp);

//        mBtnShare = (FloatingActionButton) findViewById(R.id.share);
//        mBtnDownload = (FloatingActionButton) findViewById(R.id.download);
    }

    private void initViews() {
        Log.i(TAG, "initViews");
        initToolbar();
        setImage();
        setTitle();
        setSender();
        setTimestamp();
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

    private void initToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setImage() {
        Log.i(TAG, "setImage");

        mImage.setOnTouchImageViewListener(new TouchImageView.OnTouchImageViewListener() {
            @Override
            public void onMove() {
                RectF rect = mImage.getZoomedRect();
                float currentZoom = mImage.getCurrentZoom();
                boolean isZoomed = mImage.isZoomed();
                //Log.e("sfsdfdsf", ""+currentZoom+","+isZoomed);
                //Do whater ever stuff u want
            }
        });

        if (getMessage() != null) {
            String uri = getMessage().getText();
            if (StringUtils.isValid(uri)) {

                // https://github.com/MikeOrtiz/TouchImageView/issues/135
                Glide.with(this)
                        .load(uri)
                        .asBitmap()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                mImage.setImageBitmap(resource);
                            }
                        });

//                // make the imageview zoomable
//                // source : https://github.com/chrisbanes/PhotoView
//                PhotoViewAttacher mAttacher = new PhotoViewAttacher(mImage);
//                mAttacher.update();
            }
        }
    }

    private void setTitle() {
        Log.i(TAG, "setTitle");
        if (getMessage() != null) {
            String title = getMessage().getText();
            if (StringUtils.isValid(title)) {
                mTitle.setText(title);
            }
        }
    }

    private void setSender() {
        Log.i(TAG, "setSender");
        if (getMessage() != null) {
            String sender = getMessage().getSender_fullname();
            if (StringUtils.isValid(sender)) {
                mSender.setText(sender);
            }
        }
    }

    private void setTimestamp() {
        Log.i(TAG, "setTimestamp");
        if (getMessage() != null) {
            try {
                long timestamp = getMessage().getTimestamp();
                String formattedTimestamp = TimeUtils.getFormattedTimestamp(timestamp);
                mTimestamp.setText(formattedTimestamp);
            } catch (Exception e) {
                Log.e(TAG, "cannot retrieve the timestamp. " + e.getMessage());
            }
        }
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