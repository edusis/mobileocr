package mocr.barcode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.View;
import android.widget.Button;

public class BarcodeScan extends Activity {

	private static TextToSpeech mTts;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		OnInitListener ttsInitListener = new OnInitListener() {
			public void onInit(int version) {
				mTts.speak("Hello world", 0, null);
			}
		};

		mTts = new TextToSpeech(this, ttsInitListener);

		final Button button = (Button) findViewById(R.id.mScan);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent("com.google.zxing.client.android.SCAN");
				startActivityForResult(intent, 0);
			}
		});
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				//String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
				// Handle successful scan
				mTts.stop();
				startScreenReaderView(contents);
			} else if (resultCode == RESULT_CANCELED) {
				// Handle cancel
			}
		}
	}
	
	//Creates an intent to start ScreenReader Activity
	private void startScreenReaderView(String result) {
		Intent i = new Intent(this, Screenreader.class);
		i.putExtra("resultString", result);
		startActivity (i);
	}

	public static TextToSpeech getmTts() {
		return mTts;
	}
}