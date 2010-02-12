/**
 * @author - Hussein Yapit
 */

package cs.washington.mobileocr.gestures;

import java.util.HashMap;

import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import cs.washington.mobileocr.main.MobileOCR;
import cs.washington.mobileocr.main.R;

public class ScreenReaderGestureHandler extends GestureHandler{

	private int[] loc = {0,0,0};  // Sentence number, word number, letter number
	private String[] sentenceArray;
	private String[] wordArray;
	private int[] wordsInSentences;

	private int mode = 0;
	private String[] modeSpeak = {"Sentence Mode", "Word Mode", "Letter Mode"};

	private String[] instructions = {"Fling up or down to change modes", "Tap to play or pause current text", "Fling left and right to navigate text", "Double tap to play continuously", "Tap and hold to repeat the instructions"};

	private int saySpace = 0; // 0 for don't say space, 1 for say space when moving right, 2 for say space when moving left

	HashMap<String, String> myHashAlarm = new HashMap<String, String>();
	private int autoplay = 0;
	private Boolean doneSpeaking = true;
	
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	
	protected int nextState(int event) {
		// put statemachine here.
		switch(mCurrentState)
		{
		case R.id.state_idle:
			//in screen reader waiting for input
			//change state here based on event
			break;
			
		}
		return 0;
	}
	
	public boolean onSingleTapConfirmed(MotionEvent e) {
		Log.e("MOCR","Click, loc = " + "("+loc[0]+","+loc[1]+","+loc[2]+")");
		if (doneSpeaking) {
			myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "Speaking");
			if (mode == 0)
				startPlaying(sentenceArray[loc[0]]);
			else if (mode == 1)
				startPlaying(wordArray[loc[1]]);
			else {
				if (saySpace != 0)
					startPlaying("space");
				else
					startPlaying(speakChar(wordArray[loc[1]].charAt(loc[2])));
			}
		}
		else {
			myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "Speaking");
			stopPlaying();
		}
		return false;
	}
	
	public void onLongPress(MotionEvent e) {
		Log.e("MOCR","Long Press");
		speakInstructions();
	}
	
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		try {
			myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "Speaking");
			stopPlaying();
			if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
				return false;
			if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {  //left
				playOnGesture(true);
			} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {  //right
				playOnGesture(false);
			}
			else if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {  //up
				if (mode == 0)
					mode = 3;
				mode = (mode - 1) % 3;
				startPlaying(modeSpeak[mode]);
			} else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {  //down
				mode = (mode + 1) % 3;
				startPlaying(modeSpeak[mode]);
			}
		} catch (Exception e) {
			// nothing
		}
		return true;
	}
	
	public boolean onDoubleTap(MotionEvent e) {
		Log.e("MOCR","Double Tap");
		stopPlaying();
		myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "Sentences");
		startPlaying(sentenceArray[loc[0]]);
		return false;
	}
	
	public void onUtteranceCompleted(String uttId) {
		doneSpeaking = true;
		if (uttId.equals("Instructions") && autoplay < instructions.length) {
			startPlaying(instructions[autoplay]);
			autoplay++;
			doneSpeaking = false;
		}
		if (uttId.equals("Sentences") && loc[0] < sentenceArray.length - 1) {
			loc[1] = wordsInSentences[loc[0]];
			loc[0]++;
			startPlaying(sentenceArray[loc[0]]);
			doneSpeaking = false;
		}
	}
	
	private void playOnGesture(boolean leftSwipe) {
		saySpace = 0;
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
				startPlaying(sentenceArray[loc[0]]);
			}
			else if (mode == 1) {
				if (loc[1] > 0) {
					loc[1]--;
					if (loc[0] > 0 && loc[1] < wordsInSentences[loc[0] - 1])
						loc[0]--;
				}
				loc[2] = 0;
				startPlaying(wordArray[loc[1]]);
			}
			else {
				if (loc[1] == 0 && loc[2] == 0)
					startPlaying(speakChar(wordArray[loc[1]].charAt(loc[2])));
				else if (loc[1] >= 0 && loc[2] >= -1) {
					loc[2]--;
					if (loc[2] == -1) {
						saySpace = 1;
						startPlaying("space");
						return;
					}
					else if (loc[2] < -1) {
						loc[1]--;
						loc[2] = wordArray[loc[1]].length() - 1;
					}
					else {
						//Nothing
					}
					if (loc[0] > 0 && loc[1] < wordsInSentences[loc[0] - 1])
						loc[0]--;
					startPlaying(speakChar(wordArray[loc[1]].charAt(loc[2])));
				}
				else {
					//Nothing
				}
			}
			Log.e("MOCR","Left Swipe, loc = " + "("+loc[0]+","+loc[1]+","+loc[2]+")");
		}
		else {
			if (mode == 0) {
				if (loc[0] < sentenceArray.length - 1) {
					loc[0]++;
					loc[1] = wordsInSentences[loc[0] - 1];
				}
				loc[2] = 0;
				startPlaying(sentenceArray[loc[0]]);
			}
			else if (mode == 1) {
				if (loc[1] < wordArray.length - 1) {
					loc[1]++;
					if (loc[1] >= wordsInSentences[loc[0]])
						loc[0]++;
				}
				loc[2] = 0;
				startPlaying(wordArray[loc[1]]);
			}
			else {
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
			Log.e("MOCR","Right Swipe, loc = " + "("+loc[0]+","+loc[1]+","+loc[2]+")");
		}
	}
	
	
	
	private void startPlaying(String passedStr) {
		//MobileOCR.getmTts().speak(passedStr, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
		doneSpeaking = false;
	}

	private void stopPlaying() {
		if (!doneSpeaking)
			//MobileOCR.getmTts().stop();
		doneSpeaking = true;
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

	private void speakInstructions() {
		stopPlaying();
		myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "Instructions");
		autoplay = 0;
		startPlaying("Currently in: " + modeSpeak[mode]);
	}
}
