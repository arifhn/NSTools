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
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * @author arif
 *
 */
public abstract class BasePreference<T> extends Preference {
	private final static String LOG_TAG = "NSTools.BasePreference";
	
	private final String interfacePath;
	private boolean reloadOnResume;
	private int dependencyType;
	private boolean visible;
	private OnPreferenceChangedListener changedListener;
	
	public interface OnPreferenceChangedListener {
        /**
         * Called when a Preference has been changed
         * 
         * @param preference The changed Preference.
         */
        void onPreferenceChanged(Preference preference);
    }
	
	protected abstract T readPreloadValue();
	protected abstract T readValue();
	protected abstract void writeValue(T newValue, boolean writeInterface);
	public abstract boolean isAvailable();
	
	private static View blankView;
	
	public BasePreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		setDefaultValue(-1);
		
		TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.mobi_cyann_nstools_preference_BasePreference, defStyle, 0);
		reloadOnResume = a.getBoolean(R.styleable.mobi_cyann_nstools_preference_BasePreference_reloadOnResume, false);
		interfacePath = a.getString(R.styleable.mobi_cyann_nstools_preference_BasePreference_interfacePath);
		dependencyType = a.getInt(R.styleable.mobi_cyann_nstools_preference_BasePreference_dependencyType, 0);
		a.recycle();
		
		// init blankView for hiding this preference if not available
		if(blankView == null) {
			blankView = new FrameLayout(context);
		}
		visible = true;
	}

	public BasePreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public BasePreference(Context context) {
		this(context, null);
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		if(isAvailable() && isVisible()) {
			return super.onCreateView(parent);
		}else {
			return blankView;
		}
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	protected String readFromInterface() {
		String ret = null;
		if(interfacePath != null) {
			Log.d(LOG_TAG, "read from " + interfacePath);
			SysCommand sc = SysCommand.getInstance();
			int n = sc.readSysfs(interfacePath); 
			if(n > 0) {
				ret = sc.getLastResult(0); 
				Log.d(LOG_TAG, "ROK:" + ret);
			}else if(n < 0) {
				Log.e(LOG_TAG, "RER:" + sc.getLastError(0));
			}else {
				Log.e(LOG_TAG, "RER:<empty>");
			}
		}
		return ret;
	}
	
	protected void writeToInterface(String value) {
		// echo to interfacePath
		if(interfacePath != null) {
			SysCommand sc = SysCommand.getInstance();
			if(sc.writeSysfs(interfacePath, value) >= 0) {
				Log.d(LOG_TAG, "WOK");
			}else {
				Log.e(LOG_TAG, "WER:" + sc.getLastError(0));
			}
		}
	}
	
	public void reload(boolean readFromPreload) {
		if(readFromPreload) {
			writeValue(readPreloadValue(), false);
		}else {
			writeValue(readValue(), false);
		}
	}
	
	public void setChangedListener(OnPreferenceChangedListener changedListener) {
		this.changedListener = changedListener;
	}

	@Override
	protected void notifyChanged() {
		super.notifyChanged();
		if(changedListener != null) {
			changedListener.onPreferenceChanged(this);
		}
	}

	public void onResume() {
		if(reloadOnResume) {
			reload(false);
		}
	}

	public boolean isReloadOnResume() {
		return reloadOnResume;
	}

	public void setReloadOnResume(boolean reloadOnResume) {
		this.reloadOnResume = reloadOnResume;
	}

	@Override
	public void onDependencyChanged(Preference dependency,
			boolean disableDependent) {

		if(dependencyType == 0) { // android_default
			super.onDependencyChanged(dependency, disableDependent);
		}else if(dependencyType == 1) { // reload_value
			reload(false);
		}
	}
}
