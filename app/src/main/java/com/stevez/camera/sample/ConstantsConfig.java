package com.stevez.camera.sample;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.stevez.camerakit.Direction;

/**
 * 基础全局变量的配置
 *
 * @author steve
 */
public class ConstantsConfig {

    private final String TAG = "ConstantsConfig";

    private static boolean isDebug;
    private static Application sApplicationContext = null;

    private SharedPreferences sp;

    private static ConstantsConfig config;

    private ConstantsConfig() {
    }

    public static ConstantsConfig getInstance() {
        if (config == null) {
            config = new ConstantsConfig();
        }
        return config;
    }

    public void onInit(Application appContext, boolean isDebug) {
        sApplicationContext = appContext;
        ConstantsConfig.isDebug = isDebug;
        sp = sApplicationContext.getSharedPreferences("sdk_setting", 0);
    }

    public static boolean isDebug() {
        return isDebug;
    }

    public static Context getContext() {
        return sApplicationContext;
    }

    /**
     * 人脸方向
     *
     * @param faceOri
     */
    public void setFaceOri(Direction faceOri) {
        sp.edit().putInt("faceori", faceOri.getValue()).commit();
    }

    public Direction getFaceOri() {
        int ori = sp.getInt("faceori", 0);
        if (ori == Direction.UP.getValue()) {
            return Direction.UP;
        } else if (ori == Direction.LEFT.getValue()) {
            return Direction.LEFT;
        } else if (ori == Direction.RIGHT.getValue()) {
            return Direction.RIGHT;
        } else if (ori == Direction.DOWN.getValue()) {
            return Direction.DOWN;
        }
        return Direction.AUTO;
    }

    /**
     * 宽高
     *
     * @param param
     */
    public void setWidth(int param) {
        sp.edit().putInt("width", param).commit();
    }

    public int getWidth() {
        return sp.getInt("width", 640);
    }

    public void setHeight(int param) {
        sp.edit().putInt("height", param).commit();
    }

    public int getHeight() {
        return sp.getInt("height", 480);
    }

    /**
     * 镜像
     *
     * @param mirror
     */
    public void setMirror(boolean mirror) {
        sp.edit().putBoolean("ismirror", mirror).commit();
    }

    public boolean getMirror() {
        return sp.getBoolean("ismirror", false);
    }

    /**
     * rgb
     *
     * @param cameraId
     */
    public void setRgbCameraId(int cameraId) {
        sp.edit().putInt("rgbcameraid", cameraId).commit();
    }

    public int getRgbCamereId() {
        return sp.getInt("rgbcameraid", 0);
    }

    /**
     * ir
     *
     * @param cameraId
     */
    public void setIrCameraId(int cameraId) {
        sp.edit().putInt("ircameraid", cameraId).commit();
    }

    public int getIrCamereId() {
        return sp.getInt("ircameraid", 1);
    }

    public void setVid(int cameraId) {
        sp.edit().putInt("vid", cameraId).commit();
    }

    public int getVid() {
        return sp.getInt("vid", 0);
    }

    public void setPid(int cameraId) {
        sp.edit().putInt("pid", cameraId).commit();
    }

    public int getPid() {
        return sp.getInt("pid", 0);
    }

}
