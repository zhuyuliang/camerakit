package com.stevez.camera.sample;

import android.app.Application;
import android.content.Context;

/**
 * @author: Zhu Yuliang
 * @created Create in 2020/6/23 4:23 PM.
 * @description: please add a description here
 */
public class App extends Application {

    public static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        ConstantsConfig.getInstance().onInit(this,true);
    }

}