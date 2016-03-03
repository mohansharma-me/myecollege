package com.samratinfosys.myecollege.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import com.samratinfosys.myecollege.tools.loader.JSONLoader;

import java.util.Timer;
import java.util.TimerTask;

public class MyeCollegeService extends Service {

    private Context context=null;
    private Handler handler=null;

    private static long UPDATE_INTERVAL = 1*5*1000;  //default

    private static Timer timer = new Timer();

    public MyeCollegeService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context=this;
        handler=new Handler(getMainLooper());
        return START_STICKY;
    }

    private void _startService()
    {
        try {

            timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {

                    doServiceWork();

                }
            }, 1000, UPDATE_INTERVAL);

        } catch(Exception ex) {}
    }

    private void doServiceWork()
    {
        //do something wotever you want
        //like reading file or getting data from network
        try {

            

            handler.post(new Runnable() {
                @Override
                public void run() {

                }
            });
        } catch (Exception e) {
        }

    }

    private void _shutdownService()
    {
        if (timer != null) timer.cancel();
    }

    @Override
    public void onCreate() {
        Toast.makeText(this,"Starting service",Toast.LENGTH_SHORT).show();
        _startService();
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this,"Destroying service",Toast.LENGTH_SHORT).show();
        _shutdownService();
    }
}
