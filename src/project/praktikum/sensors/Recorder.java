package project.praktikum.sensors;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;


/**
 * records ambient sound in .WAV format due to further analysis 
 * sample usage:
 * 
 * Recorder recorder = new Recorder();
 * recorder.startRecording();
 * Thread.Sleep(desired length);
 * recorder.stopRecording();
 *  
 * @author Jalal
 *
 */

public class Recorder
{
	private static int[] mSampleRates = new int[] { 44100,22050,11025,8000};
	 
	private AudioRecord recorder = null;
	private int bufferSize = 0;
	private Thread recordingThread = null;
	private boolean isRecording = false;
	private int threshold = 1000;
	private int sumLoud = 0;
	private int maxLoud;

	public void setThreshold(int threshold) {
		this.threshold = threshold;
		sumLoud = 0;
	}

	public Recorder(int maxLoud)
	{
		this.maxLoud = maxLoud;
		//this.txt = txt;
	}
	
	private AudioRecord findAudioRecord() {
	    for (int rate : mSampleRates) {
	        for (short audioFormat : new short[] {AudioFormat.ENCODING_PCM_16BIT }) {
	            for (short channelConfig : new short[] {AudioFormat.CHANNEL_IN_STEREO , AudioFormat.CHANNEL_IN_MONO}) {
	                try {
	                    //Log.d(this.TAG, "Attempting rate " + rate + "Hz, bits: " + audioFormat + ", channel: "
	                    //        + channelConfig);
	                    bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

	                    if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
	                        // check if we can instantiate and have a success
	                        AudioRecord recorder = new AudioRecord(AudioSource.DEFAULT,
	                        		rate,
	                        		channelConfig,
	                        		audioFormat,
	                        		bufferSize);

	                        if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
	                        {
	                            return recorder;
	                        }
	                    }
	                } catch (Exception e) {
	                    //Log.e(TAG, rate + "Exception, keep trying.",e);
	                }
	            }
	        }
	    }
	    return null;
	}
	     
	public void startRecording()
	{
		// TODO Auto-generated method stub
		recorder = findAudioRecord();
		
        if(recorder.getState()==AudioRecord.STATE_INITIALIZED)
            recorder.startRecording();
         
        isRecording = true;
	
        recordingThread = new Thread(new Runnable() {
                 
                @Override
                public void run() {
                	analyzeRecordedBuffer();
                }
        },"AudioRecorder Thread");
         
        recordingThread.start();
	}
	
	private void analyzeRecordedBuffer()
	{
		byte data[] = new byte[bufferSize];
		
		int read = 0;
		
		while(isRecording)
		{
			read = recorder.read(data, 0, bufferSize);
				 
			if(AudioRecord.ERROR_INVALID_OPERATION != read)
			{
				int [] avg = RMS(data);
				avg[0]++;
			}
		}
	}
	
	private int[] RMS(byte[] input)
	{
		int [] ans = new int[input.length/100 + 1];
		for(int i = 0 ; i * 100 < input.length ; i++)
		{
			int j;
			int sum = 0;
			for(j = 0 ;i * 100 + j < input.length && j < 100 ; j++)
			{
				sum += (input[i * 100 + j] * input[i * 100 + j]);
			}
			ans[i] = (int) Math.sqrt((double)(sum/j));
			if(ans[i] > threshold)
			{
				sumLoud++;
				Log.i("sumLoud" , String.valueOf(sumLoud));
			}
		}
		return ans;
	}
	
	public boolean stopRecording()
	{
		if(null != recorder)
		{
			isRecording = false;
			if(recorder.getState()==1)
			{
			    recorder.stop();
			}
			recorder.release();
			 
			recorder = null;
			recordingThread = null;
		}
		return maxLoud < sumLoud ? false : true;
	}
}
