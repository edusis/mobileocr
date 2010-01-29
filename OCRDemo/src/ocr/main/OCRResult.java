package ocr.main;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class OCRResult extends Activity{
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		TextView tv = (TextView)findViewById(R.id.hello);
		
		Bundle extras = this.getIntent().getExtras();
		
		if (extras != null) {
			String result = extras.getString("res");
			tv.setText("result: \n" + result);
		} else {
			tv.setText("extras is null");
		}
	}
}
