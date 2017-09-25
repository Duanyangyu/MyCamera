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
    }

    private View.OnClickListener mClickLfistener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this, MyCameraActivity.class);
            startActivity(intent);
        }
    };
}
