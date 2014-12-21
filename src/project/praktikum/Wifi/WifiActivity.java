package project.praktikum.Wifi;

import java.util.List;

import project.praktikum.activity.recognition.R;
import android.support.v7.app.ActionBarActivity;
import android.telephony.CellIdentityGsm;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.TelephonyManager;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
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
import android.view.Menu;
import android.widget.TextView;

public class WifiActivity extends ActionBarActivity {

	TextView mainText;
	WifiManager mainWifi;
	WifiReceiver receiverWifi;
	List<ScanResult> wifiList;
	StringBuilder sb = new StringBuilder();
	LocationManager locationManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wifi);

		mainText = (TextView) findViewById(R.id.listwifi);
		mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		receiverWifi = new WifiReceiver();
		registerReceiver(receiverWifi, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		if (!mainWifi.isWifiEnabled()) {
			mainWifi.setWifiEnabled(true);
		}
		mainWifi.startScan();
		mainText.setText("Starting WiFi Scan...");
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "Refresh");
		return super.onCreateOptionsMenu(menu);
	}

	/*
	 * public boolean onMenuItemSelected(int featureId, MenuItem item) {
	 * mainWifi.startScan(); mainText.setText("Starting Scan"); return
	 * super.onMenuItemSelected(featureId, item); }
	 */

	protected void onPause() {
		unregisterReceiver(receiverWifi);
		super.onPause();
	}

	protected void onResume() {
		registerReceiver(receiverWifi, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		super.onResume();
	}

	class WifiReceiver extends BroadcastReceiver {
		@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
		@SuppressLint("NewApi")
		public void onReceive(Context c, Intent intent) {
			sb = new StringBuilder();
			wifiList = mainWifi.getScanResults();
			for (int i = 0; i < wifiList.size(); i++) {
				sb.append(new Integer(i + 1).toString() + "- ");
				sb.append((wifiList.get(i)).toString());
				sb.append(System.getProperty("line.separator"));
			}

			// ---gps

/*			Criteria criteria = null;
			String bestProvider = locationManager.getBestProvider(criteria,
					false);

			locationManager.requestLocationUpdates(bestProvider, 10L, 10F,
					(LocationListener) this);
			if (locationManager != null) {

				Location location;
				double latitude;
				double longitude;
				location = locationManager.getLastKnownLocation(bestProvider);

				if (location != null) {
					latitude = location.getLatitude();
					longitude = location.getLongitude();

					sb.append("Lat: " + String.valueOf(latitude) + " Long: "
							+ String.valueOf(longitude));
					sb.append(System.getProperty("line.separator"));
				}
			}*/
			mainText.setText(sb);
		}
	}
}
