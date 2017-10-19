package com.duanyy.mycamera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.duanyy.mycamera.media.audio.AudioRecordActivity;
import com.duanyy.mycamera.cube.CubeActivity;
import com.duanyy.mycamera.cube.CubeActivity1;
import com.duanyy.mycamera.cube.CubeActivity2;
import com.duanyy.mycamera.framebuffer.BufferActivity;

/**
 * Created by Duanyy on 2017/9/10.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView(){
        findViewById(R.id.mBtnCamera).setOnClickListener(mClickLfistener);
        findViewById(R.id.mBtnPicture).setOnClickListener(mClickLfistener);
        findViewById(R.id.mBtnBuffer).setOnClickListener(mClickLfistener);
        findViewById(R.id.mBtnCube).setOnClickListener(mClickLfistener);
        findViewById(R.id.mBtnCubeViewMatrix).setOnClickListener(mClickLfistener);
        findViewById(R.id.mBtnCubeProjectionMatrix).setOnClickListener(mClickLfistener);
        findViewById(R.id.mBtnAudioRecord).setOnClickListener(mClickLfistener);
    }

    private View.OnClickListener mClickLfistener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view != null) {
                int id = view.getId();
                Intent intent = new Intent();
                switch (id){
                    case R.id.mBtnPicture:
                        intent.setClass(MainActivity.this,PictureActivity.class);
                        break;
                    case R.id.mBtnBuffer:
                        intent.setClass(MainActivity.this, BufferActivity.class);
                        break;
                    case R.id.mBtnCamera:
                        intent.setClass(MainActivity.this,MyCameraActivity.class);
                        break;
                    case R.id.mBtnCube:
                        intent.setClass(MainActivity.this,CubeActivity.class);
                        break;
                    case R.id.mBtnCubeViewMatrix:
                        intent.setClass(MainActivity.this,CubeActivity1.class);
                        break;
                    case R.id.mBtnCubeProjectionMatrix:
                        intent.setClass(MainActivity.this,CubeActivity2.class);
                        break;
                    case R.id.mBtnAudioRecord:
                        intent.setClass(MainActivity.this,AudioRecordActivity.class);
                        break;
                    default:
                        break;
                }
                startActivity(intent);
            }

        }
    };
}
