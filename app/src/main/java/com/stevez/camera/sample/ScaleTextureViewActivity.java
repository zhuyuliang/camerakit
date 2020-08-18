package com.stevez.camera.sample;

import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.stevez.camera.CallBackEvents;
import com.stevez.camera.CameraApiType;
import com.stevez.camera.CameraFacing;
import com.stevez.camera.CameraManager;
import com.stevez.camera.CameraPreviewCallback;
import com.stevez.camera.CameraSize;
import com.stevez.camera.FacingType;
import com.stevez.camera.IAttributes;
import com.stevez.camerakit.ScaleTextureView;

/**
 * @author: Zhu Yuliang
 * @created Create in 2020/6/23 4:23 PM.
 * @description: ScaleTextureViewActivity
 */
public class ScaleTextureViewActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private ScaleTextureView scaleTextureView;
    private CameraManager mInstance;
    private SurfaceTexture surface;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scaletexture);
        scaleTextureView = findViewById(R.id.camera_textureview);
        scaleTextureView.setDisplayDir(ConstantsConfig.getInstance().getFaceOri());
        scaleTextureView.resetPreviewSize(ConstantsConfig.sPreviewWidth,ConstantsConfig.sPreviewHeight);
        getLifecycle().addObserver(scaleTextureView);
        scaleTextureView.setSurfaceTextureListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void openCamera() {
        Log.d("TAG", "openCamera");
        mInstance = CameraManager.getInstance(
                new CameraFacing.Builder().setFacingType(FacingType.OTHER)
                        .setCameraId(0).build(),
                CameraApiType.CAMERA1, getBaseContext());
        mInstance.setCallBackEvents(
                (new CallBackEvents() {
                    @Override
                    public void onCameraOpen(IAttributes cameraAttributes) {
                        Log.e("TAGCAMERA", "onCameraOpen");
                        mInstance.setPhotoSize(new CameraSize(ConstantsConfig.sPreviewWidth,ConstantsConfig.sPreviewHeight));
                        mInstance.setPreviewSize(new CameraSize(ConstantsConfig.sPreviewWidth,ConstantsConfig.sPreviewHeight));
                        mInstance.setPreviewOrientation(ConstantsConfig.getInstance().getFaceOri().getValue() * 90);
                        mInstance.setExposureCompensation(0);
                        mInstance.addPreviewCallbackWithBuffer(new CameraPreviewCallback() {
                            @Override
                            public void onCallBackPreview(byte[] data) {
                                Log.e("TAGCAMERA", "onCallBackPreview");
                            }
                        });
                        mInstance.startPreview(surface);
                    }

                    @Override
                    public void onCameraClose() {
                        Log.e("TAGCAMERA", "onCameraClose");
                    }

                    @Override
                    public void onCameraError(String errorMsg) {
                        Log.e("TAGCAMERA", "onCameraError");
                    }

                    @Override
                    public void onPreviewStarted() {
                        Log.e("TAGCAMERA", "onPreviewStarted");
                    }

                    @Override
                    public void onPreviewStopped() {
                        Log.e("TAGCAMERA", "onPreviewStopped");
                    }

                    @Override
                    public void onPreviewError(String errorMsg) {
                        Log.e("TAGCAMERA", "onPreviewError");
                    }
                }));
        mInstance.openCamera();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.e("TAGCAMERA", "onSurfaceTextureAvailable");
        this.surface = surface;
        openCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mInstance != null) {
            this.mInstance.stopPreview();
            this.mInstance.release();
        }
        getLifecycle().removeObserver(scaleTextureView);
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, ScaleTextureViewActivity.class);
        context.startActivity(starter);
    }
}