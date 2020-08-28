package com.stevez.camera.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;

import com.blankj.utilcode.util.LogUtils;
import com.stevez.camerakit.BinocularCameraTextureView;
import com.stevez.camerakit.PermissionsListener;
import com.stevez.camerakit.ScalableType;

/**
 * @author: Zhu Yuliang
 * @created Create in 2020/6/23 4:23 PM.
 * @description: BinocularTextureViewActivity
 */
public class BinocularTextureViewActivity extends AppCompatActivity {

    public static final String TAG = "BinocularTextureViewActivity";

    private AppCompatSpinner spinner2;
    private BinocularCameraTextureView cameraView;
    private TextView textView;
    private ImageView img_back;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binocular_textureview);
        cameraView = findViewById(R.id.camera_textureview);
        spinner2 = findViewById(R.id.spinner);
        textView = findViewById(R.id.txt_title);
        img_back = findViewById(R.id.img_back);
        textView.setText("BinocularTextureView");
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String name = (String) parent.getItemAtPosition(position);
                cameraView.setStyle(Utils.SelectScaleForCameraView(name));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        cameraView.setPermissionsListener(new PermissionsListener() {
            @Override
            public void onPermissionsSuccess() {
                LogUtils.d(TAG, "onPermissionsSuccess");
            }

            @Override
            public void onPermissionsFailure() {
                LogUtils.d(TAG, "onPermissionsFailure");
                cameraView.requestPermissions(BinocularTextureViewActivity.this);
            }
        });
        cameraView.setDisplayDir(ConstantsConfig.getInstance().getFaceOri());
        cameraView.resetPreviewSize(ConstantsConfig.getInstance().getWidth(), ConstantsConfig.getInstance().getHeight());
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
        Intent starter = new Intent(context, BinocularTextureViewActivity.class);
        context.startActivity(starter);
    }

}