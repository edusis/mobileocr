package mocr.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;

//Sample Activity that listens to gestures. All the gesture detection code is passed to
//ScreenReaderGestureHandler -> This class handles gestures for screen reader
//It extends GestureHandler abstract class (check it out)

//NOTE all constants are in values/*.xml, please add new constants appropriately

public class MOCRMain extends Activity {
    private GestureDetector gestureDetector;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //In the navigation activity you would say new GestureDetector(new NavigationGestureHandler())
        //that extends GestureHandler
        gestureDetector = new GestureDetector(new ScreenReaderGestureHandler());
    }
    
    public boolean onTouchEvent(MotionEvent event)
    {
    	if (gestureDetector.onTouchEvent(event)) {
            return true;
        }
        return false;
    }
}