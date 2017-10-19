package com.duanyy.mycamera.camera;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import com.duanyy.mycamera.R;
import com.duanyy.mycamera.glutil.FboHelper;
import com.duanyy.mycamera.utils.CamParaUtil;

import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Duanyy on 2017/9/10.
 */
public class CameraGLSurfaceview extends GLSurfaceView implements GLSurfaceView.Renderer {

    private static final String TAG = "CameraGLSurfaceview";

    private Context mContext;
    private int mWidth;
    private int mHeight;
    private Camera mCamera;
    private Camera.Parameters mParams;
    private int mTextureId;
    private SurfaceTexture mSurfaceTexture;
    private DirectVideo mDirectVideo;
    private CubeOverlay mCubeOverLay;
    private BitmapOverlay mBitmapOverlay;
    private FboHelper mFbo ;

    public CameraGLSurfaceview(Context context) {
        this(context, null);
    }

    public CameraGLSurfaceview(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    public void release(){
        releaseFbo();
        releaseCamera();
    }

    private void init() {
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }


    private void initFbo(int w,int h){
        mFbo = new FboHelper(w,h);
        mFbo.createFbo();
    }

    private void releaseFbo(){
        if (mFbo != null) {
            mFbo.close();
            mFbo = null;
        }
    }

    private SurfaceTexture.OnFrameAvailableListener mFrameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {
        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            requestRender();
        }
    };

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        mTextureId = getTextureId();
        mSurfaceTexture = new SurfaceTexture(mTextureId);
        mSurfaceTexture.setOnFrameAvailableListener(mFrameAvailableListener);

        mDirectVideo = new DirectVideo(mTextureId);

        mCubeOverLay = new CubeOverlay();
        mCubeOverLay.init();
        mCubeOverLay.setBitmap(getResources(), R.mipmap.icon_cube);
        mCubeOverLay.onSurfaceCreated();

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int w, int h) {
        this.mWidth = w;
        this.mHeight = h;

        mBitmapOverlay = new BitmapOverlay();
        mBitmapOverlay.initProgram(mContext);
        mBitmapOverlay.initFrameBuffer(w,h);

        releaseFbo();
        initFbo(mWidth,mHeight);
        mDirectVideo.setFrameBuffer(mFbo);
        mCubeOverLay.onSurfaceChanged(mWidth,mHeight);
        GLES20.glViewport(0, 0, mWidth, mHeight);
        openCamera();
        Log.e(TAG,"width="+mWidth+", height="+mHeight);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        mSurfaceTexture.updateTexImage();

        int targetTexture = -1;

//        mBitmapOverlay.draw(true);
//        targetTexture = mBitmapOverlay.getTargetTexture();

        mCubeOverLay.draw(true);
        targetTexture = mCubeOverLay.getTargetTexture();

        mDirectVideo.draw(targetTexture);
        Log.e(TAG,"targetTexture="+targetTexture);

    }

    public void openCamera() {
        try {
            mCamera = Camera.open(0);
            mCamera.setPreviewTexture(mSurfaceTexture);
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    Log.e(TAG,"onPreviewFrame data.length="+data.length);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        startPreview(1.f);
    }

    private void releaseCamera(){
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
        }
    }

    private void startPreview(float previewRate){
        if(mCamera != null){
            mParams = mCamera.getParameters();
            mParams.setPictureFormat(PixelFormat.JPEG);//设置拍照后存储的图片格式
            //设置PreviewSize和PictureSize
            Camera.Size pictureSize = CamParaUtil.getInstance().getPropPictureSize(
                    mParams.getSupportedPictureSizes(),previewRate, 800);
            mParams.setPictureSize(pictureSize.width, pictureSize.height);
            Camera.Size previewSize = CamParaUtil.getInstance().getPropPreviewSize(
                    mParams.getSupportedPreviewSizes(), previewRate, 800);
            mParams.setPreviewSize(previewSize.width, previewSize.height);

            mCamera.setDisplayOrientation(90);

            List<String> focusModes = mParams.getSupportedFocusModes();
            if(focusModes.contains("continuous-video")){
                mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
            mCamera.setParameters(mParams);
            mCamera.startPreview();//开启预览

            Log.i(TAG, "最终设置:PreviewSize--With = " + mParams.getPreviewSize().width
                    + "Height = " + mParams.getPreviewSize().height);
            Log.i(TAG, "最终设置:PictureSize--With = " + mParams.getPictureSize().width
                    + "Height = " + mParams.getPictureSize().height);
        }
    }

    private int getTextureId(){
        int[] texture = new int[1];

        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        return texture[0];
    }

}
