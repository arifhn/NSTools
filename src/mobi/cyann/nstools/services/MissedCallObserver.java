/**
 * MissedCallObserver.java
 * Jan 10, 2012 11:01:12 AM
 */
package mobi.cyann.nstools.services;

import mobi.cyann.nstools.SysCommand;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.provider.CallLog.Calls;
import android.util.Log;

/**
 * @author arif
 *
 */
public class MissedCallObserver extends ContentObserver {
	private final static String LOG_TAG = "NSTools.MissedCallObserver";
	private Context context;
	private ScreenReceiver screenReceiver;
	
	public MissedCallObserver(Context context) {
		super(new Handler());
		this.context = context;
		
		context.getContentResolver().registerContentObserver(Calls.CONTENT_URI, true, this);
	}

	public void stopWatching() {
		context.getContentResolver().unregisterContentObserver(this);
	}
	
    @Override
    public void onChange(boolean selfChange) {
        Cursor cursor = context.getContentResolver().query(
            Calls.CONTENT_URI, 
            null, 
            Calls.TYPE +  " = ? AND " + Calls.NEW + " = ?", 
            new String[] { Integer.toString(Calls.MISSED_TYPE), "1" }, 
            Calls.DATE + " DESC ");

        int c = cursor.getCount();
        if(c > 0) {
        	Log.d(LOG_TAG, "missed call=" + c + " register screen off for activate bln");
        	
        	 // initialize receiver
            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            if(screenReceiver == null) {
            	screenReceiver = new ScreenReceiver();
            }
            context.registerReceiver(screenReceiver, filter);
        }else {
        	Log.d(LOG_TAG, "deactivate bln");
        	//set BLN off
			SysCommand.getInstance().writeSysfs("/sys/class/misc/backlightnotification/notification_led", "0");
        }
    }
    
    private class ScreenReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				Log.d(LOG_TAG, "screen is off, activate bln after missed call");
				//set BLN on
				SysCommand.getInstance().writeSysfs("/sys/class/misc/backlightnotification/notification_led", "1");
				// unregister this
				context.unregisterReceiver(this);
			}
		}
    }

}
