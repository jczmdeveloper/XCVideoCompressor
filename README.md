# XCVideoCompressor
A Video compressor which uses ffmpeg lib for Android App- Android 视频压缩器
The Example for Use：


        String sdcardDir = Environment.getExternalStorageDirectory().getAbsolutePath();

        String mInputStr = sdcardDir+"/DCIM/Camera/21s.mp4";
        mStrResult = "Compress begin=========\n";
        mResult.setText(mStrResult);
        VideoCompressor.compress(this, mInputStr, new VideoCompressListener() {
            @Override
            public void onSuccess(final String outputFile, String filename, long duration) {
                Worker.postMain(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext,"video compress success:"+outputFile,Toast.LENGTH_SHORT).show();
                        SGLog.e("video compress success:"+outputFile);
                        mStrResult +="Compress end=========onSuccess\n";
                        mResult.setText(mStrResult);
                        mScrollView.fullScroll(View.FOCUS_DOWN);

                    }
                });
            }

            @Override
            public void onFail(final String reason) {
                Worker.postMain(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext,"video compress failed:"+reason,Toast.LENGTH_SHORT).show();
                        SGLog.e("video compress failed:"+reason);
                        mStrResult+="Compress end=========onFail\n";
                        mResult.setText(mStrResult);
                        mScrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }

            @Override
            public void onProgress(final int progress) {
                Worker.postMain(new Runnable() {
                    @Override
                    public void run() {
                        SGLog.e("video compress progress:"+progress);
                        mStrResult += "Compress progress:"+progress +"%\n";
                        mResult.setText(mStrResult);
                        mScrollView.fullScroll(View.FOCUS_DOWN);

                    }
                });
            }
        });
