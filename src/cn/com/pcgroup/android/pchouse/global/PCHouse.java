package cn.com.pcgroup.android.pchouse.global;

import android.app.Application;

public class PCHouse extends Application {
	private static PCHouse appInstance;
	
	public static PCHouse getInstance(){
		if(appInstance == null)
			appInstance = new PCHouse();
		return appInstance;
	}
}
