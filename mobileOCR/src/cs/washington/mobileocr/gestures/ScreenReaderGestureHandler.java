/**
 * @author - Hussein Yapit
 */

package cs.washington.mobileocr.gestures;

import android.os.Message;
import android.view.MotionEvent;
import cs.washington.mobileocr.main.R;

public class ScreenReaderGestureHandler extends GestureHandler{

	//you can change MotionEvent argument to LONGPRESS, TAP, DOUBLE_TAP
	//
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
	
	public void onLongPress (MotionEvent event)
	{
		//say this turns on/off help instructions
		
	
		mCurrentState = nextState(R.id.event_longpress);
	}
	
	//HANDLE other cases:
	
	//double taps?
	
	//taps?
	
	//scroll?
	
	//fling 
	
	
	//you can do things like if nextState(e) is not good, do something. Maybe the blind
	//person is confused.
}
