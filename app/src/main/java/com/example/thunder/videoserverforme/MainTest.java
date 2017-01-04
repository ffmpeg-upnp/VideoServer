package com.example.thunder.videoserverforme;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.io.IOException;

/**
 * Created by thunder on 2017/1/4.
 */

public class MainTest extends AppCompatActivity {
    //    private static final String DEFAULT_FILE_PATH  = Environment.getExternalStorageDirectory() + "/movie.mp4";
    private static final String DEFAULT_FILE_PATH  = Environment.getExternalStorageDirectory() + "/42.mp4";
    private static final int VIDEO_WIDTH  = 640;
    private static final int VIDEO_HEIGHT = 480;

    private TextView mTipsTextView;
    private VideoServer mVideoServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        mTipsTextView = (TextView)findViewById(R.id.TipsTextView);
        mVideoServer = new VideoServer(DEFAULT_FILE_PATH, VIDEO_WIDTH, VIDEO_HEIGHT, VideoServer.DEFAULT_SERVER_PORT);
        mTipsTextView.setText("請在瀏覽器上輸入網址:\n\n" + getLocalIpStr(this) + ":" + VideoServer.DEFAULT_SERVER_PORT);
        try {
            mVideoServer.start();
        }
        catch (IOException e) {
            e.printStackTrace();
            mTipsTextView.setText(e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        mVideoServer.stop();
        super.onDestroy();
    }


    private String getLocalIpStr(Context context) {
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return intToIpAddr(wifiInfo.getIpAddress());
    }

    private String intToIpAddr(int ip) {
        return (ip & 0xff) + "." + ((ip>>8)&0xff) + "." + ((ip>>16)&0xff) + "." + ((ip>>24)&0xff);
    }
}
