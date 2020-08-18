package com.stevez.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import static android.content.Context.CAMERA_SERVICE;

/**
 * @author: SteveZ
 * @created Create in 2020/7/27 11:10 AM.
 * @description: Camera2适配
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Camera2 extends CameraApi {

    private static final String TAG = "Camera2";

    private static final int DEFAULT_PREVIEW_WIDTH = 640;
    private static final int DEFAULT_PREVIEW_HEIGHT = 480;

    //status
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAITING_LOCK = 1;
    private static final int STATE_WAITING_PRECAPTURE = 2;
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;
    private static final int STATE_PICTURE_TAKEN = 4;

    private CameraDevice mCameraDevice = null;
    private Camera2Attributes mCameraAttributes = null;
    private HashSet<CameraPreviewCallback> mCustomPreviewCallbacks;
    private CameraManager cameraManager;

    private Context context;

    private ImageReader imageReader = null;
    private CameraFlash flash = CameraFlash.OFF;
    private CameraFocus focus = CameraFocus.AUTO;
    private int exposureCompensation = 0;
    private CapturePhotoCallback photoCallback = null;
    private CameraFacing cameraFacing = new CameraFacing.Builder().build();

    private CameraCaptureSession mCaptureSession = null;
    private CaptureRequest.Builder previewRequestBuilder = null;
    private boolean previewStarted = false;
    private int captureState = STATE_PREVIEW;
    private int waitingFrames = 0;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Camera2(@NonNull CallBackEvents callBackEvents, Context context) {
        cameraHandler = CameraHandler.get();
        this.callBackEvents = callBackEvents;
        this.context = context;
        cameraManager = (CameraManager) context.getSystemService(CAMERA_SERVICE);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public synchronized void openCamera(CameraFacing cameraFacing) {
        if (cameraFacing.facingType == FacingType.BACK) {
            cameraFacing.cameraId = CameraCharacteristics.LENS_FACING_BACK;
        } else if (cameraFacing.facingType == FacingType.FRONT) {
            cameraFacing.cameraId = CameraCharacteristics.LENS_FACING_FRONT;
        }

        try {
            String[] cameraIds = cameraManager.getCameraIdList();
            for (int i = 0; i < cameraIds.length; i++) {
                String targetCameraId = cameraIds[i];
                CameraCharacteristics cameraCharacteristics =
                        cameraManager.getCameraCharacteristics(targetCameraId);
                if (Integer.valueOf(targetCameraId) == cameraFacing.cameraId) {
                    cameraManager.registerAvailabilityCallback(new CameraManager.AvailabilityCallback() {
                        @Override
                        public void onCameraAvailable(String cameraId) {
                            if (cameraId == targetCameraId) {
                                cameraManager.unregisterAvailabilityCallback(this);
                                try {
                                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                        // TODO: Consider calling
                                        //    ActivityCompat#requestPermissions
                                        // here to request the missing permissions, and then overriding
                                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                        //                                          int[] grantResults)
                                        // to handle the case where the user grants the permission. See the documentation
                                        // for ActivityCompat#requestPermissions for more details.
                                        return;
                                    }
                                    cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                                        @Override
                                        public void onOpened(CameraDevice cameraDevice) {
                                            Camera2Attributes cameraAttributes
                                                    = new Camera2Attributes(cameraCharacteristics, cameraFacing);
                                            mCameraDevice = cameraDevice;
                                            mCameraAttributes = cameraAttributes;
                                            if (callBackEvents != null) {
                                                callBackEvents.onCameraOpen(cameraAttributes);
                                            }
                                        }

                                        @Override
                                        public void onDisconnected(CameraDevice cameraDevice) {
                                            cameraDevice.close();
                                            mCameraDevice = null;
                                            mCameraAttributes = null;
                                            if (callBackEvents != null) {
                                                callBackEvents.onCameraClose();
                                            }
                                        }

                                        @Override
                                        public void onError(CameraDevice cameraDevice, int error) {
                                            cameraDevice.close();
                                            mCameraDevice = null;
                                            mCameraAttributes = null;
                                            if (callBackEvents != null) {
                                                callBackEvents.onCameraError("open camera error!");
                                            }
                                        }
                                    }, cameraHandler);
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onCameraUnavailable(String cameraId) {
                            if (cameraId == targetCameraId) {
                            }
                        }
                    }, cameraHandler);
                }
            }

        } catch (Exception e) {
            if (callBackEvents != null) {
                callBackEvents.onCameraError("open camera error!");
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public synchronized void release() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
        mCameraAttributes = null;
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
        previewStarted = false;
        if (callBackEvents != null) {
            callBackEvents.onCameraClose();
        }
    }

    @Override
    public synchronized void setPreviewOrientation(int degrees) {
    }

    @Override
    public synchronized void setPreviewSize(CameraSize size) {
        this.imageReader
                = ImageReader.newInstance(size.getWidth(), size.getHeight(),
                ImageFormat.JPEG, 2);
        this.imageReader.setOnImageAvailableListener(mOnImageAvailableListener, cameraHandler);
    }

    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image mImage = reader.acquireNextImage();
            if (mCustomPreviewCallbacks != null) {
                Iterator iterator = mCustomPreviewCallbacks.iterator();

                while (iterator.hasNext()) {
                    CameraPreviewCallback previewCallback = (CameraPreviewCallback) iterator.next();
                    //Log.e("FaceHelper", "ysj");
                    Rect crop = mImage.getCropRect();
                    int format = mImage.getFormat();
                    int width = crop.width();
                    int height = crop.height();
                    Image.Plane[] planes = mImage.getPlanes();

                    int buflen = width * height * ImageFormat.getBitsPerPixel(format) / 8;

                    //if (outData == null || outData.length != buflen) {
                    byte[] outData = new byte[buflen];
                    //}

                    ByteBuffer yBuf = planes[0].getBuffer();
                    ByteBuffer uvBuf = planes[2].getBuffer();

                    if (mImage.getPlanes()[0].getRowStride() == width) {
                        /**
                         * for case of preview size 1280x960, the row stride is 1280
                         *
                         * the size of yBuf is 1228800 (1280 x 960)
                         * the size of uvBuf is 614399 (1280 x 480 - 1)
                         */
                        yBuf.get(outData, 0, yBuf.remaining());
                        uvBuf.get(outData, yBuf.position(), uvBuf.remaining());
                    } else {
                        /**
                         * for case of preview size 1440x1080, the row stride is 1472
                         *
                         * the size of yBuf is 1589728 (1472 x 1079 + 1440)
                         * the size of uvBuf is 794847 (1472 x 539 + 1440 - 1)
                         */
                        int idx = 0;
                        int offset = 0; // the offset of output byte array
                        int rowStride = mImage.getPlanes()[0].getRowStride();

                        // get data for Y plane (plane[0])
                        for (idx = 0, offset = 0; idx < height; idx++, offset += width) {
                            yBuf.get(outData, offset, width);

                            if (idx != (height - 1)) {
                                yBuf.position(yBuf.position() + (rowStride - width));
                            }
                        }

                        // get data for V (Cr) plane (plane[2])
                        for (idx = 0; idx < (height / 2); idx++, offset += width) {
                            if (idx != (height / 2 - 1)) {
                                uvBuf.get(outData, offset, width);
                                uvBuf.position(uvBuf.position() + (rowStride - width));
                            } else {
                                uvBuf.get(outData, offset, width - 1);
                            }
                        }
                    }
                    if (previewCallback != null) {
                        previewCallback.onCallBackPreview(outData);
                    }
                }
            }
            mImage.close();
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public synchronized void startPreview(SurfaceTexture surfaceTexture) {
        if (mCameraDevice != null && imageReader != null) {
            Surface surface = new Surface(surfaceTexture);
            try {
                mCameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()), new CameraCaptureSession.StateCallback() {

                    @Override
                    public void onConfigured(CameraCaptureSession captureSession) {
                        if (captureSession != null) {
                            mCaptureSession = captureSession;
                        }
                        getCaptureSession(captureSession, surface);
                    }

                    @Override
                    public void onClosed(CameraCaptureSession session) {
                        getCaptureSession(null, null);
                        super.onClosed(session);
                    }

                    @Override
                    public void onConfigureFailed(CameraCaptureSession captureSession) {
                        getCaptureSession(null, null);
                    }
                }, cameraHandler);
            } catch (CameraAccessException e) {
                if (callBackEvents != null) {
                    callBackEvents.onPreviewError("CameraAccessException e->" + e.getMessage());
                }
            }
        } else {
            if (callBackEvents != null) {
                callBackEvents.onPreviewError("Is set PhotoSize?");
            }
        }
    }

    private void getCaptureSession(CameraCaptureSession captureSession, Surface surface) {
        try {
            if (captureSession != null && surface != null) {
                CaptureRequest.Builder previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                previewRequestBuilder.addTarget(surface);
                //flash
                previewRequestBuilder.set(CaptureRequest.FLASH_MODE,
                        (this.flash == CameraFlash.ON) ?
                                CaptureRequest.FLASH_MODE_TORCH
                                :
                                CaptureRequest.FLASH_MODE_OFF
                );
                //focus
                int foucstype = CaptureRequest.CONTROL_AF_MODE_AUTO;
                switch (focus) {
                    case AUTO:
                        foucstype = CaptureRequest.CONTROL_AF_MODE_AUTO;
                        break;
                    case MACRO:
                        foucstype = CaptureRequest.CONTROL_AF_MODE_MACRO;
                        break;
                    case EDOF:
                        foucstype = CaptureRequest.CONTROL_AF_MODE_EDOF;
                        break;
                    case CONTINUOUS_VIDEO:
                        foucstype = CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO;
                        break;
                    case CONTINUOUS_PICTURE:
                        foucstype = CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE;
                        break;
                    case OFF:
                        foucstype = CaptureRequest.CONTROL_AF_MODE_OFF;
                        break;
                }
                previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, foucstype);
                previewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);

                captureSession.setRepeatingRequest(previewRequestBuilder.build(), captureCallback, cameraHandler);
                this.previewRequestBuilder = previewRequestBuilder;
            } else {
                if (callBackEvents != null) {
                    callBackEvents.onPreviewError("captureSession is null!");
                }
            }
        } catch (CameraAccessException e) {
            if (callBackEvents != null) {
                callBackEvents.onPreviewError("CameraAccessException e->" + e.getMessage());
            }
        }
    }

    @Override
    public synchronized void stopPreview() {
        if (mCaptureSession != null) {
            try {
                mCaptureSession.stopRepeating();
                mCaptureSession.abortCaptures();
                mCaptureSession.close();
            } catch (Exception e) {
            } finally {
                if (callBackEvents != null) {
                    callBackEvents.onPreviewStopped();
                }
            }
        }
        mCaptureSession = null;
        previewStarted = false;

    }

    @Override
    public synchronized void setFlash(CameraFlash flash) {
        this.flash = flash;
    }

    @Override
    public synchronized void setFocusMode(CameraFocus focus) {
        this.focus = focus;
    }

    @Override
    public synchronized void setExposureCompensation(int exposureCompensation) {
        this.exposureCompensation = exposureCompensation;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public synchronized void setPhotoSize(CameraSize size) {
    }

    @Override
    public synchronized void capturePhoto(CapturePhotoCallback callback) {
        this.photoCallback = callback;

        if (cameraFacing.facingType == FacingType.BACK) {
            lockFocus();
        } else {
            captureStillPicture();
        }
    }

    private void lockFocus() {
        if (previewRequestBuilder != null && mCaptureSession != null) {
            try {
                previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
                captureState = STATE_WAITING_LOCK;
                waitingFrames = 0;
                mCaptureSession.capture(previewRequestBuilder.build(), captureCallback, cameraHandler);
                previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, null);
            } catch (Exception e) {
            }
        }
    }

    private void runPreCaptureSequence() {
        if (previewRequestBuilder != null && mCaptureSession != null) {
            try {
                previewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
                captureState = STATE_WAITING_PRECAPTURE;

                mCaptureSession.capture(previewRequestBuilder.build(), captureCallback, cameraHandler);

                previewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, null);
                previewRequestBuilder.set(CaptureRequest.FLASH_MODE,
                        (this.flash == CameraFlash.ON) ?
                                CaptureRequest.FLASH_MODE_TORCH
                                :
                                CaptureRequest.FLASH_MODE_OFF
                );
                mCaptureSession.setRepeatingRequest(previewRequestBuilder.build(), captureCallback, cameraHandler);
            } catch (CameraAccessException e) {
            }
        }
    }

    private void captureStillPicture() {
        if (mCaptureSession != null && mCameraDevice != null && imageReader != null) {
            try {
                CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                captureBuilder.addTarget(imageReader.getSurface());
                captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                captureBuilder.set(CaptureRequest.FLASH_MODE,
                        (this.flash == CameraFlash.ON) ?
                                CaptureRequest.FLASH_MODE_TORCH
                                :
                                CaptureRequest.FLASH_MODE_OFF
                );
                long delay = (this.flash == CameraFlash.ON) ?
                        75L : 0L;

                cameraHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mCaptureSession.capture(captureBuilder.build(),
                                    new CameraCaptureSession.CaptureCallback() {
                                        @Override
                                        public void onCaptureCompleted(
                                                CameraCaptureSession session,
                                                CaptureRequest request, TotalCaptureResult result) {
                                            unlockFocus();
                                        }
                                    }, cameraHandler);
                        } catch (CameraAccessException e) {
                        }
                    }
                }, delay);
            } catch (CameraAccessException e) {
            }
        }
    }

    private void unlockFocus() {
        if (previewRequestBuilder != null && mCaptureSession != null) {
            try {
                previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
                mCaptureSession.capture(previewRequestBuilder.build(), captureCallback, cameraHandler);
                captureState = STATE_PREVIEW;
                previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, null);
                previewRequestBuilder.set(CaptureRequest.FLASH_MODE,
                        (this.flash == CameraFlash.ON) ?
                                CaptureRequest.FLASH_MODE_TORCH
                                :
                                CaptureRequest.FLASH_MODE_OFF
                );
                mCaptureSession.setRepeatingRequest(previewRequestBuilder.build(), captureCallback, cameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {
            switch (captureState) {
                case STATE_PREVIEW: {
                    if (imageReader != null) {
                        Image image = imageReader.acquireLatestImage();
                        if (image != null) {
                            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                            byte[] bytes = new byte[buffer.remaining()];
                            buffer.get(bytes);
                            if (photoCallback != null) {
                                photoCallback.onCallBackPhoto(bytes);
                                photoCallback = null;
                            }
                            image.close();
                        }
                    }
                }
                case STATE_WAITING_LOCK: {
                    int afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        runPreCaptureSequence();
                    } else if (CaptureResult.CONTROL_AF_STATE_INACTIVE == afState) {
                        captureStillPicture();
                    } else if (waitingFrames >= 5) {
                        waitingFrames = 0;
                        captureStillPicture();
                    } else {
                        waitingFrames++;
                    }
                }
                case STATE_WAITING_PRECAPTURE: {
                    int aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        captureState = STATE_WAITING_NON_PRECAPTURE;
                    }
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    int aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        captureState = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    }
                }
            }
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            if (!previewStarted) {
                if (callBackEvents != null) {
                    callBackEvents.onPreviewStarted();
                }
                previewStarted = true;
            }
            process(result);
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
            process(partialResult);
        }

    };

    class Camera2Attributes extends IAttributes {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public Camera2Attributes(CameraCharacteristics cameraCharacteristics,
                                 CameraFacing cameraFacing) {
            this.facing = cameraFacing;
            this.orientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            this.photoSize = new ArrayList<>();
            StreamConfigurationMap map =
                    cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] photoSizes = map.getOutputSizes(ImageFormat.JPEG);
            for (Size size : photoSizes) {
                photoSize.add(new CameraSize(size.getWidth(), size.getHeight()));
            }
            this.previewSize = new ArrayList<>();
            Size[] previewSizes = map.getOutputSizes(SurfaceHolder.class);
            for (Size size : previewSizes) {
                previewSize.add(new CameraSize(size.getWidth(), size.getHeight()));
            }
            this.flashes = new ArrayList<>();
            boolean flashSupported =
                    cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            if (flashSupported) {
                flashes.add(CameraFlash.OFF);
                flashes.add(CameraFlash.ON);
                flashes.add(CameraFlash.AUTO);
                flashes.add(CameraFlash.TORCH);
            }
            this.focusList = new ArrayList<>();
            int[] afAvailableModes =
                    cameraCharacteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
            for (int mode :
                    afAvailableModes) {
                switch (mode) {
                    case CameraMetadata.CONTROL_AF_MODE_AUTO: {
                        focusList.add(CameraFocus.AUTO);
                        break;
                    }
                    case CameraMetadata.CONTROL_AF_MODE_MACRO: {
                        focusList.add(CameraFocus.MACRO);
                        break;
                    }
                    case CameraMetadata.CONTROL_AF_MODE_EDOF: {
                        focusList.add(CameraFocus.EDOF);
                        break;
                    }
                    case CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_VIDEO: {
                        focusList.add(CameraFocus.CONTINUOUS_VIDEO);
                        break;
                    }
                    case CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE: {
                        focusList.add(CameraFocus.CONTINUOUS_PICTURE);
                        break;
                    }
                }
            }
        }

    }

}
