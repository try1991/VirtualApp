package com.lody.virtual.service.process;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IApplicationThread;
import android.os.RemoteException;

import com.lody.virtual.client.IVClient;
import com.lody.virtual.service.am.ContentProviderRecord;
import com.lody.virtual.service.am.StubInfo;
import com.lody.virtual.service.am.VActivityService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ProcessRecord {
	/**
	 * Running applications on target process
	 */
	final Set<String> runningAppPkgs = new HashSet<String>();
	/**
	 * Real process name
	 */
	public String stubProcessName;
	/**
	 * Virtual process name
	 */
	public String processName;
	/**
	 * Target Stub info
	 */
	public StubInfo stubInfo;
	/**
	 * ApplicationThread of target process
	 */
	public IApplicationThread appThread;
	/**
	 * target process's Client object
	 */
	public IVClient client;
	/**
	 * target process's pid
	 */
	public int pid;
	/**
	 * target process's uid
	 */
	public int uid;

	String initialPackage;

	// class (String) -> ContentProviderRecord
	public final HashMap<String, ContentProviderRecord> pubProviders
			= new HashMap<String, ContentProviderRecord>();
	// All ContentProviderRecord process is using
	public final HashMap<ContentProviderRecord, Integer> conProviders
			= new HashMap<ContentProviderRecord, Integer>();

	public ProcessRecord(int pid, int uid) {
		this.pid = pid;
		this.uid = uid;
	}

	/* package */ synchronized void updateStubProcess(int pid) {
		try {
			List<ActivityManager.RunningAppProcessInfo> runningInfos = ActivityManagerNative.getDefault()
					.getRunningAppProcesses();
			for (ActivityManager.RunningAppProcessInfo info : runningInfos) {
				if (info.pid == pid) {
					this.stubProcessName = info.processName;
					break;
				}
			}
		} catch (RemoteException e) {
			// Ignore
		}
		stubInfo = VActivityService.getService().findStubInfo(this.stubProcessName);
	}

	/**
	 * 添加一个APK到该进程
	 *
	 * @param pkgName
	 *            apk的包名
	 */
	/* package */ synchronized boolean addPackage(String pkgName) {
		if (!runningAppPkgs.contains(pkgName)) {
			runningAppPkgs.add(pkgName);
			if (initialPackage == null) {
				initialPackage = pkgName;
			}
			return true;
		}
		return false;
	}

	public String getInitialPackage() {
		return initialPackage;
	}

	/**
	 * 该进程是否正在运行指定包名的插件
	 *
	 * @param pkgName
	 *            插件包名
	 */
	public boolean isRunning(String pkgName) {
		return runningAppPkgs.contains(pkgName);
	}


	@Override
	public String toString() {
		return "ProcessRecord{" +
				"initialPackage='" + initialPackage + '\'' +
				", pid=" + pid +
				", uid=" + uid +
				", runningAppPkgs=" + runningAppPkgs +
				", stubProcessName='" + stubProcessName + '\'' +
				", appProcessName='" + processName + '\'' +
				'}';
	}
}
