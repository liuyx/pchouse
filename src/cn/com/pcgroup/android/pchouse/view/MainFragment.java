package cn.com.pcgroup.android.pchouse.view;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.pcgroup.android.framework.http.client.AsynLoadImageUtils;
import cn.com.pcgroup.android.framework.http.download.MultiDownLoaderListener;
import cn.com.pcgroup.android.framework.http.download.bean.DownloadTask;
import cn.com.pcgroup.android.model.BookShelf;
import cn.com.pcgroup.android.pchouse.page.MainCatalogActivity;
import cn.com.pcgroup.android.pchouse.page.R;
import cn.com.pcgroup.android.pchouse.view.ImageAdapter.ViewHolder;
import cn.com.pcgroup.android.pchouse.view.MainFragment.MultiDownListenerAndViews.DownloadTaskState;
import cn.com.pcgroup.android.pchouse.view.MainFragment.MultiDownListenerAndViews.SignTaskState;
import cn.com.pcgroup.android.service.MultiDownloadService;
import cn.com.pcgroup.android.service.MultiDownloadService.MyBinder;
import cn.com.pcgroup.common.android.utils.PreferencesUtils;

public class MainFragment extends Fragment {
	public static final String ZIP_FILE_PATH = getSdcardPath() + File.separator
			+ "zipFiles";
	public static final String PREF_NAME = "pchouse_magazing";
	public static final int FLIP_MASK = 1;
	private static final int SHOW_SET_PAGE = 1;
	private static final int START_DOWNLOAD_SERVICE = 1;
	// ---------------SharePreference的键值---------------------------
	private static final String KEY_ALREADY_DOWNLOADED = "pchouse_shelf_already_downloaded";
	private static final String KEY_ALREADY_OCCUPY_SPACE = "pchouse_shelf_already_occupy_space";
	private static final String KEY_IS_DOWNLOADING = "pchouse_shelf_is_downloading";

	private static MainCatalogActivity mainActivity;
	private static Context context;
	private final MyServiceConnection conn = new MyServiceConnection();
	/**
	 * 设置Gridview每一个Item运行和暂停切换的标志
	 */
	static int[] isStartServices;
	
	/**
	 * 切换书架单本模式和多本模式的变量
	 */
	private int flipGalleryAndGridView;
	/**
	 * 任务和监听器组合成的对象列表
	 */
	private ArrayList<DownloadTaskAndListenerAndViews> globalTaskAndListeners = new ArrayList<DownloadTaskAndListenerAndViews>();
	/**
	 * 记录运行的任务及其url的Map
	 */
	private HashMap<String, DownloadTask> urlRunningTasks = new HashMap<String, DownloadTask>();
	/**
	 * url状态map，即给每个url监听一个状态，如暂停，运行等
	 */
	private HashMap<String, Integer> urlStates = new HashMap<String, Integer>();
	private HeaderStateCount headerStateCount = new HeaderStateCount();

	private static final class HeaderStateCount {
		private int downloaded; // 已经下载的杂志数量
		private long occupy; // 所占空间的大小
		private int downloading; // 正在下载的杂志数量
	}

	/**
	 * 下载服务类
	 */
	private MultiDownloadService downloadService;

	/*
	 * 启动下载服务的Intent
	 */
	private Intent startService = new Intent();
	{
		startService
				.setAction("cn.com.pcgroup.android.pchouse.service.MULTI_DOWNLOAD");
	}
	/**
	 * 显示设置页面
	 */
	private int isShowSetPage;

	/**
	 * 显示设置按钮头部
	 */
	private ViewGroup normalHeader;
	/**
	 * 显示杂志在手机中存储的状态
	 */
	private ViewGroup longTouchHeader;
	// 已经下载杂志总数
	private TextView hasDownloadedNum;
	// 已占空间总数
	private TextView hasOccupySpace;
	// 正在下载杂志总数
	private TextView isDownloadingNum;
	/**
	 * 能够滑动的自定义LinearLayout
	 */
	private SlideView slideView;
	
