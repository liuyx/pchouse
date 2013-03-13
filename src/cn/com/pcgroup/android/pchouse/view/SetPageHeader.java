package cn.com.pcgroup.android.pchouse.view;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.pcgroup.android.pchouse.page.R;

/**
	 * 点击设置页面中的某一项时，MainFragment头部显示的元素
	 * @author user
	 *
	 */
	public class SetPageHeader{
		private ViewGroup rootView;
		private TextView title;
		private ImageView backImg;
		/**
		 * 显示设置按钮头部
		 */
		private ViewGroup normalHeader;
		private LinearLayout gridLayout;
		private SlideView slideView;
		private int slideDeltaX; // 水平方向滑动的距离
		
		public SetPageHeader(ViewGroup normalHeader,LinearLayout gridLayout,SlideView slideView,int slideDeltaX){
			this.normalHeader = normalHeader;
			this.gridLayout = gridLayout;
			this.slideView = slideView;
			this.slideDeltaX = slideDeltaX;
			initViews();
		}
		
		public void show(){
			rootView.setVisibility(View.VISIBLE);
		}
		
		public void hide(){
			rootView.setVisibility(View.GONE);
		}
		
		public void setTitle(String titleStr){
			title.setText(titleStr);
		}
		
		public void initViews(){
			rootView = (ViewGroup) MainFragment.mainActivity.findViewById(R.id.set_page_head);
			title = (TextView) MainFragment.mainActivity.findViewById(R.id.header_title);
			backImg = (ImageView) MainFragment.mainActivity.findViewById(R.id.back_img);
			backImg.setOnClickListener(listener);
		}
		
		private final View.OnClickListener listener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//显示书架头部
				normalHeader.setVisibility(View.VISIBLE);
				//隐藏当前的头部
				hide();
				//显示书架
				gridLayout.setVisibility(View.VISIBLE);
				slideView.slideview(0, slideDeltaX);
//				slideToRight();
				//显示设置页面
				MainFragment.mainActivity.getLeftFragment().showRootView();
			}
		};
	}