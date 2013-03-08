package cn.com.pcgroup.android.pchouse.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import cn.com.pcgroup.android.pchouse.page.MainCatalogActivity;
import cn.com.pcgroup.android.pchouse.page.R;

public class LeftFragment extends Fragment {
	private static MainCatalogActivity mainActivity;
	
	private MainFragment mainFragment;
	
	private ViewGroup accountManageLayout;
	private ViewGroup clearMagazineLayout;
	private ViewGroup adviceResponseLayout;
	private ViewGroup aboutUsLayout;

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
		aboutUsLayout = (ViewGroup) mainActivity.findViewById(R.id.about_us);
		aboutUsLayout.setOnClickListener(listener);
		
		clearMagazineLayout = (ViewGroup) mainActivity.findViewById(R.id.clear_magazine);
		clearMagazineLayout.setOnClickListener(listener);
	}
	
	private final View.OnClickListener listener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int id = v.getId();
			if(id == aboutUsLayout.getId()){
				mainFragment.showAboutUs();
			}else if(id == clearMagazineLayout.getId()){
				//显示清除杂志选项
//				showClearMagazineOption();
				showDialog();
			}
		}
	};
	
	private void showClearMagazineOption(){
		View root = LayoutInflater.from(mainActivity).inflate(R.layout.information_menu, null);
		 final PopupWindow mPopupWindow = new PopupWindow(root,
                 LinearLayout.LayoutParams.FILL_PARENT,
                 LinearLayout.LayoutParams.FILL_PARENT);
		 View menuTopLayout = (LinearLayout) root.findViewById(R.id.information_menu_top_layout);
		 menuTopLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mPopupWindow.dismiss();
			}
		});
		 mPopupWindow.setAnimationStyle(R.style.menuPopupWindowAnimation);
		 LinearLayout rootView = (LinearLayout) mainActivity.findViewById(R.id.left_fragment_root_view);
		 mPopupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
	}
	
	private void showDialog(){
		MyDialogFragment dialog = MyDialogFragment.newInstance();
		dialog.show(getFragmentManager(), "dialog");
	}

}
