package com.stevez.camerakit;

/**
 * @author: SteveZ
 * @created on: 2020/5/25 5:57 PM.
 * @description: Camera生命周期
 */
public interface ICameraLifecycleCallback {

    /**
     * 相机开启
     */
    void onCameraOpen();

    /**
     * 相机关闭
     */
    void onCameraClose();

    /**
     * 相机错误
     */
    void onCameraError(String errorMsg);

    /**
     * 预览开启
     */
    void onPreviewStarted();

    /**
     * 停止预览
     */
    void onPreviewStopped();

    /**
     * 预览错误
     */
    void onPreviewError(String errorMsg);

}

