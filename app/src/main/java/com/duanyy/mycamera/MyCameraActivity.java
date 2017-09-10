package com.duanyy.mycamera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;

import com.duanyy.mycamera.camera.CameraGLSurfaceview;
import com.duanyy.mycamera.utils.PermissionHelper;



/**
 * Created by Duanyy on 2017/9/10.
 */

/**
 * commit test from Mac.
 */
public class MyCameraActivity extends FragmentActivity {

    private static final int RESULT_CODE_CAMERA = 2001;
    private CameraGLSurfaceview mCameraGLSurfaceview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mycamera);
        checkPermission();
        mCameraGLSurfaceview = (CameraGLSurfaceview) findViewById(R.id.mCameraGLSurfaceview);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private boolean checkPermission(){
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);
        if (permission == PackageManager.PERMISSION_GRANTED){
            return true;
        }else {
            PermissionHelper permissionHelper = new PermissionHelper(this);
            permissionHelper.permissionsCheck(Manifest.permission.CAMERA,RESULT_CODE_CAMERA);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }
}
