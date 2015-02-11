package project.praktikum.activity.recognition;

import java.text.SimpleDateFormat;
import android.os.CountDownTimer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import project.praktikum.activity.recognition.ActivityRecognitionService;
import project.praktikum.activity.recognition.MainActivity;
import project.praktikum.activity.reduction.NoiseReduction;
import project.praktikum.database.DataBase;
import project.praktikum.sensors.LightSensor;
import project.praktikum.sensors.Recorder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
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
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class ActivityCaptureService extends Service implements GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener{
	/** indicates how to behave if the service is killed */
	int mStartMode;
	/** interface for clients that bind */
	IBinder mBinder;     
	/** indicates whether onRebind should be used */
	/**
	 * We have set the earliest possible sleep time to 7pm
	 */
	static final int beginSleepCheckHour = 19;
	static final int audioThreshold = 20;
	static final int lightThreshold = 20;
	static final int sleepCheckCycle = 20; /** In minutes */
	static final double sensorCycleCheck = 0.25; /** Minutes */
	private Date sleepingSince;
	private boolean isSleep;
	private boolean isAtHome;
	boolean mAllowRebind;
	private String TAG = "ActivityCaptureService";
	private String SleepTag = "SleepDetection";
	private PendingIntent pIntent;
	private BroadcastReceiver activityReceiver;
	private BroadcastReceiver userReceiver;
	private BroadcastReceiver alarmReceiver;
	private Date lastUserAction;
	private ActivityRecognitionClient arclient;
	private DataBase db;
	private NoiseReduction noiseReduction;
	ArrayList<Messenger> mClients = new ArrayList<Messenger>();
	static final int MSG_REGISTER_CLIENT = 1;
	static final int MSG_UNREGISTER_CLIENT = 2;
	static final int MSG_SET_INT_VALUE = 3;
	static boolean isRunning = false;
	private static NotificationManager nm;
	private LightSensor lightSensor;
	private Recorder audioSensor;
	private boolean sensorInProgress = false;

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

	private void showNotification() {
		
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("Current activity")
		        .setContentText("Nothing yet!!");
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, MainActivity.class);

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(MainActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		mBuilder.setContentIntent(resultPendingIntent);
		nm =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		nm.notify(1366, mBuilder.build());
	}

	/** The service is starting, due to a call to startService() */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		/** 
		 *  To get notified whenever the user is present
		 */
		userReceiver = new BroadcastReceiver(){
		    @Override
		    public void onReceive(Context context, Intent intent) {
		        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
		          lastUserAction = new Date();
		           if(isSleep)
		        	   wakeupSequence(1);
		        	}
		        }
		};
		alarmReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) { 
				/**
				 * after we got notified when it is 7pm
				 */
				if (intent.getExtras().containsKey("SleepDetectionWakeUp"))
					Log.i(SleepTag, "Got called by AlarmService, starting sleep checking cycle" );
					checkSleeping();
					}
			}; 
		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		
		/* Set the alarm to start at 7 PM */
		
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, beginSleepCheckHour);
        calendar.set(Calendar.MINUTE, 0);
        Intent alint = new Intent(ActivityCaptureService.this, ActivityCaptureService.class);
        alint.putExtra("SleepDetectionWakeUp","SleepDetectionWakeUp");
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, alint, PendingIntent.FLAG_CANCEL_CURRENT);
        alarm.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);
        
		lightSensor = new LightSensor(getApplicationContext(), lightThreshold);
		audioSensor = new Recorder(audioThreshold);
		
		IntentFilter alarmFilter = new IntentFilter(Intent.ACTION_DEFAULT);
		registerReceiver(alarmReceiver, alarmFilter);
		
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		intentFilter.addAction(Intent.ACTION_USER_PRESENT);
		registerReceiver(userReceiver , intentFilter);
		
		int resp = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

		if(resp == ConnectionResult.SUCCESS)
		{
			arclient = new ActivityRecognitionClient(this, this, this);
			arclient.connect(); 
			//TODO Get the status of Home context later
			noiseReduction = new NoiseReduction(false);
		}
		activityReceiver = new BroadcastReceiver() 
		{
			@Override
			public void onReceive(Context context, Intent intent) 
			{
				insert(intent.getExtras().getString("activity")
						,intent.getExtras().getInt("conf"));
				sendMessageToUI(1);
			}
		};

		IntentFilter filter = new IntentFilter();
		filter.addAction("project.praktikum.recognition.ACTIVITY_RECOGNITION_DATA");
		registerReceiver(activityReceiver, filter);
		return mStartMode;
	}
	
	private void updateNotification(String activity)
	{
		NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(this)
		    .setContentTitle("Current activity")
		    .setContentText(activity)
		    .setSmallIcon(R.drawable.ic_launcher);
		
		Intent resultIntent = new Intent(this, MainActivity.class);

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(MainActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		mNotifyBuilder.setContentIntent(resultPendingIntent);
		    nm.notify(
		            1366,
		            mNotifyBuilder.build());
	}

	@SuppressLint("SimpleDateFormat")
	private void insert(String activity, int conf)
	{
		//Calendar c = Calendar.getInstance();
		Log.i(TAG, "Recieved insert command with : " + activity);
		SimpleDateFormat df = new SimpleDateFormat("MM/dd   HH:mm:ss");
		if(isSleep) {
			if(activity == "Still")
				return;
			else 
				wakeupSequence(1);
		}
		if (noiseReduction.newActivityDetected(activity)){
			updateNotification(activity);
			HashMap<Date,String> activities = noiseReduction.getActivities();
			Iterator<Entry<Date,String>> it = activities.entrySet().iterator();
			int count = 0;
			while (it.hasNext()){
				count++;
				Entry<Date,String> item = it.next();
				db.insertRecord(item.getValue(), 0, df.format(item.getKey()));
				Log.i(TAG, "Going through buffered activities for the " + count+"th time");
				it.remove();
			}
		}
		//db.insertRecord(activity, conf, date);
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
		unregisterReceiver(activityReceiver);
		unregisterReceiver(userReceiver);
		unregisterReceiver(alarmReceiver);
		cancelNotification(R.string.app_name);
		isRunning = false;
	}

	public static void cancelNotification(int notifyId) {
		nm.cancel(1366);
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

	public Date getSleepingSince() {
		return sleepingSince;
	}
	public void setSleepingSince(Date sleepingSince) {
		this.sleepingSince = sleepingSince;
	}
	public boolean isSleep() {
		return isSleep;
	}
	public void setSleep(boolean isSleep) {
		this.isSleep = isSleep;
	}
	
	private void scheduleSleepTimer(double minutes){
		long futureMillis = (long) minutes * 60 * 1000;
		Log.i(SleepTag, "Setting a new count down timer for " + minutes + " minutes ");
		CountDownTimer cnt = new CountDownTimer(futureMillis, futureMillis ) {
			
			@Override
			public void onTick(long millisUntilFinished) {
				// TODO Auto-generated method stub
				return;
			}
			
			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				if(sensorInProgress){
					boolean light = lightSensor.stopListening();
					boolean sound = audioSensor.stopRecording();
					sensorInProgress = false;
					if ( light || sound) {
						Log.i(SleepTag, "Timer finished and sensory input indicates the user is asleep");
						
						if(!isSleep)
						{
							isSleep = true;
							sleepingSince = new Date();
							noiseReduction.setState("Still");
							SimpleDateFormat df = new SimpleDateFormat("MM/dd   HH:mm:ss");
							Log.i(SleepTag, "First Sleeping event detected. Putting it in the database");
							db.insertRecord("Sleeping", 0, df.format(sleepingSince));
						}
						return;
					}
					else {
						if(isSleep){
							wakeupSequence(0);
						}
					}
				}
				Log.i(SleepTag, "Timer ended, checking sleep conditions now.");
				scheduleSleepTimer(sleepCheckCycle);
				checkSleeping();
			}
		};
	}
	public void checkSleeping(){
		if(isAtHome) {
			scheduleSleepTimer(sleepCheckCycle);
			return; 
		  }
		if(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < beginSleepCheckHour) {
			return;
		}
		Date tempDate = new Date();
		int minutesSinceLastAction = (int) (((new Date().getTime() - sleepingSince.getTime()) / 1000 ) / 60);
		if (minutesSinceLastAction < sleepCheckCycle)
		{
			scheduleSleepTimer(sleepCheckCycle);
			return;
		}
		lightSensor.startListening();
		audioSensor.startRecording();
		sensorInProgress = true;
		Log.i(SleepTag, "Other sleeping conditions are met, checking sensory input");
		scheduleSleepTimer(sensorCycleCheck);
		
	}
	
	private void wakeupSequence(int priority) { 
		Log.i(SleepTag,"wakeup detected with priority " + priority );
		Date wakeupDate = new Date();
		SimpleDateFormat df = new SimpleDateFormat("MM/dd   HH:mm:ss");
		db.insertRecord("Still", 0, df.format(wakeupDate));
		noiseReduction.setState("Still");
		lastUserAction = wakeupDate;
	}
}
