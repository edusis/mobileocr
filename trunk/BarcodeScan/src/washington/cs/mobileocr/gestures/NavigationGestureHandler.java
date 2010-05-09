
package washington.cs.mobileocr.gestures;

/**
 * Copyright 2010, Josh Scotland & Hussein Yapit
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided you follow the BSD license.
 * 
 * 
 * This class handles all the gestures for navigating in the picture taking mode.
 */

import mocr.barcode.R;
import washington.cs.mobileocr.tts.TTSHandler;
import android.view.MotionEvent;

public class NavigationGestureHandler extends GestureHandler{

	protected int nextState(int gestureEvent) {
		return 0;
	}

	public void onLongPress(MotionEvent e) {
		TTSHandler.ttsQueueMessage(R.string.tts_nav_instruction);
	}
}
