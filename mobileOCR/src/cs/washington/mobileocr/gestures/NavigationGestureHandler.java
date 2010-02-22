/**
 * @author - Hussein Yapit
 * 
 */

package cs.washington.mobileocr.gestures;

import android.util.Log;
import android.view.MotionEvent;

public class NavigationGestureHandler extends GestureHandler {

	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private String TAG = this.getClass().getSimpleName();
	
	protected int nextState(int gestureEvent) {
		return 0;
	}
	
	public boolean onSingleTapConfirmed(MotionEvent e) {
		Log.d(TAG,"Single Tap");
		//MobileOCR.startScreenReaderView("Hey");
		return false;
	}

}
