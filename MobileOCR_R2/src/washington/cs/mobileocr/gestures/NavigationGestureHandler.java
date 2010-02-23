/**
 * @author - Hussein Yapit
 * 
 */

package washington.cs.mobileocr.gestures;

import washington.cs.mobileocr.main.MobileOCR;
import washington.cs.mobileocr.main.R;
import washington.cs.mobileocr.tts.TTSHandler;
import android.view.MotionEvent;

public class NavigationGestureHandler extends GestureHandler{

	protected int nextState(int gestureEvent) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void onLongPress(MotionEvent e) {
		//TODO: need to create abstraction - mobileocr
		
		if (MobileOCR.instructionFlag) {
			TTSHandler.ttsQueueMessage(R.string.tts_nav_instruction);
		} else {
			TTSHandler.getInstance().ttsStop();
		}
		
		MobileOCR.instructionFlag = !MobileOCR.instructionFlag;
	}

}
