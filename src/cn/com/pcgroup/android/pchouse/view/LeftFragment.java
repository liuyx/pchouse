package cn.com.pcgroup.android.pchouse.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;
import cn.com.pcgroup.android.pchouse.page.MainCatalogActivity;
import cn.com.pcgroup.android.pchouse.page.R;

public class LeftFragment extends Fragment {
	private static MainCatalogActivity mainActivity;

	private MainFragment mainFragment;
	// 当前页面的根
	private ViewGroup rootView;

	// -------清除杂志----------
	private View clearMagazineOptionView;
	private PopupWindow mPopUpWindow;
	private View cancel;
	private TextView clearAll;
	private TextView reserveTheMostRecentDownloaded;
	private TextView reserveTheMostThreeRecentDownloaded;

	private ViewGroup accountManageLayout;
	private ViewGroup clearMagazineLayout;
	private ViewGroup adviceResponseLayout;
	private ViewGroup aboutUsLayout;

	// -----------账号管理---------------------
	private ViewGroup accountManageDetailLayout;
	private ViewGroup sinaAccountManage;
	private ViewGroup tencentWeiBoAccountManage;
	private ViewGroup qzoneAccountManage;
	private int showAccountManageDetail;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.left_layout, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mainActivity = (MainCatalogActivity) getActivity();
		mainFragment = mainActivity.getMainFragment();
		initViews();
	}

	private void initViews() {
		rootView = (ViewGroup) mainActivity
				.findViewById(R.id.left_fragment_root_view);

		// -----------关于我们----------------
		aboutUsLayout = (ViewGroup) mainActivity.findViewById(R.id.about_us);
		aboutUsLayout.setOnClickListener(listener);

		// ----------清除杂志------------
		clearMagazineLayout = (ViewGroup) mainActivity
				.findViewById(R.id.clear_magazine);
		clearMagazineLayout.setOnClickListener(listener);

		clearMagazineOptionView = LayoutInflater.from(mainActivity).inflate(
				R.layout.clear_magazine, null);
		mPopUpWindow = new PopupWindow(clearMagazineOptionView,
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);
		mPopUpWindow.setAnimationStyle(R.style.menuPopupWindowAnimation);

		cancel = clearMagazineOptionView.findViewById(R.id.cancel);
		cancel.setOnClickListener(clearMagazineClickListener);

		clearAll = (TextView) clearMagazineOptionView
				.findViewById(R.id.clear_all);
		clearAll.setOnClickListener(clearMagazineClickListener);

		reserveTheMostRecentDownloaded = (TextView) clearMagazineOptionView
				.findViewById(R.id.reserve_the_most_downloaded_issue);

		reserveTheMostThreeRecentDownloaded = (TextView) clearMagazineOptionView
				.findViewById(R.id.reserve_the_most_three_downloaded_issue);

		// -------账号管理------------
		accountManageLayout = (ViewGroup) mainActivity
				.findViewById(R.id.account_manage);
		accountManageLayout.setOnClickListener(listener);
		accountManageDetailLayout = (ViewGroup) mainActivity
				.findViewById(R.id.account_manage_detail_layout);
		sinaAccountManage = (ViewGroup) mainActivity
				.findViewById(R.id.sina_account_manage);
		tencentWeiBoAccountManage = (ViewGroup) mainActivity
				.findViewById(R.id.tencent_weibo_account_manage);
		qzoneAccountManage = (ViewGroup) mainActivity
				.findViewById(R.id.qzone_account_manage);
		sinaAccountManage.setOnClickListener(listener);
		tencentWeiBoAccountManage.setOnClickListener(listener);
		qzoneAccountManage.setOnClickListener(listener);

		// ----------意见反馈---------
		adviceResponseLayout = (ViewGroup) mainActivity
				.findViewById(R.id.advice_response);
		adviceResponseLayout.setOnClickListener(listener);

	}

	private final View.OnClickListener listener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			if (id == aboutUsLayout.getId()) {
				// 显示关于我们
				mainFragment.showAboutUs();
			} else if (id == clearMagazineLayout.getId()) {
				// 显示清除杂志选项
				showClearmagaizinePop();
			} else if (id == accountManageLayout.getId()) {
				showAccountManageDetailLayout();
			} else if (id == sinaAccountManage.getId()) {
				// 新浪登陆界面
				mainFragment.showSinaLogon();
			} else if (id == tencentWeiBoAccountManage.getId()) {
				// 腾讯微博登陆界面
				mainFragment.showTencentLogon();
			} else if (id == qzoneAccountManage.getId()) {
				// QQ空间登陆界面
				mainFragment.showQZoneLogon();
			} else if (id == adviceResponseLayout.getId()) {
				// 意见反馈
				rootView.setVisibility(View.INVISIBLE);
				mainFragment.showAdviceResponse();
			}
		}
	};


	private final View.OnClickListener clearMagazineClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			if (id == cancel.getId()) { /* 取消删除杂志 */
				mPopUpWindow.dismiss();
			} else if (id == clearAll.getId()) { /* 删除所有杂志 */
				initClearMagazineImg();
				mainFragment.delAllSuccesslyDownloadedTask();
				hideClearMagazineImg();
			} else if(id == reserveTheMostRecentDownloaded.getId()){/* 保留最近下载一期 */
				initClearMagazineImg();
				mainFragment.reserveTheMostRecentIssue();
				hideClearMagazineImg();
			} else if(id == reserveTheMostThreeRecentDownloaded.getId()){ /*保留最近下载三期 */
				initClearMagazineImg();
				mainFragment.reserveTheMostThreeRecentIssue();
				hideClearMagazineImg();
			}
		}
	};

	/**
	 * 显示清除杂志选项PopupWindow
	 */
	private void showClearmagaizinePop() {
		mPopUpWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
	}

	private void showAccountManageDetailLayout() {
		showAccountManageDetail ^= 1;
		if (showAccountManageDetail == 1) {
			accountManageDetailLayout.setVisibility(View.VISIBLE);
		} else
			accountManageDetailLayout.setVisibility(View.GONE);
	}

	private ImageView clearMagazineImg;
	private WindowManager windowManager;

	private void initClearMagazineImg() {
		if (clearMagazineImg == null) {
			clearMagazineImg = new ImageView(mainActivity);
			clearMagazineImg.setImageResource(R.drawable.is_clearing);
			if (windowManager == null) {
				windowManager = (WindowManager) mainActivity
						.getSystemService(Context.WINDOW_SERVICE);
				windowManager.addView(clearMagazineImg, getLayoutParams());
			}
		}
	}

	private void hideClearMagazineImg() {
		if (clearMagazineImg != null) {
			clearMagazineImg.postDelayed(new Runnable() {

				@Override
				public void run() {
					clearMagazineImg.setVisibility(View.GONE);
				}

			}, 3000);
		}
	}

	private WindowManager.LayoutParams getLayoutParams() {
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_APPLICATION,
				WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
						| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);
		return lp;
	}

	void showRootView() {
		rootView.setVisibility(View.VISIBLE);
	}

	void hideRootView() {
		rootView.setVisibility(View.GONE);
	}

}
