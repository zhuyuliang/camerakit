package com.stevez.camera;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.lgh.uvccamera.UVCCameraProxy;
import com.lgh.uvccamera.bean.PicturePath;
import com.lgh.uvccamera.callback.ConnectCallback;
import com.lgh.uvccamera.callback.PictureCallback;
import com.lgh.uvccamera.callback.PreviewCallback;
import com.lgh.uvccamera.config.CameraConfig;
import com.lgh.uvccamera.utils.LogUtil;
import com.serenegiant.usb.Size;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * @author: SteveZ
 * @created Create in 2020/7/27 11:10 AM.
 * @description: CameraUvc适配
 */
public class CameraUvc extends CameraApi {

    private static final String TAG = "CameraUvc";

    private static final int DEFAULT_PREVIEW_WIDTH = 640;
    private static final int DEFAULT_PREVIEW_HEIGHT = 480;

    private UVCCameraProxy camera = null;
    private CameraUvcAttributes cameraAttributes = null;
    private HashSet<CameraPreviewCallback> mCustomPreviewCallbacks;
    private Context context;

    private byte[] mCallbackBuffer;

    public CameraUvc(@NonNull CallBackEvents callBackEvents, Context context, Handler handler) {
        cameraHandler = new CameraHandler(handler);
        this.callBackEvents = callBackEvents;
        this.context = context;
    }

    @Override
    public synchronized void openCamera(CameraFacing cameraFacing) {
        try {
            camera = new UVCCameraProxy(context);
            // 已有默认配置，不需要可以不设置 TODO Debug
            camera.getConfig()
                    .isDebug(true)
                    .setPicturePath(PicturePath.APPCACHE)
                    .setDirName("uvccamera")
                    .setProductId(cameraFacing.pid)
                    .setVendorId(cameraFacing.vid);

            cameraAttributes = new CameraUvcAttributes(camera.getConfig(),
                    camera.getSupportedPreviewSizes(), cameraFacing);

            camera.setConnectCallback(new ConnectCallback() {
                @Override
                public void onAttached(UsbDevice usbDevice) {
                    LogUtil.i("UsbDevice-> onAttached");
                    camera.requestPermission(usbDevice);
                }

                @Override
                public void onGranted(UsbDevice usbDevice, boolean granted) {
                    if (granted) {
                        LogUtil.i("UsbDevice-> onGranted");
                        camera.connectDevice(usbDevice);
                    }
                }

                @Override
                public void onConnected(UsbDevice usbDevice) {
                    camera.openCamera();
                }

                @Override
                public void onCameraOpened() {
                    LogUtil.i("UsbDevice-> onCameraOpened");
                    camera.startPreview();
                    if (callBackEvents != null) {
                        callBackEvents.onPreviewStarted();
                    }
                }

                @Override
                public void onDetached(UsbDevice usbDevice) {
                    camera.closeCamera();
                    if (callBackEvents != null) {
                        callBackEvents.onCameraClose();
                    }
                }
            });

            if (callBackEvents != null) {
                callBackEvents.onCameraOpen(cameraAttributes);
            }
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
            camera.clearCache();
            camera.setPreviewCallback(null);
            camera.setPreviewTexture(null);
            camera.setPreviewSurfaceTexture((SurfaceTexture) null);
            LogUtil.i("onSurfaceTextureDestroyed");
            camera.unregisterReceiver();
            camera.closeCamera();
            camera.closeDevice();
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
            camera.setPreviewRotation(degrees);
        }
    }

    @Override
    public synchronized void setPreviewSize(CameraSize size) {
        if (camera != null) {
            camera.setPreviewSize(size.getWidth(), size.getHeight());
            int lenght = size.getWidth() * size.getHeight() * 3 / 2;
            this.mCallbackBuffer = new byte[lenght];
        }
    }

    @Override
    public synchronized void startPreview(SurfaceTexture surfacetexture) {
        if (camera != null) {
            this.doInitPatameters(surfacetexture);
        }
    }

