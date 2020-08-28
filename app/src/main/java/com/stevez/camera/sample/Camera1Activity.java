package com.stevez.camera.sample;

import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.LogUtils;
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
 * @description: Camera1Activity
 */
public class Camera1Activity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    public static final String TAG = "Camera1Activity";

    private TextureView mTextureView;
    private CameraManager mInstance;
    private TextView textView;
    private ImageView img_back;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mTextureView = findViewById(R.id.textureview);
        textView = findViewById(R.id.txt_title);
        img_back = findViewById(R.id.img_back);
        textView.setText("Camera1");
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTextureView.setSurfaceTextureListener(this);
    }

    private void openCamera(SurfaceTexture surface) {
        mInstance = CameraManager.getInstance(
                new CameraFacing.Builder().setFacingType(FacingType.OTHER)
                        .setCameraId(ConstantsConfig.getInstance().getRgbCamereId()).build(),
                CameraApiType.CAMERA1, getBaseContext());
        mInstance.setCallBackEvents(
                (new CallBackEvents() {
                    @Override
                    public void onCameraOpen(IAttributes cameraAttributes) {
                        LogUtils.e(TAG, "onCameraOpen");
                        mInstance.setPhotoSize(new CameraSize(ConstantsConfig.getInstance().getWidth(), ConstantsConfig.getInstance().getHeight()));
                        mInstance.setPreviewSize(new CameraSize(ConstantsConfig.getInstance().getWidth(), ConstantsConfig.getInstance().getHeight()));
                        mInstance.setPreviewOrientation(ConstantsConfig.getInstance().getFaceOri().getValue() * 90);
                        mInstance.setExposureCompensation(0);
                        mInstance.addPreviewCallbackWithBuffer(new CameraPreviewCallback() {
                            @Override
                            public void onCallBackPreview(byte[] data) {
                                LogUtils.e(TAG, "onCallBackPreview");
                            }
                        });
                        mInstance.startPreview(surface);
                    }

                    @Override
                    public void onCameraClose() {
                        LogUtils.e(TAG, "onCameraClose");
                    }

                    @Override
                    public void onCameraError(String errorMsg) {
                        LogUtils.e(TAG, "onCameraError");
                    }

                    @Override
                    public void onPreviewStarted() {
                        LogUtils.e(TAG, "onPreviewStarted");
                    }

                    @Override
                    public void onPreviewStopped() {
                        LogUtils.e(TAG, "onPreviewStopped");
                    }

                    @Override
                    public void onPreviewError(String errorMsg) {
                        LogUtils.e(TAG, "onPreviewError");
                    }
                }));
        mInstance.openCamera();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        LogUtils.e(TAG, "onSurfaceTextureAvailable");
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
        }
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, Camera1Activity.class);
        context.startActivity(starter);
    }
}