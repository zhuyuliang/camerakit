package com.stevez.camera.sample;

import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
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

/**
 * @author: Zhu Yuliang
 * @created Create in 2020/6/23 4:23 PM.
 * @description: CameraUvcActivity
 */
public class CameraUvcActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private TextureView mTextureView;
    private CameraManager mInstance;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mTextureView = findViewById(R.id.textureview);
        mTextureView.setSurfaceTextureListener(this);
        mTextureView.setRotation(ConstantsConfig.getInstance().getFaceOri().getValue() * 90);
    }

    private void openCamera(SurfaceTexture surface) {
        Log.d("TAG", "openCamera");
        mInstance = CameraManager.getInstance(
                new CameraFacing.Builder().setFacingType(FacingType.UVC)
                        .setCameraId(123)
                        .setVendorId(ConstantsConfig.getInstance().getVid())
                        .setProductId(ConstantsConfig.getInstance().getPid()).build(),
                CameraApiType.CAMERAUVC, getBaseContext(),new Handler(getMainLooper()),CameraUvcActivity.this.getLifecycle());
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
        openCamera(surface);
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
    protected void onStop() {
        super.onStop();
        if (mInstance != null) {
            this.mInstance.stopPreview();
            this.mInstance.release();
            this.mInstance = null;
        }
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, CameraUvcActivity.class);
        context.startActivity(starter);
    }
}