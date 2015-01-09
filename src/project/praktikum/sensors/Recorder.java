package project.praktikum.sensors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Environment;


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

	private static final int RECORDER_BPP = 16;
	private static String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
	private static String AUDIO_RECORDER_FOLDER = "AudioRecorder";
	private static String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
	private static int RECORDER_SAMPLERATE = 44100;
	private static boolean writeDataIsFinished = false;
	private static int[] mSampleRates = new int[] { 44100,22050,11025,8000};
	 
	private AudioRecord recorder = null;
	private int bufferSize = 0;
	private Thread recordingThread = null;
	private boolean isRecording = false;
	
	
	public Recorder()
	{
	}
	     
	private String getFilename()
	{
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath,AUDIO_RECORDER_FOLDER);
		 
		if(!file.exists()){
		        file.mkdirs();
		}
		 
		return (file.getAbsolutePath() 
				+ "/" 
				+ System.currentTimeMillis() 
				+ AUDIO_RECORDER_FILE_EXT_WAV);
	}
	     
	private String getTempFilename()
	{
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath,AUDIO_RECORDER_FOLDER);
		 
		if(!file.exists()){
		        file.mkdirs();
		}
		 
		File tempFile = new File(filepath,AUDIO_RECORDER_TEMP_FILE);
		 
		if(tempFile.exists())
		        tempFile.delete();
		 
		return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
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
                        writeAudioDataToFile();
                }
        },"AudioRecorder Thread");
         
        recordingThread.start();
	}
	
	private void writeAudioDataToFile()
	{
		writeDataIsFinished = false;
		byte data[] = new byte[bufferSize];
		String filename = getTempFilename();
		FileOutputStream os = null;
		 
		try 
		{
			os = new FileOutputStream(filename);
		} 
		catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		int read = 0;
		 
		if(null != os)
		{
			while(isRecording)
			{
				read = recorder.read(data, 0, bufferSize);
				 
				if(AudioRecord.ERROR_INVALID_OPERATION != read)
				{
					try
					{
					        os.write(data);
					} 
					catch (IOException e) 
					{
					        e.printStackTrace();
					}
				}
			}
		         
			try 
			{
				os.close();
			} 
			catch (IOException e) {
		        e.printStackTrace();
			}
			finally
			{
				writeDataIsFinished = true;
			}
		}
	}
	     
	public void stopRecording()
	{
		int channel = 1;
		if(null != recorder)
		{
			isRecording = false;
			if(recorder.getState()==1)
			{
				channel = recorder.getChannelCount();
			    recorder.stop();
			}
			recorder.release();
			 
			recorder = null;
			recordingThread = null;
		}
		 
		copyWaveFile(getTempFilename(),getFilename(),channel);
		deleteTempFile();
	}

	private void deleteTempFile()
	{
		File file = new File(getTempFilename());
		 
		file.delete();
	}
	     
	private void copyWaveFile(String inFilename,String outFilename , int channel)
	{
		FileInputStream in = null;
		FileOutputStream out = null;
		long totalAudioLen = 0;
		long totalDataLen = totalAudioLen + 36;
		long longSampleRate = RECORDER_SAMPLERATE;
		int channels = channel;
		long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels/8;
	         
        byte[] data = new byte[bufferSize];
	         
		try 
		{
			in = new FileInputStream(inFilename);
			out = new FileOutputStream(outFilename);
			totalAudioLen = in.getChannel().size();
			totalDataLen = totalAudioLen + 36;
			 
			//AppLog.logString("File size: " + totalDataLen);
			 
			WriteWaveFileHeader(out,
					totalAudioLen,
					totalDataLen,
					longSampleRate,
					channels,
					byteRate);
			 
			while(in.read(data) != -1)
			{
				out.write(data);
			}
			 
			in.close();
			out.close();
		} 
		catch (FileNotFoundException e)
		{
	        e.printStackTrace();
		} 
		catch (IOException e) 
		{
	        e.printStackTrace();
		}
	}

	private void WriteWaveFileHeader(
	                FileOutputStream out, long totalAudioLen,
	                long totalDataLen, long longSampleRate, int channels,
	                long byteRate) throws IOException
    {
	         
		byte[] header = new byte[44];
		 
		header[0] = 'R';  // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f';  // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1;  // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (2 * 16 / 8);  // block align
		header[33] = 0;
		header[34] = RECORDER_BPP;  // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
	}
}
