package cn.com.pcgroup.android.pchouse.view;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.com.pcgroup.android.framework.http.client.AsynLoadImageUtils;
import cn.com.pcgroup.android.framework.http.download.bean.DownloadTask;
import cn.com.pcgroup.android.model.BookShelf;
import cn.com.pcgroup.android.pchouse.page.R;
import cn.com.pcgroup.android.pchouse.view.MainFragment.DownloadTaskAndListenerAndViews;
import cn.com.pcgroup.android.pchouse.view.MainFragment.MultiDownListenerAndViews.DownloadTaskState;
import cn.com.pcgroup.android.pchouse.view.MainFragment.MyServiceConnection;
import cn.com.pcgroup.common.android.utils.PreferencesUtils;

public class ImageAdapter extends BaseAdapter {
	//此ImageAdapter为书架单本模式和多本模式进行适配,默认为多本模式适配
	public static final int MANY_MODE = 0;
	public static final int SINGLE_MODE = 1;
	private ArrayList<BookShelf> magsData;
	private ArrayList<DownloadTaskAndListenerAndViews> tasksAndListeners;
	private static Activity mainActivity;
	/**
	 * 各个任务的状态
	 */
	private final HashMap<String, Integer> taskUrlStates;
	private final SparseIntArray eachItemState = new SparseIntArray();
	private Intent startService;
	private MyServiceConnection conn;
	private FrameLayout.LayoutParams p;
	
	
	private int mode = MANY_MODE;
	
	public void setIntent(Intent startService){
		this.startService = startService;
	}
	
	public void setServiceConnection(MyServiceConnection conn){
		this.conn = conn;
	}

	public void setTasksAndListeners(
			ArrayList<DownloadTaskAndListenerAndViews> tasksAndListeners) {
		this.tasksAndListeners = tasksAndListeners;
	}
	
	public void setMode(int mode){
		this.mode = mode;
	}

	public ImageAdapter(ArrayList<BookShelf> magsData,
			HashMap<String, Integer> urlStates,Activity context) {
		this(magsData,urlStates,context,null);
	}
	
	public ImageAdapter(ArrayList<BookShelf> magsData,HashMap<String, Integer> urlStates,Activity context,FrameLayout.LayoutParams p){
		this.magsData = magsData;
		taskUrlStates = urlStates;
		
		mainActivity = context;
		this.p = p;
		fillEachItemState();
	}
	
	/**
	 * 将每个url的状态写到eachItemState容器中
	 */
	private void fillEachItemState() {
		int len = magsData.size();
		for (int i = 0; i < len; i++) {
			BookShelf bookShelf = magsData.get(i);
			if (bookShelf != null) {
				String url = bookShelf.getUrl();
				if (url != null) {
					Integer state = taskUrlStates.get(url);
					state = state == null ? 0 : state;
					eachItemState.put(i, state);
				}
			}
		}
	}

	@Override
	public int getCount() {
		return magsData != null ? magsData.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return magsData != null ? magsData.get(position).getUrl() : "";
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mainActivity).inflate(
					R.layout.img_adapter, null);
			holder.magazineImg = (ImageView) convertView
					.findViewById(R.id.image);
			if (p != null) {
				holder.magazineImg.setLayoutParams(p);
			}
			holder.magazineImg
					.setScaleType(ImageView.ScaleType.CENTER_CROP);
			holder.month = (TextView) convertView.findViewById(R.id.text1);
			holder.size = (TextView) convertView.findViewById(R.id.text2);
			holder.progress = (TextView) convertView
					.findViewById(R.id.progress);
			holder.loadingProgress = (ImageView) convertView
					.findViewById(R.id.img_right_top_is_downloading);
			holder.loadingPause = (ImageView) convertView
					.findViewById(R.id.img_right_top_pause);
			holder.magazingDel = (ImageView) convertView
					.findViewById(R.id.img_right_top_delete);
			holder.loadingDone = (ImageView) convertView.findViewById(R.id.img_right_top_done);
			holder.progsBar = (ProgressBar) convertView
					.findViewById(R.id.loadprogress);
			holder.summary = (TextView) convertView.findViewById(R.id.summary);
			if(mode == MANY_MODE){
				holder.summary.setVisibility(View.GONE);
			}else{
				holder.summary.setVisibility(View.VISIBLE);
			}
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		String magazingUrl = loadEachItem(position, holder);
		showEachItemState(position, holder, magazingUrl);

