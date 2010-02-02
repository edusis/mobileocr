package cs.washington.mobileocr;

import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.OnGestureListener;
import android.widget.Toast;
import com.google.tts.TextToSpeechBeta;

/*
 * Team Sparkplugs (Josh Scotland and Hussein Yapit)
 * This is the main activity for the MobileOCR application.
 * The application uses text to speech to output information
 * TODO Add more comments
 * TODO: Add logs  Log.d("MOCR","Stop Activity");
 * TODO Make the long press be "continue play" NEEDS TIMERS
 */

public class ScreenReader extends Activity implements OnGestureListener {

	private int[] loc = {0,0,0};  // Sentence number, word number, letter number
	private String[] sentenceArray;
	private String[] wordArray;
	private int mode = 0;
	private String[] modeSpeak = {"Sentence Mode", "Word Mode", "Letter Mode"};
	private int[] wordsInSentences;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screenreader);

		sentenceArray = TextParser.sentenceParse(MobileOCR.getPassedString());
		wordsInSentences = TextParser.countWordsInSentence(sentenceArray);
		//Toast.makeText(getApplicationContext(), "wordsInSentences = " + wordsInSentences.length + " at 0 = " + wordsInSentences[0], Toast.LENGTH_SHORT).show();
		wordArray = TextParser.wordParse(MobileOCR.getPassedString());

		gestureScanner = new GestureDetector(this);

		CountDown counter = new CountDown(5000,1000);
		//counter.start();
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
		MobileOCR.getmTts().shutdown();
		super.onDestroy();
	}

	@Override
	public void onDestroy() {
		MobileOCR.getmTts().shutdown();
		super.onDestroy();
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
			MobileOCR.getmTts().stop();
			if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
				return false;
			if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				//Toast.makeText(getApplicationContext(), "Left Swipe, loc = " + "("+loc[0]+","+loc[1]+","+loc[2]+")", Toast.LENGTH_SHORT).show();
				playOnGesture(true);
			} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				//Toast.makeText(getApplicationContext(), "Right Swipe, loc = " + "("+loc[0]+","+loc[1]+","+loc[2]+")", Toast.LENGTH_SHORT).show();
				playOnGesture(false);
			}
			else if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
				if (mode == 0)
					mode = 3;
				mode = (mode - 1) % 3;
				//Toast.makeText(getApplicationContext(), "Swipe up, mode = " + mode, Toast.LENGTH_SHORT).show();
				MobileOCR.getmTts().speak(modeSpeak[mode], TextToSpeechBeta.QUEUE_FLUSH, null);
			} else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
				//Toast.makeText(getApplicationContext(), "Swipe down, mode = " + mode, Toast.LENGTH_SHORT).show();
				mode = (mode + 1) % 3;
				MobileOCR.getmTts().speak(modeSpeak[mode], TextToSpeechBeta.QUEUE_FLUSH, null);
			}
		} catch (Exception e) {
			// nothing
		}
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {
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
		Toast.makeText(getApplicationContext(), "Click, loc = " + "("+loc[0]+","+loc[1]+","+loc[2]+")", Toast.LENGTH_SHORT).show();
		if (MobileOCR.getmTts().isSpeaking()) {
			MobileOCR.getmTts().stop();
		}
		else {
			if (mode == 0)
				MobileOCR.getmTts().speak(sentenceArray[loc[0]], TextToSpeechBeta.QUEUE_FLUSH, null);
			else if (mode == 1)
				MobileOCR.getmTts().speak(wordArray[loc[1]], TextToSpeechBeta.QUEUE_FLUSH, null);
			else
				MobileOCR.getmTts().speak("" + wordArray[loc[1]].charAt(loc[2]), TextToSpeechBeta.QUEUE_FLUSH, null);
		}
		return true;
	}

	public void playOnGesture(boolean leftSwipe) {
		if (leftSwipe) {
			if (mode == 0) {
				if (loc[0] > 0) {
					loc[0]--;
					loc[1] = wordsInSentences[loc[0]] - wordsInSentences[0];
					loc[2] = 0;
				}
				MobileOCR.getmTts().speak(sentenceArray[loc[0]], TextToSpeechBeta.QUEUE_FLUSH, null);
			}
			else if (mode == 1) {
				if (loc[1] > 0) {
					loc[1]--;
					loc[2] = 0;
					if (loc[1] < wordsInSentences[loc[0]])
						loc[0]--;
				}
				MobileOCR.getmTts().speak(wordArray[loc[1]], TextToSpeechBeta.QUEUE_FLUSH, null);
			}
			else {
				if (loc[1] > 0 && loc[2] > 0) {
					loc[2]--;
					if (loc[2] < 0) {
						loc[1]--;
						loc[0] = wordArray[loc[1]].length();
					}
					if (loc[1] < wordsInSentences[loc[0]])
						loc[0]--;
					MobileOCR.getmTts().speak(" " + wordArray[loc[1]].charAt(loc[2]), TextToSpeechBeta.QUEUE_FLUSH, null);
				}
			}
			Toast.makeText(getApplicationContext(), "Left Swipe, loc = " + "("+loc[0]+","+loc[1]+","+loc[2]+")", Toast.LENGTH_SHORT).show();
		}
		else {
			if (mode == 0) {
				if (loc[0] < sentenceArray.length - 1) {
					loc[0]++;
					loc[1] = wordsInSentences[loc[0] - 1];
					loc[2] = 0;
				}
				MobileOCR.getmTts().speak(sentenceArray[loc[0]], TextToSpeechBeta.QUEUE_FLUSH, null);
			}
			else if (mode == 1) {
				if (loc[1] < wordArray.length - 1) {
					loc[1]++;
					loc[2] = 0;
					if (loc[1] >= wordsInSentences[loc[0] + 1])
						loc[0]++;
				}
				MobileOCR.getmTts().speak(wordArray[loc[1]], TextToSpeechBeta.QUEUE_FLUSH, null);
			}
			else {
				if (loc[1] <= wordArray.length - 1 && loc[2] < wordArray[loc[1]].length() - 1) {
					loc[2]++;
					if (loc[2] >= wordArray[loc[1]].length()) {
						loc[1]++;
						loc[0] = 0;
					}
					if (loc[1] >= wordsInSentences[loc[0]])
						loc[0]++;
					MobileOCR.getmTts().speak("" + wordArray[loc[1]].charAt(loc[2]), TextToSpeechBeta.QUEUE_FLUSH, null);
					Toast.makeText(getApplicationContext(), "char = " + wordArray[loc[1]].charAt(loc[2]), Toast.LENGTH_SHORT).show();
				}
			}
			Toast.makeText(getApplicationContext(), "Right Swipe, loc = " + "("+loc[0]+","+loc[1]+","+loc[2]+")", Toast.LENGTH_SHORT).show();
		}
	}
}
