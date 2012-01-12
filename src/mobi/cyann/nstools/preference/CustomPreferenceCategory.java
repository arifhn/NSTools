/**
 * CustomPreferenceCategory.java
 * Jan 11, 2012 12:03:39 PM
 */
package mobi.cyann.nstools.preference;

import mobi.cyann.nstools.preference.BasePreference.OnPreferenceChangedListener;
import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * @author arif
 *
 */
public class CustomPreferenceCategory extends PreferenceCategory implements OnPreferenceChangedListener {
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
				if(p instanceof BasePreference) {
					if(!((BasePreference) p).isAvailable())
						notAvailable++;
				}else if(p instanceof Preference) {
					notAvailable++;
				}
			}
			available = (prefCount > notAvailable);
			dirty = false;
		}
		return available;
	}

	@Override
	public Preference findPreference(CharSequence key) {
        if (TextUtils.equals(getKey(), key)) {
            return this;
        }
        final int preferenceCount = super.getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            final Preference preference = getPreference(i);
            final String curKey = preference.getKey();

            if (curKey != null && curKey.equals(key)) {
                return preference;
            }
            
            if (preference instanceof PreferenceGroup) {
                final Preference returnedPreference = ((PreferenceGroup)preference)
                        .findPreference(key);
                if (returnedPreference != null) {
                    return returnedPreference;
                }
            }
        }

        return null;
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
	public boolean addPreference(Preference preference) {
		if(preference instanceof BasePreference) {
			((BasePreference) preference).addChangedListener(this);
		}
		return super.addPreference(preference);
	}
	
	public void onPreferenceChanged(Preference preference) {
		dirty = true;
	}
}
