package com.stevez.camera.sample

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ToastUtils
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_home.*

/**
 * @date: Create in 3:36 PM 2020/7/8
 * @author: zhuyuliang
 * @description Demo Home
 */
class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        checkPermission()
        btn_config.setOnClickListener {
            ConfigActivity.start(this)
        }
        // 双目 Camera TextureView 封装
        btn_binocularcameratextureview.setOnClickListener {
            BinocularTextureViewActivity.start(this)
        }
        // 双目 Camera SurfaceView 封装
        btn_BinocularCameraGLSurfaceView.setOnClickListener {
            BinocularGLSurfaceActivity.start(this)
        }
        // 单目 Camera TextureView 封装
        btn_CameraTextureView.setOnClickListener {
            CameraTextureViewActivity.start(this)
        }
        // 单目 Camera SurfaceView 封装
        btn_CameraGLSurfaceView.setOnClickListener {
            CameraGLSurfaceActivity.start(this)
        }
        // ScaleTextureView 单独封装
        btn_TextureView.setOnClickListener {
            ScaleTextureViewActivity.start(this)
        }
        // ScaleSurfaceView 单独封装
        btn_GLSurfaceView.setOnClickListener {
            ScaleGLSurfaceActivity.start(this)
        }
        // Camera1调用
        btn_Camera1.setOnClickListener {
            Camera1Activity.start(this)
        }
        // Camera2调用
        btn_Camera2.setOnClickListener {
            Camera2Activity.start(this)
        }
        // UvcCamera调用
        btn_UvcCamera.setOnClickListener {
            CameraUvcActivity.start(this)
        }
        // 两个摄像头预览
        btn_TwoPreviewCamera.setOnClickListener {
            TwoCameraScaleTextureViewActivity.start(this)
        }
    }

    /**
     * check permission
     */
    private fun checkPermission() {
        RxPermissions(this)
                .request(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .observeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ isGranted: Boolean ->
                    if (isGranted) {
                        //Toaster.show(R.string.init_success);
                    } else {
                        ToastUtils.showShort(getString(R.string.permission_denied))
                        finish()
                    }
                }, { throwable: Throwable ->
                    ToastUtils.showShort(throwable.message)
                })
    }

    companion object {
        fun start(context: Context) {
            val starter = Intent(context, HomeActivity::class.java)
            starter.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(starter)
        }
    }
}