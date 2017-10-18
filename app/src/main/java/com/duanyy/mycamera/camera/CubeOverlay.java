package com.duanyy.mycamera.camera;

/**
 * Created by duanyy on 2017/10/13.
 */

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.duanyy.mycamera.glutil.BufferUtils;
import com.duanyy.mycamera.glutil.FboHelper;
import com.duanyy.mycamera.glutil.GlUtil;
import com.duanyy.mycamera.glutil.OpenGlUtils;

import java.nio.FloatBuffer;

/**
 * 绘制在相机预览的上层。
 */
public class CubeOverlay {

    public static final String TAG = "CubeOverlay";

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mFragmentBuffer;
    private int mProgramId;
    private int mTextureId;

    private int mFrameBufferWidth;
    private int mFrameBufferHeight;

    private float[] mModelMatrix = new float[16];//model变换矩阵
    private float[] mViewMatrix = new float[16];//view变换矩阵
    private float[] mProjectionMatrix = new float[16];//4x4矩阵 投影用。 Projection：投影。
    private float[] mMVPMatrix = new float[16];

    private FboHelper mFbo;


    public void init(){
        mVertexBuffer = BufferUtils.float2Buffer(VERTEX_ARRAY);
        mFragmentBuffer = BufferUtils.float2Buffer(FRAGMENT_ARRAY);
        mProgramId = OpenGlUtils.loadProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        Log.e(TAG,"mProgramId="+mProgramId);
    }

    public void setBitmap(Resources resources, int resId){
        Bitmap bitmap = BitmapFactory.decodeResource(resources, resId);
        mTextureId = OpenGlUtils.loadTexture(bitmap, -1);
        Log.e(TAG,"mTextureId="+mTextureId);
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

    public int getTargetTexture(){
        return mFbo.textureId();
    }

    public void onSurfaceCreated() {
        Matrix.setLookAtM(mViewMatrix, 0, 3.0f, 3.0f, 10.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

    }

    public void onSurfaceChanged(int width,int height){
        mFrameBufferWidth = width;
        mFrameBufferHeight = height;
        releaseFbo();
        initFbo(width,height);
        Matrix.perspectiveM(mProjectionMatrix, 0, 45.0f, width / (float)height, 0.1f, 100.0f);
    }

    public void draw(boolean drawFramerBuffer){

        Log.e(TAG,"draw~~ " );
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT|GLES20.GL_COLOR_BUFFER_BIT);

        if (drawFramerBuffer){
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFbo.frameId());
        }

        long time = System.currentTimeMillis()%10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);
        Matrix.setIdentityM(mModelMatrix, 0); //初始化矩阵
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -5.0f);//给矩阵设置位移
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);//给矩阵设置旋转,最后三个参数为旋转轴

        GLES20.glUseProgram(mProgramId);

        int mMvpMatrixHandler = GLES20.glGetUniformLocation(mProgramId,"u_MVPMatrix");
        int sampleTextureHandler = GLES20.glGetUniformLocation(mProgramId, "u_sampleTexture");
        int mPositionHandle = GLES20.glGetAttribLocation(mProgramId, "a_position");
        int mTextureHandle = GLES20.glGetAttribLocation(mProgramId,"a_textCoord");

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glEnableVertexAttribArray(mTextureHandle);

        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        GLES20.glVertexAttribPointer(mTextureHandle, 2, GLES20.GL_FLOAT, false, 0, mFragmentBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
        GLES20.glUniform1i(sampleTextureHandler, 0);
        Matrix.multiplyMM(mMVPMatrix,0,mViewMatrix,0,mModelMatrix,0);
        Matrix.multiplyMM(mMVPMatrix,0,mProjectionMatrix,0,mMVPMatrix,0);
        GLES20.glUniformMatrix4fv(mMvpMatrixHandler,1,false,mMVPMatrix,0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 36);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureHandle);
        GLES20.glActiveTexture(0);
        GLES20.glUseProgram(0);
    }

    private static final String VERTEX_SHADER =
            "uniform mat4 u_MVPMatrix;" +
                    "attribute vec3 a_position;" +
                    "attribute vec2 a_textCoord;"+
                    "varying vec2 v_textCoord;"+
                    "void main()" +
                    "{" +
                    "    gl_Position = u_MVPMatrix * vec4(a_position.x, a_position.y, a_position.z, 1.0);" +
                    "    v_textCoord = a_textCoord;"+
                    "}";

    private static final String FRAGMENT_SHADER =
            "precision mediump float;" +
                    "varying vec2 v_textCoord;"+
                    "uniform sampler2D u_sampleTexture;"+
                    "void main(){" +
                    "  gl_FragColor = texture2D(u_sampleTexture,v_textCoord);" +
                    "}";

    private static final float[] FRAGMENT_ARRAY = {
            0f,0f,
            0f,1f,
            1f,0f,
            0f,1f,
            1f,1f,
            1f,0,

            0f,0f,
            0f,1f,
            1f,0f,
            0f,1f,
            1f,1f,
            1f,0f,

            0f,0f,
            0f,1f,
            1f,0f,
            0f,1f,
            1f,1f,
            1f,0f,

            0f,0f,
            0f,1f,
            1f,0f,
            0f,1f,
            1f,1f,
            1f,0f,

            0f,0f,
            0f,1f,
            1f,0f,
            0f,1f,
            1f,1f,
            1f,0f,

            0f,0f,
            0f,1f,
            1f,0f,
            0f,1f,
            1f,1f,
            1f,0f,
    };

    private static final float[] VERTEX_ARRAY = {
            // Front face
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            // Right face
            1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, 1.0f, -1.0f,
            // Back face
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,
            // Left face
            -1.0f, 1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f,
            // Top face
            -1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,
            // Bottom face
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
    };

}
