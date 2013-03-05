package cn.com.pcgroup.android.service;

import java.io.File;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;
import cn.com.pcgroup.android.framework.http.download.MultiDownLoader;
import cn.com.pcgroup.android.framework.http.download.MultiDownLoaderListener;
import cn.com.pcgroup.android.framework.http.download.bean.DownloadTask;
import cn.com.pcgroup.android.pchouse.view.MainFragment;
import cn.com.pcgroup.common.android.ui.SimpleToast;

public class MultiDownloadService extends Service {
	private MultiDownLoader downloader;
	private Listener lister;
	
	private MyBinder binder = new MyBinder();
	public final class MyBinder extends Binder{
		public MultiDownloadService getService(){
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
		downloader = new MultiDownLoader(getApplicationContext(), 5);
	}
	
	public void setOnDownloadListener(Listener l){
		lister = l;
	}
	
	public void addTask(DownloadTask task,MultiDownLoaderListener listener){
		SimpleToast.show(getApplicationContext(), "开始任务  ", Toast.LENGTH_SHORT);
		if (lister != null) {
			lister.onStart();
		}
		downloader.addTask(task, listener);
	}
	
	public void pauseTask(DownloadTask task){
		SimpleToast.show(getApplicationContext(), "暂停任务  ", Toast.LENGTH_SHORT);
		if (lister != null) {
			lister.onPause();
		}
		downloader.pauseTask(task);
	}
	
	/**
	 * 删除任务
	 * @param task
	 */
	public boolean cancelTask(DownloadTask task){
		boolean result = false;
		if (task != null) {
			SimpleToast.show(getApplicationContext(), "删除任务 ", Toast.LENGTH_SHORT);
			downloader.cancelTask(task);
			delZipFiles(task);
			result = true;
		}
		return result;
	}
	
	private void delZipFiles(DownloadTask task){
		final File dir = new File(MainFragment.ZIP_FILE_PATH);
		final File file = new File(dir,MainFragment.getFileName(task.getUrl()));
		file.delete();
	}
	
	public static interface Listener{
		public void onStart();
		
		public void onPause();
	}
	
	

}
