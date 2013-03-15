package cn.com.pcgroup.android.service;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;
import cn.com.pcgroup.android.framework.http.download.MultiDownLoader;
import cn.com.pcgroup.android.framework.http.download.MultiDownLoaderListener;
import cn.com.pcgroup.android.framework.http.download.bean.DownloadTask;
import cn.com.pcgroup.android.pchouse.utils.NetworkStateMonitorUtils;
import cn.com.pcgroup.android.pchouse.view.MainFragment;
import cn.com.pcgroup.common.android.ui.SimpleToast;

public class MultiDownloadService extends Service {
	private static final int MOST_TASK_NUM_PARA = 5; // 同时运行任务的最大数
	private MultiDownLoader downloader;

	private MyBinder binder = new MyBinder();

	public final class MyBinder extends Binder {
		public MultiDownloadService getService() {
			return MultiDownloadService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		downloader = new MultiDownLoader(getApplicationContext(), MOST_TASK_NUM_PARA);
	}

	public void addTask(DownloadTask task, MultiDownLoaderListener listener,Activity act) {
		if(!isInWifiEnv())
			showNetStateAlertDialog(task,listener,act);
		else 
			startTask(task,listener);
	}
	
	

	public void pauseTask(DownloadTask task) {
		SimpleToast.show(getApplicationContext(), "暂停任务  ", Toast.LENGTH_SHORT);
		downloader.pauseTask(task);
	}

	/**
	 * 删除任务
	 * 
	 * @param task
	 */
	public boolean cancelTask(DownloadTask task) {
		boolean result = false;
		if (task != null) {
			SimpleToast.show(getApplicationContext(), "删除任务 ",
					Toast.LENGTH_SHORT);
			downloader.cancelTask(task);
			delZipFiles(task);
			result = true;
		}
		return result;
	}
	
	private boolean isInWifiEnv(){
		return NetworkStateMonitorUtils.getNetworkState(getApplicationContext()) == NetworkStateMonitorUtils.STATE_WIFI;
	}
	
	private AlertDialog.Builder builder;
	
	private void showNetStateAlertDialog(DownloadTask task, MultiDownLoaderListener listener,Activity act){
		if(builder == null){
			builder = new AlertDialog.Builder(act);
			builder.setTitle("网络状态").setMessage("当前不在wifi状态下，是否确定要下载?");
			dialogOnClickListener = new MyDialogOnClickListener(task, listener);
			builder.setPositiveButton("确定", dialogOnClickListener);
			builder.setNegativeButton("取消", dialogOnClickListener);
			builder.create();
		}
		builder.show();
	}
	
	private MyDialogOnClickListener dialogOnClickListener;
	private class MyDialogOnClickListener implements DialogInterface.OnClickListener{
		private DownloadTask task;
		private MultiDownLoaderListener listener;
		public MyDialogOnClickListener(DownloadTask task, MultiDownLoaderListener listener){
			this.task = task;
			this.listener = listener;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch(which){
			case DialogInterface.BUTTON_POSITIVE:
				startTask(task, listener);
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				dialog.cancel();
				break;
			}
		}
		
	}
	
	private void startTask(DownloadTask task, MultiDownLoaderListener listener){
		SimpleToast.show(getApplicationContext(), "开始任务  ", Toast.LENGTH_SHORT);
		downloader.addTask(task, listener);
	}


	private void delZipFiles(DownloadTask task) {
		final File dir = new File(MainFragment.ZIP_FILE_PATH);
		final File file = new File(dir, MainFragment.getFileName(task.getUrl()));
		file.delete();
	}

}
