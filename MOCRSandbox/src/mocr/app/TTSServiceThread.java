package mocr.app;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public class TTSServiceThread extends HandlerThread{
	 private Handler mHandler;
	 
	 private static TTSServiceThread ttsThread;
	 //we could enforce limitations to number of tts request here
	 // so doesn't overrun buffer
	 
	 //This class just sits idly, until a Message (android class) comes in -
	 //invoked by other class like this: Handler.sendMessage(msg). All messages
	 // are properly queued (I hope this helps with queuing and flushing current
	 //speech)
	 
	 //See ScreenReaderGesture for example of using this class. (this class is initialized
	 //in abstract class GestureHandler;
	 
	 //All TTS stuff can be put here. so other part of the code is TTS free.
	 // see values/ttsMessages.xml and values/ttsStrings.xml to add constants.
	 private TTSServiceThread()
	 {
		 super(TTSServiceThread.class.getSimpleName());
		 ttsThread = this;
	 }
	 
	 //there's only a single instance of TTSServiceThread
	 public static TTSServiceThread getInstance()
	 {
		 if (ttsThread == null)
		 {
			 ttsThread = new TTSServiceThread();
		 }
		 return ttsThread;
	 }
	 
	 protected void onLooperPrepared () {
		 mHandler = new Handler(getLooper()) {
	            
			 public void handleMessage(Message msg) {
				 switch (msg.what) {
				 case R.id.tts_welcome:
					 //say welcome
					 break;
				//more messages here
				 case R.id.tts_quit:
					 getLooper().quit();
				 default:
					 super.handleMessage(msg);
				 }
			 }
		 };
	 }
	 
	 public Handler getHandler()
	 {
		 return mHandler;
	 }
}
