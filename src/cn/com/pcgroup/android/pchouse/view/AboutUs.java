package cn.com.pcgroup.android.pchouse.view;

import android.view.View;
import android.view.ViewGroup;
import cn.com.pcgroup.android.pchouse.page.R;

class AboutUs{
	SetPageHeader header;
	private ViewGroup aboutUsRootView;
	private MainFragment mainFragment;
	public AboutUs(SetPageHeader header,MainFragment mainFragment){
		this.header = header;
		this.mainFragment = mainFragment;
		initViews();
	}
	
	void initViews() {
		aboutUsRootView = (ViewGroup) MainFragment.mainActivity.findViewById(R.id.about_us_root);
	}
	
	/**
	 * 显示关于我们页面
	 */
	void show() {
		mainFragment.getAdviceResponse().hide();
		showSetPageHeader();
		showAboutUsBody();
	}
	
	private void showSetPageHeader(){
		mainFragment.normalHeader.setVisibility(View.GONE);
		header.setTitle("关于我们");
		header.show();
	}
	
	private void showAboutUsBody() {
		/*
		 * 在这里显示About us webView
		 */
		mainFragment.gridLayout.setVisibility(View.GONE);
		MainFragment.mainActivity.getLeftFragment().hideRootView();
		loadAboutUs();
	}
	
	void loadAboutUs(){
		aboutUsRootView.setVisibility(View.VISIBLE);
	}
}