package cn.com.pcgroup.android.pchouse.page;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ImageView;

public class MainActivity extends Activity {
	private static final int STAY = 2; // 2s
	private static Activity act;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		act = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ImageView image = (ImageView) act.findViewById(R.id.image);
		final AnimationDrawable anim = (AnimationDrawable) image
				.getBackground();
		anim.start();
		new PreloadThread(handler).start();
	}

	private static final Handler handler = new Handler(Looper.getMainLooper()) {
		public void handleMessage(Message msg) {
			forwordActivity(act, MainCatalogActivity.class);
		}
	};

	private static void forwordActivity(Context ctx, Class<?> klass) {
		ctx.startActivity(new Intent(ctx, klass));
	}

	private static class PreloadThread extends Thread {
		private Handler handler;

		public PreloadThread(Handler handler) {
			this.handler = handler;
		}

		public void run() {
			long start = System.currentTimeMillis();

			while ((System.currentTimeMillis() - start) <= STAY * 1000) 
				;

			if (null != handler) {
				handler.sendMessage(handler.obtainMessage());
			}
		}
	}

}
