/**
 * @author - Hussein Yapit and Josh Scotland
 * 
 */

package cs.washington.mobileocr.main;

import cs.washington.mobileocr.gestures.NavigationGestureHandler;
import cs.washington.mobileocr.tts.TTSThread;
import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class MobileOCR extends Activity {
    
	private static TextToSpeech mTts;
	private GestureDetector gestureScanner;
		
	private static String passedString;
	private int MY_DATA_CHECK_CODE;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //TODO: remove when camerafacade is ready
        setContentView(R.layout.main);
        
        //initialize gesture detector
        gestureScanner = new GestureDetector(new NavigationGestureHandler());
        
        //initialize tts engine in tts thread
        TTSThread.getInstance().ttsSetContext(this);
    }
    
    public boolean onTouchEvent(MotionEvent event)
    {
    	return gestureScanner.onTouchEvent(event);
     
    }
    
    
}