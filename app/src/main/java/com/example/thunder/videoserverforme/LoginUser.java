package com.example.thunder.videoserverforme;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import tab.list.AttachParameter;
import tab.list.FileContentProvider;
import tab.list.FileContentProvider.*;
import tab.list.FileTab;

/**
 * Created by thunder on 2017/1/4.
 */

public class LoginUser extends AppCompatActivity implements View.OnClickListener {

    //    private static final String DEFAULT_FILE_PATH  = Environment.getExternalStorageDirectory() + "/movie.mp4";
    private static final String DEFAULT_FILE_PATH  = Environment.getExternalStorageDirectory() + "/42.mp4";
    private static final int VIDEO_WIDTH  = 640;
    private static final int VIDEO_HEIGHT = 480;

    private TextView mTipsTextView;
    private VideoServer mVideoServer;
    private Button choice, delete;
    private String attachment = "";
    private ImageView previewImg;
    private String[] form = { UserSchema._FILEPATH, UserSchema._DURATION, UserSchema._FILESIZE, UserSchema._FILENAME, UserSchema._ID };

    public LoginUser(){
        FileContentProvider fileContentProvider = new FileContentProvider();
        fileContentProvider.del_table(Uri.parse("content://tab.list.d2d/file_choice"));
    }

    protected void onCreate(Bundle savedInstanced){
        super.onCreate(savedInstanced);
        setContentView(R.layout.activity_loginuser);

        previewImg = (ImageView)findViewById(R.id.previewimg);
        choice = (Button) findViewById(R.id.choice);
        delete = (Button) findViewById(R.id.delete);
        delete.setOnClickListener(this);
        choice.setOnClickListener(this);
        delete.setVisibility(View.INVISIBLE);
        previewImg.setVisibility(View.INVISIBLE);
        previewImg.setClickable(true);
        previewImg.setOnClickListener(this);

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

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();

        switch (view.getId()){
            case R.id.choice:
                intent.setClass(LoginUser.this, FileTab.class);
                startActivity(intent);

            case R.id.delete:
                // 刪除所選擇的檔案，同時把訊息隱藏起來
                FileContentProvider test = new FileContentProvider();
                test.del_table(Uri.parse("content://tab.list.d2d/file_choice"));
                previewImg.setVisibility(View.INVISIBLE);
//                tvName.setVisibility(View.INVISIBLE);
                delete.setVisibility(View.INVISIBLE);
        }

    }

    public void setPreviewImg() {
        boolean[] checktype = new boolean[AttachParameter.filetype];
        checktype = AttachParameter.checkType(attachment);

        if (checktype[AttachParameter.music]) {
            previewImg.setImageResource(R.drawable.notes);
        } else if (checktype[AttachParameter.video]) {
            Bitmap filebitmap = android.media.ThumbnailUtils.createVideoThumbnail(attachment, MediaStore.Images.Thumbnails.MICRO_KIND);
            // filebitmap=ThumbnailUtils.extractThumbnail(filebitmap,55,60);
            previewImg.setImageBitmap(filebitmap);
        } else if (checktype[AttachParameter.photo]) {
            Bitmap filebitmap = BitmapFactory.decodeFile(attachment);
            previewImg.setImageBitmap(filebitmap);
        } else {
            previewImg.setImageResource(R.drawable.message);
        }

    }

    // 當從attachment回來時，顯示剛剛所選擇的檔案在writepage上。
    public void onResume() {
        super.onResume();
        Cursor ch_tmepfile = getContentResolver().query(Uri.parse("content://tab.list.d2d/file_choice"), form, null, null, null);
        if (ch_tmepfile.getCount() > 0) {
            ch_tmepfile.moveToFirst();
            attachment = ch_tmepfile.getString(0);
            File file = new File(attachment);
//            tvName.setText(file.getName());
            previewImg.setVisibility(View.VISIBLE);
//            tvName.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);
            mTipsTextView = (TextView)findViewById(R.id.TipsTextView);
            mVideoServer = new VideoServer(attachment, VIDEO_WIDTH, VIDEO_HEIGHT, VideoServer.DEFAULT_SERVER_PORT);
            mTipsTextView.setText("請在瀏覽器上輸入網址:\n\n" + getLocalIpStr(this) + ":" + VideoServer.DEFAULT_SERVER_PORT);
            try {
                mVideoServer.start();
            }
            catch (IOException e) {
                e.printStackTrace();
                mTipsTextView.setText(e.getMessage());
            }
            setPreviewImg();
        }
        ch_tmepfile.close();
    }

}
