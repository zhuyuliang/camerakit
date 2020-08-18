package com.stevez.camerakit;

import androidx.annotation.NonNull;

/**
 * @author: SteveZ
 * @created Create in 2020/7/21 2:27 PM.
 * @description: please add a description here
 */
interface IBinocularCameraView extends IPermissionCameraView {

    /**
     * 设置rgbCameraId
     */
    void setRgbCameraId(@NonNull  int cameraId);

    /**
     * 设置irCameraId
     */
    void setIrCameraId(@NonNull  int cameraId);

    /**
     * 设置预览CameraId
     */
    void setPreviewCameraId(@NonNull  int cameraId);

}
