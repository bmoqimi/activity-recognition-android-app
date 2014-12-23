package project.praktikum.activity.recognition;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import project.praktikum.activity.recognition.ActivityCaptureService;
import project.praktikum.database.CustomCursorAdapter;
import project.praktikum.database.DataBase;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

	DataBase db;
	ListView lv;
	Cursor c;
	CustomCursorAdapter dbAdapter;

	boolean isServiceRunning;
	boolean isFingerprintingServiceRunning;

	Button btn;
	Button btnFingerprinting;

	Button btnExport;
	ImageView imgCurrentActivity;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		db = new DataBase(getApplicationContext());
		lv = (ListView) findViewById(R.id.lv);

		c = db.getAllRecords();

		dbAdapter = new CustomCursorAdapter(getApplicationContext(), c, 0, db);

		lv.setAdapter(dbAdapter);

		isFingerprintingServiceRunning = isMyServiceRunning(FingerprintingService.class);
		btnFingerprinting = (Button) findViewById(R.id.ButtonFingerprintingService);

		isServiceRunning = isMyServiceRunning(ActivityCaptureService.class);
		btn = (Button) findViewById(R.id.btnStartService);

		btnExport = (Button) findViewById(R.id.btnExport);
		imgCurrentActivity = (ImageView) findViewById(R.id.imgCurrentActivity);

		if (isFingerprintingServiceRunning) {
			btnFingerprinting.setText("Stop Fingerprinting");
			doFingerprintingBindService();
		}

		if (isServiceRunning) {
			btn.setText("STOP CAPTURE");
			doBindService();
		}
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
		if (avtivity.equals("Unknown")) {
			imgCurrentActivity.setImageResource(R.drawable.unknown);
		} else if (avtivity.equals("In Vehicle")) {
			imgCurrentActivity.setImageResource(R.drawable.in_vehicle);
		} else if (avtivity.equals("On Bicycle")) {
			imgCurrentActivity.setImageResource(R.drawable.on_bike);
		} else if (avtivity.equals("On Foot")) {
			imgCurrentActivity.setImageResource(R.drawable.on_foot);
		} else if (avtivity.equals("Still")) {
			imgCurrentActivity.setImageResource(R.drawable.still);
		} else {
			imgCurrentActivity.setImageResource(R.drawable.tillting);
		}
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

	public void startService(View view) {
		if (!isServiceRunning) {
			Intent i = new Intent(getBaseContext(),
					ActivityCaptureService.class);
			startService(i);
			isServiceRunning = true;
			btn.setText("STOP CAPTURE");
			doBindService();
		} else {
			doUnbindService();
			stopService(new Intent(getBaseContext(),
					ActivityCaptureService.class));
			isServiceRunning = false;
			btn.setText("START CAPTURE");
		}
	}

	public void startFingerprintingService(View view) {
		if (!isFingerprintingServiceRunning) {
			Intent i = new Intent(getBaseContext(), FingerprintingService.class);
			startService(i);
			isFingerprintingServiceRunning = true;
			btnFingerprinting.setText("Stop Fingerprinting");
			doFingerprintingBindService();
		} else {
			doFingerprintingUnbindService();
			stopService(new Intent(getBaseContext(),
					FingerprintingService.class));
			isFingerprintingServiceRunning = false;
			btnFingerprinting.setText("Start Fingerprinting");
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
	public void exportDB(View view) {
		File sd = Environment.getExternalStorageDirectory();
		File data = Environment.getDataDirectory();
		FileChannel source = null;
		FileChannel destination = null;
		String currentDBPath = "/data/" + "project.praktikum.recognition"
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

	// sendIntent.setType("image/jpeg");
	// sendIntent.setType("message/rfc822");
	// sendIntent.setType("*/*");
	// sendIntent.putExtra(android.content.Intent.EXTRA_STREAM, uriToZip);
	// startActivity(Intent.createChooser(sendIntent, "Send Attachment !:"));
	// }

	// public void openlocarea(View view) {
	// Intent intent = new Intent(this, GPSActivity.class);
	// startActivity(intent);
	// }

}
