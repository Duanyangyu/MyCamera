package com.duanyy.mycamera;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.duanyy.mycamera.R;
import com.duanyy.mycamera.cube.CubeGLSurfaceView;

public class CubeActivity extends Activity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    private static final String TAG = "CubeActivity";

    private CubeGLSurfaceView mCubeGLSurfaceView;
    private TextView mTextView;

    private int mCurSelectMode = 0;
    private static final int MODE_EYE = 0;
    private static final int MODE_LOOK = 1;
    private static final int MODE_UP = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cube);

        initView();
    }

    private void initView(){
        mCubeGLSurfaceView = findViewById(R.id.mCubeGLSurfaceview);
        mTextView = findViewById(R.id.mTextView);

        SeekBar seekBar0 = findViewById(R.id.mSeekbar0);
        SeekBar seekBar1 = findViewById(R.id.mSeekbar1);
        SeekBar seekBar2 = findViewById(R.id.mSeekbar2);

        seekBar0.setOnSeekBarChangeListener(this);
        seekBar1.setOnSeekBarChangeListener(this);
        seekBar2.setOnSeekBarChangeListener(this);

        RadioButton radioButton1 = findViewById(R.id.mRadioButton1);
        RadioButton radioButton2 = findViewById(R.id.mRadioButton2);

        radioButton1.setOnClickListener(this);
        radioButton2.setOnClickListener(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int value, boolean b) {
        if (seekBar != null) {
            int id = seekBar.getId();
            float seekF = value*5.0f/100;
            float x = 0.f;
            float y = 0.f;
            float z = 0.f;
            Log.e(TAG,"onProgressChanged  seek="+value);
            switch (id){
                case R.id.mSeekbar0:
                    x = seekF;
                    break;
                case R.id.mSeekbar1:
                    y = seekF;
                    break;
                case R.id.mSeekbar2:
                    z = seekF;
                    break;
                default:
                    break;

            }

            String info = " ";
            switch (mCurSelectMode){
                case MODE_EYE:
                    mCubeGLSurfaceView.updateViewMatrixEye(x,y,z);
                    info = "相机位置:  eyeX="+x+",   eyeY="+y+",   eyeZ="+z;
                    break;
                case MODE_LOOK:
                    mCubeGLSurfaceView.updateViewMatrixLook(x,y,z);
                    info = "相机焦点:  lookX="+x+",   lookY="+y+",   lookZ="+z;
                    break;
                case MODE_UP:
                    mCubeGLSurfaceView.updateViewMatrixUp(x,y,z);
                    info = "相机方向:  upX="+x+",    upY="+y+",    upZ="+z;
                    break;
            }
            mTextView.setText(info);
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onClick(View view) {
        if (view != null) {
            int id = view.getId();
            switch (id){
                case R.id.mRadioButton1:
                    mCurSelectMode = MODE_LOOK;
                    break;
                case R.id.mRadioButton2:
                    mCurSelectMode = MODE_UP;
                    break;
                default:
                    mCurSelectMode = MODE_EYE;
                    break;
            }
        }
    }
}
