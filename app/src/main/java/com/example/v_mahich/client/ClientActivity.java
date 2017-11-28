package com.example.v_mahich.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ToggleButton;


/* A sample Activity illustrating how to request for more files/feeds
 * to the Sneakernet Service
 *  */
public class ClientActivity extends AppCompatActivity {

    public static final String TAG = "ClientActivity";

    public static final int DONWLOAD_FEED = 1;

    // The two kinds of requests we can send to sneakernet
    public static final int SUBSCRIBE = 1;
    public static final int UNSUBSCRIBE = 2;

    // The result returned from sneakernet for the request made
    public static final int REQUEST_SUCCESSFUL = 1;
    public static final int REQUEST_UNSUCCESSFUL = 2;
    /**
     * Target we publish to Sneakernet to communicate with the
     * ClientActivity using the Incoming Handler     * .
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    // Messenger object which allows IPC with the sneakernet service
    Messenger mService = null;
    // Indicator if the SneakernetService is still bound to the ClientActivity
    boolean mBound;
    // Button to Subscribe/Unsubscribe to feed
    ToggleButton toggleButton;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            Log.i(TAG, "onServiceConnected");
            mService = new Messenger(service);
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;
        }
    };

    /* Method to send a Request to Sneakernet
    * @param feedUrl Provide a unique, immutable name for this course
    * @param path Where the files will be placed
    * @param subscribeMode Whether to Subscribe/Unsubscribe to a feed
    * @param callbackService A service to call when the download has been completed, to display
    *                       notification and update database accordingly
     */
    public void sendRequestToSneakernet(String feedUrl, String path, int subscribeMode, String callbackService) {
        if (!mBound) return;
        Bundle b = new Bundle();
        b.putInt("subscribeMode", subscribeMode);
        b.putSerializable("feedUrl", feedUrl);
        b.putSerializable("destinationPath", path);
        b.putSerializable("callUponSuccess", callbackService);

        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, DONWLOAD_FEED, 0, 0);
        // The Messenger object which allows the Sneakernet Service to communicate with the ClientActivity
        msg.replyTo = mMessenger;
        msg.setData(b);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to the service
        Intent i = new Intent();
        i.setComponent(new ComponentName("com.example.v_mahich.server", "com.example.v_mahich.server.MyService"));
        bindService(i, mConnection,
                Context.BIND_AUTO_CREATE);

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (!mBound) {
                    Log.w(TAG, "Not bound to Sneakernet Service");
                    return;
                }
                String feedUrl = "https://loremipsum.org";
                String path = "/lorem/ipsum/video/course";
                String callbackService = "com.example.v_mahich.client.CalbackService";
                if (isChecked) {
                    sendRequestToSneakernet(feedUrl, path, SUBSCRIBE, callbackService);
                } else {
                    sendRequestToSneakernet(feedUrl, path, UNSUBSCRIBE, callbackService);
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toggleButton = findViewById(R.id.toggleButton);
    }

    /**
     * Handler of incoming messages from SneakernetService.
     * This returns the result of our request to subscribe/unsubscribe to a field.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REQUEST_SUCCESSFUL:
                    Log.i(TAG, "Request completed Successfully.");
                    break;
                case REQUEST_UNSUCCESSFUL:
                    Log.i(TAG, "Request failed.");
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
