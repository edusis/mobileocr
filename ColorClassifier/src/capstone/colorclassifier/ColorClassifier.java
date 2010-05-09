package capstone.colorclassifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.view.Window;
import android.view.WindowManager;

import com.google.tts.TextToSpeechBeta;
import com.google.tts.TextToSpeechBeta.OnInitListener;

/*
 * Josh Scotland
 * This is a basic color classifier application using screen taps as inputs. 
 * It identifies the most common color in the center portion of the screen.
 * It outputs using TTS for blind people
 * It outputs using the vibrator for deaf blind people based on ROY G BIV
 */
public class ColorClassifier extends Activity  {

	private TextToSpeechBeta mTts;
	private CameraSetup mCameraSetup;
	private Vibrator vibrator;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.camera_surface);
		mTts = new TextToSpeechBeta(this, ttsInitListener);
		mCameraSetup = new CameraSetup(this);
		setContentView(mCameraSetup);
		mCameraSetup.setOnClickListener(mClickListener);
		vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
	}

	//TTS initialization
	//Uses a British locale
	private OnInitListener ttsInitListener = new OnInitListener() {
		public void onInit(int arg0, int arg1) {
			Log.i("ColorClassifier","TTS Initialization");
			//mTts.setLanguage(Locale.UK);
			mTts.speak("(British Locale Test): Tap the screen to identify the central color in front of the camera", 0, null);
		}
	};

	//Not a true pause, app destroys itself
	public void onPause() {
		Log.i("ColorClassifier","Pause Activity");
		mCameraSetup = null;
		mTts.shutdown();
		super.onDestroy();
	}

	/*TODO Want a better on resume activity (right now program closes on pause)
	private void onResume() {
		super.onResume();
		Log.i("ColorClassifier","Resume Activity");
		mTts.speak("Tap the screen to identify the central color in front of the camera", 0, null);
		mCameraSetup.onResume();        
	}
	 */

	//Shutdown the TTS with the application
	public void onStop() {
		Log.i("ColorClassifier","Stop Activity");
		mCameraSetup = null;
		mTts.shutdown();
		super.onDestroy();
	}

	//Shutdown the TTS with the application
	public void onShutdown() {
		Log.i("ColorClassifier","Shutdown Activity");
		mCameraSetup = null;
		mTts.shutdown();
		super.onDestroy();
	}

	//Listening for screen taps
	private View.OnClickListener mClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			Log.i("ColorClassifier","On Click");
			if(v.equals(mCameraSetup)) {
				mCameraSetup.takePicture(null, null, mPictureCallback);
			}
		}
	};


	//Create a bitmap and get the RGB values to send to the PixelColorNamer (i.e. to determine the color)
	private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] imageData, Camera c) {
			Log.i("ColorClassifier","Take Picture");
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 16;  // Get a smaller sample of the image
			Bitmap image = BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);

			/*
	        try { // catches IOException below
	            FileOutputStream fOut = openFileOutput("img.bmp", MODE_WORLD_READABLE);
	            OutputStreamWriter osw = new OutputStreamWriter(fOut);
	            osw.write(fOut);
	            imageData.compress(Bitmap.CompressFormat.PNG, 90, osw);
	            osw.flush();
	            osw.close();

	            FileInputStream fIn = openFileInput("samplefile.txt");
	            InputStreamReader isr = new InputStreamReader(fIn);

	            char[] inputBuffer = new char[TESTSTRING.length()];

	            isr.read(inputBuffer);

	            String readString = new String(inputBuffer);

	            // Check if we read back the same chars that we had written out
	            boolean isTheSame = TESTSTRING.equals(readString);

	            // WOHOO lets Celebrate =)
	            Log.w("File Reading stuff", "success = " + isTheSame);

	        } catch (IOException ioe) {
	            ioe.printStackTrace();
	        }
			 */

			/*
	       	try {
                FileOutputStream out = new FileOutputStream("img.png");
                image.compress(Bitmap.CompressFormat.PNG, 90, out);
                } catch (Exception e) {
                	Log.e("CAMERA", "DID NOT SAVE");
                        e.printStackTrace();
            }
            */
			
			

			int w = image.getWidth();
			Log.i("ColorClassifier","Width " + w);
			int h = image.getHeight();
			Log.i("ColorClassifier","Height " + h);
			int[] pixels = new int[w*h];
			Log.i("ColorClassifier","Number of Pixels: " + pixels.length);
			image.getPixels(pixels,0,w,0,0,w,h);

			//String name = MediaStore.Images.Media.insertImage(ColorClassifier.this.getContentResolver(), image, "img.jpg", "image");
			//Log.e("NAME", name);

			/*
			String myPath = "/sdcard/DCIM/Camera/testJpg.jpg";
			Bitmap marker2 = Bitmap.createBitmap(image,0,0,w,h);
			try
			{
			File compressTry = new File(myPath);
			Log.e("NAME", "ACCESSED FILE DIR");
			FileOutputStream outstream = new FileOutputStream(compressTry);
			Log.e("NAME", "CREATED OUTPUT STREAM");
			marker2.compress(CompressFormat.JPEG,100,outstream);
			Log.e("NAME", "SAVED FILE");
			}
			catch (IOException ie)
			{
			Log.e("NAME", "NO MAKE");
			}
			*/

			/*
			// save image to SD card
			try {
				File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
				Log.e("NAME", "ACCESSED FILE DIR");
				if(!directory.exists()) {
					directory.mkdir();
					Log.e("NAME", "MAKE DIR");
				}
				FileOutputStream fos = new FileOutputStream(directory+"/"+"hi"+".jpg");
				Log.e("NAME", "CREATED OUTPUT STREAM");
				Environment.getExternalStorageDirectory().mkdir();
				image.compress(Bitmap.CompressFormat.JPEG, 90, fos);
				Log.e("NAME", "SAVED FILE");
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("NAME", "NO MAKE");
			}
			*/


			int R, G, B;
			int[] count = new int[101];
			int max = 0;
			int maxIndex = 0;
			for (int y = 38; y < 58; y++) {  // Use the central pixels of the image
				for (int x = 49; x < 79; x++) {
					int index = y * w + x;
					R = (pixels[index] >> 16) & 0xff;
					G = (pixels[index] >> 8) & 0xff;
					B = pixels[index] & 0xff;
					int color = PixelColorNamer.classifyPixel(R,G,B);
					count[color]++;
					if (count[color] > max) {
						max = count[color];
						maxIndex = color;
					}
				}
			}

			image.recycle(); // Helps the GC out
			mTts.speak(PixelColorNamer.getColorName(maxIndex), 0, null);

			/*
			if (maxIndex == 30 || maxIndex == 31 || maxIndex == 90 || maxIndex == 91) {
				long[] pattern = {0L,500L};
				vibrator.vibrate(pattern,-1);
			}
			else if (maxIndex == 70 || maxIndex == 62 || maxIndex == 100) {
				long[] pattern = {0L, 500L, 500L, 500L};
				vibrator.vibrate(pattern,-1);
			}
			else if (maxIndex == 60 || maxIndex == 61) {
				long[] pattern = {0L, 500L, 500L, 500L, 500L, 500L};
				vibrator.vibrate(pattern,-1);
			}
			else if (maxIndex == 40 || maxIndex == 41 || maxIndex == 42 || maxIndex == 43 || maxIndex == 44) {
				long[] pattern = {0L, 500L, 500L, 500L, 500L, 500L, 500L, 500L};
				vibrator.vibrate(pattern,-1);
			}
			else if (maxIndex == 50 || maxIndex == 51 || maxIndex == 52 || maxIndex == 53) {
				long[] pattern = {0L, 500L, 500L, 500L, 500L, 500L, 500L, 500L, 500L, 500L};
				vibrator.vibrate(pattern,-1);
			}
			else if (maxIndex == 51) {
				long[] pattern = {0L, 500L, 500L, 500L, 500L, 500L, 500L, 500L, 500L, 500L, 500L, 500L};
				vibrator.vibrate(pattern,-1);
			}
			else if (maxIndex == 80 || maxIndex == 81 || maxIndex == 82 || maxIndex == 83) {
				long[] pattern = {0L, 500L, 500L, 500L, 500L, 500L, 500L, 500L, 500L, 500L, 500L, 500L, 500L, 500L};
				vibrator.vibrate(pattern,-1);
			}
			else {
				long[] pattern = {0L};
				vibrator.vibrate(pattern,-1);
			}
			 */
		}
	};

}