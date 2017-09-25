package com.duanyy.mycamera.picture;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * Created by duanyy on 2017/9/25.
 */

public class PictureGLSurfaceView extends GLSurfaceView {

    private static final String TAG = "PictureGLSurfaceView";

    private Context mContext;

    public PictureGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }



}
