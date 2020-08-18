package com.stevez.camerakit;

import android.graphics.SurfaceTexture;

/**
 * @author: SteveZ
 * @created Create in 2020/8/3 10:52 AM.
 * @description: GLSurface CallBack
 */
public interface GLSurfaceViewListener {
    /**
     * 回调预览
     * @param surfaceTexture
     */
    void onCallBackSurfaceTexture(SurfaceTexture surfaceTexture);
}
