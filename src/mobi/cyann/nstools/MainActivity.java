package mobi.cyann.nstools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class MainActivity extends TabActivity {
	private final static String LOG_TAG = "NSTools.MainActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// call extract 
		extractScripts();
		
		setContentView(R.layout.main);
		
		TabHost tabHost = (TabHost)findViewById(android.R.id.tabhost);
		
		TabSpec tab1 = tabHost.newTabSpec("tid1");
		tab1.setIndicator(getString(R.string.ns_tweak));
		tab1.setContent(new Intent(this, NSTweakActivity.class));
		tabHost.addTab(tab1);
		
		TabSpec tab2 = tabHost.newTabSpec("tid2");
		tab2.setIndicator(getString(R.string.about));
		tab2.setContent(new Intent(this, AboutActivity.class));
		tabHost.addTab(tab2);
		//tabHost.addTab(tab3);
	}

    private void copyAsset(String srcPath, String dstPath) throws IOException {
		AssetManager assetManager = getApplicationContext().getAssets();
    	InputStream is = assetManager.open(srcPath);
    	FileOutputStream fos = new FileOutputStream(dstPath, false);
    	byte[] buffer = new byte[100];
    	int n = -1;
    	do {
    		n = is.read(buffer);
    		if(n != -1) {
    			fos.write(buffer, 0, n);
    		}
    	}while(n != -1);
    	fos.flush();
    	fos.close();
    	is.close();
    }
    
	private void extractScripts() {
		String scriptDir = getString(R.string.SCRIPT_DIR);
		String scriptVersion = getString(R.string.SCRIPT_VERSION);
		
		String scriptVersionTagFile = scriptDir + scriptVersion;
		// first check script version (in the future we can change SCRIPT_VERSION constant to overwrite existing scripts)
		if(!new File(scriptVersionTagFile).exists()) {
			try {
				SysCommand sc = SysCommand.getInstance();
				// clean old script dir
				int r = sc.run("rm", "-r", scriptDir);
				if(r < 0) {
					Log.e(LOG_TAG, sc.getLastError(0));
				}
				// create script dir
				new File(scriptDir).mkdir();
				// copy all scripts
				String[] scripts = getResources().getStringArray(R.array.scripts);
				for(String f: scripts) {
					copyAsset(f, scriptDir + f);
					r = SysCommand.getInstance().run("chmod", "0755", scriptDir + f);
					if(r < 0) {
						Log.e(LOG_TAG, sc.getLastError(0));
					}
				}
				// mark script version
				FileWriter fw = new FileWriter(scriptVersionTagFile);
				fw.write(scriptVersion);
				fw.close();

				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
				pref.edit().clear().commit();
			}catch(IOException e) {
				Log.e(LOG_TAG, "failed to extract scripts", e);
			}
		}
	}
}
