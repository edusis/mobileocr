
package washington.cs.mobileocr.gestures;

/**
 * Copyright 2010, Josh Scotland & Hussein Yapit
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided you follow the BSD license.
 * 
 * 
 * This abstract class handles all the gestures in the application.
 */

import android.view.GestureDetector.SimpleOnGestureListener;
import mocr.barcode.R;

public abstract class GestureHandler extends SimpleOnGestureListener implements IGestureHandler{

	protected int mCurrentState;

	public GestureHandler() {
		mCurrentState = R.id.state_idle;
	}

	// Update state
	protected abstract int nextState(int gestureEvent);
}
