/**
 * @author - Hussein Yapit
 */

package cs.washington.mobileocr.tts;


import cs.washington.mobileocr.main.OCRThread;
import cs.washington.mobileocr.main.R;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.speech.tts.TextToSpeech;

public class TTSThread extends HandlerThread{
	private Handler mHandler;
	
	private static TextToSpeech mTts;
	
	private static TTSThread ttsThread;
	private static final String TAG = TTSThread.class.getSimpleName();
	 
	private TTSThread()
	{
		super(TAG);
		ttsThread = this;
	}
	 
	public static TTSThread getInstance()
	{
		if (ttsThread == null)
		{
			ttsThread = new TTSThread();
		}
		return ttsThread;
		
	}
	 
	 
	public static void ttsSetContext(Context context) {
		 mTts = new TextToSpeech(context, ttsInitListener);
	}
	 
	public static void ttsQueueMessage(int type) {
		Message msg = TTSThread.getInstance().mHandler.obtainMessage(type);
		TTSThread.getInstance().mHandler.sendMessage(msg);
	}
	 
	protected void onLooperPrepared () {
		mHandler = new Handler(getLooper()) {
	            
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case R.id.tts_welcome:
					//mTts.speak(text, queueMode, params)
					break;
					//more messages here
				case R.id.tts_quit:
					 getLooper().quit();
				default:
					super.handleMessage(msg);
				}
			}
		};
	}
	
	private final static TextToSpeech.OnInitListener ttsInitListener = new TextToSpeech.OnInitListener() {

		public void onInit(int status) {
			//Text to speech ready
		}
	};
}
