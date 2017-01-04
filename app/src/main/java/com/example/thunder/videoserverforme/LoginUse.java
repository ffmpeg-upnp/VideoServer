package com.example.thunder.videoserverforme;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.model.types.UDN;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import tab.list.AttachParameter;
import tab.list.FileContentProvider;
import tab.list.FileContentProvider.*;
import unpn.service.BrowserUpnpService;
import unpn.service.SwitchPower;
import unpn.service.UPnPDeviceFinder;

/**
 * Created by thunder on 2016/12/27.
 */
public class LoginUse extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnPreparedListener{

    private Button btnIntent, btnSelect;
    private int PICK_VIDEO_REQUEST=1;
    private MediaController mc;
    private VideoView videoView;
    private TextView tv, tv2;


//    private static final String DEFAULT_FILE_PATH  = Environment.getExternalStorageDirectory() + "/42.mp4";
    private static String DEFAULT_FILE_PATH  = "";
    private static final int VIDEO_WIDTH  = 640;
    private static final int VIDEO_HEIGHT = 480;
    private TextView mTipsTextView;
    private VideoServer mVideoServer;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



        btnSelect = (Button) findViewById(R.id.btnSelect);
        videoView = (VideoView) findViewById(R.id.videoShow);

        mTipsTextView = (TextView)findViewById(R.id.TipsTextView);
        tv = (TextView) findViewById(R.id.tv);
        tv2 = (TextView) findViewById(R.id.tv2);

        mc = new MediaController(this);
        videoView.setOnPreparedListener(this);
//        btnIntent.setOnClickListener(this);
        btnSelect.setOnClickListener(this);


        String[] testForm = { UserSchema._ID, UserSchema._SENDER, UserSchema._TITTLE, UserSchema._CONTENT,
                UserSchema._MESSAGETOKEN, UserSchema._FILESIZE, UserSchema._DATE, UserSchema._FILEPATH,
                UserSchema._RECEIVEID, UserSchema._USESTATUS, UserSchema._FILEID };

        Cursor info_cursor = getContentResolver().query(Uri.parse("content://tab.list.d2d/user_info"), null, null, null, null);
        if(info_cursor.getCount()==0){
            ContentValues values = new ContentValues();
            values.put(FileContentProvider.UserSchema._REMEMBER, "false");
            getContentResolver().insert(Uri.parse("content://tab.list.d2d/user_info"), values);
            values = null;
        }
        info_cursor.close();


    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();

        switch (view.getId()){

            case R.id.btnSelect:
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "選擇影片"), PICK_VIDEO_REQUEST);
        }
    }

    private String getLocalIpStr(Context context) {
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return intToIpAddr(wifiInfo.getIpAddress());
    }

    private String intToIpAddr(int ip) {
        return (ip & 0xff) + "." + ((ip>>8)&0xff) + "." + ((ip>>16)&0xff) + "." + ((ip>>24)&0xff);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode ==PICK_VIDEO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            Uri uri = data.getData();

            if (uri != null){
                //選擇完影片後會在videoView
                videoView.setVideoURI(uri);
                tv.setText(uri.toString());
                tv2.setText(uri.getPath().toString());
                videoView.setVideoURI(uri);
                data.setData(uri);
                File file = new File(uri.toString());

                DEFAULT_FILE_PATH = file.getName();

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

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
//        videoView.start();
    }
}
