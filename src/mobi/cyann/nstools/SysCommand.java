package mobi.cyann.nstools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class SysCommand {
	private final static String LOG_TAG = "NSTools.SysCommand";

	private StreamGobbler out;
	private StreamGobbler err;
	
	private List<String> lastResult;

	private final static SysCommand singleton;

	static {
		singleton = new SysCommand();
	}

	public static SysCommand getInstance() {
		return singleton;
	}

	private SysCommand() {
		lastResult = new ArrayList<String>();
	}
	
	public int readSysfs(String sysfsPath) {
		lastResult.clear();
		File readThis = new File(sysfsPath);
		if(readThis.canRead()) {
			// read the file directly if we have read permission
			BufferedReader br = null;
	        String line = null;
	        try {
	            br = new BufferedReader(new FileReader(readThis), 512);
	            do {
	                line = br.readLine();
	                if(line != null)
	                	lastResult.add(line);
	            }while(line != null);
	        }catch (Exception e) {
	            Log.e(LOG_TAG, "Exception when reading " + sysfsPath, e);
	        }finally {
	        	try {
	        		br.close();
	        	}catch(Exception e) {}
	        }
		}
		if(lastResult.size() == 0 && readThis.isFile()) { // the file is exists but we don't have permission
			// so.. we need SU here
			return suRun("cat", sysfsPath);
		}else {
			return lastResult.size();
		}
	}
	
	public int writeSysfs(String sysfsPath, String value) {
		int ret = -1;
		File writeThis = new File(sysfsPath);
		if(writeThis.canWrite()) {
			// write the file directly if we have read permission
			FileWriter fw = null;
			try {
				fw = new FileWriter(writeThis);
				fw.write(value);
				ret = 0;
			}catch(Exception e) {
				Log.e(LOG_TAG, "Exception when writing " + sysfsPath, e);
			}finally {
				try {
					fw.close();
				}catch(Exception e) {}
			}
		}
		if(ret < 0) {
			// so.. we need SU here
			ret = suRun("echo", "\""+ value + "\"", ">", sysfsPath);
		}
		return ret;
	}
	
	/**
	 * run shell command with super user permission
	 * 
	 * @param cmd
	 * @return
	 */
	public int suRun(String... cmd) {
		StringBuilder cmds = new StringBuilder("");
		for(int i = 0; i < cmd.length; ++i) {
			cmds.append(cmd[i]);
			cmds.append(" ");
		}
		Log.d(LOG_TAG, cmds.toString());
		int result = 0;
		try {
			// build process
			ProcessBuilder pb = new ProcessBuilder("su", "-c", "/system/bin/sh");
			// start the process
			Process p = pb.start();

			// create stream eater
			err = new StreamGobbler(p.getErrorStream());
			out = new StreamGobbler(p.getInputStream());
			// start them all
			err.start();
			out.start();

			OutputStream os = p.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os);
			osw.write(cmds.toString());
			osw.write("\nexit\n");
			osw.flush();
			osw.close();
			
			// wait until they finished
			p.waitFor();
			err.waitFor();
			out.waitFor();
			
			if (err.getLineCount() > 0) {
				result = -err.getLineCount();
			} else {
				result = out.getLineCount();
				out.copyLines(lastResult);
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "", e);
			out = null;
			err = null;
			result = -1;
		}
		return result;
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
				out.copyLines(lastResult);
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
		if(lastResult.size() > line)
			return lastResult.get(line);
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
			result.clear();
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr, 100);
				String line = null;
				while ((line = br.readLine()) != null) {
					if(line.length() > 0)
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
		
		public void copyLines(List<String> copyTo) {
			copyTo.clear();
			copyTo.addAll(result);
		}
	}
}