	private LinearLayout gridLayout;
	private GridView grid;
	private Gallery gallery;

	/*
	 * 红勾按钮
	 */
	private ImageView redOK;
	/**
	 * 最右边的切换按钮
	 */
	private ImageView flipShelfAndSingleBtn;
	/**
	 * 最左边设置按钮
	 */
	private ImageView set;
	private AlertDialog.Builder builder;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mainActivity = (MainCatalogActivity) getActivity();
		context = mainActivity.getApplicationContext();
		return inflater.inflate(R.layout.main_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getStatisticsFromPref();
		initViews();
	}

	/**
	 * 从SharePreference中获取以前下载的记录
	 */
	private void getStatisticsFromPref() {
		headerStateCount.downloading = PreferencesUtils.getPreference(context,
				PREF_NAME, KEY_IS_DOWNLOADING, 0);
		headerStateCount.downloaded = PreferencesUtils.getPreference(context,
				PREF_NAME, KEY_ALREADY_DOWNLOADED, 0);
		headerStateCount.occupy = PreferencesUtils.getPreference(context,
				PREF_NAME, KEY_ALREADY_OCCUPY_SPACE, (long) 0);
	}

	private void initViews() {
		normalHeader = (ViewGroup) mainActivity
				.findViewById(R.id.normal_header);
		longTouchHeader = (ViewGroup) mainActivity
				.findViewById(R.id.long_click_header);
		slideView = (SlideView) mainActivity.findViewById(R.id.slideview);
		gridLayout = (LinearLayout) mainActivity.findViewById(R.id.grid_layout);
		grid = (GridView) mainActivity.findViewById(R.id.grid);
		
		FrameLayout.LayoutParams gridP = new FrameLayout.LayoutParams(
				getGridViewImgWidth(),
				(int) (getGridViewImgWidth() * 1.5));
		setAdapterViewAdapter(grid, gridP);
		setAdapterViewItemClickListener(grid, true);
		setAdapterViewItemLongClick(grid,false);
		
		gallery = (Gallery) mainActivity.findViewById(R.id.gallery);
		FrameLayout.LayoutParams galleryP = new FrameLayout.LayoutParams(
				getDisplayWidth() / 2,
				(int) (getDisplayWidth() * 0.75));
		setAdapterViewAdapter(gallery, galleryP).setMode(ImageAdapter.SINGLE_MODE);
		setAdapterViewItemClickListener(gallery, false);
		setAdapterViewItemLongClick(gallery,true);
		
		
		set = (ImageView) mainActivity.findViewById(R.id.set);
		set.setOnClickListener(listener);
		redOK = (ImageView) mainActivity.findViewById(R.id.red_ok);
		redOK.setOnClickListener(listener);
		flipShelfAndSingleBtn = (ImageView) mainActivity.findViewById(R.id.flip_shelf_and_single);
		flipShelfAndSingleBtn.setOnClickListener(listener);

		/**
		 * 初始化长按Gridview出现的Header元素
		 */
		hasDownloadedNum = (TextView) mainActivity
				.findViewById(R.id.has_downloaded_num);
		hasOccupySpace = (TextView) mainActivity
				.findViewById(R.id.has_occupy_space);
		isDownloadingNum = (TextView) mainActivity
				.findViewById(R.id.is_downloading_num);
	}

	private ImageAdapter setAdapterViewAdapter(AdapterView<? super BaseAdapter> adapterView,FrameLayout.LayoutParams p) {
		final ArrayList<BookShelf> datas = mainActivity.getMagDatas();
		ImageAdapter adapter = null;
		if (datas != null) {
			int size = datas.size();
			if (size != 0) {
				isStartServices = new int[size];
				getStatesFromPrefForMap(datas);
				test(urlStates);
				
				adapter = new ImageAdapter(datas, urlStates,
						mainActivity,p);
				adapter.setTasksAndListeners(globalTaskAndListeners);
				adapter.setIntent(startService);
				adapter.setServiceConnection(conn);
				// 初始化任务和监听器
				initTasksAndListeners(datas, size);
				adapterView.setAdapter(adapter);
			}
		}
		return adapter;
	}
	

