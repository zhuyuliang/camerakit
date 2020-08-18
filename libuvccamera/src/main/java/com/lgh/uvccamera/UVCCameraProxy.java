package com.lgh.uvccamera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

import com.lgh.uvccamera.callback.ConnectCallback;
import com.lgh.uvccamera.callback.PhotographCallback;
import com.lgh.uvccamera.callback.PictureCallback;
import com.lgh.uvccamera.callback.PreviewCallback;
import com.lgh.uvccamera.config.CameraConfig;
import com.lgh.uvccamera.usb.UsbMonitor;
import com.lgh.uvccamera.utils.FileUtil;
import com.lgh.uvccamera.utils.LogUtil;
import com.serenegiant.usb.IButtonCallback;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.UVCCamera;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 描述：相机代理类
 * 作者：liugh
 * 日期：2018/11/16
 * 版本：v2.0.0
 */
public class UVCCameraProxy implements IUVCCamera {
    private static int PICTURE_WIDTH = 640;
    private static int PICTURE_HEIGHT = 480;
    private Context mContext;
    private UsbMonitor mUsbMonitor;
    protected UVCCamera mUVCCamera;
    private View mPreviewView; // 预览view
    private Surface mSurface;
    private PictureCallback mPictureCallback; // 拍照成功回调
    private PhotographCallback mPhotographCallback; // 设备上的拍照按钮点击回调
    private PreviewCallback mPreviewCallback; // 预览回调
    private ConnectCallback mConnectCallback; // usb连接回调
    protected CompositeDisposable mSubscriptions;
    private CameraConfig mConfig; // 相机相关配置
    protected float mPreviewRotation; // 相机预览旋转角度
    protected boolean isTakePhoto; // 是否拍照
    private String mPictureName; // 图片名称

    public UVCCameraProxy(Context context) {
        mContext = context;
        mConfig = new CameraConfig();
        mUsbMonitor = new UsbMonitor(context, mConfig);
        mSubscriptions = new CompositeDisposable();
    }

    /**
     * 注册usb插拔监听广播
     */
    @Override
    public void registerReceiver() {
        mUsbMonitor.registerReceiver();
    }

    /**
     * 注销usb插拔监听广播
     */
    @Override
    public void unregisterReceiver() {
        mUsbMonitor.unregisterReceiver();
    }

    /**
     * 检查是否插入了usb摄像头，用于先插入设备再打开页面的场景
     */
    @Override
    public void checkDevice() {
        mUsbMonitor.checkDevice();
    }

    /**
     * 申请打开usb设备权限
     *
     * @param usbDevice
     */
    @Override
    public void requestPermission(UsbDevice usbDevice) {
        mUsbMonitor.requestPermission(usbDevice);
    }

    /**
     * 连接usb设备
     *
     * @param usbDevice
     */
    @Override
    public void connectDevice(UsbDevice usbDevice) {
        mUsbMonitor.connectDevice(usbDevice);
    }

    /**
     * 关闭usb设备
     */
    @Override
    public void closeDevice() {
        mUsbMonitor.closeDevice();
    }

    /**
     * 打开相机
     */
    @Override
    public void openCamera() {
        try {
            mUVCCamera = new UVCCamera();
            LogUtil.i("UsbDevice-> openCamera");
            mUVCCamera.open(mUsbMonitor.getUsbController());
            LogUtil.i("UsbDevice-> openCamera");
        } catch (Exception e) {
            LogUtil.i("UsbDevice-> printStackTrace");
            e.printStackTrace();
        }
        LogUtil.i("UsbDevice-> openCamera");
        if (mUVCCamera != null && mConnectCallback != null) {
            mConnectCallback.onCameraOpened();
        }
    }

