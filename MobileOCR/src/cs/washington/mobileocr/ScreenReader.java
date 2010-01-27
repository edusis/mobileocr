package cs.washington.mobileocr;

import java.util.HashMap;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;

import com.google.tts.TextToSpeechBeta;

public class ScreenReader implements OnUtteranceCompletedListener {
	
	private static TextToSpeechBeta tts;
	static HashMap<String, String> myHashAlarm = new HashMap<String, String>();
	
	public void readText(String passedString) {
		String delims = "[.?!]+";
		String[] tokens = passedString.split(delims);
		tts.setOnUtteranceCompletedListener(this);
		//mTts.speak("Hey", TextToSpeechBeta.QUEUE_FLUSH, null);
		int count = 0;
		while (count != tokens.length - 1) {
			tts.speak(tokens[count], TextToSpeechBeta.QUEUE_ADD, null);
			count++;
		}
		myHashAlarm.put(TextToSpeechBeta.Engine.KEY_PARAM_UTTERANCE_ID, "end");
		tts.speak(tokens[count], TextToSpeechBeta.QUEUE_ADD, myHashAlarm);
	}

	@Override
	public void onUtteranceCompleted(String uttId) {
	    if (uttId == "end") {
	    	tts.speak("Utterance Complete", TextToSpeechBeta.QUEUE_ADD, myHashAlarm);
	    } 
	}

}
