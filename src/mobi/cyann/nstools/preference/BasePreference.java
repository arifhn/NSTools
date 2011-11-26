/**
 * BasePreference.java
 * Nov 25, 2011 9:59:05 PM
 */
package mobi.cyann.nstools.preference;

import mobi.cyann.nstools.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;

/**
 * @author arif
 *
 */
public abstract class BasePreference extends Preference {
	//private final static String LOG_TAG = "NSTools.BasePreference";

	private boolean reloadOnResume;

	public BasePreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.mobi_cyann_nstools_preference_BasePreference, defStyle, 0);
		reloadOnResume = a.getBoolean(R.styleable.mobi_cyann_nstools_preference_BasePreference_reloadOnResume, false);
		a.recycle();
	}

	public BasePreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public BasePreference(Context context) {
		this(context, null);
	}
	
	protected abstract void reload();
	
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
