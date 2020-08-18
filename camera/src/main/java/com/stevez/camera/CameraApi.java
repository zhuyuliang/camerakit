package com.stevez.camera;

import androidx.lifecycle.LifecycleObserver;

/**
 * @author: SteveZ
 * @created on: 2020/5/25 5:55 PM.
 * @description:
 */
abstract class CameraApi implements
        CameraActions, LifecycleObserver {

    /**
     * Handler实例
     */
    protected CameraHandler cameraHandler;

    /**
     * 设置事件回调
     *
     * @param callBackEvents
     */
    protected CallBackEvents callBackEvents;

}
