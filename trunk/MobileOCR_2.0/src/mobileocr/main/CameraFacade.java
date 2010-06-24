
package mobileocr.main;

/**
 * Copyright 2010, Josh Scotland & Hussein Yapit
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided you follow the BSD license.
 * 
 * Used Will Johnson's camera facade as an example.
 * This class handles the camera functions.
 */

import java.io.IOException;

import mobileocr.main.R;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.Size;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;

public class CameraFacade implements SurfaceHolder.Callback {

	public static final String CAMERA_TAG = "CameraFacade";

	private boolean mAutoFocusInProgress;
	private boolean mPreviewCaptureInProgress;

	public static final int AUTOFOCUS_UNKNOWN = 0;
	public static final int AUTOFOCUS_SUCCESS = 1;
	public static final int AUTOFOCUS_FAILURE = 2;
	private Handler mUIHandler = null;

	private SurfaceHolder mHolder;
	private Camera mCamera;
	private int mx;
	private int my;
	private boolean surfaceExists; //This is true between SurfaceCreated and SurfaceDestroyed
	private boolean mPreviewRunning; //This next parameter is true between a call of mCamera.startPreview() and mCamera.stopPreview
	private MediaPlayer mp;

	//Construct a CameraFacade
	public CameraFacade(Context context, SurfaceHolder holder, Handler UIHandler) {    
		mHolder = holder;
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mCamera = null;
		mx = my = 0;
		surfaceExists = mPreviewRunning = false;
		mUIHandler = UIHandler;
		mp = MediaPlayer.create(context, R.raw.camera1);
	}

	public void onResume() {
		if(surfaceExists && mCamera == null) {
			startCamera();
			setCameraParameters();
		}
	}

	public void onPause() {
		if(mCamera != null) {
			if(mPreviewRunning) {
				mCamera.stopPreview();
				mPreviewRunning = false;
			}
			mCamera.release();
			mCamera = null;
		}
	}

	//The Surface has been created, acquire the camera and tell it where to draw.
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i(CAMERA_TAG,"Surface Created");
		if(mHolder == null)
			mHolder = holder;
		startCamera();
		surfaceExists = true;
	}
	
	//Now that the size is known, set up the camera parameters and begin the preview
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.i(CAMERA_TAG,"call to surfaceChanged()");
		if(mCamera == null)
			return;
		mx = width;
		my = height;
		setCameraParameters();
	}

	public void startPreview() {
		if(!mPreviewRunning)
			mCamera.startPreview();
		mPreviewRunning = true;
	}

	private void startCamera() {
		if(mCamera == null)
			mCamera = Camera.open();
		try {
			mCamera.setPreviewDisplay(mHolder); // throws IOException
			mCamera.setErrorCallback(new ErrorCallback() {
				public void onError(int code, Camera c) {
					if(code == Camera.CAMERA_ERROR_SERVER_DIED)
						Log.e(CAMERA_TAG,"The camera server died");
					else
						Log.e(CAMERA_TAG,"Unknown camera error");
				}
			});
		}
		catch(IOException ioe) {
			mCamera.release();
			mCamera = null;
		}
		mPreviewRunning = false;
	}

	//Sets the values for the camera's parameters
	private void setCameraParameters() {
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPictureSize(mx, my);
		parameters.setPictureFormat(PixelFormat.JPEG);
		mCamera.setParameters(parameters);
		if(!mPreviewRunning)
			mCamera.startPreview();
		mPreviewRunning = true;
		initCameraStateVariables();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		surfaceExists = false;
		Log.i(CAMERA_TAG,"Surface destroyed! mPreviewRunning = " + mPreviewRunning);
		if(mCamera == null) {
			mPreviewRunning = false; //It probably should've been already anyways
			return;
		}
		if(mPreviewRunning) {
			mCamera.stopPreview();
			mPreviewRunning = false;
		}
		Log.i(CAMERA_TAG,"We've called stopPreview() (perhaps), but not yet released the camera");
		mCamera.release();
		mCamera = null;
		mHolder = null;
	}

	//Get a preview frame from the camera
	public void getPreview(Camera.PreviewCallback callback) {
		if(mCamera == null)
			return;
		mCamera.setPreviewCallback(callback);
		Log.i(CAMERA_TAG ,callback==null?"Stopping previews":"Starting to request preview frames");
	}

	public void requestAutoFocus () {
		if (mAutoFocusInProgress || mPreviewCaptureInProgress) {
			return;
		}
		mAutoFocusInProgress = true;
		mCamera.autoFocus(new Camera.AutoFocusCallback() { 
			public void onAutoFocus(boolean success, Camera camera) {
				Message msg = mUIHandler.obtainMessage(R.id.msg_camera_auto_focus, 
						success ? AUTOFOCUS_SUCCESS : AUTOFOCUS_FAILURE, -1);
				mUIHandler.sendMessage(msg);
			}
		});
	}

	public void clearAutoFocus() {
		mAutoFocusInProgress = false;
	}

	public void requestPreviewFrame () {
		if (mAutoFocusInProgress || mPreviewCaptureInProgress) {
			return;
		}
		mPreviewCaptureInProgress = true;
		mCamera.setOneShotPreviewCallback(new Camera.PreviewCallback() {
			public void onPreviewFrame(byte[] data, Camera camera) {
				mp.start();
				Message msg = mUIHandler.obtainMessage(R.id.msg_camera_preview_frame, data);
				mUIHandler.sendMessage(msg);
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}
		});
	}

	public void clearPreviewFrame() {
		mPreviewCaptureInProgress = false;
	}

	public Size getPreviewSize() {
		if (mPreviewRunning) {
			return mCamera.getParameters().getPreviewSize();
		}
		return null;
	}

	public int getWidth() {
		return mx;
	}

	public int getHeight() {
		return my;
	}

	private void initCameraStateVariables () {
		mAutoFocusInProgress = false;
		mPreviewCaptureInProgress = false;
	}

}
