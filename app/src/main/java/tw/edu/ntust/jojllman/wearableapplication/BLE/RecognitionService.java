package tw.edu.ntust.jojllman.wearableapplication.BLE;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class RecognitionService extends Service
{
	private final static String TAG = RecognitionService.class.getSimpleName();
	
	/***********************************************************
	 * JNI Part
	 */
	
	static
	{
		System.loadLibrary("SignLanguageRecognitionSystem");
		Log.d(TAG,"loadLibrary SignLanguageRecognitionSystem");
	}
	
	private native void initialize();
	private native void reset();
	private native void pass_data(float[] dataL, float[] dataR);
	private native boolean run_recognize();
	private native byte[] getString();
	
	/***********************************************************
	 * Service Binding Part
	 */
	
	// Binder given to client (Activity).
	private final IBinder mBinder = new ServiceBinder();
	
	public class ServiceBinder extends Binder
	{
		public RecognitionService getService()
		{
			return RecognitionService.this;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent)
	{
		Log.d(TAG, "onBind");
		
		// Initialize the SLR system
		initialize();
		
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent)
	{
		Log.d(TAG, "onUnbind");
		return super.onUnbind(intent);
	}

	/***********************************************************
	 * Service Public Functions
	 */
	
	private BlockingQueue<GloveSignData> mSignDatas;
	private Thread mRecognizeThread;
	private boolean mIsRecognizeThreadRunning = false;
	
	public void startRecognition()
	{
		reset();
		
		mSignDatas = new LinkedBlockingQueue<>();
		
		mRecognizeThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				recognizeSignData();
			}
		});
		
		mIsRecognizeThreadRunning = true;
		mRecognizeThread.start();
	}
	
	public void stopRecognition()
	{
		if (mIsRecognizeThreadRunning)
		{
			mIsRecognizeThreadRunning = false;
			mRecognizeThread.interrupt();
			mRecognizeThread = null;
		}
	}
	
	public void pushSignData(GloveSignData signData)
	{
		try
		{
			Log.d(TAG,"Pushing recognition data.");
			// Push data for recognition
			mSignDatas.put(signData);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	/***********************************************************
	 * Service Private Functions
	 */
	
	private void runRecognitionJNI (GloveSignData signData)
	{
		pass_data(signData.getL(), signData.getR());
		if (run_recognize())
		{					
			byte[] word = getString();
			try
			{
				String nativeString = new String(word, "US-ASCII");
				mListener.onRecognized(nativeString);
			}
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private void recognizeSignData()
	{
		try
		{
			Log.d(TAG, "Start recognition!!");
			
			while (mIsRecognizeThreadRunning)
			{
				if (mSignDatas.size() > 0)
				{
					Log.d(TAG, "Run recognition!!" +  mSignDatas.size());
					runRecognitionJNI(mSignDatas.take());
				}
			}
			
			// Recognition process over
			Log.d(TAG, "Finish recognition!!");
			mListener.onRecognizeFinished();
		} 
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	/***********************************************************
	 * Service Listener Part
	 */
	
	// Call-back listener
	private RecognitionServiceListener mListener = null;
	
	// Register a call-back listener
	public void registerListener (RecognitionServiceListener listener)
	{
		mListener = listener;
	}
	
	public void unregisterListener (RecognitionServiceListener listener)
	{
		mListener = null;
	}
	
	private float f (String string)
	{
		return Float.valueOf(string);
	}
}
