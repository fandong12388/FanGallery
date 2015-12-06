package com.fan.fangallery;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.fans.loader.FanImageLoader;
import com.fans.loader.internal.utils.L;

/**
 * time: 15/12/6
 * description:
 *
 * @author fandong
 */
public class FanService extends Service {

    private class MixBinder extends IFanService.Stub {
        @Override
        public boolean isPhotoValidate() throws RemoteException {
            return LocalPhotoManager.getInstance().isPhotoValidate();
        }

        @Override
        public void putLocalPhoto(int code, String path) throws RemoteException {
            LocalPhotoManager.getInstance().put2Gallery(code, path);
            LocalPhotoManager.getInstance().put2Map(code, path);
        }
    }

    /*退出的时候，干掉mix进程*/
    public static void stopFanService(Context context) {
        //1.停掉LocalPhotoManager
        LocalPhotoManager.destroy();
        //2.停掉mix进程
        Intent intent = new Intent(context, FanService.class);
        context.stopService(intent);
    }


    /*启动mix进程*/
    public static void startFanService(Context context) {
        Intent intent = new Intent(context, FanService.class);
        context.startService(intent);
    }

    /*绑定mix进程*/
    public static void bindFanService(Context context, ServiceConnection connection) {
        Intent intent = new Intent(context, FanService.class);
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MixBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //1.扫描本地图片到内存当中
        LocalPhotoManager.getInstance().initialize();
        //3.初始化ImageLoader
        FanImageLoader.init(getApplicationContext(), FileUtil.getPathByType(FileUtil.DIR_TYPE_CACHE));
        //5.初始化Pinguo-image-loader当中的日志系统
        L.writeDebugLogs(BuildConfig.DEBUG);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stopFanService(this);
        return super.onUnbind(intent);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
