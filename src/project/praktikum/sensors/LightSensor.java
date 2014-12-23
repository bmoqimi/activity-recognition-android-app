package project.praktikum.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class LightSensor implements SensorEventListener{

	 private SensorManager mSensorManager;
	 private Sensor mLight;
	 int minLux = 0;
	 public int currentLux = 0;
	 int maxLux;
	 Context ctx;
	
	 public LightSensor(Context ctx) 
	 {
		 this.ctx = ctx;
	     mSensorManager = (SensorManager)ctx.getSystemService(Context.SENSOR_SERVICE);
	     mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
	 }
	 
	 public void startListening()
	 {
	     mSensorManager.registerListener((SensorEventListener) this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
	 }
	 
	 public void stopListening()
	 {
	     //mSensorManager.unregisterListener((SensorListener) ctx);
	 }
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		currentLux = (int) event.values[0];
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

}
