package com.duanyy.mycamera.picture;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.util.Log;

import com.duanyy.mycamera.glutil.BufferUtils;
import com.duanyy.mycamera.glutil.OpenGlUtils;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;



/**
 * Created by duanyy on 2017/9/25.
 */

/**
 * 绘制一张图片到glsurfaceview
 */
public class Picture {

    private static final String TAG = "Picture";

    private static final String VERTIX_SHADER = "uniform mat4 u_MVPMatrix; " +
            "attribute vec3 a_position;" +
            "attribute vec2 a_textCoord;"+
            "varying vec2 v_textCoord;"+
            "void main()" +
            "{" +
            "    gl_Position = vec4(a_position.x, a_position.y, a_position.z, 1.0);" +
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

    private static final float[] VERTEX_ARRAY_2 = {
            -1f,-1f,  -1f,1f,  1f,-1f, 1f,1f
    };

    private static final float[] FRAGMENT_ARRAY_2 = {
            0f,0f,  0f,1f,  1f,0f, 1f,1f
    };

    private static final short[] VERTEX_ORDER = {
            (short)0,
            (short)1,
            (short)2,
            (short)2,
            (short)1,
            (short)3
    };

    private int mProgramId;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mFragmentBuffer;
    private ShortBuffer mVertexOrderBuffer;
    private int mTextureId;

    public Picture() {

    }

    public void init(){
        mVertexBuffer = BufferUtils.float2Buffer(VERTEX_ARRAY);
        mFragmentBuffer = BufferUtils.float2Buffer(FRAGMENT_ARRAY);
//        mVertexBuffer = BufferUtils.float2Buffer(VERTEX_ARRAY_2);
//        mFragmentBuffer = BufferUtils.float2Buffer(FRAGMENT_ARRAY_2);
        mVertexOrderBuffer = BufferUtils.short2Buffer(VERTEX_ORDER);

        mProgramId = OpenGlUtils.loadProgram(VERTIX_SHADER, FRAGMENT_SHADER);
        Log.e(TAG,"init programId:"+ mProgramId);
    }

    public void setBitmap(Resources resources,int resId){
        Bitmap bitmap = BitmapFactory.decodeResource(resources, resId);
        mTextureId = OpenGlUtils.loadTexture(bitmap, -1);
    }

    public void draw(){
        if (mTextureId == 0) {
            return;
        }

        int positionHandler = GLES20.glGetAttribLocation(mProgramId, "a_position");
        int textureHandler = GLES20.glGetAttribLocation(mProgramId, "a_textCoord");
        int sampleTextureHandler = GLES20.glGetUniformLocation(mProgramId, "u_sampleTexture");
        Log.e(TAG,"positionHandler="+positionHandler);
        Log.e(TAG,"textCoordHandler="+textureHandler);

        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT|GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mProgramId);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mTextureId);
        GLES20.glUniform1i(sampleTextureHandler,0);


        GLES20.glEnableVertexAttribArray(positionHandler);
        GLES20.glEnableVertexAttribArray(textureHandler);

        GLES20.glVertexAttribPointer(positionHandler,2,GLES20.GL_FLOAT,false,0,mVertexBuffer);
        GLES20.glVertexAttribPointer(textureHandler,2,GLES20.GL_FLOAT,false,0,mFragmentBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,6);
//        GLES20.glDrawElements(GLES20.GL_TRIANGLES,4,GLES20.GL_UNSIGNED_SHORT,mVertexOrderBuffer);

        GLES20.glDisableVertexAttribArray(positionHandler);
        GLES20.glDisableVertexAttribArray(textureHandler);
        GLES20.glUseProgram(0);
    }


}
