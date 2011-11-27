package mobi.cyann.nstools;

import android.os.Bundle;

public class NSTweakActivity extends BasePreferenceActivity {
	//private final static String LOG_TAG = "NSTools.NSTweakActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// set preference layout
		addPreferencesFromResource(R.xml.ns_tweak);
	}
}
