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

    private float[] mModelMatrix = new float[16];//model变换矩阵
    private float[] mViewMatrix = new float[16];//view变换矩阵
    private float[] mProjectionMatrix = new float[16];//4x4矩阵 投影用。 Projection：投影。
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

    public void updateViewMatrixEye(float eyeX,float eyeY,float eyeZ){
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, 0.0f, 0.0f, -5.0f, 0.0f, 1.0f, 0.0f);
    }

    public void updateViewMatrixLook(float lookX,float lookY,float lookZ){
        Matrix.setLookAtM(mViewMatrix,0,0.0f,0.0f,1f,lookX,lookY,lookZ,0.0f,1.0f,0.0f);
    }

    public void updateViewMatrixUp(float upX,float upY,float upZ){
        Matrix.setLookAtM(mViewMatrix,0,0.0f,0.0f,1f,0.0f,0.0f,-5.0f,upX,upY,upZ);
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

        // Set the view matrix. This matrix can be said to represent the camera position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
        // 通过调用此函数，就能够设定观察的场景，在这个场景中的物体就会被 OpenGL 处理。
        // 在 OpenGL 中，eye 的默认位置是在原点，指向 Z 轴的负方向（屏幕往里），up 方向为 Y 轴的正方向。
//        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
        Matrix.setLookAtM(mViewMatrix, 0, 3.0f, 3.0f, 10.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

        //开启深度测试，否则，或出现立方体前面的面透明的异常现象。From 李兆民提示。
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        Log.e(TAG,"mProgramId="+mProgramId);
    }

    public void updateProjectionMatrixTop(float top){
        final float ratio = (float) mWidth / mHeight;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float near = 1.0f;
        final float far = 10.0f;
        if (top == bottom){
            top *= 1.1f;
        }
        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    public void updateProjectionMatrixBottom(float bottom){
        final float ratio = (float) mWidth / mHeight;
        final float left = -ratio;
        final float right = ratio;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;
        if (bottom == top){
            bottom *= 0.99f;
        }
        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    public void updateProjectionMatrixNear(float near){
        final float ratio = (float) mWidth / mHeight;
        final float left = -ratio;
        final float right = ratio;
        final float top = 1.0f;
        final float bottom = -1.0f;
        final float far = 10.0f;
        if (near == far){
            near *= 0.99f;
        }
        if (near <= 0){
            near = 0.00001f;
        }
        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    public void updateProjectionMatrixFar(float far){
        final float ratio = (float) mWidth / mHeight;
        final float left = -ratio;
        final float right = ratio;
        final float top = 1.0f;
        final float bottom = -1.0f;
        final float near = 1.0f;
        if (far == near){
            far *= 1.1f;
        }
        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    private int mWidth;
    private int mHeight;
    public void onSurfaceChanged(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        this.mWidth = width;
        this.mHeight = height;

        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;

        //获得透视投影矩阵
        // http://blog.csdn.net/liyuanjinglyj/article/details/46652909
        //Android的Matrix类为它准备了两个方法------frustumM()和perspectiveM()。
        //perspectiveM()只是从Android的ICS版本开始才被引入，在早期的Android版本里并没有这个方法。
//        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);

        /**
         * m：存储透视投影矩阵
         offset：偏移量，一般为0
         fovy：视野角度，小于180度，角度越小视野越窄
         zNear：到近处平面距离，必须为正值。比如，如果此值为1，那近处平面就位于一个z值为-1处
         zFar：到远处平面的距离，必须为正值且大于zNear值
         */
        Matrix.perspectiveM(mProjectionMatrix, 0, 45.0f, width / (float)height, 0.1f, 100.0f);
    }

    public void draw(){
        if (mTextureId <= 0 || mProgramId <= 0){
            return;
        }

        long time = System.currentTimeMillis()%10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);
        Matrix.setIdentityM(mModelMatrix, 0); //初始化矩阵
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -5.0f);//给矩阵设置位移
        Log.e(TAG,"angleInDegrees="+angleInDegrees);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);//给矩阵设置旋转,最后三个参数为旋转轴

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glUseProgram(mProgramId);

        int positionHandler = GLES20.glGetAttribLocation(mProgramId, "a_position");
        int textCoordHandler = GLES20.glGetAttribLocation(mProgramId, "a_textCoord");
        int mvpMatrixHandler = GLES20.glGetUniformLocation(mProgramId,"u_MVPMatrix");
        int sampleTextureHandler = GLES20.glGetUniformLocation(mProgramId, "u_sampleTexture");

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

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);//激活GL_TEXTURE0纹理层
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mTextureId);//将纹理内容绘制到GL_TEXTURE0纹理层
        GLES20.glUniform1i(sampleTextureHandler,0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,36);

        GLES20.glDisableVertexAttribArray(positionHandler);
        GLES20.glDisableVertexAttribArray(textCoordHandler);
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
