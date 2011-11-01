/**
 * OnBootCompleteService.java
 * 9:39:01 PM
 */
package mobi.cyann.nstools;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.Properties;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * @author arif
 *
 */
public class OnBootCompleteService extends IntentService {
	private final static String LOG_TAG = "NSTools.OnBootCompleteService";
	public OnBootCompleteService() {
		super("NSToolsService");
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			Properties p = new Properties();
			p.load(new FileInputStream(getString(R.string.NS_TWEAK_FILE)));

			FileWriter tempScript = new FileWriter(getString(R.string.NS_TEMP_SCRIPT));
			tempScript.write("#!/system/bin/sh\n");
			
			// BLD
			String status = p.getProperty("bld.status", "-1");
			if(!status.equals("-1")) {
				tempScript.write("echo ");
				tempScript.write(status);
				tempScript.write(" > /sys/class/misc/backlightdimmer/enabled\n");
				
				tempScript.write("echo ");
				tempScript.write(p.getProperty("bld.delay", "5000"));
				tempScript.write(" > /sys/class/misc/backlightdimmer/delay\n");
			}
			
			// Screen dimmer
			status = p.getProperty("screendimmer.status", "-1");
			if(!status.equals("-1")) {
				tempScript.write("echo ");
				tempScript.write(status);
				tempScript.write(" > /sys/class/misc/screendimmer/enabled\n");
				
				tempScript.write("echo ");
				tempScript.write(p.getProperty("screendimmer.delay", "5000"));
				tempScript.write(" > /sys/class/misc/screendimmer/delay\n");
			}
			
			tempScript.flush();
			tempScript.close();

			// set execute permission
			SysCommand.getInstance().run("chmod", "+x", getString(R.string.NS_TEMP_SCRIPT));
			// kick the script run!
			SysCommand.getInstance().suRun(getString(R.string.NS_TEMP_SCRIPT));
		}catch(Exception e) {
			Log.e(LOG_TAG, "", e);
		}
	}
}
