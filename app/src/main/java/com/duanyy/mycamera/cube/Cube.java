package com.duanyy.mycamera.cube;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.duanyy.mycamera.glutil.BufferUtils;
import com.duanyy.mycamera.glutil.OpenGlUtils;

import java.nio.FloatBuffer;

/**
 * Created by duanyy on 2017/9/30.
 */

public class Cube {

    private static final String TAG = "Cube";

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mFragmentBuffer;
    private int mProgramId;
    private int mTextureId;

    private float[] mModelMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

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

    public void onSurfaceCreated() {
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = -0.5f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        // Set the view matrix. This matrix can be said to represent the camera position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        Log.e(TAG,"mProgramId="+mProgramId);
    }

    public void onSurfaceChanged(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;
        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    public void draw(){
        if (mTextureId <= 0 || mProgramId <= 0){
            return;
        }

        long time = System.currentTimeMillis()%1000L;
        float angleInDegrees = (360.0f / 1000.0f) * ((int) time);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -5.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 1.0f, 0.0f);

        int mvpMatrixHandler = GLES20.glGetUniformLocation(mProgramId,"u_MVPMatrix");
        int positionHandler = GLES20.glGetAttribLocation(mProgramId, "a_position");
        int textCoordHandler = GLES20.glGetAttribLocation(mProgramId, "a_textCoord");
        int sampleTextureHandler = GLES20.glGetUniformLocation(mProgramId, "u_sampleTexture");
        Log.e(TAG,"mvpMatrixHandler="+mvpMatrixHandler);
        Log.e(TAG,"positionHandler="+positionHandler);
        Log.e(TAG,"textCoordHandler="+textCoordHandler);
        Log.e(TAG,"sampleTextureHandler="+sampleTextureHandler);

        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT|GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mProgramId);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);//激活GL_TEXTURE0纹理层
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mTextureId);//将纹理内容绘制到GL_TEXTURE0纹理层
        GLES20.glUniform1i(sampleTextureHandler,0);


        GLES20.glEnableVertexAttribArray(positionHandler);
        GLES20.glEnableVertexAttribArray(textCoordHandler);

        /**
         * glVertexAttribPointer(int index,int size,int type,boolean normalized,int stride,int offset);
         * glVertexAttribPointer(int index,int size,int type,boolean normalized,int stride,Buffer buffer);
         */
        GLES20.glVertexAttribPointer(positionHandler,3,GLES20.GL_FLOAT,false,0,mVertexBuffer);
        GLES20.glVertexAttribPointer(textCoordHandler,2,GLES20.GL_FLOAT,false,0,mFragmentBuffer);

        Matrix.multiplyMM(mMVPMatrix,0,mViewMatrix,0,mModelMatrix,0);
        Matrix.multiplyMM(mMVPMatrix,0,mProjectionMatrix,0,mMVPMatrix,0);
        GLES20.glUniformMatrix4fv(mvpMatrixHandler,1,false,mMVPMatrix,0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,36);

        GLES20.glDisableVertexAttribArray(positionHandler);
        GLES20.glDisableVertexAttribArray(textCoordHandler);
        GLES20.glUseProgram(0);
    }

    private static final String VERTEX_SHADER =
            "uniform mat4 u_MVPMatrix;" +
            "attribute vec4 a_position;" +
            "attribute vec2 a_textCoord;"+
            "varying vec2 v_textCoord;"+
            "void main()" +
            "{" +
//            "    gl_Position = vec4(a_position.x, a_position.y, a_position.z, 1.0);" +
            "    gl_Position = u_MVPMatrix * a_position;" +
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
            1f,0f
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
