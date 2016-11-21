package com.czm.videocompress.util;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;

/**
 * Created by caizhiming on 2016/11/10.
 */

public class VideoUtil {

    /**
     * 获取视频缩略图
     * @param videoPath
     * @return
     */
    public static String getVideoCover(String videoPath) {
        Bitmap bmpCover = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Images.Thumbnails.MINI_KIND);
        String strCover = AppUtil.getAppDir()+"/video_cover.jpg";
        if(bmp2File(bmpCover,strCover)){
            File file = new File(strCover);
            if(file.exists()){
                return strCover;
            }else{
                return null;
            }
        }else{
            return null;
        }
    }

    /**
     * convert Bitmap to File
     *
     * @param bmp
     * @param file
     * @return
     */
    public static boolean bmp2File(Bitmap bmp, String file) {
        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        int quality = 100;
        OutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return bmp.compress(format, quality, stream);
    }

    /**
     * 获取视频时长
     * @param mUri
     */
    public static int getVideoDuration(String mUri)
    {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            mmr.setDataSource(mUri);

            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);//时长(毫秒)
            return (Integer.parseInt(duration)/1000);

        } catch (Exception ex){
            Log.e("TAG", "MediaMetadataRetriever exception " + ex);
        } finally {
            mmr.release();
        }
        return 0;

    }

    /**
     * 获取单个文件MD5
     * @param file
     * @return
     */
    public static String getFileMD5(File file)
    {
        // TODO Auto-generated method stub
        if(!file.isFile())
        {
            return null;
        }
        MessageDigest digest=null;
        FileInputStream in=null;
        byte buffer[]=new byte[1024];
        int len;
        try
        {
            digest= MessageDigest.getInstance("MD5");
            in=new FileInputStream(file);
            while((len=in.read(buffer, 0, 1024))!=-1)
            {
                digest.update(buffer, 0, len);
            }
            in.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }

        BigInteger bigint=new BigInteger(1,digest.digest());
        return bigint.toString(16);
    }
    private Bitmap createVideoThumbnail(String url, int width, int height) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        int kind = MediaStore.Video.Thumbnails.MINI_KIND;
        try {
            if (Build.VERSION.SDK_INT >= 14) {
                retriever.setDataSource(url, new HashMap<String, String>());
            } else {
                retriever.setDataSource(url);
            }
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }
        if (kind == MediaStore.Images.Thumbnails.MICRO_KIND && bitmap != null) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
        return bitmap;
    }
}
