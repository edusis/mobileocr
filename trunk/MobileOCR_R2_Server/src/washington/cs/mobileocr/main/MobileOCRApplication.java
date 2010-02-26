package washington.cs.mobileocr.main;

import washington.cs.mobileocr.main.R;
import washington.cs.mobileocr.weocr.WeOCRClient;
import washington.cs.mobileocr.weocr.WeOCRServerList;
import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MobileOCRApplication extends Application {

    private static MobileOCRApplication sMe;
    
    private WeOCRClient mWeOCRClient;
    private WeOCRServerList mWeOCRServerList;
    
    public MobileOCRApplication () {
        sMe = this;
    }
    
    public static final MobileOCRApplication getInstance () {
    	if (sMe == null)
    	{
    		sMe = new MobileOCRApplication();
    	}
        return sMe;
    }
    
    public static final WeOCRClient getOCRClient () {
        return sMe.mWeOCRClient;
    }
    
    public static final WeOCRServerList getOCRServerList () {
        return sMe.mWeOCRServerList;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();

        rebindServer(null);
        
        // Load WeOCR server list
        try {
            mWeOCRServerList = new WeOCRServerList(this, R.xml.weocr);
        } catch (Throwable t) {
            // TODO
        }
    }

    public void rebindServer (String endpointUrl) {
        SharedPreferences preferences = 
            PreferenceManager.getDefaultSharedPreferences(this);
        if (endpointUrl == null) {
            // Get preferred endpoint URL
            endpointUrl = preferences.getString("",
                    getString(R.string.pref_weocr_server_default));
        } 
        
        // Initialize new Delicious HTTP client
        mWeOCRClient = new WeOCRClient(endpointUrl);
        
    }
}
