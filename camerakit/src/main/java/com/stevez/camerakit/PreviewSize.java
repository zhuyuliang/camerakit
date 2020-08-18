package com.stevez.camerakit;

/**
 * @author: SteveZ
 * @created Create in 2020/8/3 10:20 AM.
 * @description: please add a description here
 */
public class PreviewSize {

    private int mWidth;
    private int mHeight;

    public PreviewSize(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
    }

    public void setWidth(int mWidth) {
        this.mWidth = mWidth;
    }

    public void setHeight(int mHeight) {
        this.mHeight = mHeight;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    @Override
    public PreviewSize clone() {
        return new PreviewSize(this.mWidth, this.mHeight);
    }
}
