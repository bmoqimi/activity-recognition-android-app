package project.praktikum.activity.recognition;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import project.praktikum.database.DataBase;
import android.text.TextUtils;
import android.util.Log;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.widget.TextView;

public class HomeDetection  {
	//implements LocationListener {


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
	private Context context;
	private boolean AtHome;
	private String TAG = "HomeDetection";

	//protected void onCreate(Bundle savedInstanceState)
	public HomeDetection(Context cntx) {
		//super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_wifi);
		this.context = cntx;
		db = DataBase.getInstance(cntx);
		
		//mainText = (TextView) findViewById(R.id.listwifi);
		mainWifi = (WifiManager) cntx.getSystemService(Context.WIFI_SERVICE);
		wifiStatus = mainWifi.isWifiEnabled();
		receiverWifi = new WifiReceiver();
		cntx.registerReceiver(receiverWifi, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		if (!mainWifi.isWifiEnabled()) {
			mainWifi.setWifiEnabled(true);
		}
		AtHome = false;
		Log.d(TAG, "Home Detection initialized");
		
	}

	public boolean startScan() {
		Log.d(TAG, "Scan started");
		return mainWifi.startScan();
	}

	public void destroy() {
		context.unregisterReceiver(receiverWifi);
	}

	public boolean isAtHome() {
		return AtHome;
	}
	class WifiReceiver extends BroadcastReceiver {
		@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
		@SuppressLint("NewApi")
		public void onReceive(Context c, Intent intent) {
			
			wifiList = mainWifi.getScanResults();
			String df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(new Date());

			List<String> currentBSSIds = new ArrayList<String>();
			for (int i = 0; i < wifiList.size(); i++) {
				final ScanResult scanResult = wifiList.get(i);
				if (scanResult == null) {
					continue;
				}
				if (TextUtils.isEmpty(scanResult.SSID)) {
					continue;
				}
				
				currentBSSIds.add(scanResult.BSSID);
			}
			
			Cursor  cursor = db.fetchfingerprintshome();
			
			while (cursor.moveToNext()) {
				 String dbbssid = cursor.getString(cursor.getColumnIndex("bssid"));
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
			if(percentage>=threshold)
			{
				Log.d(TAG, "Threshold is achieved so assuming the user is at home");
				AtHome = true;
			}
			else
			{
				Log.d(TAG, "Threshold NOT achieved so the user is NOT at home");
				AtHome = false;
							}
			if(!wifiStatus)
				mainWifi.setWifiEnabled(false);
		}
	}

}




