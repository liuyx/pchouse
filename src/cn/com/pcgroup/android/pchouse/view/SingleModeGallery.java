package cn.com.pcgroup.android.pchouse.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Gallery;

public class SingleModeGallery extends Gallery {
	public SingleModeGallery(Context paramContext){
	   super(paramContext);
    }

	public SingleModeGallery(Context paramContext, AttributeSet paramAttributeSet){
	    super(paramContext, paramAttributeSet);
	}

	public SingleModeGallery(Context paramContext, AttributeSet paramAttributeSet, int paramInt){
	    super(paramContext, paramAttributeSet, paramInt);
	}

	private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2)
	{  
	    return e2.getX() - e1.getX() > 20; 
	}
	  
	
	// 用户按下触摸屏、快速移动后松开
	public boolean onFling(MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2){
		int kEvent;  
		if(isScrollingLeft(paramMotionEvent1, paramMotionEvent2)){ 
		    //Check if scrolling left     
			kEvent = KeyEvent.KEYCODE_DPAD_LEFT;  
			
		} else{ 
		     //Otherwise scrolling right    
			 kEvent = KeyEvent.KEYCODE_DPAD_RIGHT;
		}  
		onKeyDown(kEvent, null);  
		return super.onFling(paramMotionEvent1, paramMotionEvent2, 0, paramFloat2);
	 }
	 
}

