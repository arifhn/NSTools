/**
 * ObserverService.java
 * Jan 19, 2012 5:14:04 PM
 */
package mobi.cyann.nstools.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.provider.CallLog.Calls;

/**
 * @author arif
 *
 */
public class ObserverService extends Service {
	private MissedCallObserver missedCallObserver;
	private BlnObserver blnObserver;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// register missed call observer
		missedCallObserver = new MissedCallObserver(this);
		getContentResolver().registerContentObserver(Calls.CONTENT_URI, true, missedCallObserver);

		// register bln observer (for bln timeout)
		blnObserver = new BlnObserver(this);
		blnObserver.startWatching();
	}

}
