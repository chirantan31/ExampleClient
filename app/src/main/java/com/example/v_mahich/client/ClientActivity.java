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
import android.widget.Toast;

public class ClientActivity extends AppCompatActivity {

    public static final String TAG = "ClientActivity";

    public static final int DONWLOAD_FEED = 1;

    public static final int SUBSCRIBE = 1;
    public static final int UNSUBSCRIBE = 2;

    Messenger mService = null;
    boolean mBound;

    /**
     * Handler of incoming messages from Sneakernet.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Toast.makeText(getApplicationContext(), "Client Hello!", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Target we publish to Sneakernet to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

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
            // Provide a unique, immutable name for this course
            // type of feedUrl: String
            // required
            String feedUrl = "https://loremipsum.org";
            // Where the files will be placed
            // type of path: String
            // optional. default: "Downloads" directory
            String path = "/lorem/ipsum/video/course";
            // A service to call when the download has been completed, to display notification and update database accordingly
            String callbackService = "com.example.v_mahich.client.CallbackService";
            requestFeedForDownload(feedUrl, path, callbackService);
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;
        }
    };

    public void requestFeedForDownload(String feedUrl, String path, String callbackService) {
        if (!mBound) return;
        Bundle b = new Bundle();
        b.putInt("subscribeMode", SUBSCRIBE);

        b.putSerializable("feedUrl", feedUrl);
        b.putSerializable("destinationPath", path);
        b.putSerializable("callUponSuccess", callbackService);

        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, DONWLOAD_FEED, 0, 0);
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
    }
}
