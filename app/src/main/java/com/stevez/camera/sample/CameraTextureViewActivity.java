package com.stevez.camera.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ToastUtils;
import com.stevez.camerakit.CameraTextureView;
import com.stevez.camerakit.PermissionsListener;
import com.stevez.camerakit.ScalableType;

/**
 * @author: Zhu Yuliang
 * @created Create in 2020/6/23 4:23 PM.
 * @description: CameraTextureViewActivity
 */
public class CameraTextureViewActivity extends AppCompatActivity {

    private CameraTextureView cameraView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cameratexture);
        cameraView = findViewById(R.id.camera_view);

        cameraView.setPermissionsListener(new PermissionsListener() {
            @Override
            public void onPermissionsSuccess() {
                ToastUtils.showShort("onPermissionsSuccess");
            }

            @Override
            public void onPermissionsFailure() {
                ToastUtils.showShort("onPermissionsFailure");
                cameraView.requestPermissions(CameraTextureViewActivity.this);
            }
        });
        cameraView.setDisplayDir(ConstantsConfig.getInstance().getFaceOri());
        cameraView.resetPreviewSize(ConstantsConfig.sPreviewWidth,ConstantsConfig.sPreviewHeight);
        cameraView.setMirror(ConstantsConfig.getInstance().getMirror());
        cameraView.setStyle(ScalableType.CENTER_INSIDE);
        cameraView.setCameraId(0);
        this.getLifecycle().addObserver(cameraView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.getLifecycle().removeObserver(cameraView);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, CameraTextureViewActivity.class);
        context.startActivity(starter);
    }


}