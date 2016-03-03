package com.samratinfosys.myecollege;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.samratinfosys.myecollege.json_classes.MyJSON;
import com.samratinfosys.myecollege.services.MyeCollegeService;
import com.samratinfosys.myecollege.tools.DownloadManager;
import com.samratinfosys.myecollege.tools.JSONDownloader;
import com.samratinfosys.myecollege.utils.Anim;
import com.samratinfosys.myecollege.utils.Helper;


public class Boot extends ActionBarActivity implements DownloadManager.DownloadStatus {

    private ImageView imgLogoIcon;
    private TextView txtLoader;
    private Context context;

    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;

    private final int DOWNLOAD_NETWORK_PARAMETERS=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        setContentView(R.layout.activity_boot);

        try {
            // create data folder or initilize data folder
            if(!Helper.validateDataFolder(this)) {
                Toast.makeText(this,"Unable to create data folder.",Toast.LENGTH_SHORT).show();
            }

        } catch (Exception ex) {}

        try {
            // initialize DeviceID constant
            Helper.getDeviceId(this);
        } catch (Exception ex) {}

        try {
            // initialize Database
            Helper.getMainDatabase(this);

        } catch (Exception ex) {

        }

        // start background updater if not running
        startBackgroundService();

        initComponents();
        startUp();
        initNetworkParameters();
    }

    private void startBackgroundService() {

        killBackgroundService();

        startService(new Intent(this, MyeCollegeService.class));

        /*Intent alarmIntent = new Intent(this, DataUpdater.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);

        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        int interval = 5000;

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);*/
    }

    private void killBackgroundService() {
        try {
            stopService(new Intent(this, MyeCollegeService.class));
        } catch(Exception ex) {}
        /*Intent alarmIntent = new Intent(this, DataUpdater.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);*/
    }

    private void initNetworkParameters() {
        if(Helper.isNetworkConnected(this)) {
            JSONDownloader.GetNetworkParameters(DOWNLOAD_NETWORK_PARAMETERS,this,this);
        } else {
            afterUpdatingNetworkParameters();
        }
    }

    private void initComponents() {
        imgLogoIcon=(ImageView)findViewById(R.id.imgLogoIcon);
        txtLoader=(TextView)findViewById(R.id.txtLoader);
    }

    private void startUp() {
        Anim.bootAnimation(imgLogoIcon);
    }

    private void initialWork() {
        // check if app is have its database in device
        // if not than prepare first attempt process if any...
        // if yes than continue
        // check if user already login or not...
        // if yes than show the college-panel
        // if not than show college panel with college list and login option

        if(Helper.validateDataFolder(this)) {
            launchCollegePanel(1000);
        } else {
            // something to do when data-directory not available
            Toast.makeText(this,"Directory problem!!",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void DownloadStarted(int downloadId) {
        if(downloadId==DOWNLOAD_NETWORK_PARAMETERS) {
            txtLoader.setText("Getting things ready...");
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Anim.bootAnimation(imgLogoIcon);
            }
        });
    }

    @Override
    public void DownloadFailed(final int downloadId, final Exception ex, final int httpResponseCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(downloadId==DOWNLOAD_NETWORK_PARAMETERS) {
                    // nothing to appear
                    afterUpdatingNetworkParameters();
                }
            }
        });
    }

    private void afterUpdatingNetworkParameters() {
        initialWork();
    }

    @Override
    public void DownloadProgress(int downloadId,long percentage, long downloadedBytes, long totalBytes) {
        //txtLoader.setText(percentage+"%");
    }

    @Override
    public void DownloadCompleted(int downloadId,byte[] data, String filepath, boolean isCancelled) {
        if(downloadId==DOWNLOAD_NETWORK_PARAMETERS && data!=null && !isCancelled) {
            MyJSON json=new MyJSON(new String(data));
            if(json.success) {
                Helper.NETWORK_CONNECT_TIMEOUT=json.getInt("network_connect_timeout",Helper.NETWORK_CONNECT_TIMEOUT);
                Helper.NETWORK_READ_BUFFER=json.getInt("network_read_buffer",Helper.NETWORK_READ_BUFFER);
                Helper.NETWORK_READ_TIMEOUT=json.getInt("network_read_timeout",Helper.NETWORK_READ_TIMEOUT);
            }
            afterUpdatingNetworkParameters();
        }
    }

    private void launchCollegePanel(final long milis) {
        txtLoader.setText("Just a moment...");
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(milis);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imgLogoIcon.clearAnimation();
                            Helper.ActivityNavigators.showCollegeSelectionActivity(context, true);
                            finish();
                        }
                    });
                } catch (Exception e) {}
            }
        });
        thread.start();
    }
}
