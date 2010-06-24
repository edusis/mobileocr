
package mobileocr.gestures;

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

import mobileocr.tts.TTSHandler;
import mobileocr.main.R;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;

public class NavigationGestureHandler extends SimpleOnGestureListener{
	public void onLongPress(MotionEvent e) {
		TTSHandler.ttsQueueMessage(R.string.tts_nav_instruction);
	}
}
