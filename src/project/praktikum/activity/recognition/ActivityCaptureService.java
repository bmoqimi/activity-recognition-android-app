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
	static final int beginSleepCheckHour = 14;
	static final int morningSleepCycleEnd = 9;
	static final int audioThreshold = 20;
	static final int lightThreshold = 2;
	static final double sleepCheckCycle = 2; /** In minutes */
	static final long sensorCycleCheck = 15; /** In seconds */
	private Date sleepingSince;
	private boolean isSleep = false;
	private boolean isAtHome = true;;
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
	private HomeDetection homeDetection;

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
		db = DataBase.getInstance(getApplicationContext());
		isRunning = true;
		isSleep = false;
		//db.insertRecord("StartService", 100, df.format(item.getKey()));
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

		/** The stack builder object will contain an artificial back stack for the
		 started Activity.
		 This ensures that navigating backward from the Activity leads out of
		 your application to the Home screen.
		*/
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
					Log.d(SleepTag, "We got a user present event");
					if(isSleep) {
						Log.i(SleepTag, "User Action event received; waking the user up");
						wakeupSequence(1);
					}
					}
			}
		};
		sensorInProgress = false;
		lightSensor = new LightSensor(getApplicationContext(), (float)lightThreshold);
		audioSensor = new Recorder(audioThreshold);

		IntentFilter alarmFilter = new IntentFilter();
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
		lastUserAction = new Date();
		homeDetection = new HomeDetection(getApplicationContext());
		if(!homeDetection.startScan()) {
			Log.i(TAG, "Wifi scanning failed for home detection");
		}
		scheduleSleepTimer(sleepCheckCycle);
		checkSleeping();
		return mStartMode;
	}

	private void updateNotification(String activity)
	{
		NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(this)
		.setContentTitle("Current activity")
		.setContentText(activity)
		.setSmallIcon(R.drawable.ic_launcher);

		Intent resultIntent = new Intent(this, MainActivity.class);

		/** The stack builder object will contain an artificial back stack for the
		 started Activity.
		 This ensures that navigating backward from the Activity leads out of
		 your application to the Home screen.
		*/
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
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		if(isSleep) {
			if(activity == "Still")
				return;
			else
				Log.i(SleepTag, "An Activity detected while the user is sleeping;waking him up");
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
		//unregisterReceiver(alarmReceiver);
		cancelNotification(R.string.app_name);
		isRunning = false;
		homeDetection.destroy();
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
		CountDownTimer cnt = new CountDownTimer(futureMillis, futureMillis ) {

			@Override
			public void onTick(long millisUntilFinished) {
				// TODO Auto-generated method stub
				Log.d(SleepTag, "Timer Ticked with ID " + this.hashCode());
				//return;
			}

			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				if (!isRunning)
					return;
				Log.d(SleepTag, "Timer with ID : " + this.hashCode() + "time is up.");

				Log.i(SleepTag, "Timer ended, checking sleep conditions now.");
				scheduleSleepTimer(sleepCheckCycle);
				checkSleeping();
			}
		};
		Log.i(SleepTag, "Setting a new count down timer for " + minutes + " minutes with ID: " + cnt.hashCode());
		cnt.start();

	}

	private void scheduleSensorTimer(long seconds){
		long futureMillis =  seconds * 1000;
		Log.i(SleepTag, "Setting a new count down timer for " + seconds + " seconds ");
		CountDownTimer cnt = new CountDownTimer(futureMillis, futureMillis ) {

			@Override
			public void onTick(long millisUntilFinished) {
				// TODO Auto-generated method stub
				Log.d(SleepTag, "Timer Ticked with ID " + this.hashCode());
				return;
			}

			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				if (!isRunning)
					return;
				Log.d(SleepTag, "Timer with ID : " + this.hashCode() + "time is up.");
				if(sensorInProgress){
					boolean light = lightSensor.stopListening();
					boolean sound = audioSensor.stopRecording();
					sensorInProgress = false;
					isAtHome = homeDetection.isAtHome();
					noiseReduction.setAtHome(isAtHome);
					if (!isAtHome) {
						if (isSleep) {
							Log.i(SleepTag, "User is not at home yet still sleeping;waking him up");
							wakeupSequence(1);
							return;
						}
						else {
							return;
						}
					}
					else {
						isAtHome = true;
					}
					Log.d(SleepTag, "Timer finished; Light is: " + light + " and sound is : " + sound);
					if ( light && sound) {
						Log.d(SleepTag, "Timer finished and sensory input indicates the user is asleep");

						if(!isSleep)
						{
							isSleep = true;
							sleepingSince = new Date();
							noiseReduction.setState("Still");
							SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
							Log.i(SleepTag, "First Sleeping event detected. Putting it in the database");
							db.insertRecord("Sleeping", 0, df.format(sleepingSince));
							return;
						}
					}
					else {
						Log.d(SleepTag, "Light and sensory input indicate the user is NOT sleeping");
						if(isSleep){
							Log.i(SleepTag, "Wake up event because of light or audio sensory input detected.");
							wakeupSequence(0);
							return;
						}
					}
				}
			}

		};
		Log.d(SleepTag, "Setting a new sensory timer for " + seconds + " seconds with ID: " + cnt.hashCode());
		cnt.start();
	}
	public void checkSleeping(){
		if(homeDetection.isAtHome()){
			isAtHome = true;
			noiseReduction.setAtHome(true);
		}
		else { 
			isAtHome = false;
			noiseReduction.setAtHome(false);
			if(isSleep) {
				Log.i(SleepTag, "User is not home so waking him up");
				wakeupSequence(1);
			}
		}
		Log.d(SleepTag, "CheckSleep started. Will check all sleeping conditions now.");
		if(!isSleep) {
			if(!isAtHome) {
				Log.d(SleepTag, "User is not at home so checking again later.");
				return; 
			}
			int currentTime = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
			if(currentTime < beginSleepCheckHour ) {
				if(currentTime > morningSleepCycleEnd) {
					Log.d(SleepTag, "CheckSleep started, Time of the day is before sleeping hours");
					return;
				}
			}
			Date tempDate = new Date();
			int minutesSinceLastAction = (int) (((new Date().getTime() - lastUserAction.getTime()) / 1000 ) / 60);
			if (minutesSinceLastAction < sleepCheckCycle)
			{
				Log.d(SleepTag, "CheckSleep started. The user was recently active on device.");
				return;
			}
		}
		Log.d(SleepTag, "Now getting sensory input");
		homeDetection.startScan();
		lightSensor.startListening();
		audioSensor.startRecording();
		sensorInProgress = true;
		Log.i(SleepTag, "Other sleeping conditions are met, checking sensory input");
		scheduleSensorTimer(sensorCycleCheck);

	}

	private void wakeupSequence(int priority) {
		isSleep = false;
		Log.i(SleepTag,"wakeup detected with priority " + priority );
		Date wakeupDate = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		db.insertRecord("Wakeup", 0, df.format(wakeupDate));
		noiseReduction.setState("Still");
		lastUserAction = wakeupDate;
	}
}
