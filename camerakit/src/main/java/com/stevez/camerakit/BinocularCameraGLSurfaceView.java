package com.stevez.camerakit;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.stevez.camera.CallBackEvents;
import com.stevez.camera.CameraApiType;
import com.stevez.camera.CameraFacing;
import com.stevez.camera.CameraManager;
import com.stevez.camera.CameraPreviewCallback;
import com.stevez.camera.CameraSize;
import com.stevez.camera.FacingType;
import com.stevez.camera.IAttributes;
import com.stevez.camerakit.R.styleable;
import com.stevez.camerakit.preview.CameraSurfaceTexture;
import com.stevez.camerakit.preview.CameraSurfaceTextureListener;
import com.stevez.camerakit.preview.CameraSurfaceView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: SteveZ
 * @created on: 2020/6/25 5:56 PM.
 * @description: Binocular CameraTextureView
 */
public class BinocularCameraGLSurfaceView extends FrameLayout
        implements LifecycleObserver,
        IBinocularCameraView {

    /**
     * Request code for a runtime permissions intent.
     */
    private static final int PERMISSION_REQUEST_CODE = 99107;

    /**
     * Flag for handling requesting the {@link Manifest.permission#CAMERA}
     * permission.
     */
    public static final int PERMISSION_CAMERA = 1;

    public static final String TAG = "BinocularCameraTextureView";
    private static final int DEFAULT_PREVIEW_WIDTH = 640;
    private static final int DEFAULT_DISPLAY_DIR;
    private static final int DEFAULT_PREVIEW_HEIGHT = 480;
    private int mPreviewWidth;
    private int mPreviewHeight;
    private int mDisplayDir;
    private int exposureCompensation;
    private boolean mIsMirror;
    private ScalableType mScalableType;
    private CameraManager mRgbInstance;
    private CameraManager mIrInstance;
    private CameraPreviewCallback mRgbPreviewCallback;
    private CameraPreviewCallback mIrPreviewCallback;

    private ICameraLifecycleCallback irCameraLifecycleCallback;
    private ICameraLifecycleCallback rgbCameraLifecycleCallback;

    private int rgbCamreaId = 0;
    private int irCameraId = 1;
    private int showCameraId = rgbCamreaId;

    private int mPermissions = 1;
    private PermissionsListener mPermissionsListener;

    private CameraSurfaceTexture surfaceTexture = null;
    private CameraSurfaceView cameraSurfaceView;
    LifecycleState lifecycleState = LifecycleState.STOPPED;
    SurfaceState surfaceState = SurfaceState.SURFACE_WAITING;

    static {
        DEFAULT_DISPLAY_DIR = Direction.AUTO.getValue();
    }

    public BinocularCameraGLSurfaceView(Context context) {
        this(context, (AttributeSet) null);
    }

    public BinocularCameraGLSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BinocularCameraGLSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mPreviewWidth = DEFAULT_PREVIEW_WIDTH;
        this.mPreviewHeight = DEFAULT_PREVIEW_HEIGHT;
        this.mDisplayDir = DEFAULT_DISPLAY_DIR;
        this.mScalableType = ScalableType.NONE;
        this.mIsMirror = false;
        if (attrs != null) {
            this.setKeepScreenOn(true);
            TypedArray a = context.obtainStyledAttributes(attrs, styleable.cameraStyle, 0, 0);
            if (a != null) {
                int scaleType = a.getInt(styleable.cameraStyle_scalableType, ScalableType.NONE.ordinal());
                this.mDisplayDir = a.getInt(styleable.cameraStyle_cameraDir, DEFAULT_DISPLAY_DIR);
                if (mDisplayDir == 4) {
                    this.mDisplayDir = Utils.getOrientation(getContext()) / 90;
                }
                this.mPreviewWidth = a.getInt(styleable.cameraStyle_previewWidth, DEFAULT_PREVIEW_WIDTH);
                this.mPreviewHeight = a.getInt(styleable.cameraStyle_previewHeight, DEFAULT_PREVIEW_HEIGHT);
                this.mIsMirror = a.getBoolean(styleable.cameraStyle_isMirror, false);
                this.rgbCamreaId = a.getInt(styleable.cameraStyle_rgbCamera, 0);
                this.irCameraId = a.getInt(styleable.cameraStyle_irCamera, 1);
                this.showCameraId = a.getInt(styleable.cameraStyle_cameraPreviewId, rgbCamreaId);
                this.exposureCompensation = a.getInt(styleable.cameraStyle_exposureCompensation, 0);
                a.recycle();
                this.mScalableType = ScalableType.values()[scaleType];
                this.post(new Runnable() {
                    @Override
                    public void run() {
                        scaleContentSize(mPreviewWidth, mPreviewHeight);
                    }
                });
                cameraSurfaceView = new CameraSurfaceView(context);
            }
        }
        this.mRgbInstance = CameraManager.getInstance(
                new CameraFacing.Builder().setFacingType(FacingType.OTHER)
                        .setCameraId(0).build(),
                CameraApiType.CAMERA1, context
        );
        this.mRgbInstance.setCallBackEvents(new CallBackEvents() {
            @Override
            public void onCameraOpen(IAttributes cameraAttributes) {
                if (rgbCameraLifecycleCallback != null) {
                    rgbCameraLifecycleCallback.onCameraOpen();
                }
            }

            @Override
            public void onCameraClose() {
                if (rgbCameraLifecycleCallback != null) {
                    rgbCameraLifecycleCallback.onCameraClose();
                }
            }

            @Override
            public void onCameraError(String errorMsg) {
                if (rgbCameraLifecycleCallback != null) {
                    rgbCameraLifecycleCallback.onCameraError(errorMsg);
                }
            }

            @Override
            public void onPreviewStarted() {
                if (rgbCameraLifecycleCallback != null) {
                    rgbCameraLifecycleCallback.onPreviewStarted();
                }
            }

            @Override
            public void onPreviewStopped() {
                if (rgbCameraLifecycleCallback != null) {
                    rgbCameraLifecycleCallback.onPreviewStopped();
                }
            }

            @Override
            public void onPreviewError(String errorMsg) {
                if (rgbCameraLifecycleCallback != null) {
                    rgbCameraLifecycleCallback.onPreviewError(errorMsg);
                }
            }
        });
        this.mIrInstance = CameraManager.getInstance(
                new CameraFacing.Builder().setFacingType(FacingType.OTHER)
                        .setCameraId(1).build(),
                CameraApiType.CAMERA1, context
        );
        this.mIrInstance.setCallBackEvents(new CallBackEvents() {
            @Override
            public void onCameraOpen(IAttributes cameraAttributes) {
                if (irCameraLifecycleCallback != null) {
                    irCameraLifecycleCallback.onCameraOpen();
                }
            }

            @Override
            public void onCameraClose() {
                if (irCameraLifecycleCallback != null) {
                    irCameraLifecycleCallback.onCameraClose();
                }
            }

            @Override
            public void onCameraError(String errorMsg) {
                if (irCameraLifecycleCallback != null) {
                    irCameraLifecycleCallback.onCameraError(errorMsg);
                }
            }

            @Override
            public void onPreviewStarted() {
                if (irCameraLifecycleCallback != null) {
                    irCameraLifecycleCallback.onPreviewStarted();
                }
            }

            @Override
            public void onPreviewStopped() {
                if (irCameraLifecycleCallback != null) {
                    irCameraLifecycleCallback.onPreviewStopped();
                }
            }

            @Override
            public void onPreviewError(String errorMsg) {
                if (irCameraLifecycleCallback != null) {
                    irCameraLifecycleCallback.onPreviewError(errorMsg);
                }
            }
        });

        cameraSurfaceView.setCameraSurfaceTextureListener(new CameraSurfaceTextureListener() {
            @Override
            public void onSurfaceReady(@NonNull CameraSurfaceTexture cameraSurfaceTexture) {
                surfaceTexture = cameraSurfaceTexture;
                surfaceState = SurfaceState.SURFACE_AVAILABLE;
                scaleContentSize(mPreviewWidth, mPreviewHeight);
                if (lifecycleState == LifecycleState.STARTED || lifecycleState == LifecycleState.RESUMED) {
                    onResume();
                }
            }
        });
        addView(cameraSurfaceView);
    }

    //change size
    private void scaleContentSize(int contentWidth, int contentHeight) {
        if (surfaceTexture != null) {

            int previewOrientation = mDisplayDir * 90;
            if (mDisplayDir == 1) {
                previewOrientation = mDisplayDir * 90 + 270;
            } else if (mDisplayDir == 2) {
                previewOrientation = mDisplayDir * 90 + 180;
            } else if (mDisplayDir == 3) {
                previewOrientation = mDisplayDir * 90 + 90;
            }
            surfaceTexture.setRotation(previewOrientation);
            surfaceTexture.setIsMirror(mIsMirror);

            int width = this.getWidth();
            int height = this.getHeight();
            boolean exChange;
            if (DEFAULT_DISPLAY_DIR == this.mDisplayDir) {
                if (width > height) {
                    exChange = false;
                } else {
                    exChange = true;
                }
            } else if (this.mDisplayDir != Direction.LEFT.getValue() && this.mDisplayDir != Direction.RIGHT.getValue()) {
                exChange = false;
            } else {
                exChange = true;
            }

            CameraSize previewSize =
                    exChange ?
                            new CameraSize(contentHeight, contentWidth)
                            : new CameraSize(contentWidth, contentHeight);
            surfaceTexture.setDefaultBufferSize(contentWidth, contentHeight);
            surfaceTexture.setSize(previewSize);

            cameraSurfaceView.post(new Runnable() {
                @Override
                public void run() {
                    int mW = width;
                    int mH = height;
                    if (exChange) {
                        mW = contentHeight;
                        mH = contentWidth;
                    } else {
                        mW = contentWidth;
                        mH = contentHeight;
                    }
                    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(mW, mH);
                    switch (mScalableType) {
                        case NONE:
                            lp.gravity = Gravity.NO_GRAVITY;
                            break;
                        case FIT_XY:
                            lp = new FrameLayout.LayoutParams(width, height);
                            break;
                        case FIT_START:
                            lp = getParamsForInsideWH(mW, mH);
                            lp.gravity = Gravity.START;
                            break;
                        case FIT_CENTER:
                            lp = getParamsForInsideWH(mW, mH);
                            lp.gravity = Gravity.CENTER;
                            break;
                        case FIT_END:
                            lp = getParamsForInsideWH(mW, mH);
                            lp.gravity = Gravity.END;
                            break;
                        case LEFT_TOP:
                            lp.gravity = Gravity.LEFT | Gravity.TOP;
                            break;
                        case LEFT_CENTER:
                            lp.gravity = Gravity.LEFT | Gravity.CENTER;
                            break;
                        case LEFT_BOTTOM:
                            lp.gravity = Gravity.LEFT | Gravity.BOTTOM;
                            break;
                        case CENTER_TOP:
                            lp.gravity = Gravity.TOP | Gravity.CENTER;
                            break;
                        case CENTER:
                            lp.gravity = Gravity.CENTER;
                            break;
                        case CENTER_BOTTOM:
                            lp.gravity = Gravity.CENTER | Gravity.BOTTOM;
                            break;
                        case RIGHT_TOP:
                            lp.gravity = Gravity.RIGHT | Gravity.TOP;
                            break;
                        case RIGHT_CENTER:
                            lp.gravity = Gravity.RIGHT | Gravity.CENTER;
                            break;
                        case RIGHT_BOTTOM:
                            lp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
                            break;
                        case LEFT_TOP_CROP:
                            lp = getParamsForCropWH(mW, mH, exChange);
                            lp.gravity = Gravity.LEFT | Gravity.TOP;
                            break;
                        case LEFT_CENTER_CROP:
                            lp = getParamsForCropWH(mW, mH, exChange);
                            lp.gravity = Gravity.LEFT | Gravity.CENTER;
                            break;
                        case LEFT_BOTTOM_CROP:
                            lp = getParamsForCropWH(mW, mH, exChange);
                            lp.gravity = Gravity.LEFT | Gravity.BOTTOM;
                            break;
                        case CENTER_TOP_CROP:
                            lp = getParamsForCropWH(mW, mH, exChange);
                            lp.gravity = Gravity.CENTER | Gravity.TOP;
                            break;
                        case CENTER_CROP:
                            lp = getParamsForCropWH(mW, mH, exChange);
                            lp.gravity = Gravity.CENTER;
                            break;
                        case CENTER_BOTTOM_CROP:
                            lp = getParamsForCropWH(mW, mH, exChange);
                            lp.gravity = Gravity.BOTTOM | Gravity.CENTER;
                            break;
                        case RIGHT_TOP_CROP:
                            lp = getParamsForCropWH(mW, mH, exChange);
                            lp.gravity = Gravity.RIGHT | Gravity.TOP;
                            break;
                        case RIGHT_CENTER_CROP:
                            lp = getParamsForCropWH(mW, mH, exChange);
                            lp.gravity = Gravity.RIGHT | Gravity.CENTER;
                            break;
                        case RIGHT_BOTTOM_CROP:
                            lp = getParamsForCropWH(mW, mH, exChange);
                            lp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
                            break;
                        case START_INSIDE:
                            lp = getParamsForInsideWH(mW, mH);
                            lp.gravity = Gravity.START;
                            break;
                        case CENTER_INSIDE:
                            lp = getParamsForInsideWH(mW, mH);
                            lp.gravity = Gravity.CENTER;
                            break;
                        case END_INSIDE:
                            lp = getParamsForInsideWH(mW, mH);
                            lp.gravity = Gravity.END;
                            break;
                        default:
                            lp.gravity = Gravity.NO_GRAVITY;
                    }
                    cameraSurfaceView.setLayoutParams(lp);
                }
            });

        }
    }

    /**
     * 自适应Inside宽高
     *
     * @param mW
     * @param mH
     * @return
     */
    private FrameLayout.LayoutParams getParamsForInsideWH(int mW, int mH) {
        if (getHeight() > getWidth()) {
            mH = (int)(mH * ((float)getWidth() / (float)mW));
            mW = getWidth();
        } else {
            mW = (int)(mW * ((float)getHeight() / (float)mH));
            mH = getHeight();
        }
        return new FrameLayout.LayoutParams(mW, mH);
    }

    /**
     * 自适应Crop宽高
     *
     * @param mW
     * @param mH
     * @return
     */
    private FrameLayout.LayoutParams getParamsForCropWH(int mW, int mH, boolean exChange) {
        if (getHeight() > getWidth()) {
            //预览的高为准
            mW = (int)((float)mW * ((float)getHeight() / (float)mH));
            mH = getHeight();
        } else {
            mH = (int)((float)mH * ((float)getWidth() / (float)mW));
            mW = getWidth();
        }
        return new FrameLayout.LayoutParams(mW, mH);
    }

    private void openCamera() {
        if (mRgbInstance != null && mIrInstance != null) {
            this.mRgbInstance.openCamera(new CameraFacing.Builder()
                    .setFacingType(FacingType.OTHER)
                    .setCameraId(rgbCamreaId)
                    .build());
            this.mRgbInstance.setPhotoSize(new CameraSize(this.mPreviewWidth, this.mPreviewHeight));
            this.mRgbInstance.setPreviewSize(new CameraSize(this.mPreviewWidth, this.mPreviewHeight));
            this.mRgbInstance.setPreviewOrientation(this.mDisplayDir * 90);
            this.mRgbInstance.setExposureCompensation(exposureCompensation);
            this.mIrInstance.openCamera(new CameraFacing.Builder()
                    .setFacingType(FacingType.OTHER)
                    .setCameraId(irCameraId)
                    .build());
            this.mIrInstance.setPhotoSize(new CameraSize(this.mPreviewWidth, this.mPreviewHeight));
            this.mIrInstance.setPreviewSize(new CameraSize(this.mPreviewWidth, this.mPreviewHeight));
            this.mIrInstance.setPreviewOrientation(this.mDisplayDir * 90);
            this.mIrInstance.setExposureCompensation(exposureCompensation);
        }
    }

    @Override
    public void resetPreviewSize(int width,int height) {
        this.mPreviewWidth = width;
        this.mPreviewHeight = height;
        this.scaleContentSize(this.mPreviewWidth, this.mPreviewHeight);
    }

    @Override
    public void setMirror(boolean mirror) {
        this.mIsMirror = mirror;
        this.scaleContentSize(this.mPreviewWidth, this.mPreviewHeight);
    }

    @Override
    public void setDisplayDir(Direction displayDirection) {
        this.mDisplayDir = displayDirection.getValue();
        if (mDisplayDir == 4) {
            this.mDisplayDir = Utils.getOrientation(getContext()) / 90;
        }
        this.scaleContentSize(this.mPreviewWidth, this.mPreviewHeight);
    }

    @Override
    public void setStyle(ScalableType style) {
        this.mScalableType = style;
        this.scaleContentSize(this.mPreviewWidth, this.mPreviewHeight);
    }

    /**
     * set Rgb camera Preview callback.
     *
     * @param callback {@link CameraPreviewCallback}
     */
    public void setRgbPreviewCallback(CameraPreviewCallback callback) {
        if (this.mRgbInstance != null) {
            this.mRgbInstance.removePreviewCallbackWithBuffer(this.mRgbPreviewCallback);
            this.mRgbPreviewCallback = callback;
            this.mRgbInstance.addPreviewCallbackWithBuffer(callback);
        }
    }

    /**
     * set IR camera Preview callback.
     *
     * @param callback {@link CameraPreviewCallback}
     */
    public void setIrPreviewCallback(CameraPreviewCallback callback) {
        if (this.mIrInstance != null) {
            this.mIrInstance.removePreviewCallbackWithBuffer(this.mIrPreviewCallback);
            this.mIrPreviewCallback = callback;
            this.mIrInstance.addPreviewCallbackWithBuffer(callback);
        }
    }

    /**
     * set RGB camera lifecycle callback.
     *
     * @param cameraLifecycleCallback {@link ICameraLifecycleCallback}
     */
    public void setRgbCameraLifecycleCallback(ICameraLifecycleCallback cameraLifecycleCallback) {
        this.rgbCameraLifecycleCallback = cameraLifecycleCallback;
    }

    /**
     * set IR camera lifecycle callback.
     *
     * @param cameraLifecycleCallback {@link ICameraLifecycleCallback}
     */
    public void setIrCameraLifecycleCallback(ICameraLifecycleCallback cameraLifecycleCallback) {
        this.irCameraLifecycleCallback = cameraLifecycleCallback;
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    @Override
    public void onStart() {
        //check permissions
        List<String> missingPermissions = getMissingPermissions();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && missingPermissions.size() > 0) {
            Activity activity = null;
            Context context = getContext();
            while (context instanceof ContextWrapper) {
                if (context instanceof Activity) {
                    activity = (Activity) context;
                }
                context = ((ContextWrapper) context).getBaseContext();
            }

            if (activity != null) {
                List<String> requestPermissions = new ArrayList<>();
                List<String> rationalePermissions = new ArrayList<>();
                for (String permission : missingPermissions) {
                    if (!activity.shouldShowRequestPermissionRationale(permission)) {
                        requestPermissions.add(permission);
                    } else {
                        rationalePermissions.add(permission);
                    }
                }

                if (requestPermissions.size() > 0) {
                    activity.requestPermissions(requestPermissions.toArray(new String[requestPermissions.size()]), PERMISSION_REQUEST_CODE);
                }

                if (rationalePermissions.size() > 0 && mPermissionsListener != null) {
                    mPermissionsListener.onPermissionsFailure();
                }
            }

            return;
        }

        //callback status
        if (mPermissionsListener != null) {
            mPermissionsListener.onPermissionsSuccess();
        }

        //set State
        lifecycleState = LifecycleState.STARTED;
        openCamera();
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    @Override
    public void onResume() {
        if (mRgbInstance != null && mIrInstance != null && (surfaceState == SurfaceState.SURFACE_AVAILABLE)) {
            lifecycleState = LifecycleState.RESUMED;
            scaleContentSize(mPreviewWidth, mPreviewHeight);
            //判断surfaceTexture并动态配置
            if (surfaceTexture != null) {
                if (showCameraId == rgbCamreaId) {
                    this.mRgbInstance.startPreview(surfaceTexture);
                    this.mIrInstance.startPreview(new SurfaceTexture(0));
                } else {
                    this.mRgbInstance.startPreview(new SurfaceTexture(0));
                    this.mIrInstance.startPreview(surfaceTexture);
                }
            }
        }
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    @Override
    public void onPause() {
        //stop preview
        if (mRgbInstance != null && mIrInstance != null) {
            lifecycleState = LifecycleState.PAUSED;
            this.mRgbInstance.stopPreview();
            this.mIrInstance.stopPreview();
        }
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    @Override
    public void onStop() {
        // release camera
        if (mRgbInstance != null) {
            lifecycleState = LifecycleState.STOPPED;
            this.mRgbInstance.stopPreview();
            this.mRgbInstance.release();
            this.setRgbPreviewCallback(null);
        }
        if (mIrInstance != null) {
            lifecycleState = LifecycleState.STOPPED;
            this.mIrInstance.stopPreview();
            this.mIrInstance.release();
            this.setIrPreviewCallback(null);
        }
    }

    /**
     * 主动检查权限
     *
     * @param activity
     */
    @Override
    public void requestPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> manifestPermissions = getMissingPermissions();

            if (manifestPermissions.size() > 0) {
                activity.requestPermissions(manifestPermissions.toArray(new String[manifestPermissions.size()]), PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            int approvedPermissions = 0;
            int deniedPermissions = 0;

            for (int i = 0; i < permissions.length; i++) {
                int flag = 0;
                switch (permissions[i]) {
                    case Manifest.permission.CAMERA: {
                        flag = PERMISSION_CAMERA;
                        break;
                    }
                }

                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    approvedPermissions = approvedPermissions | flag;
                } else {
                    deniedPermissions = deniedPermissions | flag;
                }
            }

            //权限完成后重新开始start
            onStart();
        }
    }

    /**
     * callback permissions status.
     *
     * @param permissionsListener
     */
    @Override
    public void setPermissionsListener(PermissionsListener permissionsListener) {
        mPermissionsListener = permissionsListener;
    }

    /**
     * 获取camera permission list.
     *
     * @return
     */
    private List<String> getMissingPermissions() {
        List<String> manifestPermissions = new ArrayList<>();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return manifestPermissions;
        }

        if ((mPermissions | PERMISSION_CAMERA) == mPermissions) {
            String manifestPermission = Manifest.permission.CAMERA;
            if (getContext().checkSelfPermission(manifestPermission) == PackageManager.PERMISSION_DENIED) {
                manifestPermissions.add(manifestPermission);
            }
        }

        return manifestPermissions;
    }

    @Override
    public void setRgbCameraId(@NonNull int cameraId) {
        this.rgbCamreaId = cameraId;
    }

    @Override
    public void setIrCameraId(@NonNull int cameraId) {
        this.irCameraId = cameraId;
    }

    @Override
    public void setPreviewCameraId(@NonNull int cameraId) {
        this.showCameraId = cameraId;
    }
}

