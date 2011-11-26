/**
 * StatusPreference.java
 * Nov 24, 2011 9:00:58 PM
 */
package mobi.cyann.nstools.preference;

import mobi.cyann.nstools.R;
import mobi.cyann.nstools.SysCommand;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * @author arif
 *
 */
public class StatusPreference extends BasePreference {
	private final static String LOG_TAG = "NSTools.StatusPreference";

	private String devicePath;
	private int value = -1;
	
	public StatusPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.mobi_cyann_nstools_preference_StatusPreference, defStyle, 0);
		devicePath = a.getString(R.styleable.mobi_cyann_nstools_preference_StatusPreference_devicePath);
		a.recycle();
	}

	public StatusPreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public StatusPreference(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		Log.d(LOG_TAG, "onBindView");
		// Sync the summary view
        TextView summaryView = (TextView) view.findViewById(android.R.id.summary);
        if (summaryView != null) {
        	if(value == 0) {
        		summaryView.setText(R.string.status_off);
        	}else if(value == 1) {
        		summaryView.setText(R.string.status_on);
        	}else {
        		summaryView.setText(R.string.status_not_available);
        	}
        }
	}

	private void setValue(int newValue) {
		if(newValue != value) {
			value = newValue;
			persistInt(newValue);
			// echo to devicepath
			if(devicePath != null)
				SysCommand.getInstance().suRun("echo", String.valueOf(newValue), ">", devicePath);
			notifyDependencyChange(shouldDisableDependents());
            notifyChanged();
		}
	}

	private int getValue() {
		int ret = -1;
		if(devicePath != null) {
			Log.d(LOG_TAG, "read from " + devicePath);
			SysCommand sc = SysCommand.getInstance();
			int n = sc.suRun("cat", devicePath);
			if(n > 0) {
				ret = Integer.parseInt(sc.getLastResult(0)); 
				Log.d(LOG_TAG, "return " + ret);
			}else {
				Log.e(LOG_TAG, "failed " + sc.getLastError(0));
			}
		}
		return ret;
	}
	
	protected void reload() {
		setValue(getValue());
	}
	
	@Override
	public boolean isEnabled() {
		return (value > -1) && super.isEnabled();
	}

	@Override
	protected void onClick() {
		 super.onClick();
	        
		int newValue = -1;
		if(value == 0) {
			newValue = 1;
		}else if(value == 1) {
			newValue = 0;
		}
        if (!callChangeListener(newValue)) {
            return;
        }
        setValue(newValue);
	}

	@Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
		Object ret = getValue();
		Log.d(LOG_TAG, "defaultValue=" + ret);
		return ret;
    }
	
	@Override
	protected void onSetInitialValue(boolean restorePersistedValue,
			Object defaultValue) {
		
		Log.d(LOG_TAG, "onSetInitialValue");
		if(restorePersistedValue) {
			// TODO: remove try-catch block
			// currently we need this for upgrading from old nstools version
			try {
				value = getPersistedInt(-1);
			}catch(Exception ex) {
				value = Integer.parseInt(getPersistedString("-1"));
			}
		}
		setValue(getValue());
	}
	
}
