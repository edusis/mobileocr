package washington.cs.mobileocr.weocr;

import washington.cs.mobileocr.main.R;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

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
                case R.id.msg_ocr_detect_word:
                	break;
                case R.id.msg_ocr_recognize:
                	String ocrText = Server.doFileUpload((new GrayImage((byte[])msg.obj, msg.arg1, msg.arg2).asBitmap()));
                	Message msg2 = mUIHandler.obtainMessage(R.id.msg_ui_ocr_success, ocrText);
                    mUIHandler.sendMessage(msg2);
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
}
