package mobi.cyann.nstools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class SysCommand {
	private final static String LOG_TAG = "NSTools.SysCommand";

	private StreamGobbler out;
	private StreamGobbler err;

	private final static SysCommand singleton;

	static {
		singleton = new SysCommand();
	}

	public static SysCommand getInstance() {
		return singleton;
	}

	/**
	 * run shell command with super user permission
	 * 
	 * @param cmd
	 * @return
	 */
	public int suRun(String... cmd) {
		StringBuilder cmds = new StringBuilder();
		for(int i = 0; i < cmd.length; ++i) {
			cmds.append(cmd[i]);
			cmds.append(" ");
		}
		return run("su", "-c", cmds.toString());
	}
	
	/**
	 * run shell command
	 * 
	 * @param cmd
	 * @return return number of result line (< 0 if error, >=0 if success)
	 */
	public int run(String... cmd) {
		int result = 0;
		try {
			// build process
			ProcessBuilder pb = new ProcessBuilder(cmd);
			// start the process
			Process p = pb.start();
			// create stream eater
			err = new StreamGobbler(p.getErrorStream());
			out = new StreamGobbler(p.getInputStream());
			// start them all
			err.start();
			out.start();
			// wait until they finished
			p.waitFor();
			err.waitFor();
			out.waitFor();
			
			if (err.getLineCount() > 0) {
				result = -err.getLineCount();
			} else {
				result = out.getLineCount();
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "", e);
			out = null;
			err = null;
			result = 01;
		}
		return result;
	}

	public String getLastError(int line) {
		if(err != null)
			return err.getLine(line);
		else 
			return null;
	}

	public String getLastResult(int line) {
		if(out != null)
			return out.getLine(line);
		else
			return null;
	}

	// stream eater
	class StreamGobbler extends Thread {
		InputStream is;
		List<String> result;

		boolean run = false;

		StreamGobbler(InputStream is) {
			this.is = is;
			result = new ArrayList<String>();
		}

		public void run() {
			run = true;
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr, 100);
				String line = null;
				while ((line = br.readLine()) != null) {
					result.add(line);
				}
				br.close();
				isr.close();
			} catch (IOException ioe) {
				Log.e(LOG_TAG, "", ioe);
			} finally {
				try {
					is.close();
				} catch (Exception e) {
				}
				run = false;
			}
		}

		public void waitFor() {
			try {
				while (run) {
					Thread.sleep(200);
				}
			} catch (InterruptedException e) {

			}
		}

		public int getLineCount() {
			return result.size();
		}

		public String getLine(int idx) {
			return result.get(idx);
		}
	}
}
