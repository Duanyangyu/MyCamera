package com.duanyy.mycamera.cube;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.duanyy.mycamera.R;
import com.duanyy.mycamera.camera.CubeOverlay;

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

    public void updateViewMatrixEye(float eyeX,float eyeY,float eyeZ){
        mCube.updateViewMatrixEye(eyeX,eyeY,eyeZ);
        requestRender();
    }

    public void updateViewMatrixLook(float lookX,float lookY,float lookZ){
        mCube.updateViewMatrixLook(lookX,lookY,lookZ);
        requestRender();
    }

    public void updateViewMatrixUp(float upX,float upY,float upZ){
        mCube.updateViewMatrixEye(upX,upY,upZ);
        requestRender();
    }

    public void updateProjectionMatrixTop(float top){
        mCube.updateProjectionMatrixTop(top);
        requestRender();
    }

    public void updateProjectionMatrixBottom(float bottom){
        mCube.updateProjectionMatrixBottom(bottom);
        requestRender();
    }

    public void updateProjectionMatrixNear(float near){
        mCube.updateProjectionMatrixNear(near);
        requestRender();
    }

    public void updateProjectionMatrixFar(float far){
        mCube.updateProjectionMatrixFar(far);
        requestRender();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mCube.init();
        mCube.setBitmap(getResources(), R.mipmap.icon_cube);
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
