package project.praktikum.activity.recognition;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import project.praktikum.activity.recognition.ActivityRecognitionService;
import project.praktikum.activity.recognition.MainActivity;
import project.praktikum.database.DataBase;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class ActivityCaptureService extends Service implements GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener{
	/** indicates how to behave if the service is killed */
	   int mStartMode;
	   /** interface for clients that bind */
	   IBinder mBinder;     
	   /** indicates whether onRebind should be used */
	   boolean mAllowRebind;

		private PendingIntent pIntent;
		private BroadcastReceiver receiver;
		private ActivityRecognitionClient arclient;
		private DataBase db;
		ArrayList<Messenger> mClients = new ArrayList<Messenger>();
		static final int MSG_REGISTER_CLIENT = 1;
	    static final int MSG_UNREGISTER_CLIENT = 2;
	    static final int MSG_SET_INT_VALUE = 3;
	    static boolean isRunning = false;
	    private static NotificationManager nm;
		
		final Messenger mMessenger = new Messenger(new IncomingHandler());
		
		@SuppressLint("HandlerLeak")
		class IncomingHandler extends Handler { // Handler of incoming messages from clients.
	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	            case MSG_REGISTER_CLIENT:
	                mClients.add(msg.replyTo);
	                break;
	            case MSG_UNREGISTER_CLIENT:
	                mClients.remove(msg.replyTo);
	                break;
	            default:
	                super.handleMessage(msg);
	            }
	        }
	    }
		
		private void sendMessageToUI(int intvaluetosend) {
	        for (int i=mClients.size()-1; i>=0; i--) {
	            try {
	                // Send data as an Integer
	                mClients.get(i).send(Message.obtain(null, MSG_SET_INT_VALUE, intvaluetosend, 0));
	            }
	            catch (RemoteException e) {
	                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
	                mClients.remove(i);
	            }
	        }
	    }
		
	   /** Called when the service is being created. */
	   @Override
	   public void onCreate() {  
			db = new DataBase(getApplicationContext());
			isRunning = true;
		    showNotification();
	   }
	   
	   @SuppressWarnings("deprecation")
	private void showNotification() {
	        nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
	        // In this sample, we'll use the same text for the ticker and the expanded notification
	        CharSequence text = "Detecting your activity";
	        // Set the icon, scrolling text and timestamp
	        Notification notification = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());
	        // The PendingIntent to launch our activity if the user selects this notification
	        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
	        // Set the info for the views that show in the notification panel.
	        notification.setLatestEventInfo(this, ":D", text, contentIntent);
	        // Send the notification.
	        // We use a layout id because it is a unique number.  We use it later to cancel.
	        nm.notify(R.string.app_name, notification);
	    }

	   /** The service is starting, due to a call to startService() */
	   @Override
	   public int onStartCommand(Intent intent, int flags, int startId) {
		   int resp = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
			
			if(resp == ConnectionResult.SUCCESS)
			{
				arclient = new ActivityRecognitionClient(this, this, this);
				arclient.connect(); 
			}
			receiver = new BroadcastReceiver() 
			{
				@Override
				public void onReceive(Context context, Intent intent) 
				{
					insert(intent.getExtras().getInt("Unknown"),
							intent.getExtras().getInt("In Vehicle"),
							intent.getExtras().getInt("On Bicycle"),
							intent.getExtras().getInt("On Foot"),
							intent.getExtras().getInt("Still"),
							intent.getExtras().getInt("Tilting"));
					sendMessageToUI(1);
				}
			};
			    
			IntentFilter filter = new IntentFilter();
			filter.addAction("project.praktikum.recognition.ACTIVITY_RECOGNITION_DATA");
			registerReceiver(receiver, filter);
			return mStartMode;
	   }
	   
	@SuppressLint("SimpleDateFormat")
	private void insert(int unknown, int in_vehicle, int on_bicycle, int on_foot, int still, int tilting)
	   {
		   Calendar c = Calendar.getInstance();
		   SimpleDateFormat df = new SimpleDateFormat("MM/dd   HH:mm:ss");
		   String date = df.format(c.getTime());
		   db.insertRecord(unknown,in_vehicle,on_bicycle,on_foot,still,tilting,date);
	   }

	   /** Called when The service is no longer used and is being destroyed */
	   @Override
	   public void onDestroy() 
	   {
		   if(arclient!=null)
			{
				arclient.removeActivityUpdates(pIntent);
				arclient.disconnect();
			}
			unregisterReceiver(receiver);
			cancelNotification(R.string.app_name);
			isRunning = false;
	   }
	   
	   public static void cancelNotification(int notifyId) {
		    nm.cancel(notifyId);
		}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle arg0) 
	{
		// TODO Auto-generated method stub
 		Intent intent = new Intent(getApplicationContext(), ActivityRecognitionService.class);
 		pIntent = PendingIntent.getService(getApplicationContext(), 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
 		arclient.requestActivityUpdates(1000, pIntent);
	}

	@Override
	public void onDisconnected() 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public IBinder onBind(Intent intent) 
	{
		// TODO Auto-generated method stub
		return mMessenger.getBinder();
	}
}