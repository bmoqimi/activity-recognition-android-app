package project.praktikum.activity.recognition;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import project.praktikum.activity.recognition.R;
import project.praktikum.activity.reduction.NoiseReduction;
import project.praktikum.database.DataBase;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.ActivityRecognitionClient;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.text.TextUtils;
//import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class FingerprintingService extends Service implements GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener, LocationListener{
	/** indicates how to behave if the service is killed */
	int mStartMode;
	/** interface for clients that bind */
	IBinder mBinder;     
	/** indicates whether onRebind should be used */
	boolean mAllowRebind;
	private String TAG = "FingerprintingService";
	private DataBase db;
	ArrayList<Messenger> mClients = new ArrayList<Messenger>();
	static final int MSG_FINGERPRINTING_REGISTER_CLIENT = 1;
	static final int MSG_FINGERPRINTING_UNREGISTER_CLIENT = 2;
	static final int MSG_SET_INT_VALUE = 3;
	static boolean isRunning = false;
	private static NotificationManager nm;
	
	// TextView mainText;
	WifiManager mainWifi;
	WifiReceiver receiverWifi;
	 List<ScanResult> wifiList;
	// StringBuilder sb = new StringBuilder();
	LocationManager locationManager;
	
	private LocationManager CellTowerlocationManager;
	private String provider;
	double latitude;
	double longitude;

	final Messenger mMessenger = new Messenger(new IncomingHandler());

	@SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler { // Handler of incoming messages from clients.
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_FINGERPRINTING_REGISTER_CLIENT:
				mClients.add(msg.replyTo);
				break;
			case MSG_FINGERPRINTING_UNREGISTER_CLIENT:
				mClients.remove(msg.replyTo);
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	
//	private void sendMessageToUI(int intvaluetosend) {
//		for (int i=mClients.size()-1; i>=0; i--) {
//			try {
//				// Send data as an Integer
//				mClients.get(i).send(Message.obtain(null, MSG_SET_INT_VALUE, intvaluetosend, 0));
//			}
//			catch (RemoteException e) {
//				// The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
//				mClients.remove(i);
//			}
//		}
//	}

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
		CharSequence text = "FingerprintingService is running...";
		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.digital_fingerpint, text, System.currentTimeMillis());
		// The PendingIntent to launch our activity if the user selects this notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, "Activity Recognition", text, contentIntent);
		// Send the notification.
		// We use a layout id because it is a unique number.  We use it later to cancel.
		nm.notify(R.string.app_name, notification);
	}

	/** The service is starting, due to a call to startService() */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		receiverWifi = new WifiReceiver();
		registerReceiver(receiverWifi, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		if (!mainWifi.isWifiEnabled()) {
			mainWifi.setWifiEnabled(true);
		}
		mainWifi.startScan();
		Toast.makeText(this, "Starting fingerprinting service...", Toast.LENGTH_LONG).show();
		
		registerReceiver(receiverWifi, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

		
		CellTowerlocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		Criteria criteria = new Criteria();
		provider = CellTowerlocationManager.getBestProvider(criteria, false);
		Location location = CellTowerlocationManager.getLastKnownLocation(provider);

		// Initialize the location fields
		if (location != null) {
			//System.out.println("Provider " + provider + " has been selected.");
			onLocationChanged(location);
		} else {
			//mainText.setText("Location not available");
		}
	
		Toast.makeText(this, "Fingerprinting service started.", Toast.LENGTH_LONG).show();
		
		return mStartMode;
	}

	/** Called when The service is no longer used and is being destroyed */
	@Override
	public void onDestroy() 
	{
		unregisterReceiver(receiverWifi);
		CellTowerlocationManager.removeUpdates(this);
		Toast.makeText(this, "Fingerprinting service stopped.", Toast.LENGTH_LONG).show();
		//Toast.makeText(this, db.fetchfingerprintsCount(), Toast.LENGTH_LONG).show();

		cancelNotification(R.string.app_name);
		isRunning = false;
	}

	class WifiReceiver extends BroadcastReceiver {
		@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
		@SuppressLint("NewApi")
		public void onReceive(Context c, Intent intent) {
			// sb = new StringBuilder();
			 wifiList = mainWifi.getScanResults();
			 String df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			 for (int i = 0; i < wifiList.size(); i++) {
				 final ScanResult scanResult=wifiList.get(i);
			      if (scanResult == null) {
			        continue;
			      }
			      if (TextUtils.isEmpty(scanResult.SSID)) {
			        continue;
			      }
			      db.insertFingerprintRecord("wifi", scanResult.BSSID, scanResult.SSID, scanResult.capabilities, scanResult.level, 0, 0, df);
			 }
			 db.insertFingerprintRecord("location", "", "", "", 0, (int)latitude, (int)longitude, df);
//			insert(intent.getExtras().getString("fingerprints"));
//			sendMessageToUI(1);
					
		}
		// mainText.setText(sb);
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
		CellTowerlocationManager.requestLocationUpdates(provider, 400, 1, this);
		
		//Intent intent = new Intent(getApplicationContext(), FingerprintingService.class);
		//pIntent = PendingIntent.getService(getApplicationContext(), 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
		//arclient.requestActivityUpdates(300000, pIntent);
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

	@Override
	public void onLocationChanged(Location location) {
	    latitude = (double) (location.getLatitude());
		longitude = (double) (location.getLongitude());
		//mainText.setText(String.valueOf(lat) + " , " + String.valueOf(lng));
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "Enabled new provider " + provider,
				Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this, "Disabled provider " + provider,
				Toast.LENGTH_SHORT).show();
	}
}
