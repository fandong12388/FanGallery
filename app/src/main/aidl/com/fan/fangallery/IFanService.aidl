// IFanService.aidl
package com.fan.fangallery;

// Declare any non-default types here with import statements

interface IFanService {
    boolean isPhotoValidate();
    void putLocalPhoto(int code,String path);
}
