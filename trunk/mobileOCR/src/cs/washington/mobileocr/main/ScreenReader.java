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
		passedString = extras != null ? extras.getString("res"): "Look we are supposed to be winning the hearts and the minds of the natives. Isn't that the whole point of your little puppet show? You look like them and you talk like them and they will start trusting us. We built them a school, we teach them english but after that. How many years?";

		TTSHandler.getInstance().ttsSetContext(this, this.getResources());
		ScreenReaderGestureHandler gHandler = new ScreenReaderGestureHandler(passedString);
		
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
