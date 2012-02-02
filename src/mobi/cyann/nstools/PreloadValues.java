/**
 * PreloadValues.java
 * Nov 26, 2011 2:23:21 PM
 */
package mobi.cyann.nstools;

import java.io.FileInputStream;
import java.util.Properties;

import android.util.Log;

/**
 * @author arif
 *
 */
public class PreloadValues {
	private final static String LOG_TAG = "NSTools.PreloadValues";
	
	private final static PreloadValues singleton;
	
	static {
		singleton = new PreloadValues();
	}
	
	public static PreloadValues getInstance() {
		return singleton;
	}
	
	private Properties prop;
	
	private PreloadValues() {
		reload();
	}
	
	public void reload() {
		prop = new Properties();
		try {
			FileInputStream fis = new FileInputStream("/data/data/mobi.cyann.nstools/preload.prop");
			prop.load(fis);
			fis.close();
		}catch(Exception ex) {
			Log.e(LOG_TAG, "fail to read preload file", ex);
		}
	}
	
	public int getInt(String key) {
		int ret = -2;
		try {
			ret = Integer.parseInt(prop.getProperty(key));
		}catch(Exception e) {
			
		}
		return ret;
	}
	
	public String getString(String key) {
		return prop.getProperty(key);
	}
}