    /**
     * 初始化参数
     */
    private void doInitPatameters(SurfaceTexture surfaceTexture) {
        String errorMsg = null;
        if (this.camera != null && this.cameraAttributes != null) {
            try {
                camera.setPreviewCallback(new PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] yuv) {
                        if (mCustomPreviewCallbacks != null) {
                            Iterator iterator = mCustomPreviewCallbacks.iterator();

                            while (iterator.hasNext()) {
                                CameraPreviewCallback previewCallback = (CameraPreviewCallback) iterator.next();
                                System.arraycopy(yuv, 0, mCallbackBuffer, 0, yuv.length);
                                if (previewCallback != null) {
                                    previewCallback.onCallBackPreview(mCallbackBuffer);
                                }
                            }
                        }
                    }
                });
                camera.setPreviewSurfaceTexture(surfaceTexture);
                camera.startPreview();
            } catch (RuntimeException var10) {
                var10.printStackTrace();
                errorMsg = "camera get parameters error, e->" + var10.getClass().getSimpleName() + ", msg->" + var10.getMessage();
                Log.e("CameraManager", errorMsg);
            } finally {
                if (errorMsg != null && this.callBackEvents != null) {
                    callBackEvents.onPreviewError(errorMsg);
                }

            }
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
        }
    }

    @Override
    public synchronized void setFocusMode(CameraFocus focus) {
        if (camera != null) {
        }
    }

    @Override
    public synchronized void setExposureCompensation(int exposureCompensation) {
        if (camera != null) {
        }
    }

    @Override
    public synchronized void setPhotoSize(CameraSize size) {
        if (camera != null) {
        }
    }

    @Override
    public synchronized void capturePhoto(CapturePhotoCallback callback) {
        if (camera != null) {
            camera.setPictureTakenCallback(new PictureCallback() {
                @Override
                public void onPictureTaken(String path) {
                    try {
                        Bitmap bp = getBitmap(context.getContentResolver(),Uri.parse(path));
                        callback.onCallBackPhoto(bitmapToNv21(bp,bp.getWidth(),bp.getHeight()));
                    } catch (IOException e) {
                        //e.printStackTrace();
                        callback.onCallBackPhoto(null);
                    }
                }
            });
            camera.takePicture("takepicture.jpg");
        }
    }

    public static final Bitmap getBitmap(ContentResolver cr, Uri url)
            throws FileNotFoundException, IOException {
        InputStream input = cr.openInputStream(url);
        Bitmap bitmap = BitmapFactory.decodeStream(input);
        input.close();
        return bitmap;
    }

    /**
     * Bitmap转化为ARGB数据，再转化为NV21数据
     *
     * @param src    传入ARGB_8888的Bitmap
     * @param width  NV21图像的宽度
     * @param height NV21图像的高度
     * @return nv21数据
     */
    public static byte[] bitmapToNv21(Bitmap src, int width, int height) {
        if (src != null && src.getWidth() >= width && src.getHeight() >= height) {
            int[] argb = new int[width * height];
            src.getPixels(argb, 0, width, 0, 0, width, height);
            return argbToNv21(argb, width, height);
        } else {
            return null;
        }
    }

    /**
     * ARGB数据转化为NV21数据
     *
     * @param argb   argb数据
     * @param width  宽度
     * @param height 高度
     * @return nv21数据
     */
    private static byte[] argbToNv21(int[] argb, int width, int height) {
        int frameSize = width * height;
        int yIndex = 0;
        int uvIndex = frameSize;
        int index = 0;
        byte[] nv21 = new byte[width * height * 3 / 2];
        for (int j = 0; j < height; ++j) {
            for (int i = 0; i < width; ++i) {
                int R = (argb[index] & 0xFF0000) >> 16;
                int G = (argb[index] & 0x00FF00) >> 8;
                int B = argb[index] & 0x0000FF;
                int Y = (66 * R + 129 * G + 25 * B + 128 >> 8) + 16;
                int U = (-38 * R - 74 * G + 112 * B + 128 >> 8) + 128;
                int V = (112 * R - 94 * G - 18 * B + 128 >> 8) + 128;
                nv21[yIndex++] = (byte) (Y < 0 ? 0 : (Y > 255 ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0 && uvIndex < nv21.length - 2) {
                    nv21[uvIndex++] = (byte) (V < 0 ? 0 : (V > 255 ? 255 : V));
                    nv21[uvIndex++] = (byte) (U < 0 ? 0 : (U > 255 ? 255 : U));
                }

                ++index;
            }
        }
        return nv21;
    }

    class CameraUvcAttributes extends IAttributes {

        public CameraUvcAttributes(CameraConfig config, List<Size> previewList,
                                   CameraFacing cameraFacing) {
            this.facing = cameraFacing;
            this.orientation = -1;
            this.photoSize = new ArrayList<>();
            this.previewSize = new ArrayList<>();
            for (Size size :
                    previewList) {
                previewSize.add(new CameraSize(size.width, size.height));
            }
            this.flashes = new ArrayList<>();
            this.focusList = new ArrayList<>();
        }

    }

}
