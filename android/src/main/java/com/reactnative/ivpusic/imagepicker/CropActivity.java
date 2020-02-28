package com.reactnative.ivpusic.imagepicker;

import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.yalantis.ucrop.UCropActivity;
import com.yalantis.ucrop.view.GestureCropImageView;

public class CropActivity extends UCropActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        ViewGroup parent = findViewById(R.id.ucrop_photobox);

        View toolBar = findViewById(R.id.toolbar);

        parent.removeView(toolBar);
        parent.addView(toolBar);


        View frame = findViewById(R.id.ucrop_frame);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) frame.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.addRule(RelativeLayout.BELOW, 0);
        frame.setLayoutParams(params);

        int left = getIntent().getIntExtra("LEFT", 0);
        int top = getIntent().getIntExtra("TOP", 0);
        int right = getIntent().getIntExtra("RIGHT", 0);
        int bottom = getIntent().getIntExtra("BOTTOM", 0);

        Log.i("AAAAA",  left + "  " + top +"  " +  right +"   "+ bottom);
        if (left != 0 || top != 0 || right != 0 || bottom != 0) {
            GestureCropImageView cropView = findViewById(R.id.image_view_crop);
            cropView.setCropRect(new RectF(left, top, right, bottom));
        }


    }


}
