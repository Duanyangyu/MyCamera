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
import com.duanyy.mycamera.glutil.OpenGlUtils;

import java.nio.FloatBuffer;

/**
 * 绘制在相机预览的上层。
 */
public class Overlay {

    public static final String TAG = "Overlay";

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

    public void onSurfaceCreated() {
        //eye 表示 camera/viewer 的位置
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 1f;

        // We are looking toward the distance
        //look 表示相机或眼睛的焦点
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        //up 表示 eye 的正上方向，注意 up 只表示方向，与大小无关
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }

    public void onSurfaceChanged(int width,int height){
        mFrameBufferWidth = width;
        mFrameBufferHeight = height;
        releaseFbo();
        initFbo(width,height);

        Matrix.perspectiveM(mProjectionMatrix, 0, 45.0f, width / (float)height, 0.1f, 100.0f);
    }

    private int mMvpMatrixHandler;
    private int mPositionHandle;
    private int mTextureHandle1;
    private int mTextureHandle2;

    public void draw(int bgTex){

        Log.e(TAG,"draw~~  btTex="+bgTex);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFbo.frameId());

        GLES20.glViewport(0, 0, mFrameBufferWidth, mFrameBufferHeight);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        mMvpMatrixHandler = GLES20.glGetUniformLocation(mProgramId,"u_MVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(mProgramId, "position");
        mTextureHandle1 = GLES20.glGetAttribLocation(mProgramId,"inputTextureCoordinate");
        mTextureHandle2 = GLES20.glGetAttribLocation(mProgramId,"inputTextureCoordinate2");

        GLES20.glUseProgram(mProgramId);

        Matrix.multiplyMM(mMVPMatrix,0,mViewMatrix,0,mModelMatrix,0);
        Matrix.multiplyMM(mMVPMatrix,0,mProjectionMatrix,0,mMVPMatrix,0);
        GLES20.glUniformMatrix4fv(mMvpMatrixHandler,1,false,mMVPMatrix,0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, bgTex);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(mProgramId, "inputImageTexture"), 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(mProgramId, "inputImageTexture2"), 1);
        GLES20.glUniform1f(GLES20.glGetUniformLocation(mProgramId, "matAlpha"), 1.0f);

        mVertexBuffer.position(0);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer);

        mFragmentBuffer.position(0);
        GLES20.glEnableVertexAttribArray(mTextureHandle1);
        GLES20.glVertexAttribPointer(mTextureHandle1, 2, GLES20.GL_FLOAT, false, 0, mFragmentBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureHandle1);
        GLES20.glDisableVertexAttribArray(mTextureHandle2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

    }

    private static final String VERTEX_SHADER =
            "uniform mat4 u_MVPMatrix;" +
                    "attribute vec4 position;" +
                    "attribute vec4 inputTextureCoordinate;" +
                    "attribute vec4 inputTextureCoordinate2;" +
                    "varying vec2 textureCoordinate;" +
                    "varying vec2 textureCoordinate2;" +
                    "void main(){" +
                    "   gl_Position = u_MVPMatrix * position;" +
                    "   textureCoordinate = inputTextureCoordinate.xy;" +
                    "   textureCoordinate2 = inputTextureCoordinate.xy;" +
                    "}";

    private static final String  FRAGMENT_SHADER =
            "varying highp vec2 textureCoordinate;" +
                    "varying highp vec2 textureCoordinate2;" +
                    "uniform sampler2D inputImageTexture;" +
                    "uniform sampler2D inputImageTexture2;" +
                    "uniform lowp float matAlpha;" +

                    "void main()" +
                    "{" +
                    "   lowp vec4 textureColor = texture2D(inputImageTexture,textureCoordinate);" +
                    "   lowp vec4 tempColor2 = texture2D(inputImageTexture2,textureCoordinate2);" +
                    "   lowp vec4 textureColor2 = vec4(tempColor2.rgb,tempColor.a*matAlpha);" +
                    "   lowp vec4 whiteColor = vec4(1.0);" +
                    "   lowp vec4 blendColor = whiteColor - ((whiteColor-textureColor2)*(whiteColor-textureColor));" +
                    "   gl_FragColor = mix(textureColor,blendColor,matAlpha);" +
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