	public static void test(HashMap<String, Integer> urlStates) {
		Log.v("liuyouxue", "==========test for hashMap===============");
		for (Map.Entry<String, Integer> entry : urlStates.entrySet()) {
			Log.v("liuyouxue", "key ====" + entry.getKey());
			Log.v("liuyouxue", "value ====" + entry.getValue());
		}
	}

	private DownloadTaskAndListenerAndViews getTaskAndListenerAndViews(int pos) {
		final ArrayList<DownloadTaskAndListenerAndViews> tasksAndListeners = globalTaskAndListeners;
		DownloadTaskAndListenerAndViews taskAndListener = null;
		if (tasksAndListeners.size() > pos) {
			taskAndListener = tasksAndListeners.get(pos);
		}
		return taskAndListener;
	}

	private MultiDownListenerAndViews getListenerAndViews(int pos) {
		final DownloadTaskAndListenerAndViews taskAndListAndViews = getTaskAndListenerAndViews(pos);
		if (taskAndListAndViews != null) {
			return taskAndListAndViews.listenerAndViews;
		}
		return null;
	}

	private ViewHolder getTaskViews(int pos) {
		final MultiDownListenerAndViews listAndViews = getListenerAndViews(pos);
		if (listAndViews != null) {
			return listAndViews.views;
		}
		return null;
	}

	private ImageView getBaseImg(int pos) {
		final ViewHolder views = getTaskViews(pos);
		if (views != null)
			return views.magazineImg;
		return null;
	}

	private ImageView getDelImg(int pos) {
		final ViewHolder views = getTaskViews(pos);
		if (views != null) {
			return views.magazingDel;
		}
		return null;
	}

	public ImageView getPauseImg(int pos) {
		final ViewHolder views = getTaskViews(pos);
		if (views != null) {
			return views.loadingPause;
		}
		return null;
	}
	
	public ImageView getDoneImg(int pos){
		final ViewHolder views = getTaskViews(pos);
		if(views != null)
			return views.loadingDone;
		return null;
	}

	public ImageView getRunningImg(int pos) {
		final ViewHolder views = getTaskViews(pos);
		if (views != null) {
			return views.loadingProgress;
		}
		return null;
	}

	public TextView getProgressTxt(int pos) {
		final ViewHolder views = getTaskViews(pos);
		if (views != null) {
			return views.progress;
		}
		return null;
	}

	private int getTaskState(int pos) {
		final String taskUrl = getTaskUrl(pos);
		if (taskUrl != null) {
			return urlStates.get(taskUrl);
		}
		return DownloadTaskState.BEGIN_STATE;
	}

	private void setTaskState(int pos, int state) {
		final MultiDownListenerAndViews listAndViews = getListenerAndViews(pos);
		if (listAndViews != null) {
			listAndViews.state = state;
			freshUrlStates(pos, state);
		}
	}

	private void freshUrlStates(int pos, int state) {
		String taskUrl = getTaskUrl(pos);
		if (taskUrl != null) {
			urlStates.put(taskUrl, state);
		}
	}

	private DownloadTask getTask(int pos) {
		final DownloadTaskAndListenerAndViews taskAndListAndViews = getTaskAndListenerAndViews(pos);
		if (taskAndListAndViews != null) {
			return taskAndListAndViews.task;
		}
		return null;
	}

	private String getTaskUrl(int pos) {
		final MultiDownListenerAndViews listAndViews = getListenerAndViews(pos);
		if (listAndViews != null) {
			return listAndViews.taskUrl;
		}
		return null;
	}

	/**
	 * 初始化各个任务的状态，将其值保存在urlStates Map中
	 * 
	 * @param datas
	 */
	private void getStatesFromPrefForMap(ArrayList<BookShelf> datas) {
		int len = datas.size();
		if (len != 0) {
			for (int i = 0; i < len; i++) {
				BookShelf bookShelf = datas.get(i);
				if (bookShelf != null) {
					String taskUrl = bookShelf.getUrl();
					if (taskUrl != null)
						urlStates.put(taskUrl, PreferencesUtils.getPreference(
								context, PREF_NAME, taskUrl, 0));
				}
			}
		}
	}

