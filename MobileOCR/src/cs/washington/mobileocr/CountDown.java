package cs.washington.mobileocr;

import android.os.CountDownTimer;

public class CountDown extends CountDownTimer{

	public CountDown(long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);
	}

	@Override
	public void onFinish() {
		
	}

	@Override
	public void onTick(long millisUntilFinished) {
		//MobileOCR.getmTts().speak("hey", 0, null);
	}

}