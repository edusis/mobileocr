
package mobileocr.main;

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

import mobileocr.gestures.ScreenReaderGestureHandler;
import mobileocr.tts.TTSHandler;
import mobileocr.main.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class ScreenReader extends Activity {

	private static final String TAG = "ScreenReader";
	
	private GestureDetector gestureScanner;
	private static String[] sentenceArray;
	private String[] wordArray;
	private static int[] wordsInSentences;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "Screenreader started");

		//Remove the title frame
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.screenreader);
		
		Bundle extras = this.getIntent().getExtras();
		String passedString = null;
		
		//Check to see if we already parsed the given text (wouldn't want to do it twice)
		if (passedString == null || !passedString.equals(extras.getString("resultString"))) {
			passedString = extras != null ? extras.getString("resultString"): "Error: The string is not correct";
			//passedString = passedString.replaceAll("\\s+", "");
			//passedString = passedString.replaceAll("[.?!]+\\n", "\\n");
			String cleanedPassedString = passedString.replaceAll("\\n+|\\t+|\\s+", "");
			
			//Check to see if the text file is empty
			if (cleanedPassedString.equals("[ ]+") || cleanedPassedString.equals(""))
				passedString = "There are no OCR results";
			sentenceArray = TextParser.sentenceParse(passedString);
			wordArray = TextParser.wordParse(sentenceArray);
			wordsInSentences = TextParser.countWordsInSentence(sentenceArray);
		}
		
		//Show the text we received after OCR
		TextView text = (TextView) findViewById(R.id.text);
        text.setText(passedString);
        
		ScreenReaderGestureHandler gHandler = new ScreenReaderGestureHandler(sentenceArray, wordsInSentences, wordArray);
		gestureScanner = new GestureDetector(gHandler);
	}

	public boolean onTouchEvent(MotionEvent me) {
		return gestureScanner.onTouchEvent(me);
	}

	protected void onResume() {
		super.onResume();
		TTSHandler.ttsQueueSRMessage("In the screen reader");
	}

	protected void onPause() {
		super.onPause();
	}

	public void onStop() {
		super.onDestroy();
	}

	public void onDestroy() {
		super.onDestroy();
	}

}
