package com.czm.videocompress.video;

/**
 * Created by caizhiming on 2016/11/8.
 */

public interface VideoCompressListener {
    public void onSuccess(String outputFile, String filename, long duration);
    public void onFail(String reason);
    public void onProgress(int progress);
}
