package mocr.app;

import java.util.Timer;

import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;

public abstract class GestureHandler extends SimpleOnGestureListener implements IGestureHandler{
	
	protected int currentState;
	protected Timer stateTimer; //may be handy for input that requires timing
	protected Handler ttsHandler; //listens to tts request (No TTS code here, all are in TTSServiceThread)
	
	public GestureHandler()
	{
		currentState = R.id.state_idle;
		stateTimer = new Timer();
		ttsHandler = TTSServiceThread.getInstance().getHandler();
	}
	
	//send message to TTSServiceHandler
	protected boolean ttsSendMessage(int type)
	{
		Message msg = ttsHandler.obtainMessage(type);
		return ttsHandler.sendMessage(msg);
	}
	
	//update state
	protected abstract int nextState(int gestureEvent);
}
