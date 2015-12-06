package com.fan.fangallery;

import android.support.annotation.DimenRes;
import android.support.annotation.StringRes;

import java.lang.reflect.Field;

import static com.fan.fangallery.GalleryApplication.gContext;

/**
 * time: 15/6/19
 * description:获取资源文件当中配置资源的帮助类
 *
 * @author fandong
 */
public class ResHelper {

    public static String getString(@StringRes int resId) {
        return gContext.getResources().getString(resId);
    }

    public static float getDimen(@DimenRes int dimenId) {
        return gContext.getResources().getDimension(dimenId);
    }

    public static int getScreenWidth() {
        return gContext.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return gContext.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 获取电量条statusbar的高度
     *
     * @return
     */
    public static int getStatusBarHeight() {
        int statusBarHeight = 0;
        try {
            Class clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            Field field = clazz.getField("status_bar_height");
            int id = Integer.parseInt(field.get(object).toString());
            //依据id值获取到状态栏的高度,单位为像素
            statusBarHeight = gContext.getResources().getDimensionPixelSize(id);
        } catch (Exception e) {
        }
        return statusBarHeight;
    }
}
