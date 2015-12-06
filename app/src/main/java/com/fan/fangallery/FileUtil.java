package com.fan.fangallery;

import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import com.fans.loader.core.util.NameGeneratorUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * time: 15/6/7
 * description: 文件管理类
 *
 * @author fandong
 */
public class FileUtil {
    public static String EXTERNAL_STORAGE;
    public static final int DIR_TYPE_HOME = 0x01;
    public static final int DIR_TYPE_CACHE = 0x02;
    public static final int DIR_TYPE_IMAGE = 0x03;
    public static final int DIR_TYPE_LOG = 0x04;
    public static final int DIR_TYPE_APK = 0x05;
    public static final int DIR_TYPE_DOWNLOAD = 0x06;
    public static final int DIR_TYPE_TEMP = 0x07;
    public static final int DIR_TYPE_CIRCLE = 0x08;
    public static final int DIR_TYPE_COPY_DB = 0x09;
    /* 默认最小需要的空间*/
    public static final long MIN_SPACE = 10 * 1024 * 1024;
    public static final String DISK_STORAGE_CACHE = "circle";

    static {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            String externalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            String path = externalPath;
            if (path.endsWith("0")) {
                path = path.substring(0, path.length() - 1) + "1";
                File file = new File(path);
                if (file.exists() && file.isDirectory() & file.canRead() && file.canWrite()) {
                    EXTERNAL_STORAGE = path;
                } else {
                    EXTERNAL_STORAGE = externalPath;
                }
            } else {
                EXTERNAL_STORAGE = externalPath;
            }
        } else {
            EXTERNAL_STORAGE = GalleryApplication.gContext.getCacheDir().getAbsolutePath();
        }
    }

    private static String DIR_HOME = EXTERNAL_STORAGE + "/circle";
    /*存放copy过来的db*/
    private static String DIR_COPY_DB = DIR_HOME + "/db";
    /* 该文件用来在图库中屏蔽本应用的图片.*/
    private static String DIR_NO_MEDIA_FILE = DIR_HOME + "/.nomedia";
    private static String DIR_IMAGE = DIR_HOME + "/image";
    private static String DIR_CACHE = DIR_HOME + "/cache";
    private static String DIR_LOG = DIR_HOME + "/log";
    private static String DIR_APK = DIR_HOME + "/apk";
    private static String DIR_DOWNLOAD = DIR_HOME + "/download";
    private static String DIR_TEMP = DIR_HOME + "/temp";
    private static String APK_NAME = "circle_update.apk";


    /**
     * 通过类型获取目录路径
     *
     * @param type
     * @return
     */
    public static String getPathByType(int type) {
        String dir = "/";
        String filePath;

        switch (type) {
            case DIR_TYPE_HOME:
                filePath = DIR_HOME;
                break;

            case DIR_TYPE_CACHE:
                filePath = DIR_CACHE;
                break;

            case DIR_TYPE_IMAGE:
                filePath = DIR_IMAGE;
                break;

            case DIR_TYPE_LOG:
                filePath = DIR_LOG;
                break;

            case DIR_TYPE_APK:
                filePath = DIR_APK;
                break;

            case DIR_TYPE_DOWNLOAD:
                filePath = DIR_DOWNLOAD;
                break;

            case DIR_TYPE_TEMP:
                filePath = DIR_TEMP;
                break;

            case DIR_TYPE_COPY_DB:
                filePath = DIR_COPY_DB;
                break;
            case DIR_TYPE_CIRCLE:
                filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()
                        + File.separator + ResHelper.getString(R.string.app_name);
                break;
            default:
                filePath = "";
                break;
        }

        File file = new File(filePath);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }

        if (file.exists()) {
            if (file.isDirectory()) {
                dir = file.getPath();
            }
        } else {
            // 文件没创建成功，可能是sd卡不存在，但是还是把路径返回
            dir = filePath;
        }

        return dir + "/";
    }

    /**
     * SdCard是否存在
     *
     * @return
     */
    public static boolean isSDCardExist() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 判断存储空间是否足够,默认需要 {@link FileUtil#MIN_SPACE}
     *
     * @return
     */
    public static boolean hasEnoughSpace() {
        return hasEnoughSpace(MIN_SPACE);
    }

    /**
     * 判断存储空间是否足够
     *
     * @param needSize
     * @return
     */
    public static boolean hasEnoughSpace(float needSize) {
        if (isSDCardExist()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(path.getPath());

            long blockSize;
            long availCount;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = sf.getBlockSizeLong();
                availCount = sf.getAvailableBlocksLong();
            } else {
                blockSize = sf.getBlockSize();
                availCount = sf.getAvailableBlocks();
            }

            long restSize = availCount * blockSize;
            if (restSize > needSize) {
                return true;
            }
        }
        return false;
    }

    /**
     * 生成文件路径
     *
     * @param dirType  文件路径类型
     * @param fileName 文件名，需带后缀
     * @return
     */
    public static String createPath(int dirType, String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            fileName = "temp";
        }

        fileName = fileName.replace('/', '_').replace(':', '_').replace("?", "_");

        String filePath = getPathByType(dirType) + fileName;

        File file = new File(filePath);
        // 如果文件存在则先删除
        if (file.exists()) {
            file.delete();
        }

        return filePath;
    }

    /**
     * 生成文件
     *
     * @param dirType  文件路径类型
     * @param fileName 文件名，需带后缀
     * @return
     */
    public static File createFile(int dirType, String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            fileName = "temp";
        }

        fileName = fileName.replace('/', '_').replace(':', '_').replace("?", "_");
        String filePath = getPathByType(dirType) + File.separator + fileName;

        File file = new File(filePath);

        try {
            if (file.exists() && file.isFile()) {
                file.delete();
            }
            file.createNewFile();
        } catch (IOException e) {
            // ignore
            return null;
        }

        return file;
    }

    /**
     * Delete file or folder.
     *
     * @param deleteFile
     * @return
     */
    public static boolean deleteFile(File deleteFile) {
        if (deleteFile != null) {
            if (!deleteFile.exists()) {
                return true;
            }
            if (deleteFile.isDirectory()) {
                // 处理目录
                File[] files = deleteFile.listFiles();
                //循环删除目录
                if (null != files) {
                    for (File file : files) {
                        deleteFile(file);
                    }
                }
                //删除目录自己
                return deleteFile.delete();
            } else {
                // 如果是文件，删除
                return deleteFile.delete();
            }
        }
        return false;
    }


    public static void inputStreamToFile(InputStream is, File file) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);

            int bytesRead = 0;
            byte[] buffer = new byte[3072];

            while ((bytesRead = is.read(buffer, 0, 3072)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 避免图片放入到图库中（屏蔽其他软件扫描）.
     */
    public static void hideMediaFile() {
        File file = new File(DIR_NO_MEDIA_FILE);
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
        } catch (IOException e) {
            Log.e("test", e.getMessage());
        }
    }


    public static String getApkPath() {
        File apk = createFile(DIR_TYPE_APK, APK_NAME);
        return apk.getAbsolutePath();
    }

    /**
     * 获取文件名字的前面部分，不包括文件名
     *
     * @param fileName e.g:2012.zip
     * @return 2012
     */
    public static String getFilePreName(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            return fileName;
        } else {
            return fileName.substring(0, index);
        }
    }

    /**
     * 解压zip文件
     *
     * @param zipFile
     * @param folderPath
     * @throws IOException
     */
    public static int upZipFile(File zipFile, String folderPath) throws IOException {
        ZipFile zfile = new ZipFile(zipFile);
        Enumeration zList = zfile.entries();
        ZipEntry ze = null;
        byte[] buf = new byte[1024];
        while (zList.hasMoreElements()) {
            ze = (ZipEntry) zList.nextElement();
            if (ze.isDirectory()) {
                String dirstr = folderPath + File.separator + ze.getName();
                dirstr = new String(dirstr.getBytes("8859_1"), "GB2312");
                File f = new File(dirstr);
                f.mkdirs();
                continue;
            }
            OutputStream os = new BufferedOutputStream(new FileOutputStream(getRealFileName(folderPath, ze.getName())));
            InputStream is = new BufferedInputStream(zfile.getInputStream(ze));
            int readLen = 0;
            while ((readLen = is.read(buf, 0, 1024)) != -1) {
                os.write(buf, 0, readLen);
            }
            is.close();
            os.close();
        }
        zfile.close();
        return 0;
    }

    /**
     * 给定根目录，返回一个相对路径所对应的实际文件名.
     *
     * @param baseDir     指定根目录
     * @param absFileName 相对路径名，来自于ZipEntry中的name
     * @return java.io.File 实际的文件
     */
    private static File getRealFileName(String baseDir, String absFileName) {
        String[] dirs = absFileName.split("/");
        File ret = new File(baseDir);
        String substr = null;
        if (dirs.length > 1) {
            for (int i = 0; i < dirs.length - 1; i++) {
                substr = dirs[i];
                try {
                    substr = new String(substr.getBytes("8859_1"), "GB2312");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();

                }
                ret = new File(ret, substr);
                if (!ret.exists()) {
                    ret.mkdirs();
                }
            }
            substr = dirs[dirs.length - 1];
            try {
                substr = new String(substr.getBytes("8859_1"), "GB2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            ret = new File(ret, substr);
            return ret;

        }
        return ret;
    }

    /**
     * 写入信息到文件
     *
     * @param file
     * @param content
     */
    public static void writeFile(File file, byte[] content) throws IOException {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new IOException("not crete file=" + file.getAbsolutePath());
            }
        }
        FileOutputStream fileOutputStream = null;
        ByteArrayInputStream bis = null;
        try {
            bis = new ByteArrayInputStream(content);
            fileOutputStream = new FileOutputStream(file, false);
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = bis.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, length);
            }
            fileOutputStream.flush();
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            if (bis != null) {
                bis.close();
            }
        }
    }

    /**
     * 读取文件内容
     *
     * @param file
     * @throws IOException
     */
    public static byte[] readFile(File file) throws IOException {
        if (!file.exists()) {
            throw new IOException("not crete file=" + file.getAbsolutePath());
        }
        FileInputStream fileInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            byteArrayOutputStream = new ByteArrayOutputStream(64);
            int length = 0;
            byte[] buffer = new byte[1024];
            while ((length = fileInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
            return byteArrayOutputStream.toByteArray();
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
        }
    }

    public static boolean copy(File source, File target) {
        if (source == null || target == null || !source.exists() || source.length() < 100) {
            return false;
        }
        try {
            FileOutputStream fos = new FileOutputStream(target);
            FileInputStream fis = new FileInputStream(source);
            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.flush();
            fos.close();
            fis.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 返回当前生成的图片文件
     *
     * @return
     */
    public static File getPathFile(String url) {
        String dir = getPathByType(FileUtil.DIR_TYPE_CIRCLE);
        String fileName = "IMG_CC_" + NameGeneratorUtil.generateCacheKey(url) + ".jpg";
        return new File(dir + fileName);
    }
}
