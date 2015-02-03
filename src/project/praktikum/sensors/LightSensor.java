package project.praktikum.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;

/**
 * 
 * @author Jalal
 *
 */

public class LightSensor implements SensorEventListener{

	 private SensorManager mSensorManager;
	 private Sensor mLight;
	 private float currentLux = 0;
	 private int counter = 0;
	 private float Threshold;
	 Context ctx;
	
	 public LightSensor(Context ctx , float Threshold) 
	 {
		 this.Threshold = Threshold;
		 this.ctx = ctx;
	     mSensorManager = (SensorManager)ctx.getSystemService(Context.SENSOR_SERVICE);
	     mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
	 }
	 
	 public void startListening()
	 {
	     mSensorManager.registerListener((SensorEventListener) this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
	 }
	 
	 public boolean stopListening()
	 {
	     mSensorManager.unregisterListener((SensorListener) ctx);
	     return currentLux > Threshold ? false : true;
	 }
	
	 private void calcCurrentLux(float lux)
	 {
		 currentLux = currentLux * counter + lux;
		 counter++;
		 currentLux /= (float) counter;
	 }
	 
	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		calcCurrentLux(event.values[0]);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

}
