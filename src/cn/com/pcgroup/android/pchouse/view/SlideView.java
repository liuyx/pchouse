package cn.com.pcgroup.android.pchouse.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class SlideView extends LinearLayout {

//	private static final String TAG = "SlideView";

	public SlideView(Context context) {
		super(context);
	}

	public SlideView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public int scrollToX(int x){
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getLayoutParams();
		params.leftMargin = x;
		params.rightMargin = -x;
		setLayoutParams(params);
		invalidate();
		return params.leftMargin;
	}
	
	public void slideview(final float p1, final float p2){
		TranslateAnimation anim = new TranslateAnimation(p1, p2,0,0);
		anim.setDuration(425);
		anim.setFillAfter(true);
		anim.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				int left = getLeft() + (int)(p2 - p1);
				int top = getTop();
				int width = getWidth();
				int height = getHeight();
				clearAnimation();
				layout(left, top, width+left, top+height);
			}
		});
		startAnimation(anim);
	}

}
