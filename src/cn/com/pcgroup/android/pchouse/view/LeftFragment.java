package cn.com.pcgroup.android.pchouse.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import cn.com.pcgroup.android.pchouse.page.MainCatalogActivity;
import cn.com.pcgroup.android.pchouse.page.R;

public class LeftFragment extends Fragment {
	private static MainCatalogActivity mainActivity;
	
	private MainFragment mainFragment;
	//当前页面的根
	private ViewGroup rootView;
	
	//-------清除杂志----------
	private View clearMagazineOptionView;
	private PopupWindow mPopUpWindow;
	private View cancel; 
	
	private ViewGroup accountManageLayout;
	private ViewGroup clearMagazineLayout;
	private ViewGroup adviceResponseLayout;
	private ViewGroup aboutUsLayout;
	
	//-----------账号管理---------------------
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
	
	private void initViews(){
		rootView = (ViewGroup) mainActivity.findViewById(R.id.left_fragment_root_view);
		
		//-----------关于我们----------------
		aboutUsLayout = (ViewGroup) mainActivity.findViewById(R.id.about_us);
		aboutUsLayout.setOnClickListener(listener);
		
		//----------清除杂志------------
		clearMagazineLayout = (ViewGroup) mainActivity.findViewById(R.id.clear_magazine);
		clearMagazineLayout.setOnClickListener(listener);
		
		//-------账号管理------------
		accountManageLayout = (ViewGroup) mainActivity.findViewById(R.id.account_manage);
		accountManageLayout.setOnClickListener(listener);
		accountManageDetailLayout = (ViewGroup) mainActivity.findViewById(R.id.account_manage_detail_layout);
		sinaAccountManage = (ViewGroup) mainActivity.findViewById(R.id.sina_account_manage);
		tencentWeiBoAccountManage = (ViewGroup) mainActivity.findViewById(R.id.tencent_weibo_account_manage);
		qzoneAccountManage = (ViewGroup) mainActivity.findViewById(R.id.qzone_account_manage);
		sinaAccountManage.setOnClickListener(listener);
		tencentWeiBoAccountManage.setOnClickListener(listener);
		qzoneAccountManage.setOnClickListener(listener);
		
		//----------意见反馈---------
		adviceResponseLayout = (ViewGroup) mainActivity.findViewById(R.id.advice_response);
		adviceResponseLayout.setOnClickListener(listener);
	}
	
	private final View.OnClickListener listener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int id = v.getId();
			if(id == aboutUsLayout.getId()){
				// 显示关于我们
				mainFragment.showAboutUs();
			}else if(id == clearMagazineLayout.getId()){
				//显示清除杂志选项
				showClearmagaizinePop();
			}else if(id == accountManageLayout.getId()){
				showAccountManageDetailLayout();
			}else if(id == sinaAccountManage.getId()){
				// 新浪登陆界面
				mainFragment.showSinaLogon();
			}else if(id == tencentWeiBoAccountManage.getId()){
				// 腾讯微博登陆界面
				mainFragment.showTencentLogon();
			}else if(id == qzoneAccountManage.getId()){
				// QQ空间登陆界面
				mainFragment.showQZoneLogon();
			}else if(id == adviceResponseLayout.getId()){
				// 意见反馈
				rootView.setVisibility(View.INVISIBLE);
				mainFragment.showAdviceResponse();
			}
		}
	};
	
	private void initIfViewsAreNull(){
		if(clearMagazineOptionView == null)
			clearMagazineOptionView = LayoutInflater.from(mainActivity).inflate(
					R.layout.clear_magazine, null);
		if(mPopUpWindow == null){
			mPopUpWindow = new PopupWindow(clearMagazineOptionView,
	                 LinearLayout.LayoutParams.FILL_PARENT,
	                 LinearLayout.LayoutParams.FILL_PARENT);
			mPopUpWindow.setAnimationStyle(R.style.menuPopupWindowAnimation);
		}
		
		if(cancel == null){
			cancel = clearMagazineOptionView.findViewById(R.id.cancel);
			cancel.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					mPopUpWindow.dismiss();
				}
			});
		}
		
		if(rootView == null)
			rootView = (LinearLayout) mainActivity.findViewById(R.id.left_fragment_root_view);
	}
	
	/**
	 * 显示清除杂志选项PopupWindow
	 */
	private void showClearmagaizinePop(){
		initIfViewsAreNull();
		mPopUpWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
	}
	
	private void showAccountManageDetailLayout(){
		showAccountManageDetail ^= 1;
		if(showAccountManageDetail == 1){
			accountManageDetailLayout.setVisibility(View.VISIBLE);
//			int marginRight = mainFragment.getSlideDeltaX();
//			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//			lp.setMargins(0, 0, marginRight, 0);
//			accountManageDetailLayout.setLayoutParams(lp);
		}
		else
			accountManageDetailLayout.setVisibility(View.GONE);
	}
	
	void showRootView(){
		rootView.setVisibility(View.VISIBLE);
	}
	
	void hideRootView(){
		rootView.setVisibility(View.GONE);
	}
	
}
