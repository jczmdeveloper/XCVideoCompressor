package com.czm.videocompress.demo;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.czm.videocompress.R;
import com.czm.videocompress.util.SGLog;
import com.czm.videocompress.util.Worker;
import com.czm.videocompress.video.VideoCompressListener;
import com.czm.videocompress.video.VideoCompressor;

public class MainActivity extends AppCompatActivity {

    private Context mContext;
    private TextView mResult;
    private String mStrResult;
    private ScrollView mScrollView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);
        mResult = (TextView) findViewById(R.id.tv_result);
        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        findViewById(R.id.btn_compress).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compressVideo();
            }

        });
    }

    private void compressVideo() {
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
    }
}
