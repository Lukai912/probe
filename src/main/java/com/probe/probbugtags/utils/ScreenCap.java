package com.probe.probbugtags.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by chengqianqian-xy on 2016/5/18.
 */
public class ScreenCap {

    private static final String TAG = "ScreenCap";

    private static final String SCREENSHOTS_DIR_NAME = "screenShots";
    private static final String SCREENSHOT_FILE_NAME_TEMPLATE = "Screenshot%s.jpg";
    private static final String SCREENSHOT_FILE_PATH_TEMPLATE = "%s/%s/%s";
    public  static int screenOrientation = 0;   //图片应该显示的方向

    /**
     * Activity screenCap
     *
     * @param activity
     * @return
     */
    public static Bitmap activityShot(final Activity activity) {

        // 获取windows中最顶层的view
        View view = activity.getWindow().getDecorView();

        // 允许当前窗口保存缓存信息
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();

        // 获取状态栏高度
        int statusBarHeights = getStatusBarHeight(activity
                .getApplicationContext());

        int width = view.getWidth();
        int height = view.getHeight();

        // 去掉状态栏
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache(), 0,
                statusBarHeights, width, height - statusBarHeights);

        // 销毁缓存信息
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(false);

        // width > height
        if (bitmap.getWidth() > bitmap.getHeight()) {
            screenOrientation = 1;   //1:landscape;0:portrait
        } else {
            screenOrientation = 0;
        }

        return bitmap;
    }

    /**
     * 存储到context的cache文件夹中
     *
     * @param bmp
     * @param context
     * @return
     */
    public static String saveToSD(Bitmap bmp, Context context) {
        String filePath = getFilePath(context);
        File file = new File(filePath);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            if (fos != null) {
                // 第一参数是图片格式，第二参数是图片质量，第三参数是输出流
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return filePath;
    }

    private static String getFilePath(Context context) {
        // 文件名
        long systemTime = System.currentTimeMillis();
        String imageDate = new SimpleDateFormat("yyyyMMddHHmmss")
                .format(new Date(systemTime));
        String mFileName = String.format(SCREENSHOT_FILE_NAME_TEMPLATE,
                imageDate);

        File dir = new File(context.getCacheDir() + File.separator
                + SCREENSHOTS_DIR_NAME);
        // 判断文件是否存在，不存在则创建
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filePath = dir + File.separator + mFileName;
        Logger.i(TAG, "file path:" + filePath);
        return filePath;
    }

    /**
     * 存储到sdcard
     *
     * @param bmp
     * @return
     */
    public static String saveToSD(Bitmap bmp) {
        // 判断sd卡是否存在
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            // 文件名
            long systemTime = System.currentTimeMillis();
            String imageDate = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
                    .format(new Date(systemTime));
            String mFileName = String.format(SCREENSHOT_FILE_NAME_TEMPLATE,
                    imageDate);

            File dir = new File(SCREENSHOTS_DIR_NAME);
            // 判断文件是否存在，不存在则创建
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 文件全名
            String mstrRootPath = Environment.getExternalStorageDirectory()
                    .toString();
            String mFilePath = String.format(SCREENSHOT_FILE_PATH_TEMPLATE,
                    mstrRootPath, SCREENSHOTS_DIR_NAME, mFileName);

            Logger.i(TAG, "file path:" + mFilePath);
            File file = new File(mFilePath);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Logger.i(TAG, "file path:" + file.getAbsolutePath());
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                if (fos != null) {
                    // 第一参数是图片格式，第二参数是图片质量，第三参数是输出流
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return mFilePath;
        }
        return null;
    }

    // 根据uri获取本地图片的绝对地址
    public static String getAbsolutePath(Context context, Uri uri) {
        if (null == uri) {
            return null;
        }

        String scheme = uri.getScheme();
        String path = null;
        if (scheme == null) {
            path = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            path = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri,
                    new String[]{MediaStore.Images.ImageColumns.DATA}, null,
                    null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor
                            .getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        path = cursor.getString(index);
                    }
                    cursor.close();
                }
            }
        }
        Logger.i(TAG, "picPath = " + path);
        return path;
    }

    // 取得状态栏高度
    public static int getStatusBarHeight(Context context) {
        int statusBarHeight = -1;
        // 获取status_bar_height的资源ID
        int resourceId = context.getResources().getIdentifier(
                "status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            // 根据资源ID获取响应的尺寸值
            statusBarHeight = context.getResources().getDimensionPixelSize(
                    resourceId);
        }
        return statusBarHeight;
    }

    // 计算y轴方法缩放的比例
    public static float getPhotoScale(Activity activity) {
        // 获取屏幕宽和高
        DisplayMetrics outMetrics = new DisplayMetrics();
        WindowManager windowManager = activity.getWindowManager();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);
        int outHeight = outMetrics.heightPixels;

        int statusBarHeight = getStatusBarHeight(activity);
        // 计算y轴方法缩放的比例
        float heightScale = (float) (outHeight - statusBarHeight) / outHeight;
        Logger.i(TAG, "heightScale=" + heightScale);
        return heightScale;
    }

}
