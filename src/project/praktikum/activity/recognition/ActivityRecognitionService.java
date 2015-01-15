package project.praktikum.activity.recognition;

import java.util.List;

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
			List<DetectedActivity>  onFootTypes = result.getProbableActivities();

			String activity = getType(result.getMostProbableActivity().getType(), onFootTypes);
			//Ignore Unknown,Tilting
			Log.i(TAG, "raw activity detected as: "+activity);
			if (activity != "Unknown" ) {
			Intent i = new Intent("project.praktikum.recognition.ACTIVITY_RECOGNITION_DATA");
			i.putExtra("activity", activity);
			i.putExtra("conf", result.getMostProbableActivity().getConfidence());
			sendBroadcast(i);
		}
		}
	}
	 
	private String getType(int type, List<DetectedActivity> activities)
	{
		Log.d(TAG, "getType was called with raw type: " + type);
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
		else if(type == DetectedActivity.ON_FOOT)
		{
			for (DetectedActivity item: activities) {
				int footActivity = item.getType();
				if (footActivity == DetectedActivity.WALKING)
					return "Walking";
				else if (footActivity == DetectedActivity.RUNNING)
					return "Running";
			}
			return "Walking";
		}
		else
			return "Unknown";
	}
}

