
package washington.cs.mobileocr.main;

/**
 * Copyright 2010, Josh Scotland & Hussein Yapit
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided you follow the BSD license.
 * 
 * 
 * This class is the screen reader activity. It uses the
 * screen reader gesture handler to navigate the text.
 * TODO: OnPause saves the current state of the screen reader
 */

import washington.cs.mobileocr.gestures.NavigationGestureHandler;
import washington.cs.mobileocr.tts.TTSHandler;
import washington.cs.mobileocr.weocr.OCRThread;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

public class MobileOCR extends Activity {

	private static final String INSTRUCTION_KEY = "INSTRUCTION_KEY";
	private static final String TAG = "MobileOCR";

	public static boolean instructionFlag = true;
	private GestureDetector gestureScanner;
	private ConnectivityManager mConnectivityManager;
	private CameraFacade cameraFacade;
	private OCRThread mOCRThread;
	private int MY_DATA_CHECK_CODE;
	private Vibrator mVibrator = null;
	private boolean mIsNotified = true;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Clear title bar and notification bar
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 

		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		mConnectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		initNetworkNotify();

		SurfaceView view = new SurfaceView(this);
		cameraFacade = new CameraFacade(this.getApplicationContext(), view.getHolder(), mHandler);
		setContentView(view);

		//Restore preferences
		restoreState();

		//Initialize gesture detector
		gestureScanner = new GestureDetector(new NavigationGestureHandler());

		//Initialize TTS engine and check for correct installation of TTS engine
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
	}

	//Method that checks to make sure the TTS is initialized correctly
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == MY_DATA_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				//Success, create the TTS instance
				Log.d(TAG, "TTS engine check");
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

	//Check to see if network connection exists
	private final BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver () {
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "Connectivity action broadcast");
			networkNotify();
		}
	};

	//Initial network checking. Similar to networkNotify() except this function
	//is not hooked to a network receiver
	private void initNetworkNotify() {
		NetworkInfo netInfo = mConnectivityManager.getActiveNetworkInfo();
		if (netInfo == null) {
			long[] pattern = {200, 200, 200};
			mVibrator.vibrate(pattern, 0);
		}
		else {
			mVibrator.vibrate(200);
		}
	}

	//Provide network availability feedback to user using vibration. 
	//It notifies the user if the network has been lost while MobileOCR is running
	private void networkNotify() {
		NetworkInfo netInfo = mConnectivityManager.getActiveNetworkInfo();
		if (netInfo == null) {
			if (mIsNotified) {
				long[] pattern = {200, 200, 200};
				mVibrator.vibrate(pattern, 0);
				mIsNotified = false;
			}
		} else {
			int netType = netInfo.getType();
			if (!mIsNotified) {
				switch (netType) {
				case ConnectivityManager.TYPE_WIFI:
					mVibrator.vibrate(200);
					break;
				case ConnectivityManager.TYPE_MOBILE:
					mVibrator.vibrate(200 );
					break;
				default:

					break;
				}
				this.mIsNotified = true;
			}
		}
	}

	//Initializes the OCRThread
	private void startOCRThread () {
		assert(mOCRThread == null);
		mOCRThread = new OCRThread(mHandler);
		mOCRThread.start();
	}

	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
		startOCRThread();
		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(mConnectivityReceiver, filter, null, mHandler);
		cameraFacade.onResume();
	}

	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
		unregisterReceiver(mConnectivityReceiver);
		this.mIsNotified = false;
		
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
				cameraFacade.requestAutoFocus();
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_CAMERA) {
			if (event.getRepeatCount() == 0) {
				cameraFacade.requestPreviewFrame();
			}
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	//Creates an intent to start ScreenReader Activity
	private void startScreenReaderView(String result) {
		Intent i = new Intent(this, ScreenReader.class);
		i.putExtra("resultString", result);
		startActivity (i);
	}

	//Restore preferences
	private void restoreState() {
		SharedPreferences settings = getPreferences(Activity.MODE_PRIVATE);
		Boolean toggleInstruction = settings.getBoolean(INSTRUCTION_KEY, true);
		instructionFlag = toggleInstruction;
	}

	//Message handler on navigation activity
	private final Handler mHandler = new Handler () {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case R.id.msg_camera_auto_focus:
				int status = msg.arg1;
				cameraFacade.clearAutoFocus();
				if (status == CameraFacade.AUTOFOCUS_SUCCESS) {
					cameraFacade.requestPreviewFrame();
				}
				break;
			case R.id.msg_camera_preview_frame:
				Handler ocrHandler = mOCRThread.getHandler();
				int width = cameraFacade.getWidth();
				int height = cameraFacade.getHeight();
				Message preprocessMsg = ocrHandler.obtainMessage(R.id.msg_ocr_recognize, width, height, msg.obj);
				ocrHandler.sendMessage(preprocessMsg);
				break;
			case R.id.msg_ui_ocr_success:
				cameraFacade.onPause();
				startScreenReaderView((String)msg.obj);
				break;
			case R.id.msg_ui_ocr_fail:
				cameraFacade.startPreview();
				break;
			}
		}
	};
}