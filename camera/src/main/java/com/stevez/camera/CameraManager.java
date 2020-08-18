package com.stevez.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: SteveZ
 * @created on: 2020/5/25 5:55 PM.
 * @description: <p>
 *      摄像头管理类，通过摄像头管理类来调用摄像头，并管理摄像头。
 * </p>
 */
public final class CameraManager extends CameraApi {

    private static Map<Integer, CameraManager> sCameraManagerMap;

    private RealCamera mCameraInstance;
    private CameraFacing mFacing;
    private CallBackEvents mCallBackEvents;

    static {
        sCameraManagerMap = new ConcurrentHashMap();
    }

    public static CameraManager getInstance(CameraFacing facing, CameraApiType cameraApiType, Context context) {
        return getInstance(facing, cameraApiType, context, null,null);
    }

    public static CameraManager getInstance(CameraFacing facing, CameraApiType cameraApiType, Context context, Handler handler) {
        return getInstance(facing, cameraApiType, context, handler,null);
    }

    public static CameraManager getInstance(CameraFacing facing, CameraApiType cameraApiType, Context context, Handler handler, Lifecycle lifecycle) {
        synchronized (CameraManager.class) {
            CameraManager cameraManager = (CameraManager) sCameraManagerMap.get(facing.cameraId);
            if (cameraManager == null) {
                cameraManager = new CameraManager(facing, cameraApiType, context,handler, lifecycle);
                sCameraManagerMap.put(facing.cameraId, cameraManager);
            }

            return cameraManager;
        }
    }

    public CameraManager(@NonNull CameraFacing facing, @NonNull CameraApiType cameraApiType, Context context, Handler handler, Lifecycle lifecycle) {
        this.mFacing = facing;
        if (cameraApiType == CameraApiType.CAMERAUVC){
            mCameraInstance = new RealCamera(
                    new CameraUvc(self_callBackEvents,context,handler)
            );
            return;
        } else
        if (cameraApiType == CameraApiType.AUTO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (context != null && lifecycle != null) {
                    mCameraInstance = new RealCamera(
//                            new Camera3(self_callBackEvents,context,lifecycle)
                            new Camera2(self_callBackEvents, context)
                    );
                    return;
                }
                if (context != null) {
                    mCameraInstance = new RealCamera(
                            new Camera2(self_callBackEvents, context)
                    );
                    return;
                }
            }
        } else if (cameraApiType == CameraApiType.CAMERA2) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (context != null) {
                    mCameraInstance = new RealCamera(
                            new Camera2(self_callBackEvents, context)
                    );
                    return;
                }
            }
        } else if (cameraApiType == CameraApiType.CAMERAX) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (context != null && lifecycle != null) {
                    mCameraInstance = new RealCamera(
                            new Camera2(self_callBackEvents, context)
//                            new Camera3(self_callBackEvents, context,lifecycle)
                    );
                    return;
                }
            }
        }
        mCameraInstance = new RealCamera(
                new Camera1(self_callBackEvents)
        );

    }

    private CallBackEvents self_callBackEvents = new CallBackEvents() {
        @Override
        public void onCameraOpen(IAttributes cameraAttributes) {
            if (mCallBackEvents != null) {
                mCallBackEvents.onCameraOpen(cameraAttributes);
            }
        }

        @Override
        public void onCameraClose() {
            if (mCallBackEvents != null) {
                mCallBackEvents.onCameraClose();
            }
        }

        @Override
        public void onCameraError(String errorMsg) {
            if (mCallBackEvents != null) {
                mCallBackEvents.onCameraError(errorMsg);
            }
        }

        @Override
        public void onPreviewStarted() {
            if (mCallBackEvents != null) {
                mCallBackEvents.onPreviewStarted();
            }
        }

        @Override
        public void onPreviewStopped() {
            if (mCallBackEvents != null) {
                mCallBackEvents.onPreviewStopped();
            }
        }

        @Override
        public void onPreviewError(String errorMsg) {
            if (mCallBackEvents != null) {
                mCallBackEvents.onPreviewError(errorMsg);
            }
        }
    };

    public void setCallBackEvents(CallBackEvents mCallBackEvents) {
        this.mCallBackEvents = mCallBackEvents;
    }

    public void openCamera() {
        this.mCameraInstance.openCamera(mFacing);
    }

    @Override
    public void openCamera(CameraFacing facingType) {
        this.mFacing = facingType;
        this.mCameraInstance.openCamera(facingType);
    }

    @Override
    public void release() {
        this.mCameraInstance.release();
        sCameraManagerMap.remove(mFacing.cameraId);
    }

    @Override
    public void addPreviewCallbackWithBuffer(CameraPreviewCallback callback) {
        this.mCameraInstance.addPreviewCallbackWithBuffer(callback);
    }

    @Override
    public void removePreviewCallbackWithBuffer(CameraPreviewCallback callback) {
        this.mCameraInstance.removePreviewCallbackWithBuffer(callback);
    }

    @Override
    public void clearPreviewCallbackWithBuffer() {
        this.mCameraInstance.clearPreviewCallbackWithBuffer();
    }

    @Override
    public void setPreviewOrientation(int orientation) {
        this.mCameraInstance.setPreviewOrientation(orientation);
    }

    @Override
    public void setPreviewSize(CameraSize size) {
        this.mCameraInstance.setPreviewSize(size);
    }

    @Override
    public void startPreview(SurfaceTexture surfacetexture) {
        this.mCameraInstance.startPreview(surfacetexture);
    }

    @Override
    public void stopPreview() {
        this.mCameraInstance.stopPreview();
    }

    @Override
    public void setFlash(CameraFlash flash) {
        this.mCameraInstance.setFlash(flash);
    }

    @Override
    public void setFocusMode(CameraFocus focus) {
        this.mCameraInstance.setFocusMode(focus);
    }

    @Override
    public void setExposureCompensation(int exposureCompensation) {
        this.mCameraInstance.setExposureCompensation(exposureCompensation);
    }

    @Override
    public void setPhotoSize(CameraSize size) {
        this.mCameraInstance.setPhotoSize(size);
    }

    @Override
    public void capturePhoto(CapturePhotoCallback callback) {
        this.mCameraInstance.capturePhoto(callback);
    }

}
