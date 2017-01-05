package com.example.thunder.videoserverforme;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import net.sbbi.upnp.messages.UPNPResponseException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;


import login.function.User;
import softwareinclude.ro.portforwardandroid.asyncTasks.WebServerPluginInfo;
import softwareinclude.ro.portforwardandroid.asyncTasks.openserver;
import softwareinclude.ro.portforwardandroid.network.UPnPPortMapper;
import softwareinclude.ro.portforwardandroid.util.ApplicationConstants;
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

    private openserver server;
    private String h = "1";
    private Switch control;
    private TextView mTipsTextView;
    private VideoServer mVideoServer;
    private Button choice, delete, close;
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
        close = (Button) findViewById(R.id.close);
        control = (Switch) findViewById(R.id.control);
        control.setChecked(AttachParameter.nat);
        delete.setOnClickListener(this);
        choice.setOnClickListener(this);
        close.setOnClickListener(this);
        close.setVisibility(View.INVISIBLE);
        delete.setVisibility(View.INVISIBLE);
        previewImg.setVisibility(View.INVISIBLE);
        previewImg.setClickable(true);
        previewImg.setOnClickListener(this);
        control.setOnCheckedChangeListener(lisServer);

    }

    public CompoundButton.OnCheckedChangeListener lisServer = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

            if (isChecked){
                LayoutInflater inflater = LayoutInflater.from(LoginUser.this);
                View view = inflater.inflate(R.layout.servicetime, null);
                NumberPicker timePicker = (NumberPicker) view.findViewById(R.id.timePicker);
                timePicker.setMaxValue(24);
                timePicker.setMinValue(1);
                timePicker.setValue(1);
                AlertDialog.Builder dialog = new AlertDialog.Builder(LoginUser.this);
                dialog.setTitle("請選擇手機對外開放時間");
                dialog.setView(view);
                timePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
                        h = String.valueOf(newValue);
                        System.out.println("Now time is h= " + h);
                    }
                });
                dialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (AttachParameter.out_ip.equals("0.0.0.0")){
                            control.setChecked(false);
                            Toast.makeText(getApplicationContext(), "尚未開啟wifi，無法開啟d2d功能", Toast.LENGTH_LONG).show();
                        }else {
                            ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();

                            if (mNetworkInfo.getTypeName().equalsIgnoreCase("MOBILE")){
                                Toast.makeText(getApplicationContext(), "抱歉，行動網路環境不適合開啟D2D功能", Toast.LENGTH_LONG).show();
                                control.setChecked(false);
                            }else {
                                AttachParameter.nat = true;
                                new preServer().execute();
                            }
                        }
                    }
                });

            }
        }
    };

    private class preServer extends AsyncTask<Void, Void, String>{
        private UPnPPortMapper uPnPPortMapper;
        String state = "";
        ProgressDialog sendDialog;

        @Override
        protected void onPreExecute() {
            uPnPPortMapper = new UPnPPortMapper();
            sendDialog = ProgressDialog.show(LoginUser.this,"請稍候", "server開啟中", true);
            sendDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {

            if (uPnPPortMapper != null){
                try {
                    uPnPPortMapper.openRouterPort(AttachParameter.out_ip, AttachParameter.port, AttachParameter.in_ip, AttachParameter.port, ApplicationConstants.ADD_PORT_DESCRIPTION);
                } catch (IOException | UPNPResponseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                int port = AttachParameter.port;
                String host = AttachParameter.in_ip;
                List<File> rootDirs = new ArrayList<File>();
                boolean quiet = false;
                Map<String, String> options = new HashMap<String, String>();

                if (rootDirs.isEmpty()) {
                    rootDirs.add(new File(Environment.getExternalStorageDirectory().getAbsolutePath()));
                }
                options.put("host", host);
                options.put("port", "" + port);
                options.put("quiet", String.valueOf(quiet));
                StringBuilder sb = new StringBuilder();
                for (File dir : rootDirs) {
                    if (sb.length() > 0) {
                        sb.append(":");
                    }
                    try {
                        sb.append(dir.getCanonicalPath());
                    } catch (IOException ignored) {
                    }
                }
                options.put("home", sb.toString());
                ServiceLoader<WebServerPluginInfo> serviceLoader = ServiceLoader.load(WebServerPluginInfo.class);

                for (WebServerPluginInfo info : serviceLoader) {
                    String[] mimeTypes = info.getMimeTypes();
                    for (String mime : mimeTypes) {
                        String[] indexFiles = info.getIndexFilesForMimeType(mime);
                        if (!quiet) {
                            System.out.print("# Found plugin for Mime type: \"" + mime + "\"");
                            if (indexFiles != null) {
                                System.out.print(" (serving index files: ");
                                for (String indexFile : indexFiles) {
                                    System.out.print(indexFile + " ");
                                }
                            }
                            System.out.println(").");
                        }
                    }
                    server = new openserver(host, port, rootDirs, quiet);
                    server.setContent(getContentResolver());
                    try {
                        server.start();
                    }catch (IOException ioe){
                        System.err.println("Couldn't start server:\n" + ioe);
                        System.exit(-1);
                        state="no";
                    }

                }
            }
            return state;
        }
        protected void onPostExecute(String state) {
            sendDialog.dismiss();
            if(state.equalsIgnoreCase("no")){
                AttachParameter.nat=false;
                Toast.makeText(LoginUser.this, "server開啟失敗", Toast.LENGTH_SHORT).show();
            }else if(state.equalsIgnoreCase("time_error")){
                AttachParameter.nat=false;
                new possServer(AttachParameter.out_ip, AttachParameter.port,"開放時間設定失敗,server將自動關閉").execute();
                server.stop();
            }
            else{
                Toast.makeText(LoginUser.this, "server開啟成功", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private class possServer extends AsyncTask<Void, Void, String>{
        String state = "";
        private UPnPPortMapper uPnPPortMapper;
        private String externalIP;
        private int externalPort;
        private String message;
        ProgressDialog sendDialog;

        public possServer(String externalIP, int externalPort, String msg) {
            this.message = msg;
            this.externalIP = externalIP;
            this.externalPort = externalPort;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            uPnPPortMapper = new UPnPPortMapper();
            if(message.equalsIgnoreCase("")){
                sendDialog = ProgressDialog.show(LoginUser.this, "請稍候", "server關閉中", true);

            }else{
                sendDialog = ProgressDialog.show(LoginUser.this, "請稍候", message, true);
            }
            sendDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            if(uPnPPortMapper != null){
                try {
                    User user = new User();
                    String res =user.setservicetime("H=-" + h + "&M=0");
                    user=null;

                    uPnPPortMapper.removePort(externalIP, externalPort);
                    server.stop();
                }
                catch (IOException e) {
                    state="no";
                    e.printStackTrace();
                } catch (UPNPResponseException e) {
                    state="no";
                    e.printStackTrace();
                }
                state="yes";
            }
            return state;
        }

        protected void onPostExecute(String state) {
            sendDialog.dismiss();
            control.setChecked(AttachParameter.nat);
            if(state.equalsIgnoreCase("no")){
                Toast.makeText(LoginUser.this, "server關閉失敗", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(LoginUser.this, "server關閉成功", Toast.LENGTH_SHORT).show();
            }
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

            case R.id.close:

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
