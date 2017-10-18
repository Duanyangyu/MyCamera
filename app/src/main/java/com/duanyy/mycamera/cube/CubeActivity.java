package com.duanyy.mycamera.cube;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.duanyy.mycamera.R;

/**
 * 拖动配置ViewMatrix。
 */
public class CubeActivity extends Activity{

    private static final String TAG = "CubeActivity";

    private CubeGLSurfaceView mCubeGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cube);

        initView();
    }

    private void initView(){
        mCubeGLSurfaceView = findViewById(R.id.mCubeGLSurfaceview);
        mCubeGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

}
