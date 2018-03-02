package com.nqmobile.livesdk.commons.log;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.nqmobile.livesdk.commons.info.ClientInfo;
import com.nqmobile.livesdk.commons.info.NqTest;
import com.nqmobile.livesdk.commons.system.SystemFacadeFactory;
import com.nqmobile.livesdk.utils.FileUtil;
import com.nqmobile.livesdk.utils.Tools;

public class AndroidLogger extends AbstractLogger {
	private final static String LOGFILENAME = "DEBUG";
	private final static String LOGFIX = ".log";
	public final static String LOGFOLDER = "nqtest";
	private static String logPath = "";
	private static File file = null;
	
	static {
		initLog();
	}
	
	public AndroidLogger(String tag) {
		super(tag);
	}
	
	protected void v(String tag, String message, Throwable t) {
		message = toFullMessage(tag, message);
		if (t != null){
			Log.v(tag, message, t);
			if (NqTest.isWriteFile()) {
				writeLog(tag, message + '\n' + Log.getStackTraceString(t));
			}
		}
		else {
			Log.v(tag, message);
			String statckTrace = getStackTraceString();
			Log.v(tag, statckTrace);
			if (NqTest.isWriteFile()) {
				writeLog(tag, message + '\n' + statckTrace);
			}
		}
	}

	protected void d(String tag, String message, Throwable t) {
		message = toFullMessage(tag, message);
		if (t != null) {
			Log.d(tag, message, t);
			if (NqTest.isWriteFile()) {
				writeLog(tag, message + '\n' + Log.getStackTraceString(t));
			}
		}
		else {
			Log.d(tag, message);
			if (NqTest.isWriteFile()) {
				writeLog(tag, message);
			}
		}
	}

	protected void i(String tag, String message, Throwable t) {
		message = toFullMessage(tag, message);
		if (t != null) {
			Log.i(tag, message, t);
			if (NqTest.isWriteFile()) {
				writeLog(tag, message + '\n' + Log.getStackTraceString(t));
			}
		}
		else {
			Log.i(tag, message);
			if (NqTest.isWriteFile()) {
				writeLog(tag, message);
			}
		}
	}

	protected void w(String tag, String message, Throwable t) {
		message = toFullMessage(tag, message);
		if (t != null) {
			Log.w(tag, message, t);
			if (NqTest.isWriteFile()) {
				writeLog(tag, message + '\n' + Log.getStackTraceString(t));
			}
		}
		else {
			Log.w(tag, message);
			if (NqTest.isWriteFile()) {
				writeLog(tag, message);
			}
		}
	}

	protected void e(String tag, String message, Throwable t) {
		message = toFullMessage(tag, message);
		if (t != null) {
			Log.e(tag, message, t);
			if (NqTest.isWriteFile()) {
				writeLog(tag, message + '\n' + Log.getStackTraceString(t));
			}
		}
		else {
			Log.e(tag, message);
			if (NqTest.isWriteFile()) {
				writeLog(tag, message);
			}
		}
	}

	private String toFullMessage(String tag, String message) {
		return message + getLine();
	}
	
	private String getLine() {
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		boolean find = false;
		for (StackTraceElement stackTraceElement : elements) {
			if (stackTraceElement.getClassName().equals(
					AbstractLogger.class.getName())) {
				find = true;
			} else if ((find)
					&& (!stackTraceElement.getClassName().equals(
							AbstractLogger.class.getName()))) {
				return getStackTraceElementString(stackTraceElement);
			}
		}

		return "";
	}
	
	private String getStackTraceElementString(StackTraceElement stackTraceElement) {
		StringBuilder buf = new StringBuilder(80);

		String fName = stackTraceElement.getFileName();
		buf.append(" ");

		if (fName == null) {
			buf.append("(Unknown Source)");
		} else {
			int lineNum = stackTraceElement.getLineNumber();

			buf.append('(');
			buf.append(fName);
			if (lineNum >= 0) {
				buf.append(':');
				buf.append(lineNum);
			}
			buf.append(')');
		}
		return buf.toString();
    }
	
	private String getStackTraceString() {
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		if (elements == null || elements.length == 0){
			return "";
		}
		
		List<StackTraceElement> traceList = new ArrayList<StackTraceElement>(elements.length);
		boolean find = false;
		for (StackTraceElement stackTraceElement : elements) {
			if (stackTraceElement.getClassName().equals(
					AbstractLogger.class.getName())) {
				find = true;
			} 
			if ((find)
					&& (!stackTraceElement.getClassName().equals(
							AbstractLogger.class.getName()))) {
				traceList.add(stackTraceElement);
			}
		}
		
		Throwable e = new StackTraceException();
		elements = new StackTraceElement[traceList.size()];
		elements = traceList.toArray(elements);
		e.setStackTrace(elements);
		return Log.getStackTraceString(e);
	}
	
	
	
	private static void writeLog(String tag, String message) {
		long now = SystemFacadeFactory.getSystem().currentTimeMillis();
		String time = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(now);
		if (file != null) {
			FileWriter fw = null;
			try {
				if (NqTest.isUseLocalPath()) {
					fw = new FileWriter(file, true);
					fw.append(time).append("  ").append(tag).append(" [").append(message).append("]");
					fw.append("\r\n");
				}else if (Tools.stringEquals(Environment.getExternalStorageState(), Environment.MEDIA_MOUNTED) && availableSDCard()) {
					fw = new FileWriter(file, true);
					fw.append(time).append("  ").append(tag).append(" [").append(message).append("]");
					fw.append("\r\n");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				FileUtil.closeStream(fw);
			}
		}
	}
	
	private static void writeLocal(){
		if (Tools.isEmpty(logPath)) {
			logPath = "/data/data/" + ClientInfo.getPackageName() + "/logs/";
		}
		try {
			File dir = new File(logPath);
			if (!dir.exists()) {
				dir.mkdir();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void initLog() {
		if (NqTest.isUseLocalPath()) {
			writeLocal();
			file = new File(logPath + LOGFILENAME);
			
		} else {
			logPath = Environment.getExternalStorageDirectory().getPath() + File.separator + LOGFOLDER + File.separator;
			if (Tools.stringEquals(Environment.getExternalStorageState(), Environment.MEDIA_MOUNTED)) {
				file = new File(logPath + LOGFILENAME /*+ "_" + month*/ + LOGFIX);
				if (!file.getParentFile().exists()) 
					file.getParentFile().mkdirs();
			}
		}
	}
	
	public static boolean availableSDCard(){
		return getAvailaleSize() > 100;
	}
	
	private static long getAvailaleSize(){
		File path = Environment.getExternalStorageDirectory();
		StatFs stat = new StatFs(path.getPath()); 
		long blockSize = stat.getBlockSize(); 
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize / 1024;
	}
}
