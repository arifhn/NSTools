/**
 * LulzactiveScreenOffPreference.java
 * Jan 14, 2012 9:36:34 AM
 */
package mobi.cyann.nstools.preference;

import mobi.cyann.nstools.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * @author arif
 *
 */
public class LulzactiveScreenOffPreference extends IntegerPreference {
	private String[] availableFrequencies;
	
	public LulzactiveScreenOffPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public LulzactiveScreenOffPreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LulzactiveScreenOffPreference(Context context) {
		this(context, null);
	}
	
	public void setAvailableFrequencies(String[] val) {
		availableFrequencies = val;
	}
	
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		// Sync the summary view
        TextView summaryView = (TextView) view.findViewById(android.R.id.summary);
        if (summaryView != null) {
        	if(value < 0) {
        		summaryView.setText(R.string.status_not_available);
        	}else if(availableFrequencies != null) {
        		summaryView.setText(value + " @" + availableFrequencies[value]);
        	}else {
        		summaryView.setText(String.valueOf(value));
        	}
        }
	}
}
