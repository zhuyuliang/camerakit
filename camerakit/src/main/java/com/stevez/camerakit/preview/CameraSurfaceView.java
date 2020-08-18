package com.stevez.camerakit.preview;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.os.Build;
import androidx.annotation.Keep;
import android.util.AttributeSet;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.glGenTextures;

/**
 * @author SteveZ
 */
public class CameraSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer {

    CameraSurfaceTextureListener cameraSurfaceTextureListener = null;
    private CameraSurfaceTexture cameraSurfaceTexture = null;

    public void setCameraSurfaceTextureListener(CameraSurfaceTextureListener cameraSurfaceTextureListener) {
        this.cameraSurfaceTextureListener = cameraSurfaceTextureListener;
    }

    public CameraSurfaceView(Context context){
        super(context);
        init();
    }

    public CameraSurfaceView(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        init();
    }

    private void init() {
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);

        nativeInit();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    // GLSurfaceView.Renderer:

    @Override
    public void onSurfaceCreated(GL10 gl,EGLConfig config) {
        int[] textures = new int[2];
        glGenTextures(2, textures, 0);
        int inputTexture = textures[0];
        int outputTexture = textures[1];
        cameraSurfaceTexture = new CameraSurfaceTexture(inputTexture, outputTexture);
        cameraSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                requestRender();
            }
        });
        if(cameraSurfaceTextureListener != null) {
            cameraSurfaceTextureListener.onSurfaceReady(cameraSurfaceTexture);
        }

        nativeOnSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl,int width,int height) {
        nativeOnSurfaceChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        CameraSurfaceTexture cameraSurfaceTexture = this.cameraSurfaceTexture;
        if (cameraSurfaceTexture != null) {
            nativeOnDrawFrame();

            cameraSurfaceTexture.updateTexImage();
            nativeDrawTexture(cameraSurfaceTexture.outputTexture,
                    cameraSurfaceTexture.size.getWidth(),
                    cameraSurfaceTexture.size.getHeight());
        }
    }

    // ---

    @Keep
    @Override
    protected void finalize() throws Throwable{
        super.finalize();
        try {
            nativeFinalize();
        } catch (Exception e) {
            // ignore
        }
    }

    // ---

    @Keep
    private long nativeHandle = 0L;

    private native void nativeInit();

    private native void nativeOnSurfaceCreated();

    private native void nativeOnSurfaceChanged(int width,int height);

    private native void nativeOnDrawFrame();

    private native void nativeDrawTexture(int texture,int textureWidth,int textureHeight);

    private native void nativeFinalize();

    private native void nativeRelease();

    static {
            if (Build.VERSION.SDK_INT <= 17) {
                System.loadLibrary("camerakit-core");
            }
            System.loadLibrary("camerakit");
    }

}
