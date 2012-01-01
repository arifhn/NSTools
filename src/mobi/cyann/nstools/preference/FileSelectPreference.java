/**
 * FileSelectPreference.java
 * Jan 1, 2012 12:33:20 PM
 */
package mobi.cyann.nstools.preference;

import java.io.File;

import mobi.cyann.nstools.R;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.util.AttributeSet;

/**
 * @author arif
 * 
 */
public class FileSelectPreference extends ListPreference {
	private String path;
	
	public FileSelectPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.mobi_cyann_nstools_preference_FileSelectPreference);
		path = a.getString(R.styleable.mobi_cyann_nstools_preference_FileSelectPreference_path);
		a.recycle();
	}

	public FileSelectPreference(Context context) {
		this(context, null);
	}
	
	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		// setEntries
		File dir = new File(path);
		if(dir.exists()) {
			String files[] = dir.list();
			setEntries(files);
			setEntryValues(files);
		}
		super.onPrepareDialogBuilder(builder);
	}

	@Override
	public boolean isEnabled() {
		boolean enabled = false;
		File dir = new File(path);
		if(dir.exists()) {
			String files[] = dir.list();
			if(files.length > 0)
				enabled = true;
		}
		return enabled && super.isEnabled();
	}
	
	
}
