package com.stevez.camera;

import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;

/**
 * @author: SteveZ
 * @created Create in 2020/7/27 10:05 AM.
 * @description: 相机Handler
 */
class CameraHandler extends Handler {

    /**
     * 获取CameraHandler实例
     * @return
     */
    public static CameraHandler get() {
        HandlerThread cameraThread =
                new HandlerThread("CameraHandler@"+System.currentTimeMillis());
        cameraThread.start();
        return new CameraHandler(cameraThread);
    }

    public CameraHandler(@NonNull  HandlerThread handlerThread){
        super(handlerThread.getLooper());
        handlerThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
                    // TODO
            }
        });
    }

    public CameraHandler(@NonNull  Handler handler){
        super(handler.getLooper());
    }

}
