package cs.washington.mobileocr.main;

import cs.washington.mobileocr.gestures.ScreenReaderGestureHandler;
import cs.washington.mobileocr.tts.TTSHandler;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

/*
 * Josh Scotland and Hussein Yapit
 * This is the screen reader activity
 * TODO: Fix autoplay functions
 * TODO: Fix not recognizing not playing (i.e. need two taps to play instead of one)
 */

public class ScreenReader extends Activity {

	private GestureDetector gestureScanner;
	private static final String TAG = TTSHandler.class.getSimpleName();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "Screenreader started");
		
		setContentView(R.layout.screenreader);

		Bundle extras = this.getIntent().getExtras();
		String passedString = null;
		passedString = extras != null ? extras.getString("res"): getString(R.string.tts_demo);

		ScreenReaderGestureHandler gHandler = new ScreenReaderGestureHandler(passedString);
		TTSHandler.getInstance().ttsSetContext(this, this.getResources());
		
		if (!TTSHandler.getInstance().ttsSetUtteranceListener(gHandler)) {
			Log.e(TAG, "UtteranceListener not set");
		}
		
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
