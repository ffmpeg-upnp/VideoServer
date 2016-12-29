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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.binding.LocalServiceBindingException;
import org.teleal.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.teleal.cling.model.DefaultServiceManager;
import org.teleal.cling.model.ValidationException;
import org.teleal.cling.model.meta.DeviceDetails;
import org.teleal.cling.model.meta.DeviceIdentity;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.LocalService;
import org.teleal.cling.model.meta.ManufacturerDetails;
import org.teleal.cling.model.meta.ModelDetails;
import org.teleal.cling.model.types.DeviceType;
import org.teleal.cling.model.types.UDADeviceType;
import org.teleal.cling.model.types.UDAServiceType;
import org.teleal.cling.model.types.UDN;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
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

public class LoginUse extends AppCompatActivity implements View.OnClickListener, PropertyChangeListener {

    private AndroidUpnpService upnpService;
    private UDN udn = UDN.uniqueSystemIdentifier("Demo Binary Light");
    private UPnPDeviceFinder mDevfinder  = null;
    private boolean finishUpdateList = false;
    private static final Logger log = Logger.getLogger(LoginUse.class.getName());
    private static final int REQUEST_READ_SMS = 1;
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 2;

    FileContentProvider KM_DB = new FileContentProvider();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Permissions();
        mDevfinder = null;

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        getApplicationContext().bindService(
                new Intent(this, BrowserUpnpService.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );

        File file = new File(AttachParameter.sdcardPath);
        if(!file.exists()){
            file.mkdir();
        }

        // 此為sqlite的DB的部分，新增一個table，存放所有"寄件者的簡訊"
        KM_DB.new_table(Uri.parse("content://tab.list.d2d/user_data"));
        // 紀錄"寄件者"
        KM_DB.new_table(Uri.parse("content://tab.list.d2d/user_group"));
        // 紀錄未上傳檔案
        KM_DB.new_table(Uri.parse("content://tab.list.d2d/user_reply"));
        KM_DB.new_table(Uri.parse("content://tab.list.d2d/user_info"));
        KM_DB.new_table(Uri.parse("content://tab.list.d2d/temp_content"));
        KM_DB.new_table(Uri.parse("content://tab.list.d2d/temp_ffmpeg"));
        KM_DB.new_table(Uri.parse("content://tab.list.d2d/missile_group"));
        KM_DB.new_table(Uri.parse("content://tab.list.d2d/missile_fire"));

        Button btnIntent = (Button) findViewById(R.id.btnIntent);
        btnIntent.setOnClickListener(this);


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
            case R.id.btnIntent:
                intent.setClass(LoginUse.this, MainActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Boolean want_request = false;
        switch(requestCode) {
            case REQUEST_READ_SMS :
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    want_request = true;
                    //使用者拒絕權限，顯示對話框告知
                    new AlertDialog.Builder(this)
                            .setMessage("必須允許權限才能顯示資料")
                            .setPositiveButton("OK", null)
                            .show();
                }
                break;
            case REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    want_request = true;
                    //使用者拒絕權限，顯示對話框告知
                    new AlertDialog.Builder(this)
                            .setMessage("必須允許權限才能顯示資料")
                            .setPositiveButton("OK", null)
                            .show();
                }
                break;
        }
    }

    private void Permissions() {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {Manifest.permission.READ_SMS,
                Manifest.permission.READ_EXTERNAL_STORAGE};
        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            upnpService = (AndroidUpnpService) service;

            LocalService<SwitchPower> switchPowerService = getSwitchPowerService();

            // Register the device when this activity binds to the service for the first time
            if (switchPowerService == null) {
                try {
                    LocalDevice binaryLightDevice = createDevice();

                    Toast.makeText(LoginUse.this, R.string.registering_demo_device, Toast.LENGTH_SHORT).show();
                    upnpService.getRegistry().addDevice(binaryLightDevice);

                    switchPowerService = getSwitchPowerService();

                } catch (Exception ex) {
                    log.log(Level.SEVERE, "Creating demo device failed", ex);
                    Toast.makeText(LoginUse.this, R.string.create_demo_failed, Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Obtain the state of the power switch and update the UI
            setLightbulb(switchPowerService.getManager().getImplementation().getStatus());

            // Start monitoring the power switch
            switchPowerService.getManager().getImplementation().getPropertyChangeSupport()
                    .addPropertyChangeListener(LoginUse.this);

        }

        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
        }
    };

    protected LocalService<SwitchPower> getSwitchPowerService() {
        if (upnpService == null)
            return null;

        LocalDevice binaryLightDevice;
        if ((binaryLightDevice = upnpService.getRegistry().getLocalDevice(udn, true)) == null)
            return null;

        return (LocalService<SwitchPower>)
                binaryLightDevice.findService(new UDAServiceType("MASP", 1));
    }

    protected LocalDevice createDevice() throws ValidationException, LocalServiceBindingException {

        DeviceType type =
                new UDADeviceType("BinaryLight", 1);

        DeviceDetails details =
                new DeviceDetails(
                        "MASP",
                        new ManufacturerDetails("ACME"),
                        new ModelDetails("1705A_MASP", "TEST UPNP", "v1")
                );

        LocalService service =
                new AnnotationLocalServiceBinder().read(SwitchPower.class);

        service.setManager(
                new DefaultServiceManager<SwitchPower>(service, SwitchPower.class)
        );

        return new LocalDevice(
                new DeviceIdentity(udn),
                type,
                details,
                service
        );
    }//end LocalDevice

    protected void setLightbulb(final boolean on) {
        runOnUiThread(new Runnable() {
            public void run() {
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals("status")) {
            log.info("Turning light: " + event.getNewValue());
            setLightbulb((Boolean) event.getNewValue());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AttachParameter.selfId="";
        finishUpdateList = true;

        LocalService<SwitchPower> switchPowerService = getSwitchPowerService();
        if (switchPowerService != null)
            switchPowerService.getManager().getImplementation().getPropertyChangeSupport()
                    .removePropertyChangeListener(this);
        getApplicationContext().unbindService(serviceConnection);

    }

}
