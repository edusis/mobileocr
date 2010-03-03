package washington.cs.mobileocr.main;

import washington.cs.mobileocr.gestures.ScreenReaderGestureHandler;
import washington.cs.mobileocr.tts.TTSHandler;
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
	private String passedString = null;
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
		if (passedString == null || !passedString.equals(extras.getString("resultString"))) {
			passedString = extras != null ? extras.getString("resultString"): "Error: The string is not correct";
			sentenceArray = TextParser.sentenceParse(passedString);
			wordsInSentences = TextParser.countWordsInSentence(sentenceArray);
			wordArray = TextParser.wordParse(passedString);
		}
			
		TextView text = (TextView) findViewById(R.id.text);
        text.setText(passedString);
        
		ScreenReaderGestureHandler gHandler = new ScreenReaderGestureHandler(sentenceArray, wordsInSentences, wordArray);
		
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
