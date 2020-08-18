package com.stevez.camera;

/**
 * @author: SteveZ
 * @created Create in 2020/7/27 10:04 AM.
 * @description: 相机预览回调
 */
public interface CameraPreviewCallback {

    /**
     * 返回捕获的byte数据
     *
     * @param data
     */
    void onCallBackPreview(byte[] data);

}
