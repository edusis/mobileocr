package cs.washington.mobileocr.main;

import cs.washington.mobileocr.gestures.ScreenReaderGestureHandler;
import cs.washington.mobileocr.tts.TTSHandler;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

/*
 * Josh Scotland and Hussein Yapit
 * This is the screen reader activity
 * TODO: Fix bug where it hangs after initialization
 */

public class ScreenReader extends Activity {

	private GestureDetector gestureScanner;
	private static final String TAG = TTSHandler.class.getSimpleName();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "Screenreader started");
		
		//Remove the title frame
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		
		setContentView(R.layout.screenreader);
		TextView text = (TextView) findViewById(R.id.text);
        text.setText(R.string.tts_passedstring);
        
		Bundle extras = this.getIntent().getExtras();
		String passedString = null;
		passedString = extras != null ? extras.getString("res"): "Look we are supposed to be winning the hearts and the minds of the natives. Isn't that the whole point of your little puppet show? You look like them and you talk like them and they will start trusting us. We built them a school, we teach them english but after that. How many years?";
		
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
		TTSHandler.ttsQueueSRMessage("In the screen reader");
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
