# Camera

```text
Version : V1.0.0
Date : 2020.07.24
Author : SteveZ
```

# Sample
注意：UVC摄像头调用需要依赖libuvccamera库

```text
public void openCamera(){
   CameraManager mInstance = CameraManager.getInstance(new CameraFacing.Builder().setFacingType(FacingType.FRONT).build());
   mInstance.setCallBackEvents(
           (new CallBackEvents() {
                    @Override
                    public void onCameraOpen(IAttributes cameraAttributes) {
                        mInstance.setPhotoSize(new CameraSize(CameraConfig.sPreviewWidth, CameraConfig.sPreviewHeight));
                        mInstance.setPreviewSize(new CameraSize(CameraConfig.sPreviewWidth, CameraConfig.sPreviewHeight));
                        mInstance.setPreviewOrientation(CameraConfig.sDegree * 90);
                        mInstance.setExposureCompensation(0);
                        mInstance.startPreview(surface);
                    }
                    @Override
                    public void onCameraClose() {
                    }
                    @Override
                    public void onCameraError(String errorMsg) {
                    }
                    @Override
                    public void onPreviewStarted() {
                    }
                    @Override
                    public void onPreviewStopped() {
                    }
                    @Override
                    public void onPreviewError(String errorMsg) {
                    }
                }));
        mInstance.openCamera();
}
```

```text
通过CameraManager.getInstance()可以配置调用的摄像头和摄像头的调用方式
1.Camera1的调用方式:
CameraManager.getInstance(
                new CameraFacing.Builder().setFacingType(FacingType.OTHER)
                        .setCameraId(0).build(),
                CameraApiType.CAMERA1, getBaseContext())
2.Camera2的调用方式:
CameraManager.getInstance(
                new CameraFacing.Builder().setFacingType(FacingType.OTHER)
                        .setCameraId(0).build(),
                CameraApiType.CAMERA2, getBaseContext(),new Handler(getMainLooper()),Camera2Activity.this.getLifecycle());
3.UvcCamera的调用方式:
CameraManager.getInstance(
                new CameraFacing.Builder().setFacingType(FacingType.UVC)
                        .setCameraId(123)
                        .setVendorId(ConstantsConfig.getInstance().getVid())
                        .setProductId(ConstantsConfig.getInstance().getPid()).build(),
                CameraApiType.CAMERAUVC, getBaseContext(),new Handler(getMainLooper()),CameraUvcActivity.this.getLifecycle());
注意：UVC摄像头调用需要依赖libuvccamera库
```

-----

## With improved

- 抽取CameraApi
- 实现获取，预览，拍照等功能。
- Camera1，Camera2 and CameraX 统一API封装
   *（CameraX需要新开分支，Camera只支持API21以上，无法做自动处理）
   * 目前CameraX的API并不稳定，并且变动较大，后期稳定版本在做适配。
   
- 添加UVC能力

## Init Features

```text
Version : V1.0.0
Date : 2020.05.26
Author : SteveZ
```

- 抽取相机基本能力
- 封装Camera1 API
- 封装Camera2 API
- 封装Camerax API （正式版进行）