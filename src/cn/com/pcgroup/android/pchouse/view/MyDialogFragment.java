package cn.com.pcgroup.android.pchouse.view;

import cn.com.pcgroup.android.pchouse.page.R;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

@SuppressLint("ValidFragment")
public class MyDialogFragment extends DialogFragment {
	private static MyDialogFragment instance;
	
	private MyDialogFragment(){
		
	}
	
	static MyDialogFragment newInstance(){
		if(instance == null)
			instance = new MyDialogFragment();
		return instance;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		 return inflater.inflate(R.layout.information_menu, container, false);
	}
	
}
