package com.example.android.camera2video;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImgUtils {
    //保存文件到指定路径
    public static boolean saveImageToGallery(Context context, Bitmap bmp) {
        // 首先保存图片
        String storePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "dearxy";
        File appDir = new File(storePath);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            //通过io流的方式来压缩保存图片
            boolean isSuccess = bmp.compress(Bitmap.CompressFormat.JPEG, 60, fos);
            fos.flush();
            fos.close();

            //把文件插入到系统图库
            //MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);

            //保存图片后发送广播通知更新数据库
            Uri uri = Uri.fromFile(file);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            if (isSuccess) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * Bitmap --> byte[]
     *
     * @param bmp
     * @return
     */
    public static byte[] readBitmap(Bitmap bmp)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 60, baos);
        try
        {
            baos.flush();
            baos.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }


    /**
     * 读取图片旋转的角度
     *
     * @param filename
     * @return
     */
    public static int readPictureDegree(String filename)
    {
        int rotate = 0;
        try
        {
            ExifInterface exifInterface = new ExifInterface(filename);
            int result = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            switch (result)
            {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                default:
                    break;
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return rotate;
    }

    /**
     * 旋转图片
     *
     * @param angle
     * @param bitmap
     * @return
     */
    public static Bitmap rotaingImageView(int angle, Bitmap bitmap)
    {
        // 旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);

        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (resizedBitmap != bitmap && bitmap != null && !bitmap.isRecycled())
        {
            bitmap.recycle();
            bitmap = null;
        }

        return resizedBitmap;
    }

    /**
     * 读取图片旋转的角度
     *
     * @param filename
     * @return
     */
    public static void setPictureDegree(String filename, int degree)
    {
        try
        {
            if (degree == 0)
            {
                return;
            }

            int rotate = ExifInterface.ORIENTATION_UNDEFINED;
            switch (degree)
            {
                case 90:
                    rotate = ExifInterface.ORIENTATION_ROTATE_90;
                    break;
                case 180:
                    rotate = ExifInterface.ORIENTATION_ROTATE_180;
                    break;
                case 270:
                    rotate = ExifInterface.ORIENTATION_ROTATE_270;
                    break;
                default:
                    break;
            }

            ExifInterface exifInterface = new ExifInterface(filename);
            exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION,
                    String.valueOf(rotate));
            exifInterface.saveAttributes();

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
