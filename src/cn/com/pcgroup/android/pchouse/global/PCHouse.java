package cn.com.pcgroup.android.pchouse.global;

import com.imofan.android.develop.sns.MFSnsConfig;

import android.app.Application;

public class PCHouse extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
        
		initOpenPlatformConfig();
	}
	
	public void initOpenPlatformConfig(){
		MFSnsConfig.CONSUMER_KEY_SINA = "2111164588";
        MFSnsConfig.CONSUMER_REDIRECT_URL_SINA = "http://app.pconline.com.cn/Android/";
        
        MFSnsConfig.CONSUMER_KEY_TECENT = "100261319";
        MFSnsConfig.CONSUMER_REDIRECT_URL_TECENT = "http://app.pconline.com.cn/Android/";
        
        MFSnsConfig.CONSUMER_KEY_QZONE = "100261319";
        MFSnsConfig.CONSUMER_REDIRECT_URL_QZONE = "http://app.pconline.com.cn/Android/";
        //微信appid
        MFSnsConfig.CONSUMER_WEIXIN_APPID = "wx3987f65917c3d3e9";
        MFSnsConfig.CONSUMER_KEY_RENREN = "6c79678386204ea1b3529727e9660339";
        MFSnsConfig.CONSUMER_SECRET_RENREN = "8eb9f8b527164ba4bdabaaafe62cb772";
	}
	
}
