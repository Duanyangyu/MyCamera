package com.duanyy.mycamera.framebuffer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.duanyy.mycamera.R;

public class BufferActivity extends Activity {

    private BufferGLSurfaceview mGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buffer);

        mGLSurfaceView = findViewById(R.id.mBufferGLSurfaceview);
    }

    public void btnRender(View view){
        mGLSurfaceView.requestRender();
    }
}
