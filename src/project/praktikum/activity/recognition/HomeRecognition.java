package project.praktikum.activity.recognition;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class HomeRecognition extends Activity implements LocationListener {

	TextView mainText;
	WifiManager mainWifi;
	WifiReceiver receiverWifi;
	List<ScanResult> wifiList;
	StringBuilder sb = new StringBuilder();
	LocationManager locationManager;
	double totalFingerprintRows=0;
	double matchedFingerprints=0;
	private DataBase db;
	boolean wifiStatus = false;
	double threshold=30;
	private LocationManager CellTowerlocationManager;
	private String provider;
	double latitude;
	double longitude;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wifi);

		db = db.getInstance(getApplicationContext());
		

//		Toast.makeText(this, String.valueOf(isHomeSet()), Toast.LENGTH_LONG).show();

		mainText = (TextView) findViewById(R.id.listwifi);
		mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifiStatus = mainWifi.isWifiEnabled();
		receiverWifi = new WifiReceiver();
		registerReceiver(receiverWifi, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		if (!mainWifi.isWifiEnabled()) {
			mainWifi.setWifiEnabled(true);
		}
		mainWifi.startScan();
		
		//CellTowerlocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

//		Criteria criteria = new Criteria();
//		provider = CellTowerlocationManager.getBestProvider(criteria, false);
//		Location location = CellTowerlocationManager
//				.getLastKnownLocation(provider);

		// Initialize the location fields
//		if (location != null) {
			// System.out.println("Provider " + provider +
			// " has been selected.");
//			onLocationChanged(location);
//		} else {
			// mainText.setText("Location not available");
//		}
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
		latitude = (double) (location.getLatitude());
		longitude = (double) (location.getLongitude());
		// mainText.setText(String.valueOf(lat) + " , " + String.valueOf(lng));
	}

	class WifiReceiver extends BroadcastReceiver {
		@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
		@SuppressLint("NewApi")
		public void onReceive(Context c, Intent intent) {
			
			wifiList = mainWifi.getScanResults();
			String df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(new Date());

			List<String> currentBSSIds = new ArrayList<String>();
//			List<Integer> currentLats = new ArrayList<Integer>();
//			List<Integer> currentLongs = new ArrayList<Integer>();
			for (int i = 0; i < wifiList.size(); i++) {
				final ScanResult scanResult = wifiList.get(i);
				if (scanResult == null) {
					continue;
				}
				if (TextUtils.isEmpty(scanResult.SSID)) {
					continue;
				}
				
				currentBSSIds.add(scanResult.BSSID);
//				currentLats.add((int) latitude);
//				currentLongs.add((int) longitude);
			}
			
			Cursor  cursor = db.fetchfingerprintshome();
			
			while (cursor.moveToNext()) {
				 String dbbssid = cursor.getString(cursor.getColumnIndex("bssid"));
				//	Toast.makeText(getApplicationContext(), dbbssid, Toast.LENGTH_SHORT).show();
			//	 int dblat = Integer.parseInt(cursor.getString(cursor.getColumnIndex("latitude")));
				// int dblong = Integer.parseInt(cursor.getString(cursor.getColumnIndex("longitude")));
				 totalFingerprintRows+=1.0;
				 
				for (String bssid : currentBSSIds) 
				{
				    if(bssid.equals(dbbssid))
				    {
				    	matchedFingerprints+=1.0;
				    	break;
				    }
				}
			}
			
			double percentage=0;
			if(totalFingerprintRows>0)
				{
				percentage=Math.ceil((matchedFingerprints*100.0)/totalFingerprintRows);
				}
			//Toast.makeText(getApplicationContext(),"Similarity is: "+ String.valueOf(percentage)+"%", Toast.LENGTH_LONG).show();
			if(percentage>=threshold)
			{
			    intent.putExtra("isHome", true);
				//true
			}
			else
			{
			    intent.putExtra("isHome", false);
				//false
			}
			HomeRecognition.this.setResult(RESULT_OK, intent);
			if(!wifiStatus)
				mainWifi.setWifiEnabled(false);
			finish();//to close the activity
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

//	@Override
//	public void onDestroy() {
//		Toast.makeText(this, matchedFingerprints+" out of "+totalFingerprintRows, Toast.LENGTH_LONG).show();
//	}
}
