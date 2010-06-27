
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import mobileocr.main.R;
import mobileocr.server.Server;
import mobileocr.tts.TTSHandler;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraFacade extends SurfaceView implements SurfaceHolder.Callback {

	public static final String TAG = "CameraFacade";

	/*
	private boolean mAutoFocusInProgress;
	private boolean mPreviewCaptureInProgress;

	public static final int AUTOFOCUS_UNKNOWN = 0;
	public static final int AUTOFOCUS_SUCCESS = 1;
	public static final int AUTOFOCUS_FAILURE = 2;
	*/

	private SurfaceHolder mHolder;
	private Camera mCamera;
	private MediaPlayer mp;
	
	private Handler mUIHandler = null;
	
	public CameraFacade(Context context) {
        super(context);
        
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        // MediaPlayer for the camera shutter sound
        mp = MediaPlayer.create(context, R.raw.camera1);
    }
	
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        mCamera = Camera.open();
        try {
           mCamera.setPreviewDisplay(holder);
        } catch (IOException exception) {
            mCamera.release();
            mCamera = null;
        }
    }
    
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        Camera.Parameters parameters = mCamera.getParameters();

        List<Size> sizes = parameters.getSupportedPreviewSizes();
        Size optimalSize = getOptimalPreviewSize(sizes, w, h);
        parameters.setPreviewSize(optimalSize.width, optimalSize.height);

        mCamera.setParameters(parameters);
        mCamera.startPreview();
    }
    
    public void takePicture() {
    	mCamera.takePicture(shutterCallback, rawCallback, jpegCallback); 
    }

    ShutterCallback shutterCallback = new ShutterCallback() {
    	public void onShutter() {
    		TTSHandler.ttsQueueSRMessage("Saving Picture");
    	}
    };

    PictureCallback rawCallback = new PictureCallback() {
    	public void onPictureTaken(byte[] data, Camera camera) {

    	}
    };

    PictureCallback jpegCallback = new PictureCallback() {
    	public void onPictureTaken(byte[] data, Camera camera) {
    		mp.start();
    		
    		// Camera still running
    		

			
			/*
			// This will save an image to the sdcard. Three important points:
			// 1. Allow external storage permission, 
			// 2. Use Environment.getExternalStorageDirectory()
			// 3. Create an empty file in the sdcard before writing otherwise, File Not Found errors
    		
    		BitmapFactory.Options options = new BitmapFactory.Options();
    		//options.inSampleSize = 2;
    		//options.inTargetDensity = 200;
			Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
    		
			File file = new File(Environment.getExternalStorageDirectory() + "/mocr.jpeg");
			try {
				FileOutputStream out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/mocr.jpeg");
				bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
				Log.i(TAG, "Picture Saved");
			} catch (Exception e) {
				Log.e(TAG, "Exception: " + e.getMessage(), e);
			}
			
			*/
			
			//String s = Server.doFileUpload(bmp);
			String s = Server.doFileUpload(data);
			//TTSHandler.ttsQueueSRMessage(s);

			//TTSHandler.ttsQueueSRMessage("Picture Saved");
    	}
    };

	public void onResume() {
		/*
		if(surfaceExists && mCamera == null) {
			startCamera();
			setCameraParameters();
		}
		*/
	}

	public void onPause() {
		/*
		if(mCamera != null) {
			if(mPreviewRunning) {
				mCamera.stopPreview();
				mPreviewRunning = false;
			}
			mCamera.release();
			mCamera = null;
		}
		*/
	}
	
	/*

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
				Message msg = mUIHandler.obtainMessage(R.id.msg_camera_preview_frame, data);
				mUIHandler.sendMessage(msg);
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}
		});
	}
	*/

}
