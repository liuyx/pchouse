package cn.com.pcgroup.android.pchouse.view;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import cn.com.pcgroup.android.pchouse.page.MainCatalogActivity;
import cn.com.pcgroup.android.pchouse.page.R;

public class AboutUs {
	private MainCatalogActivity mainActivity;
	private MainFragment mainFragment;
	//关于我们头部布局
	ViewGroup aboutUsHeader;
	//回退按钮
	private ImageView backImg;
	
	public AboutUs(MainCatalogActivity mainActivity){
		this.mainActivity = mainActivity;
		mainFragment = mainActivity.getMainFragment();
	}
	
	void initViews(){
		aboutUsHeader = (ViewGroup) mainActivity.findViewById(R.id.about_us_head);
		backImg = (ImageView) mainActivity.findViewById(R.id.about_us_back);
		backImg.setOnClickListener(listener);
	}
	
	private final View.OnClickListener listener = new View.OnClickListener(){

		@Override
		public void onClick(View v) {
			int id =v.getId();
			if(id == backImg.getId()){
				//显示设置页面
				mainFragment.showSetpage();
			}
		}
		
	};
	
}
