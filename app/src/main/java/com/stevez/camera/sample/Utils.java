package com.stevez.camera.sample;

import com.stevez.camerakit.ScalableType;

/**
 * @author: SteveZ
 * @created Create in 2020/8/28 1:57 PM.
 * @description: please add a description here
 */
class Utils {

    public static ScalableType SelectScaleForCameraView(String name) {
        switch (name) {
            case "none":
                return ScalableType.NONE;
            case "fitXY":
                return ScalableType.FIT_XY;
            case "fitStart":
                return ScalableType.FIT_START;
            case "fitCenter":
                return ScalableType.FIT_CENTER;
            case "fitEnd":
                return ScalableType.FIT_END;
            case "leftTop":
                return ScalableType.LEFT_TOP;
            case "leftCenter":
                return ScalableType.LEFT_CENTER;
            case "leftBottom":
                return ScalableType.LEFT_BOTTOM;
            case "centerTop":
                return ScalableType.CENTER_TOP;
            case "center":
                return ScalableType.CENTER;
            case "centerBottom":
                return ScalableType.CENTER_BOTTOM;
            case "rightTop":
                return ScalableType.RIGHT_TOP;
            case "rightCenter":
                return ScalableType.RIGHT_CENTER;
            case "rightBottom":
                return ScalableType.RIGHT_BOTTOM;
            case "leftTopCrop":
                return ScalableType.LEFT_TOP_CROP;
            case "leftCenterCrop":
                return ScalableType.LEFT_CENTER_CROP;
            case "leftBottomCrop":
                return ScalableType.LEFT_BOTTOM_CROP;
            case "centerTopCrop":
                return ScalableType.CENTER_TOP_CROP;
            case "centerCrop":
                return ScalableType.CENTER_CROP;
            case "centerBottomCrop":
                return ScalableType.CENTER_BOTTOM_CROP;
            case "rightTopCrop":
                return ScalableType.RIGHT_TOP_CROP;
            case "rightCenterCrop":
                return ScalableType.RIGHT_CENTER_CROP;
            case "rightBottomCrop":
                return ScalableType.RIGHT_BOTTOM_CROP;
            case "centerInside":
                return ScalableType.CENTER_INSIDE;
            case "startInside":
                return ScalableType.START_INSIDE;
            case "endInside":
                return ScalableType.END_INSIDE;
        }
        return ScalableType.NONE;
    }

}
