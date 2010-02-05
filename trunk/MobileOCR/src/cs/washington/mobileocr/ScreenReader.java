package cs.washington.mobileocr;

import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.widget.Toast;
import com.google.tts.TextToSpeechBeta;

/*
 * Team Sparkplugs (Josh Scotland and Hussein Yapit)
 * This is the main activity for the MobileOCR application.
 * The application uses text to speech to output information
 * TODO Add more comments
 * TODO: Add logs  Log.d("MOCR","Stop Activity");
 * TODO: Fix the space when the person moves on a space, it skips / plays spaces
 * TODO: The TTS won't STOP sometimes when you tell it to stop
 */

public class ScreenReader extends Activity implements OnGestureListener {

	private int[] loc = {0,0,0};  // Sentence number, word number, letter number
	private String[] sentenceArray;
	private String[] wordArray;
	private int mode = 0;
	private String[] modeSpeak = {"Sentence Mode", "Word Mode", "Letter Mode"};
	private int[] wordsInSentences;
	private int saySpace = 0; // 0 for don't say space, 1 for say space when moving right, 2 for say space when moving left

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screenreader);

		sentenceArray = TextParser.sentenceParse(MobileOCR.getPassedString());
		wordsInSentences = TextParser.countWordsInSentence(sentenceArray);
		//Toast.makeText(getApplicationContext(), "wordsInSentences = " + wordsInSentences.length + " at " + wordsInSentences[0] + "," + wordsInSentences[1]+ "," + wordsInSentences[2]+ "," + wordsInSentences[3], Toast.LENGTH_SHORT).show();
		wordArray = TextParser.wordParse(MobileOCR.getPassedString());

		gestureScanner = new GestureDetector(this);
		gestureScanner.setOnDoubleTapListener(new OnDoubleTapListener(){
			public boolean onDoubleTap(MotionEvent e) {
				Toast.makeText(getApplicationContext(), "Double Tap", Toast.LENGTH_SHORT).show();
				//TODO: Autoplay
				return false;
			}
			public boolean onDoubleTapEvent(MotionEvent e) {
				return false;
			}
			public boolean onSingleTapConfirmed(MotionEvent e) {
				Toast.makeText(getApplicationContext(), "Click, loc = " + "("+loc[0]+","+loc[1]+","+loc[2]+")", Toast.LENGTH_SHORT).show();
				if (MobileOCR.getmTts().isSpeaking())
					MobileOCR.getmTts().stop();
				else {
					if (mode == 0)
						MobileOCR.getmTts().speak(sentenceArray[loc[0]], TextToSpeechBeta.QUEUE_FLUSH, null);
					else if (mode == 1)
						MobileOCR.getmTts().speak(wordArray[loc[1]], TextToSpeechBeta.QUEUE_FLUSH, null);
					else {
						if (saySpace != 0)
							MobileOCR.getmTts().speak("space", TextToSpeechBeta.QUEUE_FLUSH, null);
						else
							MobileOCR.getmTts().speak(speakChar(wordArray[loc[1]].charAt(loc[2])), TextToSpeechBeta.QUEUE_FLUSH, null);
					}
				}
				return false;
			}
		});

		//CountDown counter = new CountDown(5000,1000);
		//counter.start();
	}

	//TODO: Better activity management
	@Override
	protected void onResume() {
		super.onResume();
		onLongPress(null);
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
		Toast.makeText(getApplicationContext(), "Long Press", Toast.LENGTH_SHORT).show();
		MobileOCR.getmTts().stop();
		MobileOCR.getmTts().speak("Currently in: " + modeSpeak[mode], TextToSpeechBeta.QUEUE_FLUSH, null);
		MobileOCR.getmTts().speak("Swipe up or down to change modes", TextToSpeechBeta.QUEUE_ADD, null);
		MobileOCR.getmTts().speak("Tap to play or pause current text", TextToSpeechBeta.QUEUE_ADD, null);
		MobileOCR.getmTts().speak("Swipe left and right to navigate text", TextToSpeechBeta.QUEUE_ADD, null);
		MobileOCR.getmTts().speak("Double tap to play continuously", TextToSpeechBeta.QUEUE_ADD, null);
		MobileOCR.getmTts().speak("Tap and hold to repeat the instructions", TextToSpeechBeta.QUEUE_ADD, null);
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
		return true;
	}

	public void playOnGesture(boolean leftSwipe) {
		if (leftSwipe) {
			if (mode == 0) {
				if (loc[0] > 0) {
					loc[0]--;
					if (loc[0] != 0)
						loc[1] = wordsInSentences[loc[0] - 1];
					else
						loc[1] = 0;
				}
				loc[2] = 0;
				MobileOCR.getmTts().speak(sentenceArray[loc[0]], TextToSpeechBeta.QUEUE_FLUSH, null);
			}
			else if (mode == 1) {
				if (loc[1] > 0) {
					loc[1]--;
					if (loc[0] > 0 && loc[1] < wordsInSentences[loc[0] - 1])
						loc[0]--;
				}
				loc[2] = 0;
				MobileOCR.getmTts().speak(wordArray[loc[1]], TextToSpeechBeta.QUEUE_FLUSH, null);
			}
			else {
				if(loc[1] > 0 && loc[2] >= 0) {
					loc[2]--;
					if (loc[2] < 0) {
						if (saySpace == 2) {
							loc[1]--;
							loc[2] = wordArray[loc[1]].length() - 1;
							saySpace = 0;
						}
						else {
							saySpace = 2;
							loc[2]++;
						}
					}
					if (loc[0] > 0 && loc[1] < wordsInSentences[loc[0] - 1])
						loc[0]--;
				}
				if (saySpace == 2)
					MobileOCR.getmTts().speak("space", TextToSpeechBeta.QUEUE_FLUSH, null);
				else
					MobileOCR.getmTts().speak(speakChar(wordArray[loc[1]].charAt(loc[2])), TextToSpeechBeta.QUEUE_FLUSH, null);
			}
			Toast.makeText(getApplicationContext(), "Left Swipe, loc = " + "("+loc[0]+","+loc[1]+","+loc[2]+")", Toast.LENGTH_SHORT).show();
		}
		else {
			if (mode == 0) {
				if (loc[0] < sentenceArray.length - 1) {
					loc[0]++;
					loc[1] = wordsInSentences[loc[0] - 1];
				}
				loc[2] = 0;
				MobileOCR.getmTts().speak(sentenceArray[loc[0]], TextToSpeechBeta.QUEUE_FLUSH, null);
			}
			else if (mode == 1) {
				if (loc[1] < wordArray.length - 1) {
					loc[1]++;
					if (loc[1] >= wordsInSentences[loc[0]])
						loc[0]++;
				}
				loc[2] = 0;
				MobileOCR.getmTts().speak(wordArray[loc[1]], TextToSpeechBeta.QUEUE_FLUSH, null);
			}
			else {
				if (!(loc[1] == wordArray.length - 1 && loc[2] == wordArray[wordArray.length - 1].length() - 1)) {
					loc[2]++;
					if (loc[2] >= wordArray[loc[1]].length()) {
						if (saySpace == 1) {
							loc[1]++;
							loc[2] = 0;
							saySpace = 0;
						}
						else {
							saySpace = 1;
							loc[2]--;
						}
					}
					if (loc[1] >= wordsInSentences[loc[0]])
						loc[0]++;
				}
				if (saySpace == 1)
					MobileOCR.getmTts().speak("space", TextToSpeechBeta.QUEUE_FLUSH, null);
				else
					MobileOCR.getmTts().speak(speakChar(wordArray[loc[1]].charAt(loc[2])), TextToSpeechBeta.QUEUE_FLUSH, null);
				//Toast.makeText(getApplicationContext(), "char = " + wordArray[loc[1]].charAt(loc[2]), Toast.LENGTH_SHORT).show();
			}
			Toast.makeText(getApplicationContext(), "Right Swipe, loc = " + "("+loc[0]+","+loc[1]+","+loc[2]+")", Toast.LENGTH_SHORT).show();
		}
	}

	public String speakChar(char passedChar) {
		String str = "";
		switch (passedChar) {
		case '!': str = "exclaimation"; break;
		case '.': str = "period"; break;
		case ':': str = "colon"; break;
		case ';': str = "semicolon"; break;
		case '?': str = "question mark"; break;
		case ',': str = "comma"; break;
		case '(': str = "left parenthesis"; break;
		case ')': str = "right parenthesis"; break;
		case 'a': str = "ayee"; break;
		default: str = " " + Character.toString(passedChar) + " "; break;
		}
		return str;
	}
}
