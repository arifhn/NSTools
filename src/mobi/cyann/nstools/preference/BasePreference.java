/**
 * BasePreference.java
 * Nov 25, 2011 9:59:05 PM
 */
package mobi.cyann.nstools.preference;

import mobi.cyann.nstools.R;
import mobi.cyann.nstools.SysCommand;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;

/**
 * @author arif
 *
 */
public abstract class BasePreference extends Preference {
	private final static String LOG_TAG = "NSTools.BasePreference";
	
	private final String interfacePath;
	private boolean reloadOnResume;

	public BasePreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.mobi_cyann_nstools_preference_BasePreference, defStyle, 0);
		reloadOnResume = a.getBoolean(R.styleable.mobi_cyann_nstools_preference_BasePreference_reloadOnResume, false);
		interfacePath = a.getString(R.styleable.mobi_cyann_nstools_preference_BasePreference_interfacePath);
		a.recycle();
	}

	public BasePreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public BasePreference(Context context) {
		this(context, null);
	}

	protected String readFromInterface() {
		String ret = null;
		if(interfacePath != null) {
			Log.d(LOG_TAG, "read from " + interfacePath);
			SysCommand sc = SysCommand.getInstance();
			if(sc.suRun("cat", interfacePath) > 0) {
				ret = sc.getLastResult(0); 
				Log.d(LOG_TAG, "ROK:" + ret);
			}else {
				Log.e(LOG_TAG, "RER:" + sc.getLastError(0));
			}
		}
		return ret;
	}
	
	protected void writeToInterface(String value) {
		// echo to interfacePath
		if(interfacePath != null) {
			SysCommand sc = SysCommand.getInstance();
			if(sc.suRun("echo", value, ">", interfacePath) >= 0) {
				Log.d(LOG_TAG, "WOK");
			}else {
				Log.e(LOG_TAG, "WER:" + sc.getLastError(0));
			}
		}
	}
	
	public abstract void reload();
	
	public void onResume() {
		if(reloadOnResume) {
			reload();
		}
	}

	public boolean isReloadOnResume() {
		return reloadOnResume;
	}

	public void setReloadOnResume(boolean reloadOnResume) {
		this.reloadOnResume = reloadOnResume;
	}
}
