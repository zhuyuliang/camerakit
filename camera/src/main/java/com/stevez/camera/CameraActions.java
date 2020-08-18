package com.stevez.camera;

import android.graphics.SurfaceTexture;

/**
 * @author: SteveZ
 * @created Create in 2020/7/27 10:04 AM.
 * @description: 相机操作API
 */
interface CameraActions {

    /**
     * 打开相机
     * @param facingType
     */
    void openCamera(CameraFacing facingType);

    /**
     * 释放
     */
    void release();

    /**
     * 添加 回调帧数据
     * @param callback
     */
    void addPreviewCallbackWithBuffer(CameraPreviewCallback callback);

    /**
     * 移出 回调帧数据
     * @param callback
     */
    void removePreviewCallbackWithBuffer(CameraPreviewCallback callback);


    /**
     * 清除 回调帧数据
     */
    void clearPreviewCallbackWithBuffer();

    /**
     * 设置相机预览方向
     * @param orientation
     */
    void setPreviewOrientation(int orientation);

    /**
     * 设置预览尺寸
     * @param size
     */
    void setPreviewSize(CameraSize size);

    /**
     * 开始预览
     * @param surfacetexture
     */
    void startPreview(SurfaceTexture surfacetexture);

    /**
     * 停止预览
     */
    void stopPreview();

    /**
     * 设置Flash闪光灯
     * @param flash
     */
    void setFlash(CameraFlash flash);

    /**
     * 设置焦点模式
     * @param focus
     */
    void setFocusMode(CameraFocus focus);

    /**
     * 设置相机曝光
     * @param exposureCompensation
     */
    void setExposureCompensation(int exposureCompensation);

    /**
     * 设置照片尺寸
     * @param size
     */
    void setPhotoSize(CameraSize size);

    /**
     * 开始捕获照片
     * @param callback
     */
    void capturePhoto(CapturePhotoCallback callback);

}
