package com.littlecat.powerbank.util;

import android.os.Environment;


import com.littlecat.powerbank.BaseApplication;

import java.io.File;


/**
 * Created by hero on 2018/4/18.
 */

public class SDcardUtils {
    public static final String rootDir = "/com.littlecat.powerbank/.nomedia/cache";//˽��
    public static final String apkCache = "/apk";



    /**
     * 获取当前缓存目录
     *
     * @param cache
     * @return
     */
    public static String getCachePath(String cache) {
        String headerDir;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            headerDir = Environment.getExternalStorageDirectory().getAbsolutePath() + rootDir;
        } else {
            headerDir = BaseApplication.getContext().getFilesDir().getAbsolutePath();
        }
        headerDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(headerDir + cache);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }
}
