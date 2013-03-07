package cn.com.pcgroup.android.pchouse.view;

import android.util.Log;
import cn.com.pcgroup.android.framework.http.download.MultiDownLoaderListener;
import cn.com.pcgroup.android.pchouse.view.ImageAdapter.ViewHolder;
import cn.com.pcgroup.android.pchouse.view.MainFragment.HeaderStateCount;

public class MultiDownListenerAndViews extends MultiDownLoaderListener{
	ViewHolder views;
	String taskUrl;
	int state = DownloadTaskState.BEGIN_STATE;
	int occupy;
	private long totalBytes;
	private HeaderStateCount stateCount;
	private SignTaskState signState;
	private int position;
	private MainFragment mainFragment;

	public static final class DownloadTaskState {
		public static final int BEGIN_STATE = 0;
		public static final int RUNNING_STATE = 1;
		public static final int SUCCESS_STATE = 2;
		public static final int PAUSE_STATE = 3;
		public static final int DEL_STATE = 4;
	}

	public static interface SignTaskState {
		/**
		 * 将下载任务的url及其下载状态绑定
		 * 
		 * @param url
		 * @param state
		 */
		public void signTaskState(int position, String url, int state);
	}

	/**
	 * 监听下载任务的状态，将任务的url与具体的状态绑定
	 * 
	 * @param state
	 */
	public void setOnSignTaskState(SignTaskState state) {
		signState = state;
	}

	public void setState(int state){
		this.state = state;
	}
	
	public MultiDownListenerAndViews(HeaderStateCount headerStateCount,
			int position, String url,MainFragment mainFragment) {
		stateCount = headerStateCount;
		taskUrl = url;
		this.position = position;
		this.mainFragment = mainFragment;
		if (signState != null) {
			signState.signTaskState(position, taskUrl, state);
		}
	}


	public void setDownloadingViews(ViewHolder views) {
		this.views = views;
	}

	/**
	 * 删除杂志时，需清空该监听器的占用空间
	 */
	public void clearOccupyData(){
		occupy = 0;
	}

	@Override
	public void onPause() {
		state = DownloadTaskState.PAUSE_STATE;
		if (signState != null) {
			signState.signTaskState(position, taskUrl, state);
		}
		
		ImageAdapter.showPause(mainFragment,views, position);
	}

	@Override
	public void onBegin(long totalBytes) {
		this.totalBytes = totalBytes;
	}

	@Override
	public void onRunning(long byteNum) {
		state = DownloadTaskState.RUNNING_STATE;
		if (signState != null) {
			signState.signTaskState(position, taskUrl, state);
		}

		ImageAdapter.showRunning(mainFragment,views, position,byteNum, totalBytes);
		/*
		 * 统计下载的总MB数
		 */
		if (stateCount != null) {
			occupy = (int) (byteNum / (1024 * 1024) + 0.5);
			Log.v("liuyx", "occupy = " + occupy);
		}
	}

	@Override
	public void onSuccessed() {
		state = DownloadTaskState.SUCCESS_STATE;
		if (signState != null) {
			signState.signTaskState(position, taskUrl, state);
		}
		ImageAdapter.showDone(views);
	}
}
