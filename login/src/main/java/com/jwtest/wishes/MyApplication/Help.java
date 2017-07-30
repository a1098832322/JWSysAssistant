package com.jwtest.wishes.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by 10988 on 2017/4/28.
 */

public class Help extends Activity {
    private ImageView mImgBack;
    private TextView busDetailContent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_layout2);
        mImgBack = (ImageView) findViewById(R.id.title_btn_back);

        busDetailContent = (TextView) findViewById(R.id.txt);
        busDetailContent.setMovementMethod(ScrollingMovementMethod.getInstance());

        mImgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
