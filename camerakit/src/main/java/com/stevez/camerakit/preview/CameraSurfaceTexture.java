package com.stevez.camerakit.preview;

import android.graphics.SurfaceTexture;
import android.opengl.Matrix;
import androidx.annotation.Keep;

import com.stevez.camera.CameraSize;

/**
 * @author SteveZ
 */
public class CameraSurfaceTexture extends SurfaceTexture {

    int inputTexture;
    int outputTexture;

    public CameraSurfaceTexture(int inputTexture, int outputTexture){
        super(inputTexture);
        this.inputTexture = inputTexture;
        this.outputTexture = outputTexture;
        nativeInit(inputTexture, outputTexture);
        Matrix.setIdentityM(extraTransformMatrix, 0);
    }

    CameraSize size = new CameraSize(0, 0);

    public void setSize(CameraSize size) {
        this.size = size;
        previewInvalidated = true;
    }

    private Boolean previewInvalidated = false;
    private float[] transformMatrix = new float[16];
    private float[] extraTransformMatrix = new float[16];

    @Override
    public void updateTexImage() {
        if (previewInvalidated) {
            nativeSetSize(size.getWidth(), size.getHeight());
            previewInvalidated = false;
        }

        super.updateTexImage();
        getTransformMatrix(transformMatrix);
        nativeUpdateTexImage(transformMatrix, extraTransformMatrix);
    }

    @Override
    public void release() {
        nativeRelease();
    }

    public void setRotation(int degrees) {
        Matrix.setIdentityM(extraTransformMatrix, 0);
        Matrix.rotateM(extraTransformMatrix, 0, (float) degrees, 0f, 0f, 1f);
    }

    public void setIsMirror(boolean isMirror){
        if(isMirror){
            Matrix.setIdentityM(extraTransformMatrix, 0);
            Matrix.rotateM(extraTransformMatrix, 0, 180f, 0f, 1.0f, 0f);
        }
    }

    // ---

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        try {
            nativeFinalize();
        } catch (Exception e) {
            // ignore
        }
    }

    // ---

    @Keep
    private long nativeHandle  = 0L;

    private native void nativeInit(int inputTexture, int outputTexture);

    private native void nativeSetSize(int width, int height);

    private native void nativeUpdateTexImage(float[] transformMatrix,float[] extraTransformMatrix);

    private native void nativeFinalize();

    private native void nativeRelease();

    static {
        System.loadLibrary("camerakit");
    }

}

