package com.stevez.camerakit;

/**
 * @author: SteveZ
 * @created Create in 2020/7/21 2:27 PM.
 * @description: please add a description here
 */
interface ICameraView {

    /**
     * 重置预览大小
     * @param width
     * @param height
     */
    void resetPreviewSize(int width,int height);

    /**
     * 设置镜像
     * @param mirror
     */
    void setMirror(boolean mirror);

    /**
     * 设置屏幕方向
     * @param displayDirection
     */
    void setDisplayDir(Direction displayDirection);

    /**
     * 设置style
     */
    void setStyle(ScalableType style);

    /**
     * start preview
     */
    void onStart();

    /**
     * resume preview
     */
    void onResume();

    /**
     * pause preview
     */
    void onPause();

    /**
     * stop preview
     */
    void onStop();

}
