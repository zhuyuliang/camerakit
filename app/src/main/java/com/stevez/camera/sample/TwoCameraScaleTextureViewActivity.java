package com.stevez.camera.sample;

import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
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
import com.stevez.camerakit.ScaleTextureView;

/**
 * @author: Zhu Yuliang
 * @created Create in 2020/6/23 4:23 PM.
 * @description: TwoCameraScaleTextureViewActivity
 */
public class TwoCameraScaleTextureViewActivity extends AppCompatActivity {

    public static final String TAG = "TwoCameraScaleTextureViewActivity";

    private ScaleTextureView scaleTextureView1;
    private ScaleTextureView scaleTextureView2;
    private CameraManager mInstance1;
    private CameraManager mInstance2;
    private TextView textView;
    private ImageView img_back;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_camera_scaletexture);
        textView = findViewById(R.id.txt_title);
        img_back = findViewById(R.id.img_back);
        textView.setText("TwoCameraScaleTextureView");
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        scaleTextureView1 = findViewById(R.id.camera_textureview1);
        scaleTextureView2 = findViewById(R.id.camera_textureview2);
        scaleTextureView1.setDisplayDir(ConstantsConfig.getInstance().getFaceOri());
        scaleTextureView1.resetPreviewSize(ConstantsConfig.getInstance().getWidth(), ConstantsConfig.getInstance().getHeight());
        scaleTextureView2.setDisplayDir(ConstantsConfig.getInstance().getFaceOri());
        scaleTextureView2.resetPreviewSize(ConstantsConfig.getInstance().getWidth(), ConstantsConfig.getInstance().getHeight());
        getLifecycle().addObserver(scaleTextureView1);
        getLifecycle().addObserver(scaleTextureView2);
        scaleTextureView1.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                openCamera1(surface);
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
        });
        scaleTextureView2.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                openCamera2(surface);
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
        });
    }

    private void openCamera1(SurfaceTexture surface) {
        mInstance1 = CameraManager.getInstance(
                new CameraFacing.Builder().setFacingType(FacingType.OTHER)
                        .setCameraId(ConstantsConfig.getInstance().getRgbCamereId()).build(),
                CameraApiType.CAMERA1, getBaseContext());
        mInstance1.setCallBackEvents(
                (new CallBackEvents() {
                    @Override
                    public void onCameraOpen(IAttributes cameraAttributes) {
                        LogUtils.e(TAG, "onCameraOpen");
                        mInstance1.setPhotoSize(new CameraSize(ConstantsConfig.getInstance().getWidth(), ConstantsConfig.getInstance().getHeight()));
                        mInstance1.setPreviewSize(new CameraSize(ConstantsConfig.getInstance().getWidth(), ConstantsConfig.getInstance().getHeight()));
                        mInstance1.setPreviewOrientation(ConstantsConfig.getInstance().getFaceOri().getValue() * 90);
                        mInstance1.setExposureCompensation(0);
                        mInstance1.addPreviewCallbackWithBuffer(new CameraPreviewCallback() {
                            @Override
                            public void onCallBackPreview(byte[] data) {
                                LogUtils.e(TAG, "onCallBackPreview");
                            }
                        });
                        mInstance1.startPreview(surface);
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
        mInstance1.openCamera();
    }

    private void openCamera2(SurfaceTexture surface) {
        Log.d("TAG", "openCamera");
        mInstance2 = CameraManager.getInstance(
                new CameraFacing.Builder().setFacingType(FacingType.OTHER)
                        .setCameraId(ConstantsConfig.getInstance().getIrCamereId()).build(),
                CameraApiType.CAMERA1, getBaseContext());
        mInstance2.setCallBackEvents(
                (new CallBackEvents() {
                    @Override
                    public void onCameraOpen(IAttributes cameraAttributes) {
                        LogUtils.e(TAG, "onCameraOpen");
                        mInstance2.setPhotoSize(new CameraSize(ConstantsConfig.getInstance().getWidth(), ConstantsConfig.getInstance().getHeight()));
                        mInstance2.setPreviewSize(new CameraSize(ConstantsConfig.getInstance().getWidth(), ConstantsConfig.getInstance().getHeight()));
                        mInstance2.setPreviewOrientation(ConstantsConfig.getInstance().getFaceOri().getValue() * 90);
                        mInstance2.setExposureCompensation(0);
                        mInstance2.addPreviewCallbackWithBuffer(new CameraPreviewCallback() {
                            @Override
                            public void onCallBackPreview(byte[] data) {
                                LogUtils.e(TAG, "onCallBackPreview");
                            }
                        });
                        mInstance2.startPreview(surface);
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
        mInstance2.openCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mInstance1 != null) {
            this.mInstance1.stopPreview();
            this.mInstance1.release();
        }
        if (mInstance2 != null) {
            this.mInstance2.stopPreview();
            this.mInstance2.release();
        }
        getLifecycle().removeObserver(scaleTextureView1);
        getLifecycle().removeObserver(scaleTextureView2);
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, TwoCameraScaleTextureViewActivity.class);
        context.startActivity(starter);
    }
}