package project.praktikum.Wifi;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

public class WifiService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	// TextView mainText;
	WifiManager mainWifi;
	WifiReceiver receiverWifi;
	// List<ScanResult> wifiList;
	// StringBuilder sb = new StringBuilder();
	LocationManager locationManager;

	public void onCreate(Bundle savedInstanceState) {
		Toast.makeText(this, "The new Service was Created", Toast.LENGTH_LONG)
				.show();
		// mainText = (TextView) findViewById(R.id.listwifi);
		mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		receiverWifi = new WifiReceiver();
		registerReceiver(receiverWifi, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		if (!mainWifi.isWifiEnabled()) {
			mainWifi.setWifiEnabled(true);
		}
		mainWifi.startScan();
		Toast.makeText(this, "Starting WiFi Scan...", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Toast.makeText(this, "3", Toast.LENGTH_LONG).show();
		registerReceiver(receiverWifi, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		Toast.makeText(this, " Service Started", Toast.LENGTH_LONG).show();

	}

	@Override
	public void onDestroy() {
		unregisterReceiver(receiverWifi);
		Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();

	}

	class WifiReceiver extends BroadcastReceiver {
		@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
		@SuppressLint("NewApi")
		public void onReceive(Context c, Intent intent) {
			// sb = new StringBuilder();
			// wifiList = mainWifi.getScanResults();
			// for (int i = 0; i < wifiList.size(); i++) {
			// sb.append(new Integer(i + 1).toString() + "- ");
			// sb.append((wifiList.get(i)).toString());
			// sb.append(System.getProperty("line.separator"));
		}
		// mainText.setText(sb);
	}
}
