package com.stevez.camerakit;

/**
 * @author: SteveZ
 * @created Create in 2020/7/21 3:43 PM.
 * @description: 相机权限回调
 */
public interface PermissionsListener {

    /**
     * 权限获取通过
     */
    void onPermissionsSuccess();

    /**
     * 权限获取失败
     */
    void onPermissionsFailure();

}
