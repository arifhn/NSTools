/**
 * CustomPreferenceCategory.java
 * Jan 11, 2012 12:03:39 PM
 */
package mobi.cyann.nstools.preference;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * @author arif
 *
 */
public class CustomPreferenceCategory extends PreferenceCategory {
	private boolean available;
	private boolean dirty = true;
	
	public CustomPreferenceCategory(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public CustomPreferenceCategory(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CustomPreferenceCategory(Context context) {
		super(context);
	}

	private boolean isAvailable() {
		if(dirty) {
			int prefCount = super.getPreferenceCount();
			int notAvailable = 0;
			for(int i = 0; i < prefCount; ++i) {
				Preference p = getPreference(i);
				if(p instanceof BasePreference && !((BasePreference) p).isAvailable()) {
					notAvailable++;
				}
			}
			available = (prefCount > notAvailable);
			dirty = false;
		}
		return available;
	}
	
	@Override
	public int getPreferenceCount() {
		if(isAvailable()) {
			return super.getPreferenceCount();
		}else {
			return 0;
		}
	}
	
	@Override
	public View getView(View convertView, ViewGroup parent) {
		if(isAvailable()) {
			return super.getView(convertView, parent);
		}else {
			return new FrameLayout(getContext());
		}
	}

	@Override
	public void onDependencyChanged(Preference dependency,
			boolean disableDependent) {
		super.onDependencyChanged(dependency, disableDependent);
		dirty = true;
	}
}
