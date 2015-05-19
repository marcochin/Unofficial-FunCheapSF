package com.chin.marco.uofuncheapsf;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.chin.marco.uofuncheapsf.utils.StringUtil;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * Created by Marco on 4/8/2015.
 */
public class ImageActivity extends Activity {
    public static final String IMAGE_ACTIVITY_TAG = "imgActivity";
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        mImageView = (ImageView) findViewById(R.id.thumbnail_activity_image);
        String rawImg = getRawImage(getIntent().getStringExtra(IMAGE_ACTIVITY_TAG));

        findViewById(R.id.image_activity_background).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Picasso.with(this)
                .load(rawImg)
                .into(mImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        findViewById(R.id.progress_wheel).setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onError() {

                    }
                });
    }

    private String getRawImage(String img) {
        //http://cdn.funcheap.com/wp-content/uploads/2015/04/MattKepnes1-175x130.jpg
        //http://cdn.funcheap.com/wp-content/uploads/2014/04/rsd15-630x420-175x130.jpg
        //need to remove the "-175x130" to get raw img
        return StringUtil.replaceLast(img, "-[0-9]+x[0-9]+", "");
    }

    @Override
    protected void onDestroy() {
        Picasso.with(this).cancelRequest(mImageView);
        super.onDestroy();
    }
}