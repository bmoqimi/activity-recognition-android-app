package project.praktikum.activity.recognition;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import project.praktikum.activity.recognition.R;
import project.praktikum.database.DataBase;
import android.support.v7.app.ActionBarActivity;
import android.telephony.CellIdentityGsm;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.widget.Toast;

public class SetHome extends Activity implements LocationListener {

	TextView mainText;
	WifiManager mainWifi;
	WifiReceiver receiverWifi;
	List<ScanResult> wifiList;
	StringBuilder sb = new StringBuilder();
	LocationManager locationManager;
	private String TAG = "SetHome";
	private DataBase db;

	private LocationManager CellTowerlocationManager;
	private String provider;
	double latitude;
	double longitude;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wifi);

		Log.i(TAG, "starting wifi scan...");

		db = db.getInstance(getApplicationContext());
		

//		Toast.makeText(this, String.valueOf(isHomeSet()), Toast.LENGTH_LONG).show();

		mainText = (TextView) findViewById(R.id.listwifi);
		mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		receiverWifi = new WifiReceiver();
		registerReceiver(receiverWifi, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		if (!mainWifi.isWifiEnabled()) {
			mainWifi.setWifiEnabled(true);
		}
		mainWifi.startScan();
		Log.i(TAG, "wifi scan started.");
		
		
		CellTowerlocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		Criteria criteria = new Criteria();
		provider = CellTowerlocationManager.getBestProvider(criteria, false);
		Location location = CellTowerlocationManager
				.getLastKnownLocation(provider);

		// Initialize the location fields
		if (location != null) {
			// System.out.println("Provider " + provider +
			// " has been selected.");
			onLocationChanged(location);
		} else {
			// mainText.setText("Location not available");
		}
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

	@Override
	public void onLocationChanged(Location location) {
		Log.i(TAG, "onLocationChanged Called.");
		latitude = (double) (location.getLatitude());
		longitude = (double) (location.getLongitude());
		// mainText.setText(String.valueOf(lat) + " , " + String.valueOf(lng));
	}

	class WifiReceiver extends BroadcastReceiver {
		@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
		@SuppressLint("NewApi")
		public void onReceive(Context c, Intent intent) {
			Log.i(TAG, "BroadcastReceiver Called.");
			db.deleteallrowsfingerprintshome();
			
			wifiList = mainWifi.getScanResults();
			String df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(new Date());

			Log.i(TAG, "saving fingerprint to db.");
			for (int i = 0; i < wifiList.size(); i++) {
				final ScanResult scanResult = wifiList.get(i);
				if (scanResult == null) {
					continue;
				}
				if (TextUtils.isEmpty(scanResult.SSID)) {
					continue;
				}
				db.insertFingerprintHomeRecord("wifi", scanResult.BSSID,
						scanResult.SSID, scanResult.capabilities,
						scanResult.level, (int) latitude, (int) longitude, df);
			}
			Log.i(TAG, "fingerprints saved to db.");
			Log.i(TAG, "set SharedPreferences.");
			SharedPreferences.Editor editor = getSharedPreferences(
					"project.praktikum.activity.recognition", MODE_PRIVATE)
					.edit();
			editor.putString("IsHomeSet", "True");
			editor.commit();
			Toast.makeText(getApplicationContext(), "Home fingerprint created.", Toast.LENGTH_LONG).show();
			finish();
			
			// db.insertFingerprintRecord("location", "", "", "", 0,
			// (int)latitude, (int)longitude, df);
			// insert(intent.getExtras().getString("fingerprints"));
			// sendMessageToUI(1);
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}
	
//	public boolean isHomeSet()
//	{
//		SharedPreferences shared = getSharedPreferences("project.praktikum.activity.recognition", MODE_PRIVATE);
//		return Boolean.valueOf(shared.getString("IsHomeSet", "False"));
//	}

//	@Override
//	public void onDestroy() {
//		Toast.makeText(this, db.fetchfingerprintshomeCount(), Toast.LENGTH_LONG).show();
//	}
}