    /**
     * 关闭相机
     */
    @Override
    public void closeCamera() {
        try {
            if (mUVCCamera != null) {
                mUVCCamera.destroy();
                mUVCCamera = null;
            }
            mUsbMonitor.closeDevice();
            LogUtil.i("closeCamera");
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSubscriptions.clear();
    }

    /**
     * 设置相机预览控件，这里封装了相关注册注销广播、检测设备、释放资源等操作
     *
     * @param surfaceView
     */
    @Override
    public void setPreviewSurface(SurfaceView surfaceView) {
        this.mPreviewView = surfaceView;
        if (surfaceView != null && surfaceView.getHolder() != null) {
            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    LogUtil.i("surfaceCreated");
                    mSurface = holder.getSurface();
                    checkDevice();
                    registerReceiver();
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    LogUtil.i("surfaceChanged");
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    LogUtil.i("surfaceDestroyed");
                    mSurface = null;
                    unregisterReceiver();
                    closeCamera();
                }
            });
        }
    }

    /**
     * 设置相机预览控件，这里封装了相关注册注销广播、检测设备、释放资源等操作
     *
     * @param surfaceTexture
     */
    @Override
    public void setPreviewSurfaceTexture(SurfaceTexture surfaceTexture) {
        if (surfaceTexture != null) {
            mSurface = new Surface(surfaceTexture);
            checkDevice();
            registerReceiver();
        } else {
            if (mSurface != null) {
                mSurface.release();
                mSurface = null;
            }
        }
    }

    /**
     * 设置相机预览控件，这里封装了相关注册注销广播、检测设备、释放资源等操作
     *
     * @param textureView
     */
    @Override
    public void setPreviewTexture(TextureView textureView) {
        this.mPreviewView = textureView;
        if (textureView != null) {
            if (mPreviewRotation != 0) {
                textureView.setRotation(mPreviewRotation);
            }
            textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    LogUtil.i("onSurfaceTextureAvailable");
                    mSurface = new Surface(surface);
                    checkDevice();
                    registerReceiver();
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                    LogUtil.i("onSurfaceTextureSizeChanged");
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    LogUtil.i("onSurfaceTextureDestroyed");
                    mSurface = null;
                    unregisterReceiver();
                    closeCamera();
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                }
            });
        }
    }

    /**
     * 设置相机预览旋转角度，暂时只支持TextureView
     *
     * @param rotation
     */
    @Override
    public void setPreviewRotation(float rotation) {
        if (mPreviewView != null && mPreviewView instanceof TextureView) {
            this.mPreviewRotation = rotation;
            mPreviewView.setRotation(rotation);
        }
    }

    /**
     * 设置相机预览Surface
     *
     * @param surface
     */
    @Override
    public void setPreviewDisplay(Surface surface) {
        mSurface = surface;
        try {
            if (mUVCCamera != null && mSurface != null) {
                mUVCCamera.setPreviewDisplay(mSurface);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置预览尺寸
     *
     * @param width
     * @param height
     */
    @Override
    public void setPreviewSize(int width, int height) {
        try {
            if (mUVCCamera != null) {
                this.PICTURE_WIDTH = width;
                this.PICTURE_HEIGHT = height;
                mUVCCamera.setPreviewSize(width, height);
                LogUtil.i("setPreviewSize-->" + width + " * " + height);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取相机预览尺寸
     *
     * @return
     */
    @Override
    public Size getPreviewSize() {
        try {
            if (mUVCCamera != null) {
                return mUVCCamera.getPreviewSize();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取相机支持的预览尺寸
     *
     * @return
     */
    @Override
    public List<Size> getSupportedPreviewSizes() {
        try {
            if (mUVCCamera != null) {
                return mUVCCamera.getSupportedSizeList();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 开始预览
     */
    @Override
    public void startPreview() {
        try {
            if (mUVCCamera != null) {
                LogUtil.i("startPreview");

                // 拍照按钮点击监听，使用rxjava方式，防止ui线程堵塞
                mSubscriptions.add(Observable.create(new ObservableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                        mUVCCamera.setButtonCallback(new IButtonCallback() {
                            @Override
                            public void onButton(int button, int state) {
                                LogUtil.i("button-->" + button + " state-->" + state);
                                // button等于1表示拍照按钮，state等于1表示按下，0松开
                                if (button == 1 && state == 0) {
                                    emitter.onNext(state);
                                }
                            }
                        });
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Integer>() {
                            @Override
                            public void accept(Integer integer) throws Exception {
                                if (mPhotographCallback != null) {
                                    mPhotographCallback.onPhotographClick();
                                }
                            }
                        }));

                // 图片预览流回调
                mUVCCamera.setFrameCallback(new IFrameCallback() {
                    @Override
                    public void onFrame(ByteBuffer frame) {
                        int lenght = frame.capacity();
                        byte[] yuv = new byte[lenght];
                        frame.get(yuv);
                        if (mPreviewCallback != null) {
                            mPreviewCallback.onPreviewFrame(yuv);
                        }
                        if (isTakePhoto) {
                            LogUtil.i("take picture");
                            isTakePhoto = false;
                            savePicture(yuv, PICTURE_WIDTH, PICTURE_HEIGHT, mPreviewRotation);
                        }
                    }
                }, UVCCamera.PIXEL_FORMAT_YUV420SP);

                if (mSurface != null) {
                    mUVCCamera.setPreviewDisplay(mSurface);
                }
                mUVCCamera.updateCameraParams();
                mUVCCamera.startPreview();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存图片
     *
     * @param yuv
     * @param width
     * @param height
     * @param rotation
     */
    public void savePicture(final byte[] yuv, final int width, final int height,
                            final float rotation) {
        if (mPictureCallback == null) {
            return;
        }
        LogUtil.i("savePicture");
        mSubscriptions.add(Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                File file = getPictureFile(mPictureName);
                String path = FileUtil.saveYuv2Jpeg(file, yuv, width, height, rotation);
                emitter.onNext(path);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String path) throws Exception {
                        if (mPictureCallback != null) {
                            mPictureCallback.onPictureTaken(path);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        if (mPictureCallback != null) {
                            mPictureCallback.onPictureTaken(null);
                        }
                    }
                }));
    }

    /**
     * 停止预览
     */
    @Override
    public void stopPreview() {
        try {
            if (mUVCCamera != null) {
                LogUtil.i("stopPreview");
                mUVCCamera.setButtonCallback(null);
                mUVCCamera.setFrameCallback(null, 0);
                mUVCCamera.stopPreview();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 拍照
     */
    @Override
    public void takePicture() {
        isTakePhoto = true;
        mPictureName = UUID.randomUUID().toString() + ".jpg";
    }

    /**
     * 拍照
     *
     * @param pictureName 图片名称
     */
    @Override
    public void takePicture(String pictureName) {
        isTakePhoto = true;
        mPictureName = pictureName;
    }

    /**
     * 设置usb设备连接回调
     *
     * @param callback
     */
    @Override
    public void setConnectCallback(ConnectCallback callback) {
        this.mConnectCallback = callback;
        this.mUsbMonitor.setConnectCallback(callback);
    }

    /**
     * 设置预览回调
     *
     * @param callback
     */
    @Override
    public void setPreviewCallback(PreviewCallback callback) {
        this.mPreviewCallback = callback;
    }

    /**
     * 设置拍照按钮点击回调
     *
     * @param callback
     */
    @Override
    public void setPhotographCallback(PhotographCallback callback) {
        this.mPhotographCallback = callback;
    }

    /**
     * 设置拍照回调
     *
     * @param callback
     */
    @Override
    public void setPictureTakenCallback(PictureCallback callback) {
        this.mPictureCallback = callback;
    }

    /**
     * uvc相机实例
     *
     * @return
     */
    @Override
    public UVCCamera getUVCCamera() {
        return mUVCCamera;
    }

    /**
     * 是否已经打开相机
     *
     * @return
     */
    @Override
    public boolean isCameraOpen() {
        return mUVCCamera != null;
    }

    /**
     * 配置信息
     *
     * @return
     */
    @Override
    public CameraConfig getConfig() {
        return mConfig;
    }

    /**
     * 删除图片缓存目录
     */
    @Override
    public void clearCache() {
        try {
            // 删除app缓存目录里的图片
            File cacheDir = new File(FileUtil.getDiskCacheDir(mContext, mConfig.getDirName()));
            FileUtil.deleteFile(cacheDir);

            // 删除sdcard目录里的图片
            File sdcardDir = FileUtil.getSDCardDir(mConfig.getDirName());
            FileUtil.deleteFile(sdcardDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取保存的图片文件
     *
     * @param pictureName
     * @return
     */
    protected File getPictureFile(String pictureName) {
        File file = null;
        switch (mConfig.getPicturePath()) {
            case APPCACHE:
            default:
                file = FileUtil.getCacheFile(mContext, mConfig.getDirName(), pictureName);
                break;

            case SDCARD:
                file = FileUtil.getSDCardFile(mConfig.getDirName(), pictureName);
                break;
        }
        return file;
    }

}
