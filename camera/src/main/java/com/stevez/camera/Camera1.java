package com.stevez.camera;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @author: SteveZ
 * @created Create in 2020/7/27 11:10 AM.
 * @description: Camera1适配
 */
public class Camera1 extends CameraApi {

    private static final String TAG = "Camera1";

    private static final int DEFAULT_PREVIEW_WIDTH = 640;
    private static final int DEFAULT_PREVIEW_HEIGHT = 480;

    private Camera camera = null;
    private Camera1Attributes cameraAttributes = null;
    private Camera.PreviewCallback mPreviewCallback = null;
    private HashSet<CameraPreviewCallback> mCustomPreviewCallbacks;

    private byte[] mBuffer;
    private byte[] mCallbackBuffer;

    public Camera1(@NonNull CallBackEvents callBackEvents) {
        cameraHandler = CameraHandler.get();
        this.callBackEvents = callBackEvents;
    }

    @Override
    public synchronized void openCamera(CameraFacing cameraFacing) {
        if (cameraFacing.facingType == FacingType.BACK) {
            cameraFacing.cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        } else if (cameraFacing.facingType == FacingType.FRONT) {
            cameraFacing.cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }

        try {
            int numberOfCameras = Camera.getNumberOfCameras();
            for (int i = 0; i < numberOfCameras; i++) {
                if (i == cameraFacing.cameraId) {
                    Camera camera = Camera.open(i);
                    Camera.Parameters cameraParameters = camera.getParameters();
                    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                    Camera.getCameraInfo(i, cameraInfo);
                    Camera1Attributes cameraAttributes =
                            new Camera1Attributes(cameraInfo, cameraParameters, cameraFacing);

                    this.camera = camera;
                    this.cameraAttributes = cameraAttributes;
                    if (callBackEvents != null) {
                        callBackEvents.onCameraOpen(cameraAttributes);
                    }
                }
            }
            this.mPreviewCallback = new Camera.PreviewCallback() {

                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    if (mCustomPreviewCallbacks != null) {
                        Iterator iterator = mCustomPreviewCallbacks.iterator();

                        while (iterator.hasNext()) {
                            CameraPreviewCallback previewCallback = (CameraPreviewCallback) iterator.next();
                            System.arraycopy(data, 0, mCallbackBuffer, 0, data.length);
                            if (previewCallback != null) {
                                previewCallback.onCallBackPreview(mCallbackBuffer);
                            }
                        }
                    }
                    addCallbackBuffer(camera, mBuffer);
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
            if (callBackEvents != null) {
                callBackEvents.onCameraError("open camera error!" + e.getMessage());
            }
        }
    }

    @Override
    public synchronized void addPreviewCallbackWithBuffer(CameraPreviewCallback callback) {
        if (this.mCustomPreviewCallbacks == null) {
            this.mCustomPreviewCallbacks = new HashSet();
        }
        this.mCustomPreviewCallbacks.add(callback);
    }

    @Override
    public synchronized void removePreviewCallbackWithBuffer(CameraPreviewCallback callback) {
        if (this.mCustomPreviewCallbacks == null) {
        } else {
            this.mCustomPreviewCallbacks.remove(callback);
        }
    }

    @Override
    public void clearPreviewCallbackWithBuffer() {
        if (this.mCustomPreviewCallbacks != null) {
            this.mCustomPreviewCallbacks.clear();
        }
    }

    @Override
    public synchronized void release() {
        if (camera != null) {
            camera.setPreviewCallback((Camera.PreviewCallback) null);
            camera.setPreviewCallbackWithBuffer((Camera.PreviewCallback) null);
            try {
                camera.setPreviewTexture((SurfaceTexture) null);
            } catch (IOException var3) {
                var3.printStackTrace();
                Log.e("Camera1", var3.getMessage());
            }
            camera.release();
            camera = null;
        }
        cameraAttributes = null;
        if (callBackEvents != null) {
            callBackEvents.onCameraClose();
        }
    }

    @Override
    public synchronized void setPreviewOrientation(int degrees) {
        if (camera != null) {
            camera.setDisplayOrientation(degrees);
        }
    }

    @Override
    public synchronized void setPreviewSize(CameraSize size) {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(size.getWidth(), size.getHeight());
            camera.setParameters(parameters);
        }
    }

