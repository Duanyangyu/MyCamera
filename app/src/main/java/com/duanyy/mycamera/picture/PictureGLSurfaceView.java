package com.duanyy.mycamera.picture;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.duanyy.mycamera.R;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by duanyy on 2017/9/25.
 */

public class PictureGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer {

    private static final String TAG = "PictureGLSurfaceView";

    private Context mContext;
    private PictureRender mPictureRender;

    public PictureGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    private void init(){
        mPictureRender = new PictureRender();


        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mPictureRender.init();
        mPictureRender.setBitmap(getResources(), R.mipmap.icon_test);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mPictureRender.draw();
    }
}
