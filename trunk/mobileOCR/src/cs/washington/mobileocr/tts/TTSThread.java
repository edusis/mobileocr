/**
 * @author - Hussein Yapit
 */

package cs.washington.mobileocr.tts;


import java.util.HashMap;

import cs.washington.mobileocr.main.OCRThread;
import cs.washington.mobileocr.main.R;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;

public class TTSThread {
	
	private static boolean mTtsInitialized;
	private static TextToSpeech mTts;
	private static TTSThread ttsThread;
	private static final String TAG = TTSThread.class.getSimpleName();
	private static final int queueMode = TextToSpeech.QUEUE_FLUSH;
	private static HashMap<String, String> ttsParams;
	private Resources res;

	private final Handler mHandler = new Handler() {
			
		public void handleMessage(Message msg) {
			if (mTtsInitialized) {
				switch (msg.what) {
				case R.id.tts_init:
					mTts.speak(res.getString(R.string.ttsInit), queueMode, ttsParams);
					break;
				case R.id.tts_welcome:
					mTts.speak(res.getString(R.string.ttsHello), queueMode, ttsParams);
					break;
				case R.id.tts_sreader:
					mTts.speak((String)msg.obj, queueMode, ttsParams);
					break;
				case R.id.tts_quit:
					getLooper().quit();
					break;
				default:
					super.handleMessage(msg);
				}
			}
		}
	};

	private TTSThread()
	{

		ttsThread = this;
		ttsParams = new HashMap<String, String>();
		mTtsInitialized = false;
		//onLooperPrepared();
	}
	 
	public static TTSThread getInstance()
	{
		if (ttsThread == null)
		{
			ttsThread = new TTSThread();
		}
		return ttsThread;
	}
	 
	public void ttsSetContext(Context context) {
		mTtsInitialized = false;
		mTts = new TextToSpeech(context, ttsInitListener);
	}
	 
	public void ttsSetContext(Context context, Resources res) {
		ttsSetContext(context);
		this.res = res;
	}
	public boolean ttsSetUtteranceListener (OnUtteranceCompletedListener listener) {
		if (mTts != null) {
			mTts.setOnUtteranceCompletedListener(listener);
			return true;
		}
		return false;
	}
	
	public static void ttsQueueMessage(int type) {
		Message msg = TTSThread.getInstance().mHandler.obtainMessage(type);
		TTSThread.getInstance().mHandler.sendMessage(msg);
	}
	 
	public static void ttsQueueSRMessage(String text) {
		Message msg = TTSThread.getInstance().mHandler.obtainMessage(R.id.tts_sreader, text);
		TTSThread.getInstance().mHandler.sendMessage(msg);
	}
	
	public static void setParam(String key, String value) {
		ttsParams.put(key, value);
	}
	
	public static void clearParams() {
		ttsParams.clear();
	}
	
	public void ttsStop() {
		if (mTts != null) {
			mTts.stop();
			
		}
	}
	
	
	private final static TextToSpeech.OnInitListener ttsInitListener = new TextToSpeech.OnInitListener() {

		public void onInit(int status) {
			mTtsInitialized = true;
			TTSThread.ttsQueueMessage(R.id.tts_init);
			
		}
	};
}
