package com.fan.fangallery;

import android.app.Application;

/**
 * time: 15/12/5
 * description:
 *
 * @author fandong
 */
public class GalleryApplication extends Application {


    public static GalleryApplication gContext;


    @Override
    public void onCreate() {
        super.onCreate();
        gContext = this;
        //1.启动pic进程
        FanService.startFanService(this);
    }
}

