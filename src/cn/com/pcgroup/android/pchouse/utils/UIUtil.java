package cn.com.pcgroup.android.pchouse.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class UIUtil {
	
	public static void hideSoftKeybord(Context context, View focusView) {
		if(context == null || focusView == null)
			throw new IllegalArgumentException("context 或者 focusView 为空");
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
	}

	public static void showSoftKeybord(Context context, View focusView) {
		if(context == null || focusView == null)
			throw new IllegalArgumentException("context 或者 focusView 为空");
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (focusView.isFocusable() && !focusView.isFocused()) {
			focusView.setFocusable(true);
			focusView.requestFocus();
		}
		imm.showSoftInput(focusView, 0);
	}

}
