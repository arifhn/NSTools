/**
 * OnBootCompleteService.java
 * 9:39:01 PM
 */
package mobi.cyann.nstools;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author arif
 *
 */
public class OnBootCompleteService extends IntentService {
	private final static String LOG_TAG = "NSTools.OnBootCompleteService";
	public OnBootCompleteService() {
		super("NSToolsService");
	}
	
	private void showNotification() {
		Notification notif = new Notification(R.drawable.icon, getString(R.string.app_name), System.currentTimeMillis());
		
		Intent sharingIntent = new Intent(this, MainActivity.class);
		PendingIntent intent = PendingIntent.getActivity(this, 0, sharingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		notif.setLatestEventInfo(this, getString(R.string.app_name), getString(R.string.msg_kernel_changed), intent);
		notif.flags |= Notification.FLAG_AUTO_CANCEL;
		NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(1, notif);
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			Log.d(LOG_TAG, "trying to load & write settings to kernel...");
			int ret = SettingsManager.loadSettingsOnBoot(this);
			if(ret == SettingsManager.SUCCESS) {
				Log.d(LOG_TAG, "write success");
			}else if(ret == SettingsManager.ERR_SET_ON_BOOT_FALSE) {
				Log.d(LOG_TAG, "write canceled, set_on_boot flag is false");
			}else if(ret == SettingsManager.ERR_DIFFERENT_KERNEL) {
				Log.d(LOG_TAG, "write canceled, different kernel version");
				showNotification();
			}
		}catch(Exception ex) {
			Log.e(LOG_TAG, "write failed", ex);
		}
	}
}
