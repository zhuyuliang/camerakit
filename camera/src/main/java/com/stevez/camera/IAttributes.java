package com.stevez.camera;

import java.util.List;

/**
 * @author: SteveZ
 * @created Create in 2020/7/27 10:11 AM.
 * @description: 相机属性
 */
public class IAttributes {

    public CameraFacing facing;
    public int orientation = 0;
    public List<CameraSize> previewSize;
    public List<CameraSize> photoSize;
    public List<CameraFlash> flashes;
    public List<CameraFocus> focusList;

}
