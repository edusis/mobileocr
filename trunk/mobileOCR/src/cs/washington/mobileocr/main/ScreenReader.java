package cs.washington.mobileocr.main;

import java.util.HashMap;

import cs.washington.mobileocr.gestures.ScreenReaderGestureHandler;
import cs.washington.mobileocr.tts.TTSThread;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;

/*
 * Team Sparkplugs (Josh Scotland and Hussein Yapit)
 * This is the main activity for the MobileOCR application.
 * The application uses text to speech to output information
 * TODO: Fix the space when the person moves on a space, it skips / plays spaces
 * TODO: BUG: on triple swipes or on auto playing, tapping to stop will replay the sentence
 */

public class ScreenReader extends Activity {

	private GestureDetector gestureScanner;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screenreader);
		
		Bundle extras = this.getIntent().getExtras();
		String passedString = null;
		
		passedString = extras != null ? extras.getString("res"): "TEST String";
		
		ScreenReaderGestureHandler gHandler = new ScreenReaderGestureHandler(passedString);
		TTSThread.getInstance().ttsSetContext(this);
		TTSThread.getInstance().ttsSetUtteranceListener(gHandler);
		gestureScanner = new GestureDetector(gHandler);
	}

	@Override
	public boolean onTouchEvent(MotionEvent me) {
		return gestureScanner.onTouchEvent(me);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//speakInstructions();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onDestroy();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

}