	/**
	 * 初始化每个下载任务及其监听器
	 * 
	 * @param datas
	 * @param size
	 */
	private void initTasksAndListeners(ArrayList<BookShelf> datas, int size) {
		final String sdcardPath = getSdcardPath();
		final ArrayList<DownloadTaskAndListenerAndViews> tasksAndListeners = globalTaskAndListeners;
		for (int i = 0; i < size; i++) {
			setEachTaskAndListener(i, sdcardPath, datas, tasksAndListeners);
		}
	}

	private static String getSdcardPath() {
		return Environment.getExternalStorageDirectory().getAbsolutePath();
	}

	private void setEachTaskAndListener(int pos, String sdcardPath,
			ArrayList<BookShelf> datas,
			ArrayList<DownloadTaskAndListenerAndViews> tasksAndListeners) {
		String fileUrl = datas.get(pos).getUrl();
		if (fileUrl != null && !fileUrl.equals("")) {
			String fileName = getFileName(fileUrl);
			if (!fileName.equals("")) {
				final File fileDir = new File(ZIP_FILE_PATH);
				if (!fileDir.exists()) {
					fileDir.mkdir();
				}
				File file = new File(fileDir.getAbsolutePath(),
						getFileName(fileUrl));
				MultiDownListenerAndViews downListAndViews = new MultiDownListenerAndViews(
						headerStateCount, pos, fileUrl);

				// 设置任务状态监听器
				setTaskSignListener(downListAndViews);

				tasksAndListeners.add(new DownloadTaskAndListenerAndViews(
						new DownloadTask(fileUrl, file), downListAndViews));
			}
		}
	}

	private void setTaskSignListener(MultiDownListenerAndViews downListAndViews) {
		downListAndViews.setOnSignTaskState(new SignTaskState() {

			@Override
			public void signTaskState(int position, String url, int state) {
				Log.v("ly", "任务状态 url = " + url + ", state = " + state);
				urlStates.put(url, state);
				freshViewsStates(position, state);
			}
		});
	}

