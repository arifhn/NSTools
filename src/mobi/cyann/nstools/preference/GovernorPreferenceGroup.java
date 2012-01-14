/**
 * GovernorPreferenceGroup.java
 * Jan 14, 2012 8:39:01 AM
 */
package mobi.cyann.nstools.preference;

import mobi.cyann.nstools.PreloadValues;
import mobi.cyann.nstools.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

/**
 * @author arif
 *
 */
public class GovernorPreferenceGroup extends RemovablePreferenceCategory {
	private String governor;
	
	public GovernorPreferenceGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.mobi_cyann_nstools_preference_GovernorPreferenceGroup, 0, 0);
		governor = a.getString(R.styleable.mobi_cyann_nstools_preference_GovernorPreferenceGroup_governor);
		a.recycle();
    }

    public GovernorPreferenceGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.mobi_cyann_nstools_preference_GovernorPreferenceGroup, 0, 0);
		governor = a.getString(R.styleable.mobi_cyann_nstools_preference_GovernorPreferenceGroup_governor);
		a.recycle();
    }

    public GovernorPreferenceGroup(Context context) {
        this(context, null);
    }
    
    @Override
    public boolean canBeRemoved() {
    	String availableGovernor = PreloadValues.getInstance().getString("key_available_governor");
    	return availableGovernor == null || !availableGovernor.contains(governor);
    }
}
