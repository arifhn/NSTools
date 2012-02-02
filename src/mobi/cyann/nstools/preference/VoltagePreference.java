/**
 * VoltagePreference.java
 * Nov 27, 2011 8:07:22 PM
 */
package mobi.cyann.nstools.preference;

import mobi.cyann.nstools.PreloadValues;
import android.content.Context;
import android.util.AttributeSet;

/**
 * @author arif
 *
 */
public class VoltagePreference extends IntegerPreference {
	private boolean ignoreInterface = false;
	
	public VoltagePreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public VoltagePreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public VoltagePreference(Context context) {
		this(context, null);
	}

	public void setIgnoreInterface(boolean ignore) {
		ignoreInterface = ignore;
	}
	
	@Override
	protected Integer readPreloadValue() {
		int preloadVal = -1;
    	if(!ignoreInterface) {
	    	String str =  PreloadValues.getInstance().getString(getKey());
	    	if(str == null) {
	    		preloadVal = readValue(); // if the value was not found in preload, we try to read it from interface
	    	}else if(str.endsWith(" mV")) {
				preloadVal = Integer.parseInt(str.substring(0, str.length() - 3));
			}else {
				preloadVal = Integer.parseInt(str);
			}
    	}else {
    		preloadVal = value;
    	}
    	return preloadVal;
	}
	
	@Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
    	if(restoreValue) {
    		value = getPersistedInt(-1);
    	}
		writeValue(readPreloadValue(), false);
    }

	@Override
	protected void writeValue(Integer newValue, boolean writeInterface) {
		if(!ignoreInterface && writeInterface && value > -1 && newValue != value) {
			writeToInterface(String.valueOf(newValue));
			// re-read from interface (to detect error)
			newValue = readValue();
		}
		if(newValue != value) {
			value = newValue;
			persistInt(newValue);
			
			notifyDependencyChange(shouldDisableDependents());
            notifyChanged();
		}
	}
	
	@Override
	protected Integer readValue() {
		int ret = -1;
		String str = readFromInterface();
		if(str != null) {
			if(str.endsWith(" mV")) {
				ret = Integer.parseInt(str.substring(0, str.length() - 3));
			}else {
				ret = Integer.parseInt(str);
			}
		}
		return ret;
	}
	
}
