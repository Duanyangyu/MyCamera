package com.duanyy.mycamera.cube;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.duanyy.mycamera.R;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by duanyy on 2017/9/30.
 */

public class CubeGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer {

    private static final String TAG = "CubeGLSurfaceView";

    private Context mContext;
    private Cube mCube;

    public CubeGLSurfaceView(Context context) {
        this(context,null);
    }

    public CubeGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    private void init(){
        mCube = new Cube();

        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mCube.init();
        mCube.setBitmap(getResources(), R.mipmap.icon_test);
        mCube.onSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mCube.onSurfaceChanged(width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mCube.draw();
    }
}
