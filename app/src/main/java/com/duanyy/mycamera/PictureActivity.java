package com.duanyy.mycamera;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.duanyy.mycamera.picture.PictureGLSurfaceView;

public class PictureActivity extends Activity {

    private PictureGLSurfaceView mSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        initView();
    }

    private void initView(){
        mSurfaceView = (PictureGLSurfaceView) findViewById(R.id.mPictureGLSurfaceview);

    }


    public void btnDraw(View view){
        mSurfaceView.requestRender();
    }

}
