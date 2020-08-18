package com.stevez.camera;

/**
 * @author: SteveZ
 * @created Create in 2020/7/27 10:04 AM.
 * @description: 相机拍照数据回调
 */
public interface CapturePhotoCallback {

    /**
     * 返回捕获的照片的byte数据
     *
     * @param data
     */
    void onCallBackPhoto(byte[] data);

}
