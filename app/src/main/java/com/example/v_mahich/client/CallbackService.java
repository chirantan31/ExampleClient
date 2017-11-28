package com.example.v_mahich.client;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/* This Service is called when a download has been completed by the Sneakernet Platform */
public class CallbackService extends IntentService {
    public static final String TAG = "CallbackService";

    public CallbackService() {
        super("CallbackService");
    }

    /* The Client App can extract details about the files downloaded and issue its database
    * and/or notify the user accordingly
    * */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Log.i(TAG , "onHandleIntent");
        }
        // Bundle with details about the files downloaded
        if (intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
        }

        // TODO: Update database to reflect the newly downloaded files

        // Create a notification to alert the user of the files downloaded
        Notification.Builder mBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle("Download complete!")
                        .setContentText("Click here to access new files!");

        // Sets an ID for the notification
        int mNotificationId = 1;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

        // Terminate Service after doing the necessary tasks
        stopSelf();
    }
}
