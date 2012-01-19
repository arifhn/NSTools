/**
 * RemovablePreferenceCategory.java
 * Jan 13, 2012 5:34:25 PM
 */
package mobi.cyann.nstools.preference;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;

/**
 * @author arif
 *
 */
public class RemovablePreferenceCategory extends PreferenceCategory {
	
    public RemovablePreferenceCategory(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public RemovablePreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RemovablePreferenceCategory(Context context) {
        super(context, null);
    }
    
    public boolean canBeRemoved() {
    	int notAvailable = 0;
    	int count = getPreferenceCount();
    	for(int i = 0; i < count; ++i) {
    		Preference p = getPreference(i);
    		if(p instanceof BasePreference) {
    			if(!((BasePreference<?>) p).isAvailable()) {
    				++notAvailable;
    			}
    		}else {
    			++notAvailable;
    		}
    	}
    	return count == notAvailable;
    }
}
