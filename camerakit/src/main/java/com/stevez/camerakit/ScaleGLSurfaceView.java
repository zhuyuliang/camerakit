package com.stevez.camerakit;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.stevez.camera.CameraSize;
import com.stevez.camerakit.R.styleable;
import com.stevez.camerakit.preview.CameraSurfaceTexture;
import com.stevez.camerakit.preview.CameraSurfaceTextureListener;
import com.stevez.camerakit.preview.CameraSurfaceView;

/**
 * @author: Zhu Yuliang
 * @created on: 2020/5/25 5:59 PM.
 * @description: Common GLSurfaceView
 * <p>
 * Caution：
 * set @{code LifecycleObserver} add Lifecycle
 * or
 * set onResume for Lifecycle.
 * <p/>
 */
public class ScaleGLSurfaceView extends FrameLayout
        implements ICameraView, LifecycleObserver {
    private static final int DEFAULT_PREVIEW_WIDTH = 640;
    private static final int DEFAULT_DISPLAY_DIR;
    private static final int DEFAULT_PREVIEW_HEIGHT = 480;
    private ScalableType mScalableType;
    private int mPreviewWidth;
    private int mPreviewHeight;
    private int mDisplayDir;
    private boolean mIsMirror;

    private CameraSurfaceTexture surfaceTexture = null;
    private CameraSurfaceView cameraSurfaceView;
    LifecycleState lifecycleState = LifecycleState.STOPPED;
    SurfaceState surfaceState = SurfaceState.SURFACE_WAITING;

    /**
     * 状态监听
     */
    private GLSurfaceViewListener listener = null;

    public void setListener(GLSurfaceViewListener listener) {
        this.listener = listener;
    }

    static {
        DEFAULT_DISPLAY_DIR = Direction.AUTO.getValue();
    }

    public ScaleGLSurfaceView(Context context) {
        this(context, (AttributeSet) null);
    }

    public ScaleGLSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScaleGLSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mScalableType = ScalableType.NONE;
        this.mPreviewWidth = DEFAULT_PREVIEW_WIDTH;
        this.mPreviewHeight = DEFAULT_PREVIEW_HEIGHT;
        this.mDisplayDir = DEFAULT_DISPLAY_DIR;
        this.mIsMirror = false;
        if (attrs != null) {
            //保持
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
                a.recycle();
                this.mScalableType = ScalableType.values()[scaleType];
                this.post(new Runnable() {
                    @Override
                    public void run() {
                        scaleContentSize(mPreviewWidth, mPreviewHeight);
                    }
                });
            }
        }
    }

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
            if (Build.VERSION.SDK_INT >= 21) {
                surfaceTexture.setRotation(previewOrientation);
                surfaceTexture.setIsMirror(mIsMirror);
            }

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

    @Override
    public void resetPreviewSize(int width, int height) {
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
            this.mDisplayDir = Utils.getOrientation(getContext()) % 360;
        }
        this.scaleContentSize(this.mPreviewWidth, this.mPreviewHeight);
    }

    @Override
    public void setStyle(ScalableType style) {
        this.mScalableType = style;
        this.scaleContentSize(this.mPreviewWidth, this.mPreviewHeight);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    @Override
    public void onStart() {
        lifecycleState = LifecycleState.STARTED;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    @Override
    public void onResume() {
        cameraSurfaceView = new CameraSurfaceView(getContext());
        cameraSurfaceView.setCameraSurfaceTextureListener(new CameraSurfaceTextureListener() {
            @Override
            public void onSurfaceReady(@NonNull CameraSurfaceTexture cameraSurfaceTexture) {
                surfaceTexture = cameraSurfaceTexture;
                scaleContentSize(mPreviewWidth, mPreviewHeight);
                surfaceState = SurfaceState.SURFACE_AVAILABLE;
                if (lifecycleState == LifecycleState.STARTED || lifecycleState == LifecycleState.RESUMED) {
                    if (listener != null) {
                        listener.onCallBackSurfaceTexture(surfaceTexture);
                    }
                }
            }
        });
        addView(cameraSurfaceView);
        lifecycleState = LifecycleState.RESUMED;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    @Override
    public void onPause() {
        lifecycleState = LifecycleState.PAUSED;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    @Override
    public void onStop() {
        lifecycleState = LifecycleState.STOPPED;
    }
}

