/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.image.utils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.jwtest.wishes.myapplication.R;

import java.io.IOException;

import dependce.pack.OnMatrixChangedListener;
import dependce.pack.OnPhotoTapListener;
import dependce.pack.OnSingleFlingListener;
import dependce.pack.PhotoView;


public class SimpleSampleActivity extends AppCompatActivity {

    static final String PHOTO_TAP_TOAST_STRING = "Photo Tap! X: %.2f %% Y:%.2f %% ID: %d";
    static final String SCALE_TOAST_STRING = "Scaled to: %.2ff";
    static final String FLING_LOG_STRING = "Fling velocityX: %.2f, velocityY: %.2f";

    private PhotoView mPhotoView;

    //传递Bitmap进来显示
    private Intent intent;
    private Bitmap mBitmap;//用于存放储存用的Bitmap

    private Toast mCurrentToast;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imginfo_layout);

        //获取Intent
        intent = getIntent();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("查看详情");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.inflateMenu(R.menu.main_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //保存图片到本地
                if (item.getItemId() == R.id.menu_zoom_saver) {
                    String name = intent.getExtras().getString("bitmapName");

                    try {
                        SaveBitmap.save(name, mBitmap);
                        Toast.makeText(getApplicationContext(), "保存成功！文件保存于：/sdcard/MyJwSysInfo/", Toast.LENGTH_SHORT).show();
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "保存失败，请重试", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                }
                return false;
            }
        });
        mPhotoView = (PhotoView) findViewById(R.id.iv_photo);

        //从Intent中取出值
        byte[] bis = intent.getByteArrayExtra("bitmap");
        mBitmap = BitmapFactory.decodeByteArray(bis, 0, bis.length);

        Drawable bitmap = new BitmapDrawable(mBitmap);
        mPhotoView.setImageDrawable(bitmap);

        // Lets attach some listeners, not required though!
        mPhotoView.setOnMatrixChangeListener(new MatrixChangeListener());
        mPhotoView.setOnPhotoTapListener(new PhotoTapListener());
        mPhotoView.setOnSingleFlingListener(new SingleFlingListener());
    }

    private class PhotoTapListener implements OnPhotoTapListener {

        @Override
        public void onPhotoTap(ImageView view, float x, float y) {
            float xPercentage = x * 100f;
            float yPercentage = y * 100f;

            //不需要显示触摸位置
            //showToast(String.format(PHOTO_TAP_TOAST_STRING, xPercentage, yPercentage, view == null ? 0 : view.getId()));
        }
    }

    private void showToast(CharSequence text) {
        if (mCurrentToast != null) {
            mCurrentToast.cancel();
        }

        mCurrentToast = Toast.makeText(SimpleSampleActivity.this, text, Toast.LENGTH_SHORT);
        mCurrentToast.show();
    }

    private class MatrixChangeListener implements OnMatrixChangedListener {

        @Override
        public void onMatrixChanged(RectF rect) {
            //不需要显示触摸位置
            //mCurrMatrixTv.setText(rect.toString());
        }
    }

    private class SingleFlingListener implements OnSingleFlingListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d("PhotoView", String.format(FLING_LOG_STRING, velocityX, velocityY));
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
