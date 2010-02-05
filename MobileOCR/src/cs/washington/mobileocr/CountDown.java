package cs.washington.mobileocr;

import com.google.tts.TextToSpeechBeta;

import android.os.CountDownTimer;

public class CountDown extends CountDownTimer{

	public CountDown(long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);
	}

	@Override
	public void onFinish() {
		//MobileOCR.getmTts().speak("finish", TextToSpeechBeta.QUEUE_FLUSH, null);
	}

	@Override
	public void onTick(long millisUntilFinished) {
		//MobileOCR.getmTts().speak("test", TextToSpeechBeta.QUEUE_FLUSH, null);
		if (!(MobileOCR.getmTts().isSpeaking()) && ScreenReader.sentenceLoc < ScreenReader.autoplaySentences.length) {
			MobileOCR.getmTts().speak(ScreenReader.autoplaySentences[ScreenReader.sentenceLoc], TextToSpeechBeta.QUEUE_ADD, null);
			ScreenReader.sentenceLoc++;
		}
	}

}