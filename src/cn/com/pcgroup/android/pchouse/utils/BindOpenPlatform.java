package cn.com.pcgroup.android.pchouse.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.SparseArray;
import android.widget.Toast;
import cn.com.pcgroup.common.android.ui.SimpleToast;

import com.imofan.android.develop.sns.MFSnsAuthListener;
import com.imofan.android.develop.sns.MFSnsService;
import com.imofan.android.develop.sns.MFSnsUser;
import com.imofan.android.develop.sns.MFSnsUtil;

public class BindOpenPlatform{
	private static final SparseArray<String> platformMsgs = new SparseArray<String>();
	static {
		platformMsgs.put(MFSnsService.PLATFORM_SINA_WEIBO, "登录成功，已绑定分享到新浪微博");
		platformMsgs.put(MFSnsService.PLATFORM_QQ_WEIBO, "登录成功，已绑定分享到腾讯微博");
		platformMsgs.put(MFSnsService.PLATFORM_QQ_QZONE, "登录成功，已绑定分享到QQ空间");
	}
	
	private DefaultAuthListener defaultListener = new DefaultAuthListener();
	private MFSnsAuthListener authListener;
	private AccountLogoutForViews accountLogout;
	private AlertDialog.Builder dialogBuilder;
	
	/**
	 * 认证某个具体的开放平台，注意，在调用该方法之前,可以调用{@link setOnAuthListener},这样，便可以使用
	 * 用户自定义的监听器，否则，使用默认的监听器{@link DefaultAuthListener}
	 * @param act
	 * @param platform
	 */
	public void auth(Activity act,int platform){
		MFSnsAuthListener snsAuthListener;
		if(authListener != null){
			snsAuthListener = authListener;
		}else{
			snsAuthListener = defaultListener.setToken(platform);
		}
		
		if (act != null) {
			if (!MFSnsUtil.isAuthorized(act, platform)) {
				MFSnsService.auth(act, platform, snsAuthListener);
			}else{
				showLogoutDialog(act,platform);
			}
		}
	}
	
	/**
	 * 当认证某个具体开放平台时，注册一个认证成功后的回调方法
	 * @param authListener
	 */
	public void setOnAuthListener(MFSnsAuthListener authListener){
		this.authListener = authListener;
	}
	
	/**
	 * 当注销账户时，注册一个与界面相关的回调方法
	 * @param accountLogout
	 */
	public void setOnAccountLogoutForViews(AccountLogoutForViews accountLogout){
		this.accountLogout = accountLogout;
	}
	
	/**
	 * 注册一个当某一个具体开放平台认证成功后与界面相关的回调方法
	 * @param authSuccess
	 */
	public void setOnAuthSuccessForViews(AuthSuccessForViews authSuccess){
		defaultListener.authSuccess = authSuccess;
	}
	
	private void showLogoutDialog(final Context context,final int platform){
		if(dialogBuilder == null){
			dialogBuilder = new AlertDialog.Builder(context);
			dialogBuilder.setTitle("请选择操作");
			dialogBuilder.setPositiveButton("注销", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					logoutAndItsStatusToast(context,platform);
					if(accountLogout != null){
						accountLogout.onAccountLogoutForViews(which);
					}
				}
			});
			
			dialogBuilder.setNegativeButton("取消", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
		}
		dialogBuilder.create().show();
	}
	
	/**
	 * 注销账号
	 * @param ctx
	 * @param platform
	 */
	private void logoutAndItsStatusToast(Context ctx,int platform){
		 if(MFSnsUtil.loginOut(ctx, platform)){
			 SimpleToast.show(ctx, "注销成功!", Toast.LENGTH_SHORT);
         }else{
        	 SimpleToast.show(ctx, "抱歉,注销失败!", Toast.LENGTH_SHORT);
         }
	}
	
	/**
	 * 自定义监听器，该监听器只做了一件事——认证成功显示一条跟具体平台相关的信息
	 * 如认证新浪微博成功时，则显示"登录成功，已绑定分享到新浪微博"
	 * @author liuyx
	 *
	 */
	public static class DefaultAuthListener implements MFSnsAuthListener{
		private int token;
		private AuthSuccessForViews authSuccess;
		
		public DefaultAuthListener setToken(int newToken){
			token = newToken;
			return this;
		}

		@Override
		public void onSucceeded(Context context, MFSnsUser user) {
			SimpleToast.show(context, BindOpenPlatform.platformMsgs.get(token), Toast.LENGTH_SHORT);
			if(authSuccess != null){
				authSuccess.onAuthSuccessForViews(token,user);
			}
	        ((Activity)context).finish();
		}
	}
	
	/**
	 * 认证成功时，界面的显示信息
	 * @author user
	 *
	 */
	public static interface AuthSuccessForViews{
		/**
		 * 认证成功时，界面需要使用开放平台拿到的MFSnsUser，进行页面的展示
		 * @param platform 具体平台的token，可取值范围为 MFSnsService.PLATFORM_SINA_WEIBO,MFSnsService.PLATFORM_QQ_WEIBO,MFSnsService.PLATFORM_QQ_QZONE
		 * @param user 从开放平台拿到的用户信息
		 */
		public void onAuthSuccessForViews(int platform,MFSnsUser user);
	}
	
	/**
	 * 账号注销时，界面的显示
	 * @author liuyx
	 *
	 */
	public static interface AccountLogoutForViews{
		public void onAccountLogoutForViews(int viewId);
	}
	
}