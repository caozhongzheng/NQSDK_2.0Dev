package com.nqmobile.livesdk.modules.stat.network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.os.Environment;
import android.text.format.DateUtils;
import android.text.format.Time;

import com.nq.interfaces.launcher.TLauncherService;
import com.nq.interfaces.launcher.TLogUploadRequest;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.stat.StatModule;
import com.nqmobile.livesdk.utils.FileUtil;
import com.nqmobile.livesdk.utils.GZipUtils;

/**
 * Created by Rainbow on 14-4-23.
 */
public class UploadCrashLogProtocol extends AbsLauncherProtocol {
	private static final ILogger NqLog = LoggerFactory.getLogger(StatModule.MODULE_NAME);

	private static final String PATH = "/lqLauncher/CrashLog";

	private static final int MAX_SIZE = 5;

	private ArrayList<File> logPath = new ArrayList<File>();

    @Override
    protected int getProtocolId() {
        return 0x15;
    }

    @Override
	protected void process() {
		NqLog.i("UploadCrashLogProtocol process");
		try {
			getLogPath();
			NqLog.i("log.size=" + logPath.size());
			if (logPath.size() == 0) {
				return;
			}

			// upload to server
            TLauncherService.Iface client = TLauncherServiceClientFactory.getClient(getThriftProtocol());
			if (logPath.size() <= MAX_SIZE) {
				for (File f : logPath) {
					NqLog.i("logPath=" + f.getPath());
					TLogUploadRequest req = new TLogUploadRequest();
					req.logType = "launcher_crash_zip";
					req.byteStream = ByteBuffer.wrap(GZipUtils.compress(getFileContent(f).getBytes()));
					client.uploadLog(getUserInfo(), req);
				}
			} else {
				NqLog.i("before sort=====");
				printList();
				Collections.sort(logPath, new FileComparator());
				NqLog.i("after sort=====");
				printList();
				for (int i = 0; i < MAX_SIZE; i++) {
					TLogUploadRequest req = new TLogUploadRequest();
					req.logType = "launcher_crash";
					req.byteStream = ByteBuffer.wrap(getFileContent(logPath.get(i)).getBytes());
					client.uploadLog(getUserInfo(), req);
				}
			}
			NqLog.i("UploadCrashLogProtocol succ!");
		} catch (Exception e) {
			e.printStackTrace();onError();
		}
	}
    @Override
	protected void onError() {
	}
	private void printList() {
		for (int i = 0; i < logPath.size(); i++) {
			NqLog.i("filename=" + logPath.get(i).getName() + " time="
					+ logPath.get(i).lastModified());
		}
	}

	private String getFileContent(File f) {
		StringBuilder sb = new StringBuilder();
		InputStream is = null;
		try {
			is = new FileInputStream(f.getPath());
			String line;
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(is));
			line = reader.readLine();
			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = reader.readLine();
			}
			reader.close();
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtil.closeStream(is);
		}
		return sb.toString();
	}

	private void getLogPath() {
		NqLog.i( "getLogPath");
		String path = Environment.getExternalStorageDirectory() + PATH;
		File logDir = new File(path);
		File[] filelist = logDir.listFiles();
		NqLog.i("filelist.size=" + filelist.length);
		for (File f : filelist) {
			long time = f.lastModified();
			Time t = new Time();
			t.set(time);
			if (System.currentTimeMillis() - time <= 3 * DateUtils.DAY_IN_MILLIS) {
				logPath.add(f);
			}
		}
	}

	private class FileComparator implements Comparator<File> {

		@Override
		public int compare(File file, File file2) {
			long time1 = file.lastModified();
			long time2 = file2.lastModified();
			if (time1 > time2) {
				return 1;
			} else if (time1 < time2) {
				return -1;
			} else
				return 0;
		}
	}

	
}