		return convertView;
	}

	static final class ViewHolder {
		ImageView magazineImg; // gridview 图片
		TextView month; // 月份
		TextView size; // 大小
		TextView progress; //显示加载百分比进度
		ImageView loadingProgress; //正在下载动画
		ImageView loadingPause; //暂停下载图标
		ImageView magazingDel; //删除杂志图标
		ImageView loadingDone; //加载完成图标
		ProgressBar progsBar;
		TextView summary; //简介,用于单本模式下的内容介绍
	}

	private String loadEachItem(int position, ViewHolder holder) {
		if (magsData != null && magsData.size() > position) {
			BookShelf bookShelf = magsData.get(position);
			if (bookShelf != null) {
				holder.month
						.setText(bookShelf.getIssue() != null ? bookShelf
								.getIssue() : "");
				holder.size.setText(bookShelf.getSize() != null ? bookShelf
						.getSize() : "0MB");
				
				String summary = bookShelf.getSummary();
				if(summary != null){
					holder.summary.setText(summary);
				}
				String magazineImgUrl = bookShelf.getCover();

				if (magazineImgUrl != null && !magazineImgUrl.equals("")
						&& !magazineImgUrl.equals("null")) {
					holder.magazineImg.setTag(magazineImgUrl);
					AsynLoadImageUtils.getInstance().loadAndFillImg(
							mainActivity, holder.magazineImg, magazineImgUrl,
							false, holder.progsBar);
				}
				return magazineImgUrl;
			}
		}
		return "";
	}

	private int getEachItemSavedState(int pos) {
		return eachItemState.get(pos);
	}

	private void showEachItemState(int position, ViewHolder holder,
			String taskUrl) {
		/**
		 * 设置每项显示或隐藏的view
		 */
		if (tasksAndListeners != null
				&& tasksAndListeners.size() > position) {
			DownloadTaskAndListenerAndViews taskAndListener = tasksAndListeners
					.get(position);
			if (taskAndListener != null) {
//				DownloadingViews views = new DownloadingViews(
//						holder.progress, holder.loadingProgress,
//						holder.loadingPause, holder.magazingDel,holder.magazineImg,holder.loadingDone,position);
				taskAndListener.listenerAndViews.setDownloadingViews(holder);
				setEachTaskShowState(position, holder, taskUrl);
			}
		}
	}

	/**
	 * 测试每个url的状态
	 * 
	 * @param position
	 * @param taskUrl
	 */
	private void testState(int position, int state) {
		switch (state) {
		case DownloadTaskState.BEGIN_STATE:
			Log.v("lyx", "pos = " + position + ", state = begin");
			break;
		case DownloadTaskState.PAUSE_STATE:
			Log.v("lyx", "pos = " + position + ", state = pause");
			break;
		case DownloadTaskState.SUCCESS_STATE:
			Log.v("lyx", "pos = " + position + ", state = success");
			break;
		case DownloadTaskState.RUNNING_STATE:
			Log.v("lyx", "pos = " + position + ", state = running");
			break;
		}
	}

	/**
	 * 设置各个任务的显示状态
	 */
	private void setEachTaskShowState(int position, ViewHolder views,
			String taskUrl) {
		/**
		 * 遍历所有的任务状态
		 */
		showStateForViews(position, views, taskUrl);
	}

	private void showStateForViews(int position, ViewHolder views,
			String taskUrl) {
		int state = getEachItemSavedState(position);
		testState(position, state);
		switch (state) {
		case DownloadTaskState.PAUSE_STATE:
			MainFragment.showPause(views,position);
			break;
		case DownloadTaskState.RUNNING_STATE:
			startShowProgress(position,taskUrl, views);
			break;
		case DownloadTaskState.SUCCESS_STATE:
			MainFragment.showDone(views);
			break;
		default:
			break;
		}
	}

	/**
	 * 启动的时候显示正在下载的状态
	 * 
	 * @param views
	 */
	private void startShowProgress(int pos,String taskUrl, ViewHolder views) {
		if (views != null) {
			//获取上次下载进度
			getLastTimeDownloadProgress(taskUrl);
			//启动下载服务
			startDownloadService(pos,tasksAndListeners.get(pos).task);
			
			MainFragment.showRunning(views, byteNums, totalBytes);
		}
	}
	
	private long byteNums;
	private long totalBytes;
	
	private void getLastTimeDownloadProgress(String taskUrl){
		byteNums = PreferencesUtils.getPreference(mainActivity, MainFragment.PREF_NAME, taskUrl+"byteNums", 0);
		totalBytes = PreferencesUtils.getPreference(mainActivity, MainFragment.PREF_NAME, taskUrl+"totalBytes", 0);
	}
	
	private void startDownloadService(int pos,DownloadTask task){
		final Intent service = startService;
		if(service != null){
			conn.setTask(task, pos,DownloadTaskState.RUNNING_STATE);
			mainActivity.bindService(service, conn, Service.BIND_AUTO_CREATE);
			// 将该位置的状态设置为运行状态
			MainFragment.isStartServices[pos] ^= MainFragment.FLIP_MASK;
		}
	}

}
