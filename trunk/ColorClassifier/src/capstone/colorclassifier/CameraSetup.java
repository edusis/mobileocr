package capstone.colorclassifier;

import java.io.IOException;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/*
 * Josh Scotland
 * This class handles all the camera set up functions
 */

public class CameraSetup extends SurfaceView implements SurfaceHolder.Callback{
	
	private SurfaceHolder mHolder;
	private Camera mCamera;
	private boolean mPreviewRunning;

	CameraSetup(Context context) {
		super(context);
		Log.i("CameraSetup","Camera Creation");
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	/*TODO Want a better on pause and resume activities
	public void onResume() {
		if(mCamera == null) {
			Log.i("CameraSetup","Camera Resume");
			mCamera = Camera.open();
		}
	}
        
    public void onPause() {
    	Log.i("CameraSetup","Camera Pause");
    	if(mCamera != null) {
    		if(mPreviewRunning) {
    			mCamera.stopPreview();
    			mPreviewRunning = false;
    		}
    		mCamera.release();
    		mCamera = null;
    	}
    }
    */

	public void takePicture(Camera.ShutterCallback shutter, Camera.PictureCallback raw, Camera.PictureCallback jpeg) {
		mCamera.stopPreview();
		mCamera.takePicture(shutter, raw, jpeg);
		mCamera.startPreview();
	}
	
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i("CameraSetup","Surface Created");
		mCamera = Camera.open();
		mPreviewRunning = true;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.i("CameraSetup","Surface Changed");
		if (mPreviewRunning) {
			mCamera.stopPreview();
		}
		Camera.Parameters p = mCamera.getParameters();
		p.setPreviewSize(height, width);
		mCamera.setParameters(p);
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		mCamera.startPreview();
		mPreviewRunning = true;
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i("CameraSetup","Surface Destroyed");
		mCamera.stopPreview();
		mPreviewRunning = false;
		mCamera.release();
	}
	
}
