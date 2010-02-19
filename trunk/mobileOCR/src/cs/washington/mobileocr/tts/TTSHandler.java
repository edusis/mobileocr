package cs.washington.mobileocr.tts;

import java.util.HashMap;
import cs.washington.mobileocr.main.R;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;

/**
 * Hussein Yapit & Josh Scotland
 * A TTS handler to play, pause, and queue TTS
 */

public class TTSHandler {

	private static boolean mTtsInitialized;
	private TextToSpeech mTts;
	private static TTSHandler ttsThread;
	private final static String TAG = "TTS";
	private final int queueMode = TextToSpeech.QUEUE_FLUSH;
	private HashMap<String, String> ttsParams;
	private Resources res;

	private final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (mTtsInitialized) {
				switch (msg.what) {

				case R.id.screenreader_tts:
					mTts.speak((String)msg.obj, queueMode, ttsParams);
					break;
				default:
					String ttsText = res.getString(msg.what);
					
					if (ttsText != null) {
						mTts.speak(ttsText, queueMode, ttsParams);
					}
					else {
						Log.e(TAG , "TTS text undefined in resources." );
					}
				}
			}
		}
	};

	private TTSHandler() {
		ttsThread = this;
		ttsParams = new HashMap<String, String>();
		mTtsInitialized = false;
	}

	public static TTSHandler getInstance() {
		if (ttsThread == null)
			ttsThread = new TTSHandler();
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
		Message msg = TTSHandler.getInstance().mHandler.obtainMessage(type);
		TTSHandler.getInstance().mHandler.sendMessage(msg);
	}

	public static void ttsQueueSRMessage(String text) {
		Message msg = TTSHandler.getInstance().mHandler.obtainMessage(R.id.screenreader_tts, text);
		TTSHandler.getInstance().mHandler.sendMessage(msg);
	}

	public void setParam(String key, String value) {
		ttsParams.put(key, value);
	}

	public void clearParams() {
		ttsParams.clear();
	}

	public void ttsStop() {
		if (mTts != null)
			mTts.stop();
	}

	private final static TextToSpeech.OnInitListener ttsInitListener = new TextToSpeech.OnInitListener() {
		public void onInit(int status) {
			Log.d(TAG, "TTS init");
			mTtsInitialized = true;
			TTSHandler.ttsQueueMessage(R.string.tts_init);
		}
	};
}