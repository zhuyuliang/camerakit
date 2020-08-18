package com.stevez.camerakit;

/**
 * @author: SteveZ
 * @created on: 2020/5/25 5:57 PM.
 * @description: Direction
 */
public enum Direction {
    /**
     * Up
     */
    UP(0),
    /**
     * Left
     */
    LEFT(1),
    /**
     * Down
     */
    DOWN(2),
    /**
     * Right
     */
    RIGHT(3),
    /**
     * Auto
     */
    AUTO(4);

    private static Direction[] sDirection = new Direction[]{UP, LEFT, DOWN, RIGHT};
    final int mNativeInt;

    private Direction(int ni) {
        this.mNativeInt = ni;
    }

    /**
     * 根据Value获取Enum
     * @return @Direction
     */
    public static Direction nativeToDir(int ni) {
        return sDirection[ni];
    }

    /**
     * 获取方向Value
     * @return int
     */
    public int getValue() {
        return this.mNativeInt;
    }
}
