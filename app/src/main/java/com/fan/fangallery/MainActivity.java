package com.fan.fangallery;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private IFanService iService;
    private FanServiceConnection mConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //1.绑定服务
        mConnection = new FanServiceConnection();
        FanService.bindFanService(this, mConnection);
    }

    public void click(View view) {
        try {
            if (null != iService && iService.isPhotoValidate()) {
                GalleryActivity.launch(this);
            } else {
                Toast.makeText(this, "启动相机界面进行拍照", Toast.LENGTH_SHORT).show();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private class FanServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            iService = IFanService.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }
}
