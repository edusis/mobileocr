package mobileocr.main;

/**
 * Copyright 2010, Josh Scotland & Hussein Yapit
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided you follow the BSD license.
 */

import mobileocr.gestures.NavigationGestureHandler;
import mobileocr.tts.TTSHandler;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

/*
 * This class is the screen reader activity. It uses the
 * screen reader gesture handler to navigate the text.
 */

public class MobileOCR extends Activity {

	private static final String INSTRUCTION_KEY = "INSTRUCTION_KEY";
	private static final String TAG = "Mobile OCR";

	public static boolean instructionFlag = true;
	private GestureDetector gestureScanner;
	private static ConnectivityManager mConnectivityManager;
	private int ttsDataCheck;

	private CameraFacade mPreview;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Clear title bar and notification bar
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 

		// Restore preferences
		//restoreState();

		// Check for a network connection
		mConnectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

		// Initialize TTS engine and check for correct installation of TTS engine
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, ttsDataCheck);

		// Initialize gesture detector
		gestureScanner = new GestureDetector(new NavigationGestureHandler());

		// Begin the camera
		mPreview = new CameraFacade(this, mHandler);
		setContentView(mPreview);
	}

	//Method that checks to make sure the TTS is initialized correctly
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ttsDataCheck) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				//Success, create the TTS instance
				Log.d(TAG, "TTS engine is installed");
				TTSHandler.getInstance().ttsSetContext(this, this.getResources());
			} else {
				//Missing data, install it
				Intent installIntent = new Intent();
				installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		}
	}

	//Send a gesture notification
	public boolean onTouchEvent(MotionEvent event) {
		return gestureScanner.onTouchEvent(event);
	}

	// Returns true if there is no network
	public static boolean initNetworkNotify() {
		NetworkInfo netInfo = mConnectivityManager.getActiveNetworkInfo();
		return netInfo == null;
	}

	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
		//IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		//registerReceiver(mConnectivityReceiver, filter, null, mHandler);
		//cameraFacade.onResume();

		if (MobileOCR.initNetworkNotify())
			TTSHandler.ttsQueueMessage(R.string.tts_no_network);
	}

	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
		//unregisterReceiver(mConnectivityReceiver);

		//Store preferences
		SharedPreferences state = getPreferences(Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = state.edit();
		editor.putBoolean(INSTRUCTION_KEY, instructionFlag);
	}

	public void onDestroy() {
		super.onDestroy();
		TTSHandler.getInstance().TTSDestroy();
	}

	//Key down events for the camera buttons
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_FOCUS) {
			if (event.getRepeatCount() == 0) {
				mPreview.requestAutoFocus();
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_CAMERA) {
			if (event.getRepeatCount() == 0) {
				mPreview.takePicture();
			}
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	/*
	//Restore preferences
	private void restoreState() {
		SharedPreferences settings = getPreferences(Activity.MODE_PRIVATE);
		Boolean toggleInstruction = settings.getBoolean(INSTRUCTION_KEY, true);
		instructionFlag = toggleInstruction;
	}
	 */

	//Message handler on navigation activity
	private final Handler mHandler = new Handler () {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case R.id.msg_ocr_success:
				//cameraFacade.onPause();
				Intent i = new Intent(MobileOCR.this, ScreenReader.class);
				i.putExtra("resultString", (String)msg.obj);
				startActivity (i);
				break;
			case R.id.msg_ocr_fail:
				//cameraFacade.startPreview();
				break;
			}
		}
	};
}