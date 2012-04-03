/**
 * ListPreference.java
 * Nov 27, 2011 1:18:40 PM
 */
package mobi.cyann.nstools.preference;

import mobi.cyann.nstools.PreloadValues;
import mobi.cyann.nstools.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * @author arif
 *
 */
public class ListPreference extends BasePreference<Object> implements DialogInterface.OnClickListener {
	private final static String LOG_TAG = "NSTools.ListPreference";
	private Object value = null;
	private Object listValues[];
	private String listLabels[];
	private int valueType;
	
	public ListPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.mobi_cyann_nstools_preference_ListPreference, defStyle, 0);
		valueType = a.getInt(R.styleable.mobi_cyann_nstools_preference_ListPreference_valueType, 0);
		a.recycle();
	}

	public ListPreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ListPreference(Context context) {
		this(context, null);
	}
	
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		// Sync the summary view
        TextView summaryView = (TextView) view.findViewById(android.R.id.summary);
        if (summaryView != null) {
        	if(value == null) {
        		summaryView.setText(R.string.status_not_available);
        	}else if(listValues != null && listValues.length > 0) {
        		int idx = selectedIndex();
        		if(idx >= 0) {
        			summaryView.setText(listLabels[idx]);
        		}else {
        			// currently don't know what to do :D
        			summaryView.setText("?");
        		}
        	}
        }
	}

	@Override
	protected void writeValue(Object newValue, boolean writeInterface) {
		if(newValue == null) {
			return;
		}
		if(writeInterface && value != null && !newValue.equals(value)) {
			writeToInterface(String.valueOf(newValue));
			// re-read from interface (to detect error)
			newValue = readValue();
		}
		if(!newValue.equals(value)) {
			value = newValue;
			if(valueType == 0) { // int
				persistInt((Integer)newValue);
			}else { // string
				persistString((String)newValue);
			}
			
			notifyDependencyChange(shouldDisableDependents());
            notifyChanged();
		}
	}

	@Override
	protected Object readValue() {
		Object ret = null;
		String str = readFromInterface();
		try {
			if(valueType == 0) { // int
				ret = Integer.parseInt(str);
			}else { // string
				ret = str;
			}
		}catch(NumberFormatException ex) {
			
		}catch(Exception ex) {
			Log.e(LOG_TAG, "str:"+str, ex);
		}
		return ret;
	}
	
	@Override
	protected Object readPreloadValue() {
		Object preloadVal = null;
		if(valueType == 0) { // int
			preloadVal = PreloadValues.getInstance().getInt(getKey());
    	}else { // string
    		preloadVal = PreloadValues.getInstance().getString(getKey());
    	}
		if(preloadVal == null || (valueType == 0 && (Integer)preloadVal == -2)) { // if the value was not found in preload, we try to read it from interface
			return readValue();
		}else {
			return preloadVal;
		}
	}
	
	@Override
	public boolean isEnabled() {
		return (value != null) && listValues != null && super.isEnabled();
	}

	private int selectedIndex() {
		for(int i = 0; i < listValues.length; ++i) {
			if(listValues[i].equals(value)) {
				return i;
			}
		}
		return -1;
	}
	
	@Override
	protected void onClick() {
		// show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setSingleChoiceItems(listLabels, selectedIndex(), this);
        builder.setTitle(getTitle());
        builder.setNegativeButton(R.string.label_cancel, this);
        builder.create().show();
	}
	
	@Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
		return null;
    }
    
    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
    	if(restoreValue) {
    		if(valueType == 0) { // int
    			value = getPersistedInt(-1);
    		}else { // string
    			value = getPersistedString(null);
    		}
    	}
		writeValue(readPreloadValue(), false);
    }
    
	@Override
	public boolean shouldDisableDependents() {
		return (value == null) || super.shouldDisableDependents();
	}
	
	@Override
	public void onClick(DialogInterface d, int which) {
		if(which != DialogInterface.BUTTON_NEGATIVE) {			
			Object newValue = listValues[which];
			if (!callChangeListener(newValue)) {
	            return;
	        }
	        writeValue(newValue, true);
		}
		d.dismiss();
	}
	
	public Object[] getListValues() {
		return listValues;
	}

	public void setListValues(Object[] values) {
		this.listValues = values;
	}

	public String[] getListLabels() {
		return listLabels;
	}

	public void setListLabels(String[] labels) {
		this.listLabels = labels;
	}

	@Override
	public boolean isAvailable() {
		if(valueType == 0 && value != null) {
			return (Integer)value != -1;
		}else {
			return value != null;
		}
	}
}
