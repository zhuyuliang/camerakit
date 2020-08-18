package com.stevez.camera;

/**
 * Camera Facing.
 *
 * @author SteveZ
 * <p>
 * Caution：当摄像头类型为OTHER的时候，cameraid需要手动设置
 * <p/>
 */
public class CameraFacing {

    /**
     * 摄像头类型
     */
    public FacingType facingType;

    /**
     * 当摄像头类型为OTHER时，cameraId自定义.
     */
    public int cameraId;

    public int pid;
    public int vid;

    private CameraFacing(Builder builder) {
        this.facingType = builder.facingType;
        this.cameraId = builder.cameraId;
        this.pid = builder.pid;
        this.vid = builder.vid;
    }

    public static final class Builder {
        public FacingType facingType = FacingType.BACK;
        public int cameraId = 0;
        public int pid = 0;
        public int vid = 0;

        public Builder setFacingType(FacingType facingType) {
            this.facingType = facingType;
            return this;
        }

        public Builder setCameraId(int cameraId) {
            this.cameraId = cameraId;
            return this;
        }

        public Builder setProductId(int pid){
            this.pid = pid;
            return this;
        }

        public Builder setVendorId(int vid){
            this.vid = vid;
            return this;
        }

        public CameraFacing build() {
            return new CameraFacing(this);
        }
    }

}