package com.duanyy.mycamera.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.util.Log;

import com.duanyy.mycamera.R;
import com.duanyy.mycamera.glutil.BufferUtils;
import com.duanyy.mycamera.glutil.FboHelper;
import com.duanyy.mycamera.glutil.OpenGlUtils;

import java.nio.FloatBuffer;

/**
 * Created by duanyy on 2017/10/17.
 */

public class BitmapOverlay {

    private static final String TAG = "BitmapOverlay";

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mFragmentBuffer;

    private int mProgramId;
    private int mTextureId;
    private FboHelper mFbo;

    public void initProgram(Context context){
        mProgramId = OpenGlUtils.loadProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        Log.e(TAG,"initProgram mProgramId="+mProgramId);

        mVertexBuffer = BufferUtils.float2Buffer(VERTEX_ARRAY);
        mFragmentBuffer = BufferUtils.float2Buffer(FRAGMENT_ARRAY);

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon_cube);
        mTextureId = OpenGlUtils.loadTexture(bitmap,-1);
        Log.e(TAG,"initProgram mTextureId="+mTextureId);
    }

    public void initFrameBuffer(int w,int h){
        mFbo = new FboHelper(w,h);
        mFbo.createFbo();
    }

    public void release(){
        if (mFbo != null) {
            mFbo.close();
        }
    }

    public int getTargetTexture(){
            return mFbo.textureId();
    }

    public void draw(boolean drawFrameBuffer){

        if (drawFrameBuffer){
            int framebufferId = mFbo.frameId();
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebufferId);
            Log.e(TAG,"draw bindFrameBufferId="+framebufferId);
        }

        int positionIndex = GLES20.glGetAttribLocation(mProgramId, "a_position");
        int textCoordIndex = GLES20.glGetAttribLocation(mProgramId, "a_textCoord");
        int sampleTextureIndex = GLES20.glGetUniformLocation(mProgramId, "u_sampleTexture");
        Log.e(TAG,"mProgramId="+mProgramId);
        Log.e(TAG,"positionIndex="+positionIndex);
        Log.e(TAG,"textCoordIndex="+textCoordIndex);
        Log.e(TAG,"sampleTextureIndex="+sampleTextureIndex);

        GLES20.glUseProgram(mProgramId);

        GLES20.glEnableVertexAttribArray(positionIndex);
        GLES20.glEnableVertexAttribArray(textCoordIndex);

        GLES20.glVertexAttribPointer(positionIndex,2,GLES20.GL_FLOAT,false,0,mVertexBuffer);
        GLES20.glVertexAttribPointer(textCoordIndex,2,GLES20.GL_FLOAT,false,0,mFragmentBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mTextureId);
        GLES20.glUniform1i(sampleTextureIndex,0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,6);

        GLES20.glDisableVertexAttribArray(positionIndex);
        GLES20.glDisableVertexAttribArray(textCoordIndex);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);
        GLES20.glUseProgram(0);
    }

    private static final float[] VERTEX_ARRAY = {
            -1f,-1f,  -1f,1f,  1f,-1f,  -1f,1f,  1f,1f,  1f,-1f
    };

    private static final float[] FRAGMENT_ARRAY = {
            0f,0f,  0f,1f,  1f,0f,  0f,1f,  1f,1f,  1f,0f
    };

    private static final String VERTEX_SHADER =
                    "attribute vec3 a_position;" +
                    "attribute vec2 a_textCoord;"+
                    "varying vec2 v_textCoord;"+
                    "void main()" +
                    "{" +
                    "   gl_Position = vec4(a_position.x, a_position.y, a_position.z, 1.0);"+
                    "    v_textCoord = a_textCoord;"+
                    "}";

    private static final String FRAGMENT_SHADER =
            "precision mediump float;" +
                    "varying vec2 v_textCoord;"+
                    "uniform sampler2D u_sampleTexture;"+
                    "void main(){" +
                    "  gl_FragColor = texture2D(u_sampleTexture,v_textCoord);" +
                    "}";

}
