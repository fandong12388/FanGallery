package com.fan.fangallery;

import java.util.Collection;
import java.util.Map;

/**
 * time: 15/6/7
 * description: 数据有效性的判断类
 *
 * @author fandong
 */
public class ValidateUtil {

    public static boolean isValidate(Collection<?> collection) {
        return null != collection && !collection.isEmpty();
    }

    public static boolean isValidate(Map<?, ?> map) {
        return map != null && !map.isEmpty();
    }


}
