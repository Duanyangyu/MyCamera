package com.duanyy.mycamera.camera;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.duanyy.mycamera.glutil.FboHelper;
import com.duanyy.mycamera.glutil.OpenGlUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Duanyy on 2017/9/10.
 */
public class DirectVideo{

    public static final String TAG = "DirectVideo";

    private final String vertexShaderCode =
            "uniform mat4 u_MVPMatrix;" +
            "attribute vec4 vPosition;" +
                    "attribute vec2 inputTextureCoordinate;" +
                    "varying vec2 textureCoordinate;" +
                    "void main()" +
                    "{"+
                    "gl_Position = u_MVPMatrix * vPosition;"+
                    "textureCoordinate = inputTextureCoordinate;" +
                    "}";

    private final String fragmentShaderCode_EOS =
            "#extension GL_OES_EGL_image_external : require\n"+
                    "precision mediump float;" +
                    "varying vec2 textureCoordinate;\n" +
                    "uniform samplerExternalOES s_texture;\n" +
                    "void main() {" +
                    "  gl_FragColor = texture2D( s_texture, textureCoordinate );\n" +
                    "}";

    public static final String fragmentShaderCode = "" +
            "varying highp vec2 textureCoordinate;" +
            "uniform sampler2D inputImageTexture;" +
            "void main()" +
            "{" +
            "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);" +
            "}";

    private FloatBuffer vertexBuffer, textureVerticesBuffer;
    private ShortBuffer drawListBuffer;
    private final int mProgramEOS;
    private final int mProgram;
    private float[] mModelMatrix = new float[16];
    private float[] mIdentityMatrix = new float[16];

    private short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

    static float squareCoords[] = {
            -1.0f,  1.0f,
            -1.0f, -1.0f,
            1.0f, -1.0f,
            1.0f,  1.0f,
    };

    static float textureVertices[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,
    };

    private int texture;

    public DirectVideo(int texture){
        this.texture = texture;

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        ByteBuffer bb2 = ByteBuffer.allocateDirect(textureVertices.length * 4);
        bb2.order(ByteOrder.nativeOrder());
        textureVerticesBuffer = bb2.asFloatBuffer();
        textureVerticesBuffer.put(textureVertices);
        textureVerticesBuffer.position(0);

        mProgram = OpenGlUtils.loadProgram(vertexShaderCode,fragmentShaderCode);
        mProgramEOS = OpenGlUtils.loadProgram(vertexShaderCode,fragmentShaderCode_EOS);

        Matrix.setIdentityM(mIdentityMatrix,0);

        Matrix.setIdentityM(mModelMatrix,0);
        Matrix.scaleM(mModelMatrix,0,0.5f,0.5f,0.5f);
    }

    private FboHelper mFbo;
    public void setFrameBuffer(FboHelper fbo){
        this.mFbo = fbo;
    }

    public int getTargetTexture(){
        return mFbo.textureId();
    }

    public void draw(int bgTexture){

        if (bgTexture == -1) {
            return;
        }

        if (mFbo != null) {
            int framebufferId = mFbo.frameId();
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebufferId);
            Log.e(TAG,"draw glBindFramebuffer frameId="+framebufferId);
        }

        GLES20.glUseProgram(mProgramEOS);

        GLES20.glEnableVertexAttribArray(GLES20.glGetAttribLocation(mProgramEOS, "vPosition"));
        GLES20.glEnableVertexAttribArray(GLES20.glGetAttribLocation(mProgramEOS, "inputTextureCoordinate"));
        GLES20.glVertexAttribPointer(GLES20.glGetAttribLocation(mProgramEOS, "vPosition"), 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glVertexAttribPointer(GLES20.glGetAttribLocation(mProgramEOS, "inputTextureCoordinate"), 2, GLES20.GL_FLOAT, false, 0, textureVerticesBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture);
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(mProgramEOS,"u_MVPMatrix"),1,false,mIdentityMatrix,0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
        GLES20.glDisableVertexAttribArray(GLES20.glGetAttribLocation(mProgramEOS, "vPosition"));
        GLES20.glDisableVertexAttribArray(GLES20.glGetAttribLocation(mProgramEOS, "inputTextureCoordinate"));

        GLES20.glUseProgram(0);
        GLES20.glUseProgram(mProgram);

        GLES20.glEnableVertexAttribArray(GLES20.glGetAttribLocation(mProgram,"vPosition"));
        GLES20.glEnableVertexAttribArray(GLES20.glGetAttribLocation(mProgram,"inputTextureCoordinate"));
        GLES20.glVertexAttribPointer(GLES20.glGetAttribLocation(mProgram,"vPosition"), 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glVertexAttribPointer(GLES20.glGetAttribLocation(mProgram,"inputTextureCoordinate"), 2, GLES20.GL_FLOAT, false, 0, textureVerticesBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, bgTexture);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(mProgram,"inputImageTexture"),1);
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(mProgram,"u_MVPMatrix"),1,false,mModelMatrix,0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFbo.textureId());
        GLES20.glUniform1i(GLES20.glGetUniformLocation(mProgram,"inputImageTexture"),2);
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(mProgram,"u_MVPMatrix"),1,false,mIdentityMatrix,0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        GLES20.glDisableVertexAttribArray(GLES20.glGetAttribLocation(mProgram, "vPosition"));
        GLES20.glDisableVertexAttribArray(GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate"));
        GLES20.glUseProgram(0);
    }
}
