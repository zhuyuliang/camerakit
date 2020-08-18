package com.stevez.camera;

/**
 * @author: SteveZ
 * @created Create in 2020/7/27 10:04 AM.
 * @description: 相机事件回调
 */
public interface CallBackEvents {

    /**
     * 相机开启
     *
     * @param cameraAttributes
     * <p> 返回支持的的当前相机参数 <p/>
     */
    void onCameraOpen(IAttributes cameraAttributes);

    /**
     * 相机关闭
     */
    void onCameraClose();

    /**
     * 相机错误
     *
     * @param errorMsg 错误信息
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
     *
     * @param errorMsg 错误信息
     */
    void onPreviewError(String errorMsg);

}
