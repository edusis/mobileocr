package washington.cs.mobileocr.weocr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import washington.cs.mobileocr.main.MobileOCRApplication;
import washington.cs.mobileocr.main.R;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

public class OCRThread extends HandlerThread {
	
    private static final String TAG = OCRThread.class.getSimpleName();
    
    private Handler mUIHandler;

    private Handler mHandler;
    
    @Override
    protected void onLooperPrepared () {
        mHandler = new Handler(getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
   
                case R.id.msg_ocr_recognize:
              
                	sendOCRRequest(asBitmap(msg.arg1, msg.arg2,(byte[])msg.obj));
                	break;
                case R.id.msg_ocr_quit:
                    getLooper().quit();
                    break;
                default:
                    super.handleMessage(msg);
                }
            }
        };
    }
    
    public OCRThread (Handler uiHandler) {
        super(TAG);
        mUIHandler = uiHandler;
    }
    
    public final Handler getHandler () {
        return mHandler;
    }
    
    private void sendOCRRequest (Bitmap textBitmap) {
    	
        WeOCRClient weOCRClient = MobileOCRApplication.getInstance().getOCRClient();
        try {
            String ocrText = weOCRClient.doOCR(textBitmap);
            Message msg = mUIHandler.obtainMessage(R.id.msg_ui_ocr_success, ocrText);
            mUIHandler.sendMessage(msg);
        } catch (IOException ioe) {
            // TODO
            Log.e(TAG, "WeOCR failed", ioe);
            mUIHandler.sendEmptyMessage(R.id.msg_ui_ocr_fail);
        }
    }
    
    private static void decodeYUV(int[] out, byte[] fg, int width, int
    		height) throws NullPointerException, IllegalArgumentException {
    			final int sz = width * height;
    			if(out == null) throw new NullPointerException("buffer 'out' is null");
    			if(out.length < sz) throw new IllegalArgumentException("buffer 'out' size " + out.length + " < minimum " + sz);
    			if(fg == null) throw new NullPointerException("buffer 'fg' is null");
    			if(fg.length < sz) throw new IllegalArgumentException("buffer 'fg'	size " + fg.length + " < minimum " + sz * 3/ 2);
    			int i, j;
    			int Y, Cr = 0, Cb = 0;
    			for(j = 0; j < height; j++) {
    				int pixPtr = j * width;
    				final int jDiv2 = j >> 1;
    				for(i = 0; i < width; i++) {
    					Y = fg[pixPtr]; if(Y < 0) Y += 255;
    					if((i & 0x1) != 1) {
    						final int cOff = sz + jDiv2 * width + (i >> 1) * 2;
    						Cb = fg[cOff];
    						if(Cb < 0) Cb += 127; else Cb -= 128;
    						Cr = fg[cOff + 1];
    						if(Cr < 0) Cr += 127; else Cr -= 128;
    					}
    					int R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5);
    					if(R < 0) R = 0; else if(R > 255) R = 255;
    					int G = Y - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1) + (Cr >>
    		3) + (Cr >> 4) + (Cr >> 5);
    					if(G < 0) G = 0; else if(G > 255) G = 255;
    					int B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6);
    					if(B < 0) B = 0; else if(B > 255) B = 255;
    					out[pixPtr++] = 0xff000000 + (B << 16) + (G << 8) + R;
    		}
    	}
    }
    
    final public Bitmap asBitmap ( int width, int height, byte[] rawData) {
       
        int imgWidth = width;
        int imgHeight = height;
        int[] buf = new int[imgWidth * imgHeight];
        
        decodeYUV(buf, rawData, imgWidth, imgHeight);
        
        Bitmap b = Bitmap.createBitmap(buf, width, height, Config.ARGB_8888);
        return b;        
    }
    
}
