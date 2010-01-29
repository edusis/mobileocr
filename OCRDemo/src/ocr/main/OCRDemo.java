package ocr.main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;


import ocr.main.OCRThread;

import ocr.main.OCRApplication;

import ocr.weocr.WeOCRClient;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;


public class OCRDemo extends Activity {
    
	private static final String TAG = "OCR";
	
    private ConnectivityManager mConnectivityManager;
    
    private OCRThread mOCRThread;
    private Handler mCameraHandler;
    
    private CameraFacade cameraFacade;
    
    private boolean mProcessingInProgress;
    
    private TextView mResultText = null;
    
    private static final int AUTOFOCUS_UNKNOWN = 0;
    private static final int AUTOFOCUS_SUCCESS = 1;
    private static final int AUTOFOCUS_FAILURE = 2;
	
    private static PrintWriter mPW = null;
    
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //clear title bar and notification bar
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);   
        
        mConnectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        
        SurfaceView view = new SurfaceView(this);
        
        cameraFacade = new CameraFacade(view.getHolder(), mHandler);
        
        setContentView(view);
        
        mResultText = (TextView)findViewById(R.id.hello);
    }
	
	private void startOCRThread () {
        assert(mOCRThread == null);
        mOCRThread = new OCRThread(mHandler);
        mOCRThread.start();
    }
    
    private void stopOCRThread () {
        if (mOCRThread != null) {
            mOCRThread.getHandler().sendEmptyMessage(R.id.msg_ocr_quit);
            try {
                mOCRThread.join();
            } catch (InterruptedException ie) { }
            mOCRThread = null;
            // Don't send any messages that will cause a NullPointerException
            mHandler.removeMessages(R.id.msg_camera_preview_frame);
        }
    }
    
	protected void onResume() {
		Log.d(TAG, "onResume");
		startOCRThread();
		cameraFacade.onResume();
        super.onResume();
	}
	
	 protected void onPause() {
	        Log.d(TAG, "onPause");
	        super.onPause();
	        //unregisterReceiver(mConnectivityReceiver);
	        stopOCRThread();
	    }
	
	private void sendOCRRequest (final Bitmap textBitmap) {
        //mStatusText.setText(R.string.status_processing_text);
        Handler ocrHandler = mOCRThread.getHandler();
        Message ocrMessage = ocrHandler.obtainMessage(R.id.msg_ocr_recognize, textBitmap);
        ocrHandler.sendMessage(ocrMessage);
    }
	
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_FOCUS) {
            if (event.getRepeatCount() == 0) {
                cameraFacade.requestAutoFocus();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_CAMERA) {
            if (event.getRepeatCount() == 0) {
            	cameraFacade.requestPreviewFrame();
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
    
	private final Handler mHandler = new Handler () {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case R.id.msg_camera_auto_focus:
				int status = msg.arg1;
				cameraFacade.clearAutoFocus();
				
				if (status == AUTOFOCUS_SUCCESS)
					cameraFacade.requestPreviewFrame();
				break;
			case R.id.msg_camera_preview_frame:
				mProcessingInProgress = true;
                Handler ocrHandler = mOCRThread.getHandler();
                
                int width = cameraFacade.getWidth();
                int height = cameraFacade.getHeight();
                Message preprocessMsg = ocrHandler.obtainMessage(R.id.msg_ocr_recognize, width, height, msg.obj);
                ocrHandler.sendMessage(preprocessMsg);
				break;
			case R.id.msg_ui_ocr_success:
				mProcessingInProgress = false;
				
				//mResultText.setText((String)msg.obj);
				/*try {
					initFile();
				} catch (Exception e) {
					Log.d(TAG, e.toString());
					Log.d(TAG, (String)msg.obj);
				}
				
				writeToFile((String)msg.obj);*/
				cameraFacade.onPause();
				startOCRResultView((String)msg.obj);
				break;
			case R.id.msg_ui_ocr_fail:
				mResultText.setText("Request fails");
				break;
			}
		}
	};
	
	/*private void initFile() throws Exception
	{
    	File root = Environment.getExternalStorageDirectory();
    	
		String filename = new String("demoOut");
		int count = 0;
		String ext = ".txt";
		File outputFile = new File(root, filename + count + ext);
		//File outputFile = File.createTempFile(filename, ext, root);
		outputFile.createNewFile();
		while (outputFile.exists())
		{
			count++;
			outputFile = new File(filename + count + ext);
		}
		
		mPW = new PrintWriter(outputFile);
	
	}
    
    private void writeToFile(String text) {
    	if (mPW != null)
    	{
    		mPW.println(text);
    		mPW.close();
    	}
    }*/
	private void startOCRResultView(String result)
	{
		Intent i = new Intent(this, OCRResult.class);
		i.putExtra("res", result);
		i.putExtra("name", "The value");
		startActivity (i);
	}
}