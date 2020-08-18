//package com.stevez.camera;
//
//import android.content.Context;
//import android.graphics.SurfaceTexture;
//import android.hardware.camera2.CameraCharacteristics;
//import android.os.Build;
//import android.util.Log;
//import android.view.Surface;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.RequiresApi;
//import androidx.camera.core.Camera;
//import androidx.camera.core.CameraInfo;
//import androidx.camera.core.CameraInfoUnavailableException;
//import androidx.camera.core.CameraSelector;
//import androidx.camera.core.Preview;
//import androidx.camera.core.SurfaceRequest;
//import androidx.camera.core.impl.PreviewConfig;
//import androidx.camera.lifecycle.ProcessCameraProvider;
//import androidx.core.content.ContextCompat;
//import androidx.lifecycle.Lifecycle;
//import androidx.lifecycle.LifecycleOwner;
//
//import com.google.common.util.concurrent.ListenableFuture;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.concurrent.ExecutionException;
//
//import static com.stevez.camera.CameraFlash.AUTO;
//import static com.stevez.camera.CameraFlash.OFF;
//import static com.stevez.camera.CameraFlash.ON;
//import static com.stevez.camera.CameraFlash.TORCH;
//
///**
// * @author: SteveZ
// * @created Create in 2020/7/27 11:10 AM.
// * @description: please add a description here
// */
//public class Camera3 extends CameraApi implements LifecycleOwner {
//
//    private static final String TAG = "CameraX";
//
//    private static final int DEFAULT_PREVIEW_WIDTH = 640;
//    private static final int DEFAULT_PREVIEW_HEIGHT = 480;
//
//    private Camera mCamera = null;
//    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture = null;
//    private ProcessCameraProvider cameraProvider = null;
//    private CameraSelector cameraSelector = null;
//    private CameraXAttributes mCameraAttributes = null;
//    private HashSet<CameraPreviewCallback> mCustomPreviewCallbacks;
//
//    private Context context;
//
//    private CameraFlash flash = CameraFlash.OFF;
//    private CameraFocus focus = CameraFocus.AUTO;
//    private int exposureCompensation = 0;
//    private int mOrientation = 0;
//    private CapturePhotoCallback photoCallback = null;
//    private CameraFacing cameraFacing = new CameraFacing.Builder().build();
//    private Lifecycle lifecycle;
//
//    private PreviewConfig previewConfig;
//    private Preview preview;
//    private CameraSize mPreviewSize;
//
//    public Camera3(@NonNull CallBackEvents callBackEvents, Context context, Lifecycle lifecycle) {
//        cameraHandler = CameraHandler.get();
//        this.callBackEvents = callBackEvents;
//        this.context = context;
//        this.lifecycle = lifecycle;
//        lifecycle.addObserver(this);
//    }
//
//    @Override
//    public synchronized void openCamera(CameraFacing cameraFacing) {
//        if (cameraFacing.facingType == FacingType.BACK) {
//            cameraFacing.cameraId = CameraCharacteristics.LENS_FACING_BACK;
//        } else if (cameraFacing.facingType == FacingType.FRONT) {
//            cameraFacing.cameraId = CameraCharacteristics.LENS_FACING_FRONT;
//        }
//
//        cameraProviderFuture = ProcessCameraProvider.getInstance(context);
//        cameraProviderFuture.addListener(new Runnable() {
//
//            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//            @Override
//            public void run() {
//                try {
//                    // Used to bind the lifecycle of cameras to the lifecycle owner
//                    cameraProvider = cameraProviderFuture.get();
//                    // Select camera
//                    cameraSelector = new CameraSelector.Builder()
//                            .requireLensFacing(cameraFacing.cameraId).build();
//
//                    boolean isSuccess = cameraProvider.hasCamera(cameraSelector);
//                    if (!isSuccess) {
//                        if (callBackEvents != null) {
//                            callBackEvents.onCameraError("open camera error!");
//                        }
//                        return;
//                    }
//
//                    mCamera = cameraProvider.bindToLifecycle(Camera3.this, cameraSelector);
//                    mCameraAttributes = new CameraXAttributes(mCamera.getCameraInfo(), cameraFacing);
//
//                    if (callBackEvents != null) {
//                        callBackEvents.onCameraOpen(mCameraAttributes);
//                    }
//                } catch (ExecutionException e) {
//                    if (callBackEvents != null) {
//                        callBackEvents.onCameraError("open camera error!" + e.getMessage());
//                    }
//                } catch (InterruptedException e) {
//                    if (callBackEvents != null) {
//                        callBackEvents.onCameraError("open camera error!" + e.getMessage());
//                    }
//                } catch (CameraInfoUnavailableException e) {
//                    if (callBackEvents != null) {
//                        callBackEvents.onCameraError("open camera error!" + e.getMessage());
//                    }
//                }
//            }
//        }, ContextCompat.getMainExecutor(context));
//        //camerahandler
//
//    }
//
//    @Override
//    public synchronized void addPreviewCallbackWithBuffer(CameraPreviewCallback callback) {
//        if (this.mCustomPreviewCallbacks == null) {
//            this.mCustomPreviewCallbacks = new HashSet();
//        }
//        this.mCustomPreviewCallbacks.add(callback);
//    }
//
//    @Override
//    public synchronized void removePreviewCallbackWithBuffer(CameraPreviewCallback callback) {
//        if (this.mCustomPreviewCallbacks == null) {
//        } else {
//            this.mCustomPreviewCallbacks.remove(callback);
//        }
//    }
//
//    @Override
//    public void clearPreviewCallbackWithBuffer() {
//        if (this.mCustomPreviewCallbacks != null) {
//            this.mCustomPreviewCallbacks.clear();
//        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    public synchronized void release() {
//        mCameraAttributes = null;
//        if (callBackEvents != null) {
//            callBackEvents.onCameraClose();
//        }
//    }
//
//    @Override
//    public synchronized void setPreviewOrientation(int degrees) {
//        this.mOrientation = degrees;
//    }
//
//    @Override
//    public synchronized void setPreviewSize(CameraSize size) {
//        this.mPreviewSize = size;
//    }
//
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    public synchronized void startPreview(SurfaceTexture surfaceTexture) {
//        try {
//            // Unbind use cases before rebinding
//            cameraProvider.unbindAll();
//
//            buildPreviewUseCase(surfaceTexture);
//            // Bind use cases to camera
//            mCamera = cameraProvider.bindToLifecycle(
//                    this, cameraSelector, preview);
//        } catch (Exception exc) {
//            Log.e(TAG, "Use case binding failed", exc);
//        }
//
//
//        if (callBackEvents != null) {
//            callBackEvents.onPreviewStarted();
//        }
//        if (callBackEvents != null) {
//            callBackEvents.onPreviewError("Is set PhotoSize?");
//        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private void buildPreviewUseCase(SurfaceTexture surfaceTexture) {
////        preview = new Preview.Builder()
////                // 宽高比
////                //.setTargetAspectRatio(aspectRatio)
////                // 旋转
////                .setTargetRotation(mOrientation)
////                // 分辨率
////                .setTargetResolution(mPreviewSize.getSize())
////                .build();
////        // 设置监听
////        preview.setOnPreviewOutputUpdateListener { previewOutput ->
////                // PreviewOutput 会返回一个 SurfaceTexture
////                cameraTextureView.surfaceTexture = previewOutput.surfaceTexture
////
////            // Compute the center of preview (TextureView)
////            val centerX = cameraTextureView.width.toFloat() / 2
////            val centerY = cameraTextureView.height.toFloat() / 2
////
////            // Correct preview output to account for display rotation
////            val rotationDegrees = when (cameraTextureView.display.rotation) {
////                Surface.ROTATION_0 -> 0
////                Surface.ROTATION_90 -> 90
////                Surface.ROTATION_180 -> 180
////                Surface.ROTATION_270 -> 270
////        else -> return@setOnPreviewOutputUpdateListener
////            }
////
////            val matrix = Matrix()
////            matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)
////
////            // Finally, apply transformations to TextureView
////            cameraTextureView.setTransform(matrix)
////        }
//
//    }
//
////    fun buildImageAnalysisUseCase(): ImageAnalysis {
////        // 分析器配置 Config 的建造者
////        val analysisConfig = ImageAnalysisConfig.Builder()
////                // 宽高比例
////                .setTargetAspectRatio(aspectRatio)
////                // 旋转
////                .setTargetRotation(rotation)
////                // 分辨率
////                .setTargetResolution(resolution)
////                // 图像渲染模式
////                .setImageReaderMode(readerMode)
////                // 图像队列深度
////                .setImageQueueDepth(queueDepth)
////                // 设置回调的线程
////                .setCallbackHandler(handler)
////                .build()
////
////        // 创建分析器 ImageAnalysis 对象
////        val analysis = ImageAnalysis(analysisConfig)
////
////        // setAnalyzer 传入实现了 analyze 接口的类
////        analysis.setAnalyzer { image, rotationDegrees ->
////                // 可以得到的一些图像信息，参见 ImageProxy 类相关方法
////                val rect = image.cropRect
////            val format = image.format
////            val width = image.width
////            val height = image.height
////            val planes = image.planes
////        }
////
////        return analysis
////    }
////
////    fun buildImageCaptureUseCase(): ImageCapture {
////        val captureConfig = ImageCaptureConfig.Builder()
////                .setTargetAspectRatio(aspectRatio)
////                .setTargetRotation(rotation)
////                .setTargetResolution(resolution)
////                .setFlashMode(flashMode)
////                // 拍摄模式
////                .setCaptureMode(captureMode)
////                .build()
////
////        // 创建 ImageCapture 对象
////        val capture = ImageCapture(captureConfig)
////        cameraCaptureImageButton.setOnClickListener {
////            // Create temporary file
////            val fileName = System.currentTimeMillis().toString()
////            val fileFormat = ".jpg"
////            val imageFile = createTempFile(fileName, fileFormat)
////
////            // Store captured image in the temporary file
////            capture.takePicture(imageFile, object : ImageCapture.OnImageSavedListener {
////                override fun onImageSaved(file: File) {
////                    // You may display the image for example using its path file.absolutePath
////                }
////
////                override fun onError(useCaseError: ImageCapture.UseCaseError, message: String, cause: Throwable?) {
////                    // Display error message
////                }
////            })
////        }
////
////        return capture
////    }
//
//    @Override
//    public synchronized void stopPreview() {
//        try {
//            cameraProvider.unbindAll();
//        } catch (Exception e) {
//        } finally {
//            if (callBackEvents != null) {
//                callBackEvents.onPreviewStopped();
//            }
//        }
//    }
//
//    @Override
//    public synchronized void setFlash(CameraFlash flash) {
//        this.flash = flash;
//    }
//
//    @Override
//    public synchronized void setFocusMode(CameraFocus focus) {
//        this.focus = focus;
//    }
//
//    @Override
//    public synchronized void setExposureCompensation(int exposureCompensation) {
//        this.exposureCompensation = exposureCompensation;
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    public synchronized void setPhotoSize(CameraSize size) {
//    }
//
//    @Override
//    public synchronized void capturePhoto(CapturePhotoCallback callback) {
//        this.photoCallback = callback;
//
//    }
//
//    @NonNull
//    @Override
//    public Lifecycle getLifecycle() {
//        return lifecycle;
//    }
//
//    class CameraXAttributes extends IAttributes {
//
//        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//        public CameraXAttributes(CameraInfo cameraInfo, CameraFacing cameraFacing) {
//            this.facing = cameraFacing;
//            this.orientation = -1;
//            this.photoSize = new ArrayList<>();
//            this.previewSize = new ArrayList<>();
//            this.flashes = new ArrayList<>();
//            boolean flashSupported =
//                    cameraInfo.hasFlashUnit();
//            if (flashSupported) {
//                flashes.add(OFF);
//                flashes.add(ON);
//                flashes.add(AUTO);
//                flashes.add(TORCH);
//            }
//            this.focusList = new ArrayList<>();
//            focusList.add(CameraFocus.AUTO);
//        }
//
//    }
//
//}
