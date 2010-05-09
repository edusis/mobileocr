package washington.cs.mobileocr.tts;

/**
 * Copyright 2010, Josh Scotland & Hussein Yapit
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided you follow the BSD license.
 * 
 * 
 * This class handles all text to speech functions.
 */

import java.util.HashMap;

import washington.cs.mobileocr.gestures.ScreenReaderGestureHandler;
import washington.cs.mobileocr.main.R;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;

public class TTSHandler implements OnUtteranceCompletedListener{

	private final static String TAG = "TTSHandler";
	private final int queueMode = TextToSpeech.QUEUE_FLUSH;
	
	private static boolean mTtsInitialized;
	private  TextToSpeech mTts;
	private static TTSHandler ttsThread;
	private HashMap<String, String> ttsParams;
	private Resources res;
	private static Boolean doneSpeaking = true;

	//Play speech based on where the message came from
	private final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (mTtsInitialized) {
				setDoneSpeaking(false);
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

	//Create an instance of the class
	private TTSHandler() {
		ttsThread = this;
		ttsParams = new HashMap<String, String>();
		mTtsInitialized = false;
	}

	//Return the instance of the class
	public static TTSHandler getInstance() {
		if (ttsThread == null)
			ttsThread = new TTSHandler();
		return ttsThread;
	}
	
	//Free up resources when the TTS is done being used
	public void TTSDestroy() {
		mTts.shutdown();
	}

	//Set the context for the TTS
	public void ttsSetContext(Context context) {
		mTtsInitialized = false;
		mTts = new TextToSpeech(context, ttsInitListener);
	}

	//Set the context for the TTS
	public void ttsSetContext(Context context, Resources res) {
		ttsSetContext(context);
		this.res = res;
	}

	//Queue the message for the TTS engine
	public static void ttsQueueMessage(int type) {
		Message msg = TTSHandler.getInstance().mHandler.obtainMessage(type);
		TTSHandler.getInstance().mHandler.sendMessage(msg);
	}

	//Queue the message received from the screen reader for the TTS engine
	public static void ttsQueueSRMessage(String text) {
		Message msg = TTSHandler.getInstance().mHandler.obtainMessage(R.id.screenreader_tts, text);
		TTSHandler.getInstance().mHandler.sendMessage(msg);
	}

	//Set the parameters of the TTS HashMap
	public void setParam(String key, String value) {
		ttsParams.put(key, value);
	}

	//Clear the parameters of the TTS HashMap
	public void clearParams() {
		ttsParams.clear();
	}

	//Stop the speaking TTS
	public void ttsStop() {
		if (mTts != null) {
			mTts.stop();
			setDoneSpeaking(true);
		}
	}

	//Initialization of the TTS, set the listener
	private final TextToSpeech.OnInitListener ttsInitListener = new TextToSpeech.OnInitListener() {
		public void onInit(int status) {
			Log.d(TAG, "TTS initialization");
			mTtsInitialized = true;
			mTts.setOnUtteranceCompletedListener(TTSHandler.getInstance());
			TTSHandler.ttsQueueMessage(R.string.tts_init);
		}
	};

	//Listen for when the TTS is finished speaking (used in autoplay)
	public void onUtteranceCompleted(String uttId) {
		Log.d(TAG, "Utterance Complete");
		setDoneSpeaking(true);
		if (uttId.equals("Sentences")) {
			ScreenReaderGestureHandler.autoplaySentences();
			setDoneSpeaking(false);
		}
	}

	//Allows for publicly setting the variable
	public void setDoneSpeaking(Boolean doneSpeaking) {
		TTSHandler.doneSpeaking = doneSpeaking;
	}

	//Allows for publicly getting the variable
	public static Boolean getDoneSpeaking() {
		return doneSpeaking;
	}
}