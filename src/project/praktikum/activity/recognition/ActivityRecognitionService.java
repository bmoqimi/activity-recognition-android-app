package project.praktikum.activity.recognition;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class ActivityRecognitionService extends IntentService
{

	private String TAG = this.getClass().getSimpleName();
	
	public ActivityRecognitionService() 
	{
		super("Activity Recognition Service");
	}
	
	@Override
	protected void onHandleIntent(Intent intent) 
	{
		if(ActivityRecognitionResult.hasResult(intent))
		{
			
			ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
			String activity = getType(result.getMostProbableActivity().getType());
			//Ignore Unknown,Tilting
			if (activity != "Unknown" ) {
			Log.i(TAG, activity +"t" 
					+ result.getMostProbableActivity().getConfidence());
			
			Intent i = new Intent("project.praktikum.recognition.ACTIVITY_RECOGNITION_DATA");
			i.putExtra("activity", activity);
			i.putExtra("conf", result.getMostProbableActivity().getConfidence());
			sendBroadcast(i);
		}
		}
	}
	 
	private String getType(int type)
	{
		if(type == DetectedActivity.WALKING)
			return "Walking";
		else if(type == DetectedActivity.IN_VEHICLE)
			return "InVehicle";
		else if(type == DetectedActivity.ON_BICYCLE)
			return "OnBicycle";
		else if(type == DetectedActivity.RUNNING)
			return "Running";
		else if(type == DetectedActivity.STILL)
			return "Still";
		else if(type == DetectedActivity.TILTING)
			return "InVehicle";
		else
			return "Unknown";
	}
}

