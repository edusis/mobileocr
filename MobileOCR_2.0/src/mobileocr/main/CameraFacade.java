package mobileocr.main;

/**
 * Copyright 2010, Josh Scotland & Hussein Yapit
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided you follow the BSD license.
 */

import java.io.IOException;
import java.util.List;

import mobileocr.server.DoServerOCR;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/*
 * Based off of Google's camera API example
 * This class handles the camera functions.
 */

public class CameraFacade extends SurfaceView implements SurfaceHolder.Callback {

	public static final String TAG = "CameraFacade";

	private boolean autoFocusInProgress;
	private boolean previewCaptureInProgress;

	public static final int AUTOFOCUS_UNKNOWN = 0;
	public static final int AUTOFOCUS_SUCCESS = 1;
	public static final int AUTOFOCUS_FAILURE = 2;

	private SurfaceHolder mHolder;
	private Camera mCamera;
	private MediaPlayer mMediaPlayer;
	
	private Handler mHandler = null;
	
	public CameraFacade(Context context, Handler UIHandler) {
        super(context);
        
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        // Handles messages Mobile OCR Main is listening to
        mHandler = UIHandler;
        
        // MediaPlayer for the camera shutter sound
        mMediaPlayer = MediaPlayer.create(context, R.raw.camera1);
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
        // Stop the preview and free the camera from the resources
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
        // Set up the camera parameters and begin the preview.
        Camera.Parameters parameters = mCamera.getParameters();

        List<Size> sizes = parameters.getSupportedPreviewSizes();
        Size optimalSize = getOptimalPreviewSize(sizes, w, h);
        parameters.setPreviewSize(optimalSize.width, optimalSize.height);

        mCamera.setParameters(parameters);
        mCamera.startPreview();
    }
    
    public void takePicture() {
		if (autoFocusInProgress || previewCaptureInProgress) {
			return;
		}
		previewCaptureInProgress = true;
    	mCamera.takePicture(shutterCallback, rawCallback, jpegCallback); 
    }

    ShutterCallback shutterCallback = new ShutterCallback() {
    	public void onShutter() {
    		// Play a shutter sound
    		mMediaPlayer.start();
    	}
    };

    PictureCallback rawCallback = new PictureCallback() {
    	public void onPictureTaken(byte[] data, Camera camera) {
    		// Not using the raw YUV format
    	}
    };

    PictureCallback jpegCallback = new PictureCallback() {
    	public void onPictureTaken(byte[] data, Camera camera) {
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
    		
    		String ocrResult = DoServerOCR.getOCRResponse(data);
    		
    		Message success = mHandler.obtainMessage(R.id.msg_ocr_success, ocrResult);
			mHandler.sendMessage(success);
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
	
	public void requestAutoFocus () {
		if (autoFocusInProgress || previewCaptureInProgress) {
			return;
		}
		autoFocusInProgress = true;
		mCamera.autoFocus(new Camera.AutoFocusCallback() { 
			public void onAutoFocus(boolean success, Camera camera) {
				autoFocusInProgress = false;
			}
		});
	}
}
