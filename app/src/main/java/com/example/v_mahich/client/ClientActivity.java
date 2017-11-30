package com.example.v_mahich.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
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


/**
 * Communication Flow between ClientActivity and FileFetcherService:
 *
 * 1. Client sends request to FileFetcherService to download bundle
 * using mFileFetcherServiceMessenger
 * 2. FileFetcherService starts up, checks if EULA is signed
 * 2a. If not signed, FileFetcherService requests ClientActivity to start PermissionsActivity
 * 2b. If signed, takes note of the requested download bundle and calls stopSelf(),
 * sends errors through mMessenger object if any
 * 3. FileFetcherService receives the bundle of files from a hub or a peer Calls startService on the
 * passed callbackService
 * 4.CallbackService is started, which acts on the new bundle of files.
 */
public class ClientActivity extends AppCompatActivity {

  public static final String TAG = "ClientActivity";

  public static final int DOWNLOAD_FEED = 1;

  // We can either request to download a new bundle of files, or cancel an existing request to download a bundle of files
  public static final int SUBSCRIBE = 1;
  public static final int UNSUBSCRIBE = 2;

  // The result returned from FileFetcherService for the request made
  public static final int REQUEST_SUCCESSFUL = 1;
  public static final int REQUEST_UNSUCCESSFUL = 2;
  public static final int REQUEST_PERMISSIONS_REQUIRED = 3;
  /**
   * Messenger object we pass to FileFetcherService to communicate with the
   * ClientActivity using the Incoming Handler
   */
  final Messenger mMessenger = new Messenger(new IncomingHandler());
  // Messenger object which allows IPC with the FileFetcherService
  Messenger mFileFetcherServiceMessenger = null;
  // Indicator if the FileFetcherService is still bound to the ClientActivity
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
      mFileFetcherServiceMessenger = new Messenger(service);
      mBound = true;
    }

    public void onServiceDisconnected(ComponentName className) {
      // This is called when the connection with the FileFetcherService has been
      // unexpectedly disconnected -- that is, its process crashed.
      mFileFetcherServiceMessenger = null;
      mBound = false;
    }
  };

  /**
   * Method to send a Request to FileFetcherService
   *
   * @param feedUrl Provide a unique, immutable name for this course
   * @param path Where the files will be placed
   * @param subscribeMode Whether to Subscribe/Unsubscribe to a feed
   * @param callbackService A service to call when the download has been completed, to display
   * notification and update database accordingly
   */
  public void sendRequestToFileFetcherService(String feedUrl, Uri path, int subscribeMode,
      ComponentName callbackService) {
    if (!mBound) {
      return;
    }
    Bundle b = new Bundle();
    b.putInt("subscribeMode", subscribeMode);
    b.putSerializable("feedUrl", feedUrl);
    b.putParcelable("destinationPath", path);
    b.putParcelable("callUponSuccess", callbackService);

    // Create and send a message to the FileFetcherService, using a supported 'what' value
    Message msg = Message.obtain(null, DOWNLOAD_FEED, 0, 0);
    // The Messenger object which allows the FileFetcherService to communicate with the ClientActivity
    msg.replyTo = mMessenger;
    msg.setData(b);
    try {
      mFileFetcherServiceMessenger.send(msg);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    // Bind to the service
   /* Intent i = new Intent();
    i.setComponent(
        new ComponentName("com.example.v_mahich.server", "com.example.v_mahich.server.MyService"));
    bindService(i, mConnection,
        Context.BIND_AUTO_CREATE);*/



    toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (!mBound) {
          Log.w(TAG, "Not bound to FileFetcherService");
          return;
        }
        String feedUrl = "https://loremipsum.org";
        Uri path = Uri.parse("/lorem/ipsum/video/course");
        ComponentName callbackService = new ComponentName("com.example.v_mahich.client",
            "com.example.v_mahich.client.CallbackService");
        if (isChecked) {
          sendRequestToFileFetcherService(feedUrl, path, SUBSCRIBE, callbackService);
        } else {
          sendRequestToFileFetcherService(feedUrl, path, UNSUBSCRIBE, callbackService);
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
   * Handler of incoming messages from FileFetcherService.
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
          Log.i(TAG, "Request failed:" + msg.getData().getString("errorMsg"));
          break;
        case REQUEST_PERMISSIONS_REQUIRED:
          Intent i = new Intent();
          i.setComponent(
              new ComponentName("com.msr.mediafeed", "com.msr.mediafeed.activities.PermissionsActivity"));
          startActivity(i);
          break;
        default:
          super.handleMessage(msg);
      }
    }
  }
}
