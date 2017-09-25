package com.duanyy.mycamera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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
                        startActivity(intent);
                        break;
                    case R.id.mBtnCamera:
                        intent.setClass(MainActivity.this,MyCameraActivity.class);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
            }

        }
    };
}
