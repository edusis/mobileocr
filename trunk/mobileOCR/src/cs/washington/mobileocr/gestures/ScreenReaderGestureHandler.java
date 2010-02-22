package cs.washington.mobileocr.gestures;

import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import cs.washington.mobileocr.main.R;
import cs.washington.mobileocr.main.TextParser;
import cs.washington.mobileocr.tts.TTSHandler;

/*
 * Josh Scotland and Hussein Yapit
 * This is the screen reader activity
 * TODO: BUG: on triple swipes or on auto playing, tapping to stop will replay the sentence
 */

public class ScreenReaderGestureHandler extends GestureHandler {

	private String TAG = this.getClass().getSimpleName();
	private static int[] loc = {0,0,0};  // Sentence number, word number, letter number
	private static String[] sentenceArray;
	private String[] wordArray;
	private static int[] wordsInSentences;
	private static final int SENTENCE_MODE = 0;
	private static final int WORD_MODE = 1;
	private int mode = SENTENCE_MODE;
	private String[] modeSpeak = {"Sentence Mode", "Word Mode", "Letter Mode"};
	private int saySpace = 0; // 0 for don't say space, 1 for say space when moving right, 2 for say space when moving left
	private static Boolean autoplay = false;

	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;

	public ScreenReaderGestureHandler(String passedString) {
		super();
		initialize(passedString);
	}

	protected int nextState(int event) {
		// put statemachine here.
		switch(mCurrentState) {
		case R.id.state_idle:
			//in screen reader waiting for input
			//change state here based on event
			break;
		}
		return 0;
	}

	public boolean onSingleTapConfirmed(MotionEvent e) {
		Log.d("MOCR","Click, loc = " + "("+loc[0]+","+loc[1]+","+loc[2]+")");
		TTSHandler.getInstance().setParam(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "Speaking");
		autoplay = false;
		if (TTSHandler.getDoneSpeaking()) {
			if (mode == SENTENCE_MODE)
				startPlaying(sentenceArray[loc[0]]);
			else if (mode == WORD_MODE)
				startPlaying(wordArray[loc[1]]);
			else {
				if (saySpace != 0)
					startPlaying("space");
				else
					startPlaying(speakChar(wordArray[loc[1]].charAt(loc[2])));
			}
		} else
			stopPlaying();
		return false;
	}

	public void onLongPress(MotionEvent e) {
		Log.d("MOCR","Long Press");
		startPlaying("Currently in: " + modeSpeak[mode] + ". Fling up or down to change modes. Tap to play or pause current text. Fling left and right to navigate text. Double tap to play continuously. Tap and hold to repeat the instructions");
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		try {
			TTSHandler.getInstance().setParam(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "Speaking");
			autoplay = false;
			stopPlaying();
			if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
				return false;
			if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {  //left
				playOnGesture(true);
			} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {  //right
				playOnGesture(false);
			} else if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {  //up
				if (mode == SENTENCE_MODE) {
					mode = 3;
				}
				mode = (mode - 1) % 3;
				startPlaying(modeSpeak[mode]);
			} else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {  //down
				mode = (mode + 1) % 3;
				startPlaying(modeSpeak[mode]);
			}
		} catch (Exception e) {
			Log.e(TAG, "Exception on fling. Details: " + e.toString());
		}
		return true;
	}

	public boolean onDoubleTap(MotionEvent e) {
		Log.d("MOCR","Double Tap");
		stopPlaying();
		TTSHandler.getInstance().setParam(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "Sentences");
		autoplay = true;
		startPlaying(sentenceArray[loc[0]]);
		return false;
	}

	private void initialize(String passedString) {
		sentenceArray = TextParser.sentenceParse(passedString);
		wordsInSentences = TextParser.countWordsInSentence(sentenceArray);
		wordArray = TextParser.wordParse(passedString);
	}

	private void playOnGesture(boolean leftSwipe) {
		saySpace = 0;
		if (leftSwipe) {
			if (mode == SENTENCE_MODE) {
				if (loc[0] > 0) {
					loc[0]--;
					if (loc[0] != 0)
						loc[1] = wordsInSentences[loc[0] - 1];
					else
						loc[1] = 0;
				}
				loc[2] = 0;
				startPlaying(sentenceArray[loc[0]]);
			}
			else if (mode == WORD_MODE) {
				if (loc[1] > 0) {
					loc[1]--;
					if (loc[0] > 0 && loc[1] < wordsInSentences[loc[0] - 1])
						loc[0]--;
				}
				loc[2] = 0;
				startPlaying(wordArray[loc[1]]);
			}
			else { //mode == LETTER_MODE
				if (loc[1] == 0 && loc[2] == 0)
					startPlaying(speakChar(wordArray[loc[1]].charAt(loc[2])));
				else if (loc[1] >= 0 && loc[2] >= -1) {
					loc[2]--;
					if (loc[2] == -1) {
						saySpace = 1;
						startPlaying("space");
						return;
					} else if (loc[2] < -1) {
						loc[1]--;
						loc[2] = wordArray[loc[1]].length() - 1;
					} else {
						//Nothing
					}
					if (loc[0] > 0 && loc[1] < wordsInSentences[loc[0] - 1])
						loc[0]--;
					startPlaying(speakChar(wordArray[loc[1]].charAt(loc[2])));
				} else {
					//Nothing
				}
			}
			Log.d("MOCR","Left Swipe, loc = " + "("+loc[0]+","+loc[1]+","+loc[2]+")");
		}
		else { //right swipe
			if (mode == SENTENCE_MODE) {
				if (loc[0] < sentenceArray.length - 1) {
					loc[0]++;
					loc[1] = wordsInSentences[loc[0] - 1];
				}
				loc[2] = 0;
				startPlaying(sentenceArray[loc[0]]);
			}
			else if (mode == WORD_MODE) {
				if (loc[1] < wordArray.length - 1) {
					loc[1]++;
					if (loc[1] >= wordsInSentences[loc[0]])
						loc[0]++;
				}
				loc[2] = 0;
				startPlaying(wordArray[loc[1]]);
			}
			else { //mode == LETTER_MODE
				if (!(loc[1] == wordArray.length - 1 && loc[2] == wordArray[wordArray.length - 1].length() - 1)) {
					loc[2]++;
					if (loc[2] == wordArray[loc[1]].length()) {
						saySpace = 1;
						startPlaying("space");
						return;
					}
					else if (loc[2] > wordArray[loc[1]].length()) {
						loc[1]++;
						loc[2] = 0;
					}
					else {
						//Nothing
					}
					if (loc[1] >= wordsInSentences[loc[0]])
						loc[0]++;
				}
				startPlaying(speakChar(wordArray[loc[1]].charAt(loc[2])));
			}
			Log.d("MOCR","Right Swipe, loc = " + "("+loc[0]+","+loc[1]+","+loc[2]+")");
		}
	}

	private static void startPlaying(String passedStr) {
		TTSHandler.ttsQueueSRMessage(passedStr);
	}

	private void stopPlaying() {
		TTSHandler.getInstance().ttsStop();
	}
	
	public static void autoplaySentences() {
		if (loc[0] < sentenceArray.length - 1 && autoplay) {
			loc[1] = wordsInSentences[loc[0]];
			loc[0]++;
			startPlaying(sentenceArray[loc[0]]);
		}
	}

	private String speakChar(char passedChar) {
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