    @Override
    public synchronized void startPreview(SurfaceTexture surfacetexture) {
        if (camera != null) {
            this.doInitPatameters(surfacetexture);
            camera.startPreview();
        }
    }

    /**
     * 初始化参数
     */
    private void doInitPatameters(SurfaceTexture surfaceTexture) {
        String errorMsg = null;
        if (this.camera != null && this.cameraAttributes != null) {

            try {
                Camera.Parameters parameters = this.camera.getParameters();
                if (parameters != null) {
                    //set buffer size
                    Camera.Size camerasize = parameters.getPreviewSize();
                    if (camerasize == null) {
                        parameters.setPreviewSize(DEFAULT_PREVIEW_WIDTH, DEFAULT_PREVIEW_HEIGHT);
                        camerasize = parameters.getPreviewSize();
                    }
                    int size = camerasize.width * camerasize.height * 3 / 2;
                    if (this.mBuffer == null || this.mBuffer.length != size) {
                        this.mBuffer = new byte[size];
                        this.mCallbackBuffer = new byte[size];
                    }

                    parameters.setPreviewSize(camerasize.width, camerasize.height);
                    parameters.setPreviewFormat(ImageFormat.NV21);
                    parameters.setPictureFormat(ImageFormat.JPEG);
                    parameters.setAutoExposureLock(true);

                    camera.setPreviewTexture(surfaceTexture);

                    this.camera.setParameters(parameters);
                    this.addCallbackBuffer(this.camera, this.mBuffer);
                    if (mPreviewCallback != null) {
                        this.camera.setPreviewCallbackWithBuffer(this.mPreviewCallback);
                    }

                    if (callBackEvents != null) {
                        callBackEvents.onPreviewStarted();
                    }

                } else {
                    errorMsg = "getParameters is null";
                }
            } catch (RuntimeException var10) {
                var10.printStackTrace();
                errorMsg = "camera get parameters error, e->" + var10.getClass().getSimpleName() + ", msg->" + var10.getMessage();
                Log.e("CameraManager", errorMsg);
            } catch (IOException var11) {
                var11.printStackTrace();
                errorMsg = "camera set preview texture error, e->" + var11.getClass().getSimpleName() + ", msg->" + var11.getMessage();
                Log.e("CameraManager", errorMsg);
            } finally {
                if (errorMsg != null && this.callBackEvents != null) {
                    callBackEvents.onPreviewError(errorMsg);
                }

            }
        }
    }

    private void addCallbackBuffer(Camera camera, byte[] buffer) {
        if (camera != null) {
            camera.addCallbackBuffer(buffer);
        }
    }

    @Override
    public synchronized void stopPreview() {
        if (camera != null) {
            camera.stopPreview();
            if (callBackEvents != null) {
                callBackEvents.onPreviewStopped();
            }
        }
    }

