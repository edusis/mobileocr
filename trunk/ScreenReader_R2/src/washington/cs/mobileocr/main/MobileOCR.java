
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

import washington.cs.mobileocr.gestures.ScreenReaderGestureHandler;
import washington.cs.mobileocr.tts.TTSHandler;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class MobileOCR extends Activity {

	private static final String TAG = "MobileOCR";
	private int MY_DATA_CHECK_CODE;
	
	private GestureDetector gestureScanner;
	private static String[] sentenceArray;
	private String[] wordArray;
	private static int[] wordsInSentences;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Clear title bar and notification bar
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		setContentView(R.layout.main);
		
		//Initialize TTS engine and check for correct installation of TTS engine
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
		
		String passedString = "This is some test text for the screen reader application you are using. You can tap and hold the screen for instructions. Here is one more sentence just for the heck of it!";
		
		sentenceArray = TextParser.sentenceParse(passedString);
		wordArray = TextParser.wordParse(sentenceArray);
		wordsInSentences = TextParser.countWordsInSentence(sentenceArray);
		
		TextView text = (TextView) findViewById(R.id.text);
        text.setText(passedString);
        
        //Initialize gesture detector
        ScreenReaderGestureHandler gHandler = new ScreenReaderGestureHandler(sentenceArray, wordsInSentences, wordArray);
		gestureScanner = new GestureDetector(gHandler);
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

	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
	}

	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
	}

	public void onDestroy() {
		super.onDestroy();
		TTSHandler.getInstance().TTSDestroy();
	}
}