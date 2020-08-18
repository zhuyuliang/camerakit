package com.stevez.camera.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ToastUtils;
import com.stevez.camerakit.BinocularCameraGLSurfaceView;
import com.stevez.camerakit.PermissionsListener;
import com.stevez.camerakit.ScalableType;

/**
 * @author: Zhu Yuliang
 * @created Create in 2020/6/23 4:23 PM.
 * @description: BinocularGLSurfaceActivity
 */
public class BinocularGLSurfaceActivity extends AppCompatActivity {

    private BinocularCameraGLSurfaceView cameraView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binocular_glsurface);
        cameraView = findViewById(R.id.camera_textureview);
        cameraView.setPermissionsListener(new PermissionsListener() {
            @Override
            public void onPermissionsSuccess() {
                ToastUtils.showShort("onPermissionsSuccess");
            }

            @Override
            public void onPermissionsFailure() {
                ToastUtils.showShort("onPermissionsFailure");
                cameraView.requestPermissions(BinocularGLSurfaceActivity.this);
            }
        });
        cameraView.setDisplayDir(ConstantsConfig.getInstance().getFaceOri());
        cameraView.resetPreviewSize(ConstantsConfig.sPreviewWidth,ConstantsConfig.sPreviewHeight);
        cameraView.setRgbCameraId(ConstantsConfig.getInstance().getRgbCamereId());
        cameraView.setIrCameraId(ConstantsConfig.getInstance().getIrCamereId());
        cameraView.setPreviewCameraId(ConstantsConfig.getInstance().getRgbCamereId());
        cameraView.setMirror(ConstantsConfig.getInstance().getMirror());
        cameraView.setStyle(ScalableType.CENTER_INSIDE);
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
        Intent starter = new Intent(context, BinocularGLSurfaceActivity.class);
        context.startActivity(starter);
    }

}