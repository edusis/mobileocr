package cs.washington.mobileocr;

import java.util.HashMap;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.tts.TextToSpeechBeta;
import com.google.tts.TextToSpeechBeta.OnInitListener;

public class MobileOCR extends Activity implements OnInitListener, OnGestureListener, OnUtteranceCompletedListener{

	public EditText entry;
	private TextToSpeechBeta mTts;
	public String passedString2 = "These are the instructions. First hit the button that says push first! This parses the paragraph into sentences. Now tap the screen to play and pause the speech. Swipe up and down to change sentences.";
	private int MY_DATA_CHECK_CODE;
	int count = 0;
	HashMap<String, String> mHash = new HashMap<String, String>();
	String[] tokens;

	//TODO: Add logs  Log.e("MOCR","Stop Activity");
	//TODO: Add comments

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		final Button speak2 = (Button) findViewById(R.id.speak2);
		speak2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				screenReader(passedString2);
			}
		});

		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeechBeta.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);

		TextView text = (TextView) findViewById(R.id.text);
		text.setText(passedString2);

		gestureScanner = new GestureDetector(this);
	}

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

	//TTS initialization
	protected void onActivityResult(
			int requestCode, int resultCode, Intent data) {
		if (requestCode == MY_DATA_CHECK_CODE) {
			if (resultCode == TextToSpeechBeta.Engine.CHECK_VOICE_DATA_PASS) {
				// success, create the TTS instance
				mTts = new TextToSpeechBeta(this, this);
			} else {
				// missing data, install it
				Intent installIntent = new Intent();
				installIntent.setAction(
						TextToSpeechBeta.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		}
	}

	@Override
	public void onInit(int arg0, int arg1) {
		Log.i("MOCR","TTS Initialization");
		mTts.speak("Welcome to the Mobile OCR user interface!", 0, null);
	}

	private void screenReader(String passedString) {
		String delims = "[.?!]+";
		tokens = passedString.split(delims);
		mTts.setOnUtteranceCompletedListener(this);
		count = 0;
		/*
		while (count != tokens.length - 1) {
			mTts.speak(tokens[count], TextToSpeechBeta.QUEUE_ADD, null);
			count++;
		}
		
		mHash.put(TextToSpeechBeta.Engine.KEY_PARAM_UTTERANCE_ID, "end");
		mTts.speak(tokens[count], TextToSpeechBeta.QUEUE_ADD, mHash);
		*/
	}

	@Override
	public void onUtteranceCompleted(String uttId) {
		if (uttId == "end") {
			mTts.speak("Utterance Complete", TextToSpeechBeta.QUEUE_ADD, mHash);
		} 
	}

	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private static final int SWIPE_THRESHOLD_VERTICAL_CORRECTION = 50;
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
				Toast.makeText(getApplicationContext(), "Left Swipe, Count = " + count, Toast.LENGTH_SHORT).show();
			} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				Toast.makeText(getApplicationContext(), "Right Swipe, Count = " + count, Toast.LENGTH_SHORT).show();
			}
			else if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY - SWIPE_THRESHOLD_VERTICAL_CORRECTION) {
				Toast.makeText(getApplicationContext(), "Swipe up, Count = " + count, Toast.LENGTH_SHORT).show();
				if (count > 0) {
					mTts.stop();
					count--;
					mTts.speak(tokens[count], TextToSpeechBeta.QUEUE_ADD, null);
				}
				else { //count == 0
					mTts.speak(tokens[0], TextToSpeechBeta.QUEUE_ADD, null);
				}
			} else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY - SWIPE_THRESHOLD_VERTICAL_CORRECTION) {
				Toast.makeText(getApplicationContext(), "Swipe down, Count = " + count, Toast.LENGTH_SHORT).show();
				if (count < tokens.length - 1) {
					mTts.stop();
					count++;
					mTts.speak(tokens[count], TextToSpeechBeta.QUEUE_ADD, null);
				}
				else {
					mTts.speak(tokens[tokens.length], TextToSpeechBeta.QUEUE_ADD, null);
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
		Toast mToast = Toast.makeText(getApplicationContext(), "Single Tap, Count = " + count, Toast.LENGTH_SHORT);
        mToast.show();
		if (mTts.isSpeaking())
			mTts.stop();
		else if (count < tokens.length) {
			mTts.speak(tokens[count], TextToSpeechBeta.QUEUE_ADD, null);
			count++;
		}
		else {
			
		}
		return true;
	}


}