    @Override
    public synchronized void setFlash(CameraFlash flash) {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            switch (flash) {
                case OFF: {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    break;
                }
                case ON: {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    break;
                }
                case AUTO: {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                    break;
                }
                case TORCH: {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    break;
                }
                default:
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    break;
            }
            try {
                camera.setParameters(parameters);
            } catch (Exception e) {
                // ignore failures for minor parameters like this for now
            }
        }
    }

    @Override
    public synchronized void setFocusMode(CameraFocus focus) {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            switch (focus) {
                case AUTO: {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    break;
                }
                case INFINITY: {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
                    break;
                }
                case MACRO: {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
                    break;
                }
                case FIXED: {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
                    break;
                }
                case EDOF: {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_EDOF);
                    break;
                }
                case CONTINUOUS_VIDEO: {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    break;
                }
                default:
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    break;
            }
            try {
                camera.setParameters(parameters);
            } catch (Exception e) {
                // ignore failures for minor parameters like this for now
            }
        }
    }

    @Override
    public synchronized void setExposureCompensation(int exposureCompensation) {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setExposureCompensation(exposureCompensation);

            try {
                camera.setParameters(parameters);
            } catch (Exception e) {
                // ignore failures for minor parameters like this for now
            }
        }
    }

    @Override
    public synchronized void setPhotoSize(CameraSize size) {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPictureSize(size.getWidth(), size.getHeight());

            try {
                camera.setParameters(parameters);
            } catch (Exception e) {
                // ignore failures for minor parameters like this for now
            }
        }
    }

    @Override
    public synchronized void capturePhoto(CapturePhotoCallback callback) {
        if (camera != null) {
            camera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    callback.onCallBackPhoto(data);
                    camera.startPreview();
                }
            });
        }
    }

    class Camera1Attributes extends IAttributes {

        public Camera1Attributes(Camera.CameraInfo cameraInfo,
                                 @NonNull Camera.Parameters cameraParameters,
                                 CameraFacing cameraFacing) {
            this.facing = cameraFacing;
            this.orientation = cameraInfo.orientation;
            this.photoSize = new ArrayList<>();
            for (Camera.Size size :
                    cameraParameters.getSupportedPictureSizes()) {
                photoSize.add(new CameraSize(size.width, size.height));
            }
            this.previewSize = new ArrayList<>();
            for (Camera.Size size :
                    cameraParameters.getSupportedPreviewSizes()) {
                previewSize.add(new CameraSize(size.width, size.height));
            }
            this.flashes = new ArrayList<>();
            if (cameraParameters.getSupportedFlashModes() != null) {
                if (cameraParameters.getSupportedFlashModes() != null) {
                    for (String mode :
                            cameraParameters.getSupportedFlashModes()) {
                        switch (mode) {
                            case Camera.Parameters.FLASH_MODE_OFF: {
                                flashes.add(CameraFlash.OFF);
                                break;
                            }
                            case Camera.Parameters.FLASH_MODE_ON: {
                                flashes.add(CameraFlash.ON);
                                break;
                            }
                            case Camera.Parameters.FLASH_MODE_AUTO: {
                                flashes.add(CameraFlash.AUTO);
                                break;
                            }
                            case Camera.Parameters.FLASH_MODE_TORCH: {
                                flashes.add(CameraFlash.TORCH);
                                break;
                            }
                            default:
                                flashes.add(CameraFlash.OFF);
                        }
                    }
                }
            }
            this.focusList = new ArrayList<>();
            if (cameraParameters.getSupportedFocusModes() != null) {
                for (String mode :
                        cameraParameters.getSupportedFocusModes()) {
                    switch (mode) {
                        case Camera.Parameters.FOCUS_MODE_AUTO: {
                            focusList.add(CameraFocus.AUTO);
                            break;
                        }
                        case Camera.Parameters.FOCUS_MODE_INFINITY: {
                            focusList.add(CameraFocus.INFINITY);
                            break;
                        }
                        case Camera.Parameters.FOCUS_MODE_MACRO: {
                            focusList.add(CameraFocus.MACRO);
                            break;
                        }
                        case Camera.Parameters.FOCUS_MODE_EDOF: {
                            focusList.add(CameraFocus.EDOF);
                            break;
                        }
                        case Camera.Parameters.FOCUS_MODE_FIXED: {
                            focusList.add(CameraFocus.FIXED);
                            break;
                        }
                        case Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO: {
                            focusList.add(CameraFocus.CONTINUOUS_VIDEO);
                            break;
                        }
                        default:
                            flashes.add(CameraFlash.AUTO);
                    }
                }
            }
        }

    }

}
