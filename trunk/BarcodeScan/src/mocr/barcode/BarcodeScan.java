package mocr.barcode;

import washington.cs.mobileocr.tts.TTSHandler;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;

public class BarcodeScan extends Activity implements OnClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Clear title bar and notification bar
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		setContentView(R.layout.main);
		SurfaceView sv = (SurfaceView)findViewById(R.id.sv);
        sv.setOnClickListener(this);

		TTSHandler.getInstance().ttsSetContext(this, this.getResources());
	}

	private Boolean error = false;
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				error = false;
				startScreenReaderView(contents);
			} else if (resultCode == RESULT_CANCELED) {
				error = true;
			}
		}
	}
	
	//Creates an intent to start ScreenReader Activity
	private void startScreenReaderView(String result) {
		Intent i = new Intent(this, ScreenReader.class);
		i.putExtra("resultString", result);
		startActivity (i);
	}

	public void onClick(View arg0) {
		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		startActivityForResult(intent, 0);
	}
	
	protected void onResume() {
		if (error)
			TTSHandler.ttsQueueSRMessage("Scanning failed. Tap the screen and scan a barcode.");
		else
			TTSHandler.ttsQueueSRMessage("Tap the screen and scan a barcode.");
		super.onResume();
	}

	protected void onPause() {
		super.onPause();
	}

	public void onStop() {
		super.onDestroy();
	}

	public void onDestroy() {
		TTSHandler.getInstance().TTSDestroy();
		super.onDestroy();
	}
}