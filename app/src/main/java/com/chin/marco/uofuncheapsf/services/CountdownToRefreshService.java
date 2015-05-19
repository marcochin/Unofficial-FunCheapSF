package com.chin.marco.uofuncheapsf.services;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Marco on 3/29/2015.
 */
public class CountdownToRefreshService extends Service {
    private final int SERVICE_WAIT_TIME = 120000;
    private Thread mCountdownThread;

    public CountdownToRefreshService(){
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // starts a sleep of 2 minutes, then kills itself
        // MainActivity will check if service is running.
        // If it is running, tables wont be deleted. This is to prevent frequent/annoying loading.
        Log.d("poo", "service is started");
        mCountdownThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(SERVICE_WAIT_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
                Log.d("poo", "service is stopped");
                stopSelf();
            }
        });
        mCountdownThread.start();

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d("poo", "onDestroy Service");
        mCountdownThread.interrupt();
        super.onDestroy();
    }
}

