package com.stevez.camerakit;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
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
import com.stevez.camera.CameraManager;
import com.stevez.camera.CameraPreviewCallback;
import com.stevez.camera.CameraFacing;
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
 * @created on: 2020/5/25 5:56 PM.
 * @description: 封装了Camera的TextureView
 */
public class CameraGLSurfaceView extends FrameLayout
        implements LifecycleObserver,
        IPermissionCameraView {

    /**
     * Request code for a runtime permissions intent.
     */
    private static final int PERMISSION_REQUEST_CODE = 99107;

    /**
     * Flag for handling requesting the {@link Manifest.permission#CAMERA}
     * permission.
     */
    public static final int PERMISSION_CAMERA = 1;

    public static final String TAG = "CameraTextureView";
    private static final int DEFAULT_PREVIEW_WIDTH = 640;
    private static final int DEFAULT_DISPLAY_DIR;
    private static final int DEFAULT_PREVIEW_HEIGHT = 480;
    private ScalableType mScalableType;
    private int mPreviewWidth;
    private int mPreviewHeight;
    private int mDisplayDir;
    private int camreaId = 0;
    private boolean mIsMirror;
    private CameraManager mInstance;
    private CameraPreviewCallback mPreviewCallback;
    private ICameraLifecycleCallback iCameraLifecycleCallback;

    private int exposureCompensation;
    private int mPermissions = 1;
    private PermissionsListener mPermissionsListener;

    private CameraSurfaceTexture surfaceTexture = null;
    private CameraSurfaceView cameraSurfaceView;
    private LifecycleState lifecycleState  = LifecycleState.STOPPED;
    private SurfaceState surfaceState = SurfaceState.SURFACE_WAITING;

    static {
        DEFAULT_DISPLAY_DIR = Direction.AUTO.getValue();
    }

    public CameraGLSurfaceView(Context context) {
        this(context, (AttributeSet)null);
    }

    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraGLSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mScalableType = ScalableType.NONE;
        this.mPreviewWidth = DEFAULT_PREVIEW_WIDTH;
        this.mPreviewHeight = DEFAULT_PREVIEW_HEIGHT;
        this.mDisplayDir = DEFAULT_DISPLAY_DIR;
        this.mIsMirror = false;
        if (attrs != null) {
            this.setKeepScreenOn(true);
            TypedArray a = context.obtainStyledAttributes(attrs, styleable.cameraStyle, 0, 0);
            if (a != null) {
                int scaleType = a.getInt(styleable.cameraStyle_scalableType, ScalableType.NONE.ordinal());
                this.mDisplayDir = a.getInt(styleable.cameraStyle_cameraDir, DEFAULT_DISPLAY_DIR);
                if(mDisplayDir == 4){
                    this.mDisplayDir = Utils.getOrientation(getContext()) / 90;
                }
                this.mPreviewWidth = a.getInt(styleable.cameraStyle_previewWidth, DEFAULT_PREVIEW_WIDTH);
                this.mPreviewHeight = a.getInt(styleable.cameraStyle_previewHeight, DEFAULT_PREVIEW_HEIGHT);
                this.mIsMirror = a.getBoolean(styleable.cameraStyle_isMirror, false);
                this.camreaId = a.getInt(styleable.cameraStyle_myCameraFacing, 0);
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
        this.mInstance = CameraManager.getInstance(
                new CameraFacing.Builder().setFacingType(FacingType.OTHER)
                        .setCameraId(0).build(),
                CameraApiType.CAMERA1, context
        );
        this.mInstance.setCallBackEvents(new CallBackEvents() {
            @Override
            public void onCameraOpen(IAttributes cameraAttributes) {
                if(iCameraLifecycleCallback != null){
                    iCameraLifecycleCallback.onCameraOpen();
                }
            }

            @Override
            public void onCameraClose() {
                if(iCameraLifecycleCallback != null){
                    iCameraLifecycleCallback.onCameraClose();
                }
            }

            @Override
            public void onCameraError(String errorMsg) {
                if(iCameraLifecycleCallback != null){
                    iCameraLifecycleCallback.onCameraError(errorMsg);
                }
            }

            @Override
            public void onPreviewStarted() {
                if(iCameraLifecycleCallback != null){
                    iCameraLifecycleCallback.onPreviewStarted();
                }
            }

            @Override
            public void onPreviewStopped() {
                if(iCameraLifecycleCallback != null){
                    iCameraLifecycleCallback.onPreviewStopped();
                }
            }

            @Override
            public void onPreviewError(String errorMsg) {
                if(iCameraLifecycleCallback != null){
                    iCameraLifecycleCallback.onPreviewError(errorMsg);
                }
            }
        });

        cameraSurfaceView.setCameraSurfaceTextureListener(new CameraSurfaceTextureListener() {
            @Override
            public void onSurfaceReady(@NonNull CameraSurfaceTexture cameraSurfaceTexture) {
                surfaceTexture = cameraSurfaceTexture;
                surfaceState = SurfaceState.SURFACE_AVAILABLE;
                scaleContentSize(mPreviewWidth,mPreviewHeight);
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
        if (mInstance != null) {
            this.mInstance.openCamera(new CameraFacing.Builder().setFacingType(FacingType.OTHER).setCameraId(camreaId).build());
            this.mInstance.setPhotoSize(new CameraSize(this.mPreviewWidth, this.mPreviewHeight));
            this.mInstance.setPreviewSize(new CameraSize(this.mPreviewWidth, this.mPreviewHeight));
            this.mInstance.setPreviewOrientation(this.mDisplayDir * 90);
            this.mInstance.setExposureCompensation(exposureCompensation);
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
        if(mDisplayDir == 4){
            this.mDisplayDir = Utils.getOrientation(getContext()) / 90;
        }
        this.scaleContentSize(this.mPreviewWidth, this.mPreviewHeight);
    }

    @Override
    public void setStyle(ScalableType style) {
        this.mScalableType = style;
        this.scaleContentSize(this.mPreviewWidth, this.mPreviewHeight);
    }

    public void setCameraId(int cameraId){
        this.camreaId = cameraId;
    }

    public void setPreviewCallback(CameraPreviewCallback callback) {
        if (this.mInstance != null) {
            this.mInstance.removePreviewCallbackWithBuffer(this.mPreviewCallback);
            this.mPreviewCallback = callback;
            this.mInstance.addPreviewCallbackWithBuffer(callback);
        }
    }

    public void setCameraLifecycleCallback(ICameraLifecycleCallback cameraLifecycleCallback) {
        this.iCameraLifecycleCallback = cameraLifecycleCallback;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    @Override
    public void onStart() {
        List<String> missingPermissions = getMissingPermissions();
        if (Build.VERSION.SDK_INT >= 23 && missingPermissions.size() > 0) {
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

        if (mPermissionsListener != null) {
            mPermissionsListener.onPermissionsSuccess();
        }

        lifecycleState = LifecycleState.STARTED;
        openCamera();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    @Override
    public void onResume() {
        if (mInstance != null && surfaceState == SurfaceState.SURFACE_AVAILABLE) {
                lifecycleState = LifecycleState.RESUMED;
                scaleContentSize(mPreviewWidth,mPreviewHeight);
                if(surfaceTexture != null){
                    this.mInstance.startPreview(surfaceTexture);
                }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    @Override
    public void onPause() {
        if (mInstance != null) {
            lifecycleState = LifecycleState.PAUSED;
            this.mInstance.stopPreview();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    @Override
    public void onStop() {
        if (mInstance != null) {
            lifecycleState = LifecycleState.STOPPED;
            this.mInstance.stopPreview();
            this.mInstance.release();
            this.setPreviewCallback(null);
        }
    }

    @Override
    public void requestPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
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

            onStart();
        }
    }

    @Override
    public void setPermissionsListener(PermissionsListener permissionsListener) {
        mPermissionsListener = permissionsListener;
    }

    /**
     * @return
     */
    private List<String> getMissingPermissions() {
        List<String> manifestPermissions = new ArrayList<>();

        if (Build.VERSION.SDK_INT < 23) {
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
}

