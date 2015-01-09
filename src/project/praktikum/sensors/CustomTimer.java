package project.praktikum.sensors;

import android.os.CountDownTimer;

/**
 * Since we have so many scheduling tasks that we have to count down 
 * I decided to develop this class so that anybody else can use it easier
 * 
 * 
 * 
 * @author Jalal
 *
 */

public class CustomTimer {

	private boolean isFinished;
	private CountDownTimer countDownTimer;
	private int currentTime;
	
	public CustomTimer() {
		// TODO Auto-generated constructor stub
		this.isFinished = true;
	}
	
	
	/**
	 * Start to count down
	 * @param countDown seconds you want to set your countdownt timer
	 */
	public void startCountDownTimer(int countDown)
	{
		isFinished = false;
		currentTime = -1;
		countDownTimer = new CountDownTimer(countDown * 1000, 1000)
		{

		     public void onTick(long millisUntilFinished)
		     {
		    	 currentTime++;
		     }

		     public void onFinish() 
		     {
		    	 isFinished = true;
		     }
		};
		countDownTimer.start();
	}
	
	/**
	 * Stop counting down
	 */
	public void stop()
	{
		countDownTimer.cancel();
	}

	/**
	 * check if counter is finished or not
	 * @return boolean if false counter is still counting down
	 */
	public boolean isFinished() {
		return isFinished;
	}

	/**
	 * you can seed how long it remains
	 * @return
	 */
	public int getCurrentTime() {
		return currentTime;
	}
}












