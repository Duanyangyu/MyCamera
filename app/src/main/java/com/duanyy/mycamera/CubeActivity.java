package com.duanyy.mycamera;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.duanyy.mycamera.R;
import com.duanyy.mycamera.cube.CubeGLSurfaceView;

public class CubeActivity extends Activity {

    private CubeGLSurfaceView mCubeGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cube);

        initView();
    }

    private void initView(){
        mCubeGLSurfaceView = findViewById(R.id.mCubeGLSurfaceview);
    }

    public void btnDraw(View view){
        mCubeGLSurfaceView.requestRender();
    }
}
