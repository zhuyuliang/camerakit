package com.stevez.camerakit;

import android.app.Activity;

/**
 * @author: SteveZ
 * @created Create in 2020/7/21 2:27 PM.
 * @description: please add a description here
 */
interface IPermissionCameraView extends ICameraView {

    /**
     * 检查权限
     * @param activity
     */
    void requestPermissions(Activity activity);

    /**
     * permissions check.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults);

    /**
     * set permission listener
     * @param permissionsListener
     */
    void setPermissionsListener(PermissionsListener permissionsListener);

}
