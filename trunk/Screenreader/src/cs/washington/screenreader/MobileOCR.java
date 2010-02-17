package cs.washington.screenreader;

import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/*
 * Team Sparkplugs (Josh Scotland and Hussein Yapit)
 * This is the main activity for the MobileOCR application.
 * The application uses text to speech to output information
 * TODO Add more comments
 * TODO: Add logs  Log.d("MOCR","Stop Activity");
 */

public class MobileOCR extends Activity implements OnGestureListener, TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {

	private static TextToSpeech mTts;
	private static String passedString;
	private int MY_DATA_CHECK_CODE;
	//private CountDown counter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		MobileOCR.setPassedString("Look we are supposed to be winning the hearts and the minds of the natives. Isn't that the whole point of your little puppet show? You look like them and you talk like them and they will start trusting us. We built them a school, we teach them english but after that. How many years?");
		final Button speak2 = (Button) findViewById(R.id.go);
		speak2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mTts.stop();
				Intent myIntent = new Intent(v.getContext(), Screenreader.class);
                //startActivityForResult(myIntent, 0);
				myIntent.putExtra("Str1", getPassedString());
				startActivity(myIntent);
			}
		});
		
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);

		gestureScanner = new GestureDetector(this);
		gestureScanner.setOnDoubleTapListener(new OnDoubleTapListener(){
			public boolean onDoubleTap(MotionEvent e) {
				Toast.makeText(getApplicationContext(), "Double Tap", Toast.LENGTH_SHORT).show();
				return false;
			}
			public boolean onDoubleTapEvent(MotionEvent e) {
				return false;
			}
			public boolean onSingleTapConfirmed(MotionEvent e) {
				Toast.makeText(getApplicationContext(), "Single Tap 2", Toast.LENGTH_SHORT).show();
				return false;
			}

		});

		
		TextView text = (TextView) findViewById(R.id.text);
        text.setText(passedString);
        
        //counter = new CountDown(5000,1000);
       
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
		getmTts().shutdown();
		super.onDestroy();
	}

	@Override
	public void onDestroy() {
		getmTts().shutdown();
		super.onDestroy();
	}

	//TTS initialization
	protected void onActivityResult(
			int requestCode, int resultCode, Intent data) {
		if (requestCode == MY_DATA_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// success, create the TTS instance
				setmTts(new TextToSpeech(this, this));
			} else {
				// missing data, install it
				Intent installIntent = new Intent();
				installIntent.setAction(
						TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		}
	}

	@Override
	public void onInit(int arg0) {
		Log.i("MOCR","TTS Initialization");
		getmTts().speak("Welcome to the Mobile OCR user interface!", 0, null);
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
				Toast.makeText(getApplicationContext(), "Left Swipe", Toast.LENGTH_SHORT).show();
			} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				Toast.makeText(getApplicationContext(), "Right Swipe", Toast.LENGTH_SHORT).show();
			}
			else if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
				Toast.makeText(getApplicationContext(), "Swipe up", Toast.LENGTH_SHORT).show();
			} else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
				Toast.makeText(getApplicationContext(), "Swipe down", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			// nothing
		}
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		//Toast mToast = Toast.makeText(getApplicationContext(), "Long Press", Toast.LENGTH_SHORT);
		//mToast.show();
		mTts.setOnUtteranceCompletedListener(this);
		HashMap<String, String> myHashAlarm = new HashMap<String, String>();
		myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ID");
		mTts.speak("It was a clear black night", TextToSpeech.QUEUE_ADD, myHashAlarm);
	}
	
	@Override
	public void onUtteranceCompleted(String utteranceId) {
		//Toast.makeText(getApplicationContext(), "Good!!!!", Toast.LENGTH_SHORT).show();
		mTts.speak("From MOCR UI", TextToSpeech.QUEUE_ADD, null);
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
		Toast mToast = Toast.makeText(getApplicationContext(), "Single Tap", Toast.LENGTH_SHORT);
        mToast.show();
		return true;
	}

	public void setmTts(TextToSpeech mTts) {
		MobileOCR.mTts = mTts;
	}

	public static TextToSpeech getmTts() {
		return mTts;
	}

	public static void setPassedString(String passedString) {
		MobileOCR.passedString = passedString;
	}

	public static String getPassedString() {
		return passedString;
	}




}
