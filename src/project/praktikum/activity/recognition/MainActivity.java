package project.praktikum.activity.recognition;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.security.acl.LastOwnerException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import project.praktikum.activity.recognition.ActivityCaptureService;
import project.praktikum.activity.report.ShowReport;
import project.praktikum.activity.report.WeeklyReport;
import project.praktikum.database.CustomCursorAdapter;
import project.praktikum.database.DataBase;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	ExpandableListView xLv;
	
	DataBase db;
	ListView lv;
	Cursor c;
	CustomCursorAdapter dbAdapter;
	View hvw;
	ImageView imgCurrentActivity;
	TextView txtLastConfirmedActivity;
	

	boolean isServiceRunning;
	boolean isFingerprintingServiceRunning;

	//Button btn;
	//Button btnFingerprinting;

	//Button btnExport;
	//ImageView imgCurrentActivity;
	Handler handler;

	Messenger mFingerprintingService = null;
	boolean mFingerprintingIsBound;
	final Messenger mFingerprintingMessenger = new Messenger(
			new IncomingHandler());

	Messenger mService = null;
	boolean mIsBound;
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	@SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ActivityCaptureService.MSG_SET_INT_VALUE:
				updateLv();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = new Messenger(service);
			try {
				Message msg = Message.obtain(null,
						ActivityCaptureService.MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mService.send(msg);
			} catch (RemoteException e) {
				// In this case the service has crashed before we could even do
				// anything with it
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected - process crashed.
			mService = null;
		}
	};

	private ServiceConnection mFingerprintingConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mFingerprintingService = new Messenger(service);
			try {
				Message msg = Message
						.obtain(null,
								FingerprintingService.MSG_FINGERPRINTING_REGISTER_CLIENT);
				msg.replyTo = mFingerprintingMessenger;
				mFingerprintingService.send(msg);
			} catch (RemoteException e) {
				// In this case the service has crashed before we could even do
				// anything with it
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected - process crashed.
			mFingerprintingService = null;
		}
	};

	@SuppressWarnings("static-access")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		SharedPreferences shared = getSharedPreferences("project.praktikum.activity.recognition", MODE_PRIVATE);
		if(!Boolean.valueOf(shared.getString("IsHomeSet", "False")))
			Toast.makeText(getApplicationContext(),"HOME IS NOT SET YET!", Toast.LENGTH_LONG).show();
		
		db = db.getInstance(getApplicationContext());
		//lv = (ListView) findViewById(R.id.lv);

		c = db.getAllRecords();
		
		dbAdapter = new CustomCursorAdapter(getApplicationContext(), c, 0, db);

		//lv.setAdapter(dbAdapter);
		xLv = (ExpandableListView) findViewById(R.id.expandableListView1);
		
        hvw = (View)getLayoutInflater().inflate(R.layout.lv_header,null);
        //Suppose you have one textview in that Row
        //ImageView img = (ImageView) hvw.findViewById(R.id.img_header); // get textview object

        imgCurrentActivity = (ImageView) hvw.findViewById(R.id.img_header);
		txtLastConfirmedActivity = (TextView) hvw.findViewById(R.id.txtCurrentActivity);
		db.fillTimeLine();
        
        loadHosts(prepareListData()); 
        xLv.addHeaderView(hvw); // add header view

		isFingerprintingServiceRunning = isMyServiceRunning(FingerprintingService.class);
		//btnFingerprinting = (Button) findViewById(R.id.ButtonFingerprintingService);

		isServiceRunning = isMyServiceRunning(ActivityCaptureService.class);
		//btn = (Button) findViewById(R.id.btnStartService);

		//btnExport = (Button) findViewById(R.id.btnExport);
		//imgCurrentActivity = (ImageView) findViewById(R.id.imgCurrentActivity);

		if (isFingerprintingServiceRunning) {
			//btnFingerprinting.setText("Stop Fingerprinting");
			doFingerprintingBindService();
		}

		if (isServiceRunning) {
			//btn.setText("STOP CAPTURE");
			doBindService();
		}
	}
	
    @SuppressWarnings("deprecation")
	private ArrayList<Parent> prepareListData() {
    	
    	final ArrayList<Parent> list = new ArrayList<Parent>();
    	
        Date currentDay = new Date();
        Date currentActivity = new Date();
    	Cursor c = db.fetchTimeLine();
    	if(c.moveToFirst())
    	{
    		do
    		{
    			Parent header = new Parent();
    			currentActivity = stringToDate(c.getString(2));
            	currentDay = currentActivity;
            	header.setDate(currentDay);
            	
            	ArrayList<Child> children = new ArrayList<Child>();
            	
    			while(currentActivity.getDate() == currentDay.getDate() && !c.isAfterLast())
    			{
    				Child mChild = new Child();
    				mChild.setActivity(c.getString(c.getColumnIndex("activity")));
    				mChild.setStart(c.getString(c.getColumnIndex("start")));
    				mChild.setEnd(c.getString(c.getColumnIndex("end")));
    				mChild.setDuration(
    						stringToDate(mChild.getEnd()).getTime() - 
    						stringToDate(mChild.getStart()).getTime());
    				children.add(mChild);
    				setActivityTime(header,
    						stringToDate(mChild.getEnd()).getTime() - stringToDate(mChild.getStart()).getTime(),
    						mChild.getActivity());
    				c.moveToNext();
    				if(!c.isAfterLast())
    					currentActivity = stringToDate(c.getString(2));
    			}
    			
    			header.setChildren(children);
    			list.add(header);
    			
    		}while(c.moveToNext());
    	}
    	return list;
    }
    
    private void setActivityTime(Parent parent , long amount , String activity)
    {
    	if(activity.equals("Still"))
    	{
    		parent.setStill(parent.getStill() + amount);
    	}
    	
    	else if(activity.equals("Walking"))
    	{
    		parent.setWalking(parent.getWalking() + amount);
    	}
    	else if(activity.equals("Sleeping"))
    	{
    		parent.setSleeping(parent.getSleeping() + amount);
    	}
    	else if(activity.equals("InVehicle"))
    	{
    		parent.setDriving(parent.getDriving() + amount);
    	}
    	else if(activity.equals("OnBicycle"))
    	{
    		parent.setCycling(parent.getCycling() + amount);
    	}
    	else if(activity.equals("Running"))
    	{
    		parent.setRunning(parent.getRunning() + amount);
    	}
    }
    
    private void loadHosts(final ArrayList<Parent> newParents)
    {
        if (newParents == null)
            return;
        if (xLv.getExpandableListAdapter() == null)
        {
            final ExpandableListAdapter mAdapter = new ExpandableListAdapter(this , newParents,xLv);
            xLv.setAdapter(mAdapter);
        }
        else
        {
            xLv.deferNotifyDataSetChanged();
        }   
    }
    
    @SuppressLint("SimpleDateFormat")
	private Date stringToDate(String aDate) {

        if(aDate==null) return null;
        ParsePosition pos = new ParsePosition(0);
        SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date stringDate = simpledateformat.parse(aDate, pos);
        return stringDate;            

     }

	@SuppressWarnings("static-access")
	private void updateLv() {
		c = db.getAllRecords();
		c.moveToFirst();
		setImgCurrentActivity(c.getString(c.getColumnIndex(db.COLUMN_ACTIVITY)));
		dbAdapter.changeCursor(c);
		dbAdapter.notifyDataSetChanged();
	}
	
	private void setImgCurrentActivity(String avtivity) {
		if (avtivity.equals("Walking")) {
			imgCurrentActivity.setImageResource(R.drawable.walking);
		} else if (avtivity.equals("InVehicle")) {
			imgCurrentActivity.setImageResource(R.drawable.driving);
		} else if (avtivity.equals("OnBicycle")) {
			imgCurrentActivity.setImageResource(R.drawable.biking);
		} else if (avtivity.equals("Running")) {
			imgCurrentActivity.setImageResource(R.drawable.running);
		} else if (avtivity.equals("Still")) {
			imgCurrentActivity.setImageResource(R.drawable.standing);
		} else {
			imgCurrentActivity.setImageResource(R.drawable.sleeping);
		}
		txtLastConfirmedActivity.setText(avtivity);
	}

	private boolean isMyServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public void startService() {
		if (!isServiceRunning) {
			Intent i = new Intent(getBaseContext(),
					ActivityCaptureService.class);
			startService(i);
			isServiceRunning = true;
			//btn.setText("STOP CAPTURE");
			doBindService();
		} else {
			doUnbindService();
			stopService(new Intent(getBaseContext(),
					ActivityCaptureService.class));
			isServiceRunning = false;
			//btn.setText("START CAPTURE");
		}
	}

	public void startFingerprintingService() {
		if (!isFingerprintingServiceRunning) {
			Intent i = new Intent(getBaseContext(), FingerprintingService.class);
			startService(i);
			isFingerprintingServiceRunning = true;
			//btnFingerprinting.setText("Stop Fingerprinting");
			doFingerprintingBindService();
		} else {
			doFingerprintingUnbindService();
			stopService(new Intent(getBaseContext(),
					FingerprintingService.class));
			isFingerprintingServiceRunning = false;
			//btnFingerprinting.setText("Start Fingerprinting");
		}
	}

	void doBindService() {
		bindService(new Intent(this, ActivityCaptureService.class),
				mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	void doFingerprintingBindService() {
		bindService(new Intent(this, FingerprintingService.class),
				mFingerprintingConnection, Context.BIND_AUTO_CREATE);
		mFingerprintingIsBound = true;
	}

	void doUnbindService() {
		if (mIsBound) {
			// If we have received the service, and hence registered with it,
			// then now is the time to unregister.
			if (mService != null) {
				try {
					Message msg = Message.obtain(null,
							ActivityCaptureService.MSG_UNREGISTER_CLIENT);
					msg.replyTo = mMessenger;
					mService.send(msg);
				} catch (RemoteException e) {
					// There is nothing special we need to do if the service has
					// crashed.
				}
			}
			// Detach our existing connection.
			unbindService(mConnection);
			mIsBound = false;
		}
	}

	void doFingerprintingUnbindService() {
		if (mFingerprintingIsBound) {
			// If we have received the service, and hence registered with it,
			// then now is the time to unregister.
			if (mFingerprintingService != null) {
				try {
					Message msg = Message
							.obtain(null,
									FingerprintingService.MSG_FINGERPRINTING_UNREGISTER_CLIENT);
					msg.replyTo = mFingerprintingMessenger;
					mFingerprintingService.send(msg);
				} catch (RemoteException e) {
					// There is nothing special we need to do if the service has
					// crashed.
				}
			}
			// Detach our existing connection.
			unbindService(mFingerprintingConnection);
			mFingerprintingIsBound = false;
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		try {
			doFingerprintingUnbindService();
		} catch (Throwable t) {
			Log.e("MainActivity",
					"Failed to unbind from the Fingerprinting service", t);
		}

		try {
			doUnbindService();
		} catch (Throwable t) {
			Log.e("MainActivity", "Failed to unbind from the service", t);
		}
	}

	@SuppressLint("SimpleDateFormat")
	public void exportDB() {
		File sd = Environment.getExternalStorageDirectory();
		File data = Environment.getDataDirectory();
		FileChannel source = null;
		FileChannel destination = null;
		String currentDBPath = "/data/" + "project.praktikum.activity.recognition"
				+ "/databases/" + "MyDBName.db";
		File currentDB = new File(data, currentDBPath);
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("MM-dd-HH:mm:ss");
		String date = df.format(c.getTime());
		File backupDB = new File(sd, date);
		sendMail(backupDB);
		try {
			source = new FileInputStream(currentDB).getChannel();
			destination = new FileOutputStream(backupDB).getChannel();
			destination.transferFrom(source, 0, source.size());
			source.close();
			destination.close();
			Toast.makeText(this, "DB Exported!", Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendMail(File outFile) {
		Uri uriToZip = Uri.fromFile(outFile);
		String sendText = "Dear friend,\n\n...";

		Intent sendIntent = new Intent(Intent.ACTION_SEND);
		sendIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
				new String[] { "jalal.khademi66@gmail.com" });
		sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, sendText);
		sendIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				"Project Praktikum");

		// sendIntent.setType("image/jpeg");
		// sendIntent.setType("message/rfc822");
		sendIntent.setType("*/*");
		sendIntent.putExtra(android.content.Intent.EXTRA_STREAM, uriToZip);
		startActivity(Intent.createChooser(sendIntent, "Send Attachment !:"));
	}

	public void SetHome_OnClick() {
	 Intent intent = new Intent(this, SetHome.class);
	 startActivity(intent);
	 }
	 
	public void HomeSimilarity_OnClick() {
			Intent intent = new Intent(this, HomeRecognition.class);

			// intent.setType(LOCATION_SERVICE);
			startActivityForResult(intent, 1);
			// startActivity(intent);
		}

		@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
			if (requestCode == 1) {
				if (resultCode == RESULT_OK) {
					boolean isHome = data.getBooleanExtra("isHome", false);
					Toast.makeText(this, String.valueOf(isHome), Toast.LENGTH_SHORT).show();
				}
			}
		}
	 
		public void ButtonReport_OnClick() {
			 Intent intent = new Intent(this, ShowReport.class);
			 startActivity(intent);
			 }
			
			public void ButtonWeeklyReport_OnClick() {
				 Intent intent = new Intent(this, WeeklyReport.class);
				 startActivity(intent);
				 }
			 
			 @Override
			public boolean onCreateOptionsMenu(Menu menu) {
				// TODO Auto-generated method stub
				 MenuInflater inflater = getMenuInflater();
				 inflater.inflate(R.menu.main, menu);
				 return true;
			}
			 
			 @Override
			 public boolean onOptionsItemSelected(MenuItem item) {
			     // Handle item selection
			     switch (item.getItemId()) {
			         case R.id.action_settings:
			             Intent intent = new Intent(getApplicationContext(), ActivityExpandableListView.class);
			             startActivity(intent);
			             return true;

			         case R.id.action_Dreport:
			        	 ButtonReport_OnClick();
			             return true;
			             
			         case R.id.action_Wreport:
			        	 ButtonWeeklyReport_OnClick();
			             return true;

			         case R.id.action_export:
			             exportDB();
			             return true;

			         case R.id.action_service:
			        	 startService();
			             return true;

			         case R.id.action_fingertpring:
			        	 startFingerprintingService();
			             return true;

			         case R.id.action_sethome:
			        	 SetHome_OnClick();
			             return true;

			         case R.id.action_home_similarity:
			        	 HomeSimilarity_OnClick();
			             return true;
			             
			         default:
			             return super.onOptionsItemSelected(item);
			     }
			 }
		}






