	/**
	 * 刷新views的任务状态
	 * 
	 * @param pos
	 * @param state
	 */
	private void freshViewsStates(int pos, int state) {
		MultiDownListenerAndViews listAndViews = getListenerAndViews(pos);
		if (listAndViews != null) {
			listAndViews.state = state;
		}
	}

	
	private void setAdapterViewItemLongClick(AdapterView<? super BaseAdapter> adapterView,final boolean isGallery){
		adapterView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				performAdapterViewOnItemLongClick();
				if(isGallery)
					flipGalleryAndGridView();
				return true;
			}
		});
	}
	
	private void setAdapterViewItemClickListener(AdapterView<? super BaseAdapter> adapterView, final boolean isGridView){
		adapterView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				/**
				 * 如果出现删除按钮，显示Dialog后退出该方法
				 */
				if(isGridView)
					if(showDelDialog(position))
						return;
				performAdapterViewOnItemClick(position);
			}
		});
	}
	
	
	private void performAdapterViewOnItemLongClick(){
		longTouchHeader.setVisibility(View.VISIBLE);
		normalHeader.setVisibility(View.GONE);
		// 显示可以删除杂志的选项，一般只有下载成功的，或者正在下载的才能删除杂志，没有开始下载的不能删除
		iterateShowOrHideEachCanDelOption(View.VISIBLE);

		setLongTouchHeaderState();
	}
	
	private void performAdapterViewOnItemClick(int position){
		/**
		 * 第一次点击某个任务时启动下载服务然后退出该方法
		 */
		startDownloadService(position);
		// 以后该任务就在运行和暂停状态切换
		flipStartAndPause(position);
	}
	
	public class MyDialogClickListener implements DialogInterface.OnClickListener{
		private int pos;
		public DialogInterface.OnClickListener setPos(int pos){
			this.pos = pos;
			return this;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			delMagazine(pos);
		}
	}
	
	private MyDialogClickListener dialogClickListener;

	private boolean showDelDialog(final int pos) {
		ImageView delImg = getDelImg(pos);
		if (delImg != null && delImg.getVisibility() == View.VISIBLE) {
			if(builder == null){
				builder = new Builder(mainActivity);
				builder.setMessage("确认删除").setTitle("删除杂志");
				
				builder.setNegativeButton("取消", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				
				if(dialogClickListener == null){
					dialogClickListener = new MyDialogClickListener();
				}
			}
			builder.setPositiveButton("确认", dialogClickListener.setPos(pos));
			builder.create().show();
			return true;
		}
		return false;
	}

	/**
	 * 删除杂志
	 * 
	 * @param pos
	 */
	private boolean delMagazine(int pos) {
		final MultiDownloadService service = downloadService;
		final DownloadTask task = getTask(pos);
		if (service == null) {
			final Intent serviceIntent = startService;
			if (task != null && serviceIntent != null) {
				onDelMagazine(pos);

				conn.setTask(task, pos, DownloadTaskState.DEL_STATE);
				mainActivity.bindService(serviceIntent, conn,
						Service.BIND_AUTO_CREATE);
				return true;
			}
		} else {
			onDelMagazine(pos);
			return service.cancelTask(task);
		}
		return false;
	}

	/**
	 * 删除杂志时，设置Item的默认图，及其状态，刷新统计任务的数据，隐藏所有的标签views
	 * 
	 * @param pos
	 */
	private void onDelMagazine(int pos) {
		setDelAdminImg(pos);
		// 刷新下载数据
		onDelFreshTaskData(pos);
		// 设置该项的状态
		setTaskState(pos, DownloadTaskState.DEL_STATE);
		// 隐藏所有标签view
		hideAllTagViews(pos);
	}

	private void setDelAdminImg(int pos) {
		ImageView gridBaseImg = getBaseImg(pos);
		if (gridBaseImg != null) {
			gridBaseImg.setImageResource(R.drawable.del_admin_img);
		}
	}

	/**
	 * 在启动任务和暂停任务之间不断切换
	 * 
	 * @param downService
	 * @param position
	 * @param task
	 */
	private void flipStartAndPause(int position) {
		final ArrayList<DownloadTaskAndListenerAndViews> tasksAndListeners = globalTaskAndListeners;
		if (tasksAndListeners.size() > position) {
			final DownloadTask task = getTask(position);
			if (task != null) {
				if (isStartServices != null
						&& isStartServices.length > position) {
					isStartServices[position] ^= FLIP_MASK;
					/**
					 * 启动下载服务
					 */
					if (isStartServices[position] == START_DOWNLOAD_SERVICE) {
						startDownloadTask(position);
					} else {// 暂停下载任务
						pauseTask(task);
					}
				}
			}
		}
	}

	/**
	 * 启动下载服务
	 * 
	 * @param position
	 */
	private void startDownloadService(int position) {
		final MultiDownloadService downService = downloadService;
		final Intent i = startService;
		final ArrayList<DownloadTaskAndListenerAndViews> tasksAndListeners = globalTaskAndListeners;
		if (tasksAndListeners.size() > position) {
			final DownloadTask task = getTask(position);
			if (downService == null && task != null) {
				conn.setTask(task, position, DownloadTaskState.RUNNING_STATE);
				mainActivity.bindService(i, conn, Service.BIND_AUTO_CREATE);
				isStartServices[position] ^= FLIP_MASK;
				return;
			}
		}
	}

	/**
	 * 开始下载任务
	 * 
	 * @param downService
	 * @param position
	 */
	private void startDownloadTask(int position) {
		final MultiDownloadService downService = downloadService;
		final ArrayList<DownloadTaskAndListenerAndViews> tasksAndListeners = globalTaskAndListeners;
		final DownloadTask task = getTask(position);
		if (downService != null) {
			int size = tasksAndListeners.size();
			if (size > position) {
				if (task != null) {
					if (downService != null) {
						loadDownloadedImg(position);
						downService
								.addTask(task, getListenerAndViews(position));
						// 将任务与url绑定添加到map中
						urlRunningTasks.put(task.getUrl(), task);
					}
				}
			}
		}
	}

	private void loadDownloadedImg(int position) {
		ImageView baseImg = getBaseImg(position);
		String taskUrl = getTaskUrl(position);
		if (baseImg != null && taskUrl != null) {
			baseImg.setTag(taskUrl);
			AsynLoadImageUtils.getInstance().loadAndFillImg(
					mainActivity.getApplicationContext(), baseImg, taskUrl,
					false);
		}
	}

	private void pauseTask(DownloadTask task) {
		final MultiDownloadService downService = downloadService;
		if (downService != null && task != null) {
			downService.pauseTask(task);
			urlRunningTasks.remove(task);
		}
	}

	/**
	 * 显示或隐藏每个Item的可删除图标
	 * 
	 * @param visibilty ,是否显示可删除图标,View.VISIBLE为显示，View.GONE为隐藏
	 */
	private void iterateShowOrHideEachCanDelOption(int visibilty) {
		int len = globalTaskAndListeners.size();
		for (int pos = 0; pos < len; pos++) {
			Integer state = getTaskState(pos);
			if (state != DownloadTaskState.BEGIN_STATE) {
				final ImageView delImg = getDelImg(pos);
				if (delImg != null) {

					// 当按红勾按钮时，如果之前的状态是已经删除的状态，则隐藏所有的tag标签图标
					if (visibilty == View.GONE
							&& state == DownloadTaskState.DEL_STATE) {
						hideAllTagViews(pos);
					}else if(state != DownloadTaskState.DEL_STATE){
						delImg.setVisibility(visibilty);
					}
					setTaskState(pos, state);
				}
			}
		}
	}

	/**
	 * 隐藏右上角所有的标签views
	 * 
	 * @param pos
	 */
	private void hideAllTagViews(int pos) {
		ImageView delImg = getDelImg(pos);
		if (delImg != null)
			delImg.setVisibility(View.GONE);
		ImageView pauseImg = getPauseImg(pos);
		if (pauseImg != null)
			pauseImg.setVisibility(View.GONE);

		TextView progress = getProgressTxt(pos);
		if (progress != null)
			progress.setVisibility(View.GONE);

		ImageView loadingProgress = getPauseImg(pos);
		if (loadingProgress != null) {
			final AnimationDrawable anim = (AnimationDrawable) loadingProgress
					.getBackground();
			if (anim != null) {
				anim.stop();
			}
			loadingProgress.setVisibility(View.GONE);
		}
		
		ImageView loadingDone = getDoneImg(pos);
		if(loadingDone != null)
			loadingDone.setVisibility(View.GONE);
	}
	

	/**
	 * 长按Gridview设置标题view的显示状态
	 */
	private void setLongTouchHeaderState() {
		statisticsTaskData();
		flushHeaderData();
	}

	/**
	 * 统计下载数据
	 */
	private void statisticsTaskData() {
		final ArrayList<DownloadTaskAndListenerAndViews> tasksAndListeners = globalTaskAndListeners;
		int len = tasksAndListeners.size();
		int isDownloading = 0;
		int hasDownloaded = 0;
		int occupy = 0;
		for (int i = 0; i < len; i++) {
			int state = getTaskState(i);
			if (state == DownloadTaskState.PAUSE_STATE
					|| state == DownloadTaskState.RUNNING_STATE) {
				++isDownloading;
			} else if (state == DownloadTaskState.SUCCESS_STATE) {
				++hasDownloaded;
			}

			final MultiDownListenerAndViews listAndViews = getListenerAndViews(i);
			if (listAndViews != null) {
				occupy += listAndViews.occupy;
			}
		}
		headerStateCount.downloaded = hasDownloaded;
		headerStateCount.downloading = isDownloading;
		headerStateCount.occupy = occupy > headerStateCount.occupy ? occupy : headerStateCount.occupy;
		Log.v("liuyx", "altogether occupy = " + occupy);
	}

	/**
	 * 删除杂志的时候刷新下载的头部统计数据
	 * 
	 * @param pos
	 */
	private void onDelFreshTaskData(int pos) {
		int state = getTaskState(pos);
		DownloadTask task = getTask(pos);
		
		//清空监听器的占用空间数据
		MultiDownListenerAndViews listAndViews = getListenerAndViews(pos);
		if(listAndViews != null)
			listAndViews.clearOccupyData();
		
		//更新头部数据
		if (state == DownloadTaskState.PAUSE_STATE) {
			headerStateCount.downloading--;
			if (task != null) {
				// headerStateCount.staticsBytes -= task.getFileDownSize();
				headerStateCount.occupy -= task.getFileDownSize()
						/ (1024 * 1024);
			}
		} else if (state == DownloadTaskState.SUCCESS_STATE) {
			headerStateCount.downloaded--;
			if (task != null) {
				// headerStateCount.staticsBytes -= task.getFileTotalSize();
				headerStateCount.occupy -= task.getFileTotalSize()
						/ (1024 * 1024);
			}
		}

		flushHeaderData();
	}

	/**
	 * 显示刷新数据
	 */
	private void flushHeaderData() {
		hasDownloadedNum.setText(headerStateCount.downloaded + "本");
		hasOccupySpace.setText(headerStateCount.occupy + "MB");
		isDownloadingNum.setText(headerStateCount.downloading + "本"); 
	}

	private final View.OnClickListener listener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			//红勾按钮
			if (id == redOK.getId()) {
				longTouchHeader.setVisibility(View.GONE);
				normalHeader.setVisibility(View.VISIBLE);
				iterateShowOrHideEachCanDelOption(View.GONE);
			} else if (id == set.getId()) { /* 最左边设置按钮 */
				isShowSetPage ^= FLIP_MASK;
				int deltaX = mainActivity.getWindowManager()
						.getDefaultDisplay().getWidth() * 5 / 6;
				if (isShowSetPage == SHOW_SET_PAGE) { //显示设置页面时向右滑动
					slideView.slideview(0, deltaX + 15);
				} else {
					// 滑回原位
					slideView.slideview(0, -deltaX - 15);
				}
			}else if(id == flipShelfAndSingleBtn.getId()){ /* 最右边切换按钮 */
				flipGalleryAndGridView();
			}
		}
	};
	
	private void flipGalleryAndGridView(){
		flipGalleryAndGridView ^= FLIP_MASK;
		if(flipGalleryAndGridView == 1){ //显示单本模式
			gridLayout.setVisibility(View.GONE);
			gallery.setVisibility(View.VISIBLE);
		}else{//显示多本模式
			gridLayout.setVisibility(View.VISIBLE);
			gallery.setVisibility(View.GONE);
		}
	}

	public static int getGridViewImgWidth() {
		return getDisplayWidth() / 3 - 3;
	}

	private static int getDisplayWidth() {
		return ((Activity) mainActivity).getWindowManager().getDefaultDisplay()
				.getWidth();
	}

	public final class MyServiceConnection implements ServiceConnection {
		private DownloadTask task;
		private int pos;
		// 链接状态
		private int state;

		public void setTask(DownloadTask task, int pos, int state) {
			this.task = task;
			this.pos = pos;
			this.state = state;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			MyBinder binder = (MyBinder) service;
			if (downloadService == null) {
				downloadService = binder.getService();
				if (task != null) {
					final ArrayList<DownloadTaskAndListenerAndViews> tasksAndListeners = globalTaskAndListeners;
					if (tasksAndListeners.size() > pos) {
						MultiDownLoaderListener listenerAndViews = getListenerAndViews(pos);

						// 假如要求启动任务状态
						if (state == DownloadTaskState.RUNNING_STATE) {
							downloadService.addTask(task, listenerAndViews);
						}

						// 假如要求删除任务状态
						if (state == DownloadTaskState.DEL_STATE) {
							downloadService.cancelTask(task);
						}
					}
				}
			}
		}
	}

	/**
	 * 从url返回最后一个文件名
	 * 
	 * @param path
	 * @return
	 */
	public static String getFileName(String path) {
		if (path != null && !path.equals(""))
			return path.substring(path.lastIndexOf("/") + 1);
		return "";
	}

	/**
	 * 记录下载的杂志占用空间数，下载的杂志数，已经下载好的杂志数
	 */
	private void saveDownloadStatus() {
		statisticsTaskData();

		PreferencesUtils.setPreferences(context, PREF_NAME,
				KEY_ALREADY_DOWNLOADED, headerStateCount.downloaded);
		PreferencesUtils.setPreferences(context, PREF_NAME, KEY_IS_DOWNLOADING,
				headerStateCount.downloading);
		PreferencesUtils.setPreferences(context, PREF_NAME,
				KEY_ALREADY_OCCUPY_SPACE, headerStateCount.occupy);
	}

	/**
	 * 保存每项任务下载的状态
	 */
	private void saveEachTaskStatus() {
		int len = globalTaskAndListeners.size();
		for (int i = 0; i < len; i++) {
			if (listener != null) {
				final MultiDownListenerAndViews views = getListenerAndViews(i);
				if (views != null) {
					/**
					 * 保存所有url的下载状态
					 */
					PreferencesUtils.setPreferences(context, PREF_NAME,
							views.taskUrl, urlStates.get(views.taskUrl));

					// 关闭所有正在下载的任务，并保存其下载进度，以便下次启动的时候从该进度开始
					shutAllDownloadingTask();
					saveHaltTasksProgress();
				}
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// 保存下载的杂志占用空间，下载杂志总数，和正在下载总数
		saveDownloadStatus();

		saveEachTaskStatus();
	}

	private void shutAllDownloadingTask() {
		if (downloadService != null)
			for (DownloadTask task : urlRunningTasks.values()) {
				downloadService.pauseTask(task);
			}
	}

	private void saveHaltTasksProgress() {
		for (Map.Entry<String, DownloadTask> entry : urlRunningTasks.entrySet()) {
			PreferencesUtils.setPreferences(context, PREF_NAME, entry.getKey()
					+ "totalBytes", entry.getValue().getFileTotalSize());
			PreferencesUtils.setPreferences(context, PREF_NAME, entry.getKey()
					+ "byteNums", entry.getValue().getFileDownSize());
		}

		// 关闭下载服务
		// mainActivity.unbindService(conn);
	}

	/**
	 * 让view显示暂停状态
	 * 
	 * @param views
	 */
	static void showPause(ViewHolder views,int position) {
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
			
			if(views.loadingDone != null)
				views.loadingDone.setVisibility(View.GONE);
		}
	}

	/**
	 * 正在下载时显示views的状态
	 * 
	 * @param views
	 * @param byteNum
	 * @param totalBytes
	 */
	static double showRunning(ViewHolder views, long byteNum,
			long totalBytes) {
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
			
			if(views.loadingDone != null)
				views.loadingDone.setVisibility(View.GONE);
			return finPercent;
		}
		return 0.0;
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
			
			if(views.loadingDone != null)
				views.loadingDone.setVisibility(View.VISIBLE);
		}
	}

	public static final class DownloadTaskAndListenerAndViews {
		DownloadTask task;
		MultiDownListenerAndViews listenerAndViews;

		public DownloadTaskAndListenerAndViews(DownloadTask task,
				MultiDownListenerAndViews listener) {
			this.task = task;
			this.listenerAndViews = listener;
			
		}
	}


	public static final class MultiDownListenerAndViews extends
			MultiDownLoaderListener {
		private long totalBytes;
		private HeaderStateCount stateCount;
		private ViewHolder views;
		private String taskUrl;
		private SignTaskState signState;
		private int position;
		private int occupy;
		private int state = DownloadTaskState.BEGIN_STATE;

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
			showPause(views,position);
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

			showRunning(views, byteNum, totalBytes);
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
	}
}
