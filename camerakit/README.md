# Camera

```text
Version : V1.0.8
Date : 2020.07.21
Author : SteveZ
```

## Sample
注意：1.UVC摄像头调用需要依赖libuvccamera库
     2.camerakit的相机部分需要依赖camera库

### BinocularCameraTextureView / BinocularCameraGLSurfaceView

-----

```xml
<com.stevez.camerakit.BinocularCameraTextureView
            android:id="@+id/camera_textureview"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            stevez:cameraDir="up"
            stevez:rgbCamera="0"
            stevez:irCamera="1"
            stevez:cameraPreviewId="1"
            stevez:isMirror="false"
            stevez:exposureCompensation="4"
            stevez:previewHeight="720"
            stevez:previewWidth="1280"
            stevez:scalableType="centerInside"/>
```

```text
- stevez:rgbCamera="0" //RGB CameraID
- stevez:irCamera="1" //IR CameraID
- stevez:cameraPreviewId="1" //至于预览的CameraID

- BinocularCameraGLSurfaceView
     - stevez:scalableType="centerInside" 无效
- 
```

```java
public class DemoActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private BinocularCameraTextureView cameraTextureView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //权限回调
        binocularCameraTextureView.setPermissionsListener(new PermissionsListener() {
            @Override
            public void onPermissionsSuccess() {
                ToastUtils.showShort("onPermissionsSuccess");
            }

            @Override
            public void onPermissionsFailure() {
                ToastUtils.showShort("onPermissionsFailure");
                //失败重新请求权限
                binocularCameraTextureView.requestPermissions(MainActivity.this);
            }
        });        
        
        binocularCameraTextureView.setDisplayDir(CameraConfig.sDegree);
        binocularCameraTextureView.resetPreviewSize(CameraConfig.sCameraPreviewWidth,CameraConfig.sCameraPreviewHeight);
        binocularCameraTextureView.setRgbCameraId(1);
        binocularCameraTextureView.setRgbCameraId(0);

        cameraTextureView.setRgbPreviewCallback(RGBPreviewCallback());
        cameraTextureView.setIrPreviewCallback(IRPreviewCallback());
        this.getLifecycle().addObserver(cameraTextureView);
        
        ...

    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.getLifecycle().removeObserver(binocularCameraTextureView);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        binocularCameraTextureView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    ...

}
```

-----

### CameraTextureView / CameraGLSurfaceView

-----

```xml
<com.stevez.camerakit.CameraTextureView
            android:id="@+id/camera_textureview"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            stevez:cameraDir="left"
            stevez:isMirror="true"
            stevez:myCameraFacing="back"
            stevez:previewHeight="720"
            stevez:previewWidth="1280"
            stevez:exposureCompensation="4"
            stevez:scalableType="centerTopCrop"/>
```

```java
public class DemoActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private CameraTextureView cameraTextureView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //权限回调
        binocularCameraTextureView.setPermissionsListener(new PermissionsListener() {
            @Override
            public void onPermissionsSuccess() {
                ToastUtils.showShort("onPermissionsSuccess");
            }

            @Override
            public void onPermissionsFailure() {
                ToastUtils.showShort("onPermissionsFailure");
                //失败重新请求权限
                binocularCameraTextureView.requestPermissions(MainActivity.this);
            }
        });        
        
        binocularCameraTextureView.setDisplayDir(CameraConfig.sDegree);
        binocularCameraTextureView.resetPreviewSize(CameraConfig.sCameraPreviewWidth,CameraConfig.sCameraPreviewHeight);
        binocularCameraTextureView.setCameraId(0);

        cameraTextureView.setPreviewCallback(RGBPreviewCallback());
        this.getLifecycle().addObserver(cameraTextureView);
        ...

    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.getLifecycle().removeObserver(binocularCameraTextureView);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        binocularCameraTextureView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    ...

}
```

### ScaleTextureView / ScaleGLSurfaceView

-----

```xml
<com.stevez.camerakit.ScaleTextureView
            android:id="@+id/camera_textureview"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            stevez:cameraDir="left"
            stevez:isMirror="true"
            stevez:previewHeight="720"
            stevez:previewWidth="1280"
            stevez:scalableType="centerTopCrop"/>
```

```java
public class DemoActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private ScaleTextureView cameraTextureView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ...

        //TextureView
        cameraTextureView.setSurfaceTextureListener(this);
        
        //GLSurfaceView
        scaleTextureView.setListener(new ScaleGLSurfaceView.Listener() {
                @Override
                public void onPreviewStatus(LifecycleState lifecycleState) {
                    if(lifecycleState == LifecycleState.STARTED) {
                        openCamera();
                    }
                }
            });
        
        caleTextureView.setDisplayDir(CameraConfig.sDegree);
        scaleTextureView.resetPreviewSize(CameraConfig.sCameraPreviewWidth,CameraConfig.sCameraPreviewHeight);


    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
    }

    ...

}
```

-----

## With improved

- 抽取CameraManager
- Camera实现，拍照，录像等功能。
- 预览界面封装及单独使用

- Camera and CameraX 统一API封装

## Init Features

```text
Version : V1.0.0
Date : 2020.05.26
Author : SteveZ
```

- 双目相机使用

## update

```text
Version : V1.0.4
Date : 2020.06.24
Author : SteveZ
```

- 新增双目TextureView (BinocularCameraTextureView)

## update

```text
Version : V1.0.5
Date : 2020.07.01
Author : SteveZ
```

- 新增BinocularCameraTextureView曝光补偿配置exposureCompensation=default=0

## update

```text
Version : V1.0.6
Date : 2020.07.21
Author : SteveZ
```

- 新增BinocularCameraTextureView权限检查和生命周期控制
- 新增CameraTextureView权限检查和生命周期控制

## update

```text
Version : V1.0.7
Date : 2020.07.23
Author : SteveZ
```

- 新增GLSurfaceView预览方式 （TextureView只能在硬件加速的环境下使用）
- 新增CameraSurfaceTexture C++实现
- 新增CameraSurfaceView C++实现

## update

```text
Version : V1.0.8
Date : 2020.07.23
Author : SteveZ
```

- 优化对焦方式
- 优化支持android10




