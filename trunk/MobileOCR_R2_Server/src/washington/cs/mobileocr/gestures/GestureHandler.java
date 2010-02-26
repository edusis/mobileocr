/**
 * @author - Hussein Yapit
 * 
 */

package washington.cs.mobileocr.gestures;

import java.util.Timer;

import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;
import washington.cs.mobileocr.main.R;

public abstract class GestureHandler extends SimpleOnGestureListener implements IGestureHandler{
	
	protected int mCurrentState;
	protected Timer mStateTimer; //may be handy for input that requires timing
	
	public GestureHandler()
	{
		mCurrentState = R.id.state_idle;
		mStateTimer = new Timer();
		//ttsHandler = TTSServiceThread.getInstance().getHandler();
	}
	
	//update state
	protected abstract int nextState(int gestureEvent);
}
