package com.duanyy.mycamera.cube;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.duanyy.mycamera.R;

/**
 * 拖动配置ProjectionMatrix。
 */
public class CubeActivity2 extends Activity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    private static final String TAG = "CubeActivity1";

    private CubeGLSurfaceView mCubeGLSurfaceView;
    private TextView mTextView;

    private int mCurSelectMode = 0;
    private static final int MODE_LEFT = 0;
    private static final int MODE_TOP = 1;
    private static final int MODE_RIGHT = 2;
    private static final int MODE_BOTTOM = 3;
    private static final int MODE_NEAE = 4;
    private static final int MODE_FAR = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cube_2);

        initView();
    }

    private void initView(){
        mCubeGLSurfaceView = findViewById(R.id.mCubeGLSurfaceview);
        mTextView = findViewById(R.id.mTextView);

        SeekBar seekBar0 = findViewById(R.id.mSeekbar0);
        seekBar0.setOnSeekBarChangeListener(this);

        RadioButton radioButton0 = findViewById(R.id.mRadioButton0);
        RadioButton radioButton1 = findViewById(R.id.mRadioButton1);
        RadioButton radioButton2 = findViewById(R.id.mRadioButton2);
        RadioButton radioButton3 = findViewById(R.id.mRadioButton3);
        RadioButton radioButton4 = findViewById(R.id.mRadioButton4);
        RadioButton radioButton5 = findViewById(R.id.mRadioButton5);

        radioButton0.setOnClickListener(this);
        radioButton1.setOnClickListener(this);
        radioButton2.setOnClickListener(this);
        radioButton3.setOnClickListener(this);
        radioButton4.setOnClickListener(this);
        radioButton5.setOnClickListener(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int value, boolean b) {
        if (seekBar != null) {
            int id = seekBar.getId();
            float seekF = value*5.0f/100;
            float temp = 0.f;
            Log.e(TAG,"onProgressChanged  seek="+value);
            switch (id){
                case R.id.mSeekbar0:
                    temp = seekF;
                    break;
                default:
                    break;

            }

            String info = " ";
            switch (mCurSelectMode){
                case MODE_LEFT:


                    break;
                case MODE_TOP:
                    mCubeGLSurfaceView.updateProjectionMatrixTop(temp);
                    info = "视锥体 top:="+temp;
                    break;
                case MODE_RIGHT:
                    break;
                case MODE_BOTTOM:
                    mCubeGLSurfaceView.updateProjectionMatrixBottom(temp);
                    info = "视锥体 bottom:="+temp;
                    break;
                case MODE_NEAE:
                    mCubeGLSurfaceView.updateProjectionMatrixNear(temp);
                    info = "视锥体 near:="+temp;
                    break;
                case MODE_FAR:
                    temp += 0.1f;
                    mCubeGLSurfaceView.updateProjectionMatrixFar(temp);
                    info = "视锥体 far:="+temp;
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
                case R.id.mRadioButton0:
                    mCurSelectMode = MODE_LEFT;
                    break;
                case R.id.mRadioButton1:
                    mCurSelectMode = MODE_TOP;
                    break;
                case R.id.mRadioButton2:
                    mCurSelectMode = MODE_RIGHT;
                    break;
                case R.id.mRadioButton3:
                    mCurSelectMode = MODE_BOTTOM;
                    break;
                case R.id.mRadioButton4:
                    mCurSelectMode = MODE_NEAE;
                    break;
                case R.id.mRadioButton5:
                    mCurSelectMode = MODE_FAR;
                    break;
                default:
                    mCurSelectMode = MODE_LEFT;
                    break;
            }
        }
    }
}
