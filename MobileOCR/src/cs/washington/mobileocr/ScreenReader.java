package cs.washington.mobileocr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.tts.TextToSpeechBeta;
import com.google.tts.TextToSpeechBeta.OnInitListener;

/*
 * Team Sparkplugs (Josh Scotland and Hussein Yapit)
 * This is the main activity for the MobileOCR application.
 * The application uses text to speech to output information
 * TODO Add more comments
 * TODO: Add logs  Log.e("MOCR","Stop Activity");
 * TODO Make the long press be "continue play" NEEDS TIMERS
 * TODO Add the left and right swipes to scroll by word
 */

public class ScreenReader extends Activity implements OnInitListener, OnGestureListener {

	private TextToSpeechBeta mTts;
	private String passedString = "These are the instructions. First hit the button that says push first! " +
			"This parses the paragraph into sentences. Now tap the screen to play and pause the speech. " +
			"Swipe up and down to change sentences";
	private int textArrayCount = -1;
	private int wordArrayCount = -1;
	private String[] textArray;
	private String[] wordArray;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screenreader);

		final Button speak2 = (Button) findViewById(R.id.speak2);
		speak2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				textArray = TextParser.textParse(passedString);
				textArrayCount = 0;
			}
		});
		
		final Button back = (Button) findViewById(R.id.back);
		back.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
			}
		});
		
		TextView text = (TextView) findViewById(R.id.text);
        text.setText(passedString);

		gestureScanner = new GestureDetector(this);
		mTts = new TextToSpeechBeta(this, ttsInitListener);
	}
	
	private OnInitListener ttsInitListener = new OnInitListener() {
		public void onInit(int arg0, int arg1) {
		}
	};

	//TODO: Better activity management
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		mTts.shutdown();
		super.onDestroy();
	}

	@Override
	public void onDestroy() {
		mTts.shutdown();
		super.onDestroy();
	}

	@Override
	public void onInit(int arg0, int arg1) {
		Log.i("MOCR","TTS Initialization");
		mTts.speak("Welcome to the Mobile OCR user interface!", 0, null);
	}

	/*
	 * The rest of the code is gesture detection for screen reading
	 */
	
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private GestureDetector gestureScanner;

	@Override
	public boolean onTouchEvent(MotionEvent me) {
		return gestureScanner.onTouchEvent(me);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		try {
			if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
				return false;
			if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				Toast.makeText(getApplicationContext(), "Left Swipe, textArrayCount = " + textArrayCount, Toast.LENGTH_SHORT).show();
			} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				Toast.makeText(getApplicationContext(), "Right Swipe, textArrayCount = " + textArrayCount, Toast.LENGTH_SHORT).show();
			}
			else if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
				Toast.makeText(getApplicationContext(), "Swipe up, textArrayCount = " + textArrayCount, Toast.LENGTH_SHORT).show();
				if (textArrayCount > 0) {
					mTts.stop();
					textArrayCount--;
					mTts.speak(textArray[textArrayCount], TextToSpeechBeta.QUEUE_ADD, null);
				}
				else { //textArrayCount == 0
					mTts.speak(textArray[0], TextToSpeechBeta.QUEUE_ADD, null);
				}
			} else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
				Toast.makeText(getApplicationContext(), "Swipe down, textArrayCount = " + textArrayCount, Toast.LENGTH_SHORT).show();
				if (textArrayCount < textArray.length - 1) {
					mTts.stop();
					textArrayCount++;
					mTts.speak(textArray[textArrayCount], TextToSpeechBeta.QUEUE_ADD, null);
				}
				else {
					mTts.speak(textArray[textArray.length], TextToSpeechBeta.QUEUE_ADD, null);
				}
			}
		} catch (Exception e) {
			// nothing
		}
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		Toast mToast = Toast.makeText(getApplicationContext(), "Long Press", Toast.LENGTH_SHORT);
		mToast.show();
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	} 

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		Toast mToast = Toast.makeText(getApplicationContext(), "Single Tap, textArrayCount = " + textArrayCount, Toast.LENGTH_SHORT);
        mToast.show();
		if (mTts.isSpeaking())
			mTts.stop();
		else if (textArrayCount > -1 && textArrayCount < textArray.length) {
			mTts.speak(textArray[textArrayCount], TextToSpeechBeta.QUEUE_ADD, null);
			textArrayCount++;
		}
		else {
			
		}
		return true;
	}


}
