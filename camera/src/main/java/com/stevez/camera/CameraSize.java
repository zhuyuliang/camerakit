package com.stevez.camera;

import android.os.Build;
import android.util.Size;

import androidx.annotation.RequiresApi;

/**
 * @author SteveZ
 */
public class CameraSize implements Comparable<CameraSize> {

    private int width;
    private int height;

    public CameraSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int area() {
        return width * height;
    }

    public float aspectRatio() {
        if (width == 0 || height == 0) {
            return 1f;
        }

        return (float) width / (float) height;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Size getSize() {
        return new Size(width, height);
    }

    @Override
    public int compareTo(CameraSize other) {
        int areaDiff = width * height - other.width * other.height;
        if (areaDiff > 0) {
            return 1;
        } else if (areaDiff < 0) {
            return -1;
        } else {
            return 0;
        }
    }

}