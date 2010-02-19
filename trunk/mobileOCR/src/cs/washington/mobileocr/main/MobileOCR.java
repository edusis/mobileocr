/**
 * @author - Hussein Yapit and Josh Scotland
 * 
 */

package cs.washington.mobileocr.main;

import cs.washington.mobileocr.gestures.NavigationGestureHandler;
import cs.washington.mobileocr.tts.TTSHandler;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

public class MobileOCR extends Activity {

	private static TextToSpeech mTts;
	private GestureDetector gestureScanner;

	private static final String TAG = "MobileOCR";

	private ConnectivityManager mConnectivityManager;

	private CameraFacade cameraFacade;
	private OCRThread mOCRThread;

	private static String passedString;
	private int MY_DATA_CHECK_CODE;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//clear title bar and notification bar
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		
		Intent myIntent = new Intent(this, ScreenReader.class);
		startActivity(myIntent);

		mConnectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

		SurfaceView view = new SurfaceView(this);

		cameraFacade = new CameraFacade(view.getHolder(), mHandler);

		setContentView(view);

		//initialize gesture detector
		gestureScanner = new GestureDetector(new NavigationGestureHandler());

		//initialize tts engine and check for correct installation of TTS engine
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == MY_DATA_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// success, create the TTS instance
				TTSHandler.getInstance().ttsSetContext(this);
				Log.d(TAG, "TTS engine check");
			} else {
				// missing data, install it
				Intent installIntent = new Intent();
				installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		}
	}

	public boolean onTouchEvent(MotionEvent event)
	{
		return gestureScanner.onTouchEvent(event);

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
		//startOCRThread();
		//cameraFacade.onResume();
		super.onResume();
	}

	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
		//unregisterReceiver(mConnectivityReceiver);
		stopOCRThread();
	}

	private void sendOCRRequest(final Bitmap textBitmap) {

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

	private void startScreenReaderView(String result)
	{
		Intent i = new Intent(this, ScreenReader.class);
		i.putExtra("resultString", result);
		startActivity (i);
	}

	private final Handler mHandler = new Handler () {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case R.id.msg_camera_auto_focus:
				int status = msg.arg1;
				cameraFacade.clearAutoFocus();

				if (status == CameraFacade.AUTOFOCUS_SUCCESS) {
					cameraFacade.requestPreviewFrame();
				}

				break;
			case R.id.msg_camera_preview_frame:
				Handler ocrHandler = mOCRThread.getHandler();

				int width = cameraFacade.getWidth();
				int height = cameraFacade.getHeight();
				Message preprocessMsg = ocrHandler.obtainMessage(R.id.msg_ocr_recognize, width, height, msg.obj);
				ocrHandler.sendMessage(preprocessMsg);
				break;
			case R.id.msg_ui_ocr_success:

				cameraFacade.onPause();

				startScreenReaderView((String)msg.obj);

				break;
			case R.id.msg_ui_ocr_fail:
				cameraFacade.startPreview();
				break;
			}
		}
	};
}