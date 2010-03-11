
package washington.cs.mobileocr.weocr;

/**
 * Copyright 2010, Josh Scotland & Hussein Yapit
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided you follow the BSD license.
 * 
 * 
 * This class connects several parts of the OCR process together.
 * After a picture is taken, this class will convert that preview frame
 * into a bitmap. The bitmap is then sent to a server where OCR is done
 * and the text is received. After the text is received, this class will
 * tell the application that there is text to display in the screen reader.
 */

import washington.cs.mobileocr.main.R;
import washington.cs.mobileocr.tts.TTSHandler;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public class OCRThread extends HandlerThread {

	private static final String TAG = "OCRThread";

	private Handler mUIHandler;
	private Handler mHandler;

	//Handles an OCR request when the camera has taken a picture
	protected void onLooperPrepared () {
		mHandler = new Handler(getLooper()) {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case R.id.msg_ocr_recognize:
					TTSHandler.ttsQueueSRMessage("Processing image, please wait");
					Bitmap bmp = asBitmap(msg.arg1, msg.arg2,(byte[])msg.obj);
					String ocrText = Server.doFileUpload(bmp);
					Message success = mUIHandler.obtainMessage(R.id.msg_ui_ocr_success, ocrText);
					mUIHandler.sendMessage(success);
					break;
				case R.id.msg_ocr_quit:
					getLooper().quit();
					break;
				default:
					super.handleMessage(msg);
				}
			}
		};
	}

	//Creates an instance of OCRThread
	public OCRThread (Handler uiHandler) {
		super(TAG);
		mUIHandler = uiHandler;
	}

	//Returns a handler
	public final Handler getHandler () {
		return mHandler;
	}

	//Thanks to WordSnap for this method!
	//Given a byte array from the camera, decodes the YUV for bitmap processing
	private static void decodeYUV(int[] out, byte[] fg, int width, int
			height) throws NullPointerException, IllegalArgumentException {
		final int sz = width * height;
		if(out == null) throw new NullPointerException("buffer 'out' is null");
		if(out.length < sz) throw new IllegalArgumentException("buffer 'out' size " + out.length + " < minimum " + sz);
		if(fg == null) throw new NullPointerException("buffer 'fg' is null");
		if(fg.length < sz) throw new IllegalArgumentException("buffer 'fg'	size " + fg.length + " < minimum " + sz * 3/ 2);
		int i, j;
		int Y, Cr = 0, Cb = 0;
		for(j = 0; j < height; j++) {
			int pixPtr = j * width;
			final int jDiv2 = j >> 1;
		for(i = 0; i < width; i++) {
			Y = fg[pixPtr]; if(Y < 0) Y += 255;
			if((i & 0x1) != 1) {
				final int cOff = sz + jDiv2 * width + (i >> 1) * 2;
				Cb = fg[cOff];
				if(Cb < 0) Cb += 127; else Cb -= 128;
				Cr = fg[cOff + 1];
				if(Cr < 0) Cr += 127; else Cr -= 128;
			}
			int R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5);
			if(R < 0) R = 0; else if(R > 255) R = 255;
			int G = Y - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1) + (Cr >>
			3) + (Cr >> 4) + (Cr >> 5);
			if(G < 0) G = 0; else if(G > 255) G = 255;
			int B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6);
			if(B < 0) B = 0; else if(B > 255) B = 255;
			out[pixPtr++] = 0xff000000 + (B << 16) + (G << 8) + R;
		}
		}
	}

	//Create a bitmap to send to the server after YUV decoding
	public Bitmap asBitmap ( int width, int height, byte[] rawData) {
		int imgWidth = width;
		int imgHeight = height;
		int[] buf = new int[imgWidth * imgHeight];
		decodeYUV(buf, rawData, imgWidth, imgHeight);
		Bitmap b = Bitmap.createBitmap(buf, width, height, Config.ARGB_8888);
		return b;
	}
}
