package com.duanyy.mycamera.framebuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.util.Log;

import com.duanyy.mycamera.R;
import com.duanyy.mycamera.glutil.BufferUtils;
import com.duanyy.mycamera.glutil.FboHelper;
import com.duanyy.mycamera.glutil.OpenGlUtils;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by duanyy on 2017/10/16.
 */

public class BufferGLSurfaceview extends GLSurfaceView implements GLSurfaceView.Renderer {

    public static final String TAG = "BufferGLSurfaceview";

    private Context mContext;
    private int mProgramId;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mFragmentBuffer;
    private FboHelper mFbo;
    private int mTextureId;
    private int mBitmapOverlayTextureId;
    private float[] mIdentiryMatrix = new float[16];
    private float[] mModelMatrix = new float[16];

    public BufferGLSurfaceview(Context context) {
        this(context,null);
    }

    public BufferGLSurfaceview(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    private void init(){
        //OpenGL版本设置，这是必须的，否则内容将不会被绘制。
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        mVertexBuffer = BufferUtils.float2Buffer(VERTEX_ARRAY);
        mFragmentBuffer = BufferUtils.float2Buffer(FRAGMENT_ARRAY);

        mProgramId = OpenGlUtils.loadProgram(VERTIX_SHADER, FRAGMENT_SHADER);
        Log.e(TAG,"init programId:"+ mProgramId);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_test);
        mTextureId = OpenGlUtils.loadTexture(bitmap, -1);

        Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_cube);
        mBitmapOverlayTextureId = OpenGlUtils.loadTexture(bitmap2, -1);

        Matrix.setIdentityM(mIdentiryMatrix,0);
        Matrix.setIdentityM(mModelMatrix,0);
        Matrix.scaleM(mModelMatrix,0,0.25f,0.25f,1.0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int w, int h) {
        mFbo = new FboHelper(w,h);
        mFbo.createFbo();
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glUseProgram(mProgramId);

        int framebufferId = mFbo.frameId();
        Log.e(TAG,"framebufferId="+framebufferId);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebufferId);

        /**
         * 离屏渲染：先将内容绘制到缓冲区，再将缓冲区内容一坨绘制进屏幕。
         * 类比：自定义view，Android双缓冲绘图，先将所有图形加载到内存中，然后一起绘制到屏幕。
         */
        //step1:draw content to FrameBuffer.
        GLES20.glEnableVertexAttribArray(GLES20.glGetAttribLocation(mProgramId, "a_position"));
        GLES20.glEnableVertexAttribArray(GLES20.glGetAttribLocation(mProgramId, "a_textCoord"));
        GLES20.glVertexAttribPointer(GLES20.glGetAttribLocation(mProgramId, "a_position"),2,GLES20.GL_FLOAT,false,0,mVertexBuffer);
        GLES20.glVertexAttribPointer(GLES20.glGetAttribLocation(mProgramId, "a_textCoord"),2,GLES20.GL_FLOAT,false,0,mFragmentBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mTextureId);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(mProgramId, "u_sampleTexture"),0);
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(mProgramId,"u_MVPMatrix"),1,false,mIdentiryMatrix,0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,6);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mBitmapOverlayTextureId);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(mProgramId,"u_sampleTexture"),1);
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(mProgramId,"u_MVPMatrix"),1,false,mModelMatrix,0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,6);

//        //step2:draw content to Screen.
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
        /**
         * issue: glActiveTexture 激活纹理单元时，有问题。
         * GLES20.GL_TEXTURE1 可以绘制
         * GLES20.GL_TEXTURE0 绘制有问题
         * 原因待查！！！
         */
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mFbo.textureId());
        GLES20.glUniform1i(GLES20.glGetUniformLocation(mProgramId, "u_sampleTexture"),1);
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(mProgramId,"u_MVPMatrix"),1,false,mIdentiryMatrix,0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,6);

        GLES20.glDisableVertexAttribArray(GLES20.glGetAttribLocation(mProgramId, "a_position"));
        GLES20.glDisableVertexAttribArray(GLES20.glGetAttribLocation(mProgramId, "a_textCoord"));
        GLES20.glUseProgram(0);
    }

    private static final String VERTIX_SHADER = "uniform mat4 u_MVPMatrix; " +
            "attribute vec3 a_position;" +
            "attribute vec2 a_textCoord;"+
            "varying vec2 v_textCoord;"+
            "void main()" +
            "{" +
            "    gl_Position = u_MVPMatrix * vec4(a_position.x, a_position.y, a_position.z, 1.0);" +
            "    v_textCoord = a_textCoord;"+
            "}";

    private static final String FRAGMENT_SHADER = "precision mediump float;" +
            "varying vec2 v_textCoord;"+
            "uniform sampler2D u_sampleTexture;"+
            "void main(){" +
            "  gl_FragColor = texture2D(u_sampleTexture,v_textCoord);" +
            "}";

    private static final float[] VERTEX_ARRAY = {
            -1f,-1f,  -1f,1f,  1f,-1f,  -1f,1f,  1f,1f,  1f,-1f
    };

    private static final float[] FRAGMENT_ARRAY = {
            0f,0f,  0f,1f,  1f,0f,  0f,1f,  1f,1f,  1f,0f
    };

}
