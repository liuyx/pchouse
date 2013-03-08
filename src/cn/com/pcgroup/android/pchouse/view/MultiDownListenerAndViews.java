package cn.com.pcgroup.android.pchouse.view;

import android.graphics.drawable.AnimationDrawable;
import android.util.Log;
import android.view.View;
import cn.com.pcgroup.android.framework.http.download.MultiDownLoaderListener;
import cn.com.pcgroup.android.pchouse.view.MainFragmentImageAdapter.ViewHolder;
import cn.com.pcgroup.android.pchouse.view.MainFragment.HeaderStateCount;

public class MultiDownListenerAndViews extends MultiDownLoaderListener{
	ViewHolder views;
	String taskUrl;
	int state = DownloadTaskState.UN_BEGIN_STATE;
	int occupy;
	private long totalBytes;
	private HeaderStateCount stateCount;
	private SignTaskState signState;
	private int position;

	public static final class DownloadTaskState {
		public static final int UN_BEGIN_STATE = 0; //未开始下载状态
		public static final int RUNNING_STATE = 0x2; //运行状态
		public static final int PAUSE_STATE = 0x4; //暂停状态
		public static final int SUCCESS_STATE = 0x8; //下载成功状态
		public static final int DEL_STATE = 0x10; //已经删除状态
		/**
		 * 暂存态,设计该暂存态的原因是当Gallery和GridView之间切换时，会导致其Adapter调用notifyDataHasChanged方法，刷新Adapter，导致无法正确
		 * 显示长按AdapterView出现可以删除图标的状态
		 */
		public static final int SHOW_DEL_STATE = 0xE; //显示删除按钮状态
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
			int position, String url) {
		stateCount = headerStateCount;
		taskUrl = url;
		this.position = position;
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
		
		showPause(views, position);
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

		showRunning(views, position,byteNum, totalBytes);
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
		showDone(views);
	}
	
	/**
	 * 正在下载时显示views的状态
	 * 
	 * @param views
	 * @param byteNum
	 * @param totalBytes
	 */
	static void showRunning(ViewHolder views,
			int pos, long byteNum, long totalBytes) {
		if (views != null) {
			double percent = (byteNum * 100.0 / totalBytes);
			double finPercent = ((int) (percent * 100 + 0.5)) * 1.0 / 100.0;
			Log.v("liuyx", "percent = " + finPercent + "%");
			if (views.progress != null) {
				views.progress.setVisibility(View.VISIBLE);
				views.progress.setText(finPercent + "%");
			}
			if (views.loadingProgress != null) {
				views.loadingProgress.setVisibility(View.VISIBLE);
				final AnimationDrawable anim = (AnimationDrawable) views.loadingProgress
						.getBackground();
				if (anim != null) {
					anim.start();
				}
			}
			if (views.loadingPause != null)
				views.loadingPause.setVisibility(View.GONE);

			if (views.magazingDel != null)
				views.magazingDel.setVisibility(View.GONE);

			if (views.loadingDone != null)
				views.loadingDone.setVisibility(View.GONE);
			
			if(views.betwenStartAndPauseImg != null)
				views.betwenStartAndPauseImg.setVisibility(View.GONE);
			
			if(views.magazingDel != null)
				views.magazingDel.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 下载完成时显示views的状态
	 * 
	 * @param views
	 */
	static void showDone(ViewHolder views) {
		if (views != null) {
			if (views.progress != null) {
				views.progress.setVisibility(View.GONE);
			}
			if (views.loadingProgress != null) {
				views.loadingProgress.setVisibility(View.GONE);
				final AnimationDrawable anim = (AnimationDrawable) views.loadingProgress
						.getBackground();
				if (anim != null) {
					anim.stop();
				}
			}

			if (views.loadingPause != null)
				views.loadingPause.setVisibility(View.GONE);

			if (views.magazingDel != null)
				views.magazingDel.setVisibility(View.GONE);

			if (views.loadingDone != null)
				views.loadingDone.setVisibility(View.VISIBLE);
			
			if(views.betwenStartAndPauseImg != null)
				views.betwenStartAndPauseImg.setVisibility(View.GONE);
			
			if(views.magazingDel != null)
				views.magazingDel.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 让view显示暂停状态
	 * 
	 * @param views
	 */
	static void showPause(ViewHolder views,
			int position) {
		if (views != null) {
			if (views.progress != null)
				views.progress.setVisibility(View.GONE);
			if (views.loadingProgress != null) {
				views.loadingProgress.setVisibility(View.GONE);
				final AnimationDrawable anim = (AnimationDrawable) views.loadingProgress
						.getBackground();
				if (anim != null) {
					anim.stop();
				}
			}

			if (views.loadingPause != null)
				views.loadingPause.setVisibility(View.VISIBLE);
			if (views.magazingDel != null)
				views.magazingDel.setVisibility(View.GONE);

			if (views.loadingDone != null)
				views.loadingDone.setVisibility(View.GONE);
			
			if(views.betwenStartAndPauseImg != null)
				views.betwenStartAndPauseImg.setVisibility(View.GONE);
			
			if(views.magazingDel != null)
				views.magazingDel.setVisibility(View.GONE);
		}
	}
	
	static void showBetweenStartAndPause(ViewHolder views){
		if(views != null){
			if (views.progress != null)
				views.progress.setVisibility(View.GONE);
			if (views.loadingProgress != null) {
				views.loadingProgress.setVisibility(View.GONE);
				final AnimationDrawable anim = (AnimationDrawable) views.loadingProgress
						.getBackground();
				if (anim != null) {
					anim.stop();
				}
			}

			if (views.loadingPause != null)
				views.loadingPause.setVisibility(View.GONE);
			if (views.magazingDel != null)
				views.magazingDel.setVisibility(View.GONE);

			if (views.loadingDone != null)
				views.loadingDone.setVisibility(View.GONE);
			
			if(views.betwenStartAndPauseImg != null)
				views.betwenStartAndPauseImg.setVisibility(View.VISIBLE);
			
			if(views.magazingDel != null)
				views.magazingDel.setVisibility(View.GONE);
		}
	}
	
	static void showCanDelState(ViewHolder views){
		if(views != null){
			if (views.progress != null)
				views.progress.setVisibility(View.GONE);
			if (views.loadingProgress != null) {
				views.loadingProgress.setVisibility(View.GONE);
				final AnimationDrawable anim = (AnimationDrawable) views.loadingProgress
						.getBackground();
				if (anim != null) {
					anim.stop();
				}
			}

			if (views.loadingPause != null)
				views.loadingPause.setVisibility(View.GONE);
			if (views.magazingDel != null)
				views.magazingDel.setVisibility(View.GONE);

			if (views.loadingDone != null)
				views.loadingDone.setVisibility(View.GONE);
			
			if(views.betwenStartAndPauseImg != null)
				views.betwenStartAndPauseImg.setVisibility(View.GONE);
			
			if(views.magazingDel != null)
				views.magazingDel.setVisibility(View.VISIBLE);
		}
	}
}
