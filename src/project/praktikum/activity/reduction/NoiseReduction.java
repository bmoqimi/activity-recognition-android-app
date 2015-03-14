package project.praktikum.activity.reduction;

import java.util.Date;
import java.util.HashMap;

import android.util.Log;

public class NoiseReduction {

	private String TAG = "NoiseReduction";
	private boolean atHome = false;
	private String state = null;
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	private Date activityTimer = null;
	private HashMap<Date,String> bufferedActivities = new HashMap<Date,String>();
	private final HashMap<String,Boolean> TransitionsHomeTable = new HashMap<String,Boolean>(){
		private static final long serialVersionUID = 754254727691411266L;

	{
		put("Still:Walking", true);
		put("Still:Running", true);
		put("Still:OnBicycle", false);
		put("Still:InVehicle", false);
		put("Walking:Still", true);
		put("Walking:Running", true);
		put("Walking:OnBicycle", false);
		put("Walking:InVehicle", false);
		put("Running:Still", true);
		put("Running:Walking", true);
		put("Running:OnBicycle", false);
		put("Running:InVehicle", false);
		put("OnBicycle:Still", false);
		put("OnBicycle:Walking", false);
		put("OnBicycle:Running", false);
		put("OnBicycle:InVehicle", false);
		put("InVehicle:Still", false);
		put("InVehicle:Walking", false);
		put("InVehicle:Running", false);
		put("InVehicle:OnBicycle", false);
	}};
	
	private final HashMap<String,Boolean> TransitionsTable = new HashMap<String,Boolean>(){
		private static final long serialVersionUID = 8221455711900084373L;

	{
		put("Still:Walking", true);
		put("Still:Running", true);
		put("Still:OnBicycle", false);
		put("Still:InVehicle", false);
		put("Walking:Still", true);
		put("Walking:Running", true);
		put("Walking:OnBicycle", false);
		put("Walking:InVehicle", false);
		put("Running:Still", true);
		put("Running:Walking", true);
		put("Running:OnBicycle", false);
		put("Running:InVehicle", false);
		put("OnBicycle:Still", false);
		put("OnBicycle:Walking", false);
		put("OnBicycle:Running", false);
		put("OnBicycle:InVehicle", false);
		put("InVehicle:Still", false);
		put("InVehicle:Walking", false);
		put("InVehicle:Running", false);
		put("InVehicle:OnBicycle", false);
	}};
	private final HashMap<String,Integer> ThresholdTable = new HashMap<String,Integer>(){
		private static final long serialVersionUID = -5418684736378684996L;

	{
		put("Still:OnBicycle", 150);
		put("Still:InVehicle", 30);
		put("Walking:OnBicycle", 60);
		put("Walking:InVehicle", 60);
		put("Running:OnBicycle", 60);
		put("Running:InVehicle", 60);
		put("OnBicycle:Still", 60);
		put("OnBicycle:Walking", 60);
		put("OnBicycle:Running", 60);
		put("OnBicycle:InVehicle", 150);
		put("InVehicle:Still", 120);
		put("InVehicle:Walking", 30);
		put("InVehicle:Running", 30);
		put("InVehicle:OnBicycle", 150);
	}};
	
	public NoiseReduction(boolean atHome) {
		this.atHome = atHome;
		state = "Still";
		this.activityTimer = new Date();
	}
	
	public HashMap<Date,String> getActivities () {
		HashMap<Date,String> temp = new HashMap<Date,String>(this.bufferedActivities);
		bufferedActivities.clear();
		return temp;
	}
	private boolean isThresholdPassed (String activity, Date detectionTime) {
		String key = state + ":" + activity;
		int threshold = 0;
		if(ThresholdTable.containsKey(key)) {
			threshold =  ThresholdTable.get(key);
		int passedTime = (int) (detectionTime.getTime() - activityTimer.getTime());
		passedTime = passedTime / 1000;
		Log.i(TAG, "Passed Time is: "+passedTime + " and threshold was: " + threshold);
		if (passedTime > threshold)
			return true;
		else 
			return false;
		}
		return false;
		}
	
	private boolean isTransitionAllowed (String activity) {
		if (atHome)
			return TransitionsHomeTable.get(state+":"+activity).booleanValue();
		else 
			return TransitionsTable.get(state+":"+activity).booleanValue();
	}
	
	public void setAtHome(boolean isAtHome){
		if(isAtHome)
			state = "Still";
		this.atHome = isAtHome;
		
	}
	
	public boolean newActivityDetected(String activity) {
	
		Date now = new Date();
		Log.i(TAG, "New activity detected while State is: "+state+" and activity is: "+activity);
		if (this.atHome){
			if (activity.equals("OnBicycle") || activity.equals("InVehicle")) {
				Log.i(TAG, "Illegal activity detected; Droping it and moving forward");
				return false;
			}
		if (state.equals(activity)){
			bufferedActivities.clear();
			bufferedActivities.put( now, activity);
			this.activityTimer = new Date();//reset the timer
			Log.i(TAG, "Circular Transition detected. Putting "+ activity + " into buffer.");
			return true;
		}
		if(isTransitionAllowed(activity)) {
			bufferedActivities.clear();
			bufferedActivities.put(now, activity);
			this.state = activity;
			this.activityTimer = new Date();
			Log.i(TAG, "Allowed Transition detected. Putting "+ activity + " into buffer.");
			return true;
		}
		}
		if(isThresholdPassed(activity,now)) {
			this.state = activity;
			bufferedActivities.put(now, activity);
			this.activityTimer = new Date();
			Log.i(TAG, "Expired Threshold detected. Putting "+ activity + " into buffer.");
			return true;
		}
		else {
			bufferedActivities.put(now, activity);
			Log.i(TAG, "Illegal Transition detected. Withholding "+ activity + " into buffer.");
			return false;
		}

	}
}
