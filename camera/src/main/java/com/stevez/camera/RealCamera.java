package com.stevez.camera;

import android.graphics.SurfaceTexture;

/**
 * @author: SteveZ
 * @created on: 2020/5/25 5:55 PM.
 * @description: 相机实现类
 */
final class RealCamera extends CameraApi {

    private CameraApi delegate;

    /**
     * 代理CameraApi
     *
     * @param cameraApi
     */
    public RealCamera(CameraApi cameraApi) {
        this.delegate = cameraApi;
    }

    @Override
    public void openCamera(CameraFacing facingType) {
        delegate.cameraHandler.post(new Runnable() {
            @Override
            public void run() {
                delegate.openCamera(facingType);
            }
        });
    }

    @Override
    public void release() {
        delegate.cameraHandler.post(new Runnable() {
            @Override
            public void run() {
                delegate.release();
            }
        });
    }

    @Override
    public void addPreviewCallbackWithBuffer(CameraPreviewCallback callback) {
        delegate.cameraHandler.post(new Runnable() {
            @Override
            public void run() {
                delegate.addPreviewCallbackWithBuffer(callback);
            }
        });
    }

    @Override
    public void removePreviewCallbackWithBuffer(CameraPreviewCallback callback) {
        delegate.cameraHandler.post(new Runnable() {
            @Override
            public void run() {
                delegate.removePreviewCallbackWithBuffer(callback);
            }
        });
    }

    @Override
    public void clearPreviewCallbackWithBuffer() {
        delegate.cameraHandler.post(new Runnable() {
            @Override
            public void run() {
                delegate.clearPreviewCallbackWithBuffer();
            }
        });
    }

    @Override
    public void setPreviewOrientation(int orientation) {
        delegate.cameraHandler.post(new Runnable() {
            @Override
            public void run() {
                delegate.setPreviewOrientation(orientation);
            }
        });
    }

    @Override
    public void setPreviewSize(CameraSize size) {
        delegate.cameraHandler.post(new Runnable() {
            @Override
            public void run() {
                delegate.setPreviewSize(size);
            }
        });
    }

    @Override
    public void startPreview(SurfaceTexture surfacetexture) {
        delegate.cameraHandler.post(new Runnable() {
            @Override
            public void run() {
                delegate.startPreview(surfacetexture);
            }
        });
    }

    @Override
    public void stopPreview() {
        delegate.cameraHandler.post(new Runnable() {
            @Override
            public void run() {
                delegate.stopPreview();
            }
        });
    }

    @Override
    public void setFlash(CameraFlash flash) {
        delegate.cameraHandler.post(new Runnable() {
            @Override
            public void run() {
                delegate.setFlash(flash);
            }
        });
    }

    @Override
    public void setFocusMode(CameraFocus focus) {
        delegate.cameraHandler.post(new Runnable() {
            @Override
            public void run() {
                delegate.setFocusMode(focus);
            }
        });
    }

    @Override
    public void setExposureCompensation(int exposureCompensation) {
        delegate.cameraHandler.post(new Runnable() {
            @Override
            public void run() {
                delegate.setExposureCompensation(exposureCompensation);
            }
        });
    }

    @Override
    public void setPhotoSize(CameraSize size) {
        delegate.cameraHandler.post(new Runnable() {
            @Override
            public void run() {
                delegate.setPhotoSize(size);
            }
        });
    }

    @Override
    public void capturePhoto(CapturePhotoCallback callback) {
        delegate.cameraHandler.post(new Runnable() {
            @Override
            public void run() {
                delegate.capturePhoto(callback);
            }
        });
    }


}
