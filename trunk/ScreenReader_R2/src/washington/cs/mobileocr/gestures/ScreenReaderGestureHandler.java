
package washington.cs.mobileocr.gestures;

/**
 * Copyright 2010, Josh Scotland & Hussein Yapit
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided you follow the BSD license.
 * 
 * 
 * This class handles all the gestures when in the screen reader activity.
 * TODO: Fix the triple swipe bug (the tapping to stop will only repeat the sentence, not stop it)
 * TODO: Save preferences so that we don't initialize every time
 */

import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import washington.cs.mobileocr.main.R;
import washington.cs.mobileocr.tts.TTSHandler;

public class ScreenReaderGestureHandler extends GestureHandler {

	private static final String TAG = "ScreenReaderGesture";
	private static final int SENTENCE_MODE = 0; //Sentence mode speaks by sentences
	private static final int WORD_MODE = 1; //Word mode speaks by words
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;

	private static int[] loc = {0,0,0}; //Sentence number, word number, letter number
	private static String[] sentenceArray; //Array that has whole sentences (parsed by line end or punctuation)
	private String[] wordArray; //Array that has all the words (parsed by spaces)
	private static int[] wordsInSentences; //Count of the number words in wordArray
	private int mode = SENTENCE_MODE;
	private String[] modeSpeak = {"Sentence Mode", "Word Mode", "Letter Mode"};
	private int saySpace = 0; //0 = don't say "space", 1 = say "space" when moving right, 2 = say "space" when moving left
	private static Boolean autoplay = false; //Continue to speak sentences without the use of gestures

	//Initialization of screen reader activity
	public ScreenReaderGestureHandler(String[] sentenceArray, int[] wordsInSentences, String[] wordArray) {
		super();
		ScreenReaderGestureHandler.sentenceArray = sentenceArray;
		ScreenReaderGestureHandler.wordsInSentences = wordsInSentences;
		this.wordArray = wordArray;
		loc[0] = loc[1] = loc[2] = 0;
		mode = SENTENCE_MODE;
		saySpace = 0;
		autoplay = false;
	}

	protected int nextState(int event) {
		switch(mCurrentState) {
		case R.id.state_idle:
			break;
		}
		return 0;
	}

	//Tapping the screen reader will either stop what is playing or repeat what was played
	public boolean onSingleTapConfirmed(MotionEvent e) {
		Log.i(TAG,"Click");
		
		//Make sure we turn off autoplay
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

	//Play the instructions on a long screen press
	public void onLongPress(MotionEvent e) {
		Log.i(TAG,"LongPress");
		startPlaying("Currently in: " + modeSpeak[mode] + ". Fling up or down to change modes. Tap to play or pause current text. Fling left and right to navigate text. Double tap to play continuously. Tap and hold to repeat the instructions");
	}

	//When the a flinging gesture is recognized, determine if it is up, down, left, or right
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		try {
			
			//Turn off autoplay and stop playing
			TTSHandler.getInstance().setParam(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "Speaking");
			autoplay = false;
			stopPlaying();
			
			if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
				return false;
			if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) { //Left
				playOnGesture(true);
			} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) { //Right
				playOnGesture(false);
			} else if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) { //Up
				if (mode == SENTENCE_MODE)
                    mode = 3;
				mode = (mode - 1) % 3;
				startPlaying(modeSpeak[mode]);
			} else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) { //Down
				mode = (mode + 1) % 3;
				startPlaying(modeSpeak[mode]);
			}
		} catch (Exception e) {
			Log.e(TAG, "Exception on fling. Details: " + e.toString());
		}
		return true;
	}

	//If gesture is a double tap, autoplay by sentences.
	public boolean onDoubleTap(MotionEvent e) {
		Log.i(TAG,"DoubleTap");
		
		//Prepare the TTS for autoplay
		stopPlaying();
		TTSHandler.getInstance().setParam(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "Sentences");
		
		autoplay = true;
		startPlaying(sentenceArray[loc[0]]);
		return false;
	}

	//If the gesture is a left or right swipe, play text based on the mode we are in.
	//Handles all the logic for updating the current location in each of the arrays
	private void playOnGesture(boolean leftSwipe) {
		saySpace = 0; //Needed for the case when you get a left swipe gesture after when the last character read was "space"
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
			Log.d(TAG,"Left Swipe");
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
			Log.d(TAG,"Right Swipe");
		}
	}

	//Prepare to call on the TTSHandler to play the text
	private static void startPlaying(String passedStr) {
		Log.i(TAG,"Speak @ loc = " + "("+loc[0]+","+loc[1]+","+loc[2]+")");
		if (passedStr.equals("") || passedStr.equals("[ ]+"))
			TTSHandler.ttsQueueSRMessage("blank");
		else
			TTSHandler.ttsQueueSRMessage(passedStr);
	}

	//Stop playing text
	private void stopPlaying() {
		TTSHandler.getInstance().ttsStop();
	}

	//Called on from the TTS Handler. Autoplay sentences if autoplay is true
	public static void autoplaySentences() {
		if (loc[0] < sentenceArray.length - 1 && autoplay) {
			loc[1] = wordsInSentences[loc[0]];
			loc[0]++;
			startPlaying(sentenceArray[loc[0]]);
		}
	}

	//Helps the TTS speak characters it doesn't say right or doesn't speak
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
