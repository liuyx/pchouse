package cn.com.pcgroup.android.pchouse.page;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import cn.com.pcgroup.android.model.BookShelf;
import cn.com.pcgroup.android.pchouse.view.LeftFragment;
import cn.com.pcgroup.android.pchouse.view.MainFragment;

public class MainCatalogActivity extends FragmentActivity {
	private static final String JSON_FILE = "magazines-ip5-hd.json";
	/**
	 * 杂志数据列表
	 */
	private ArrayList<BookShelf> mags;
	
	private LeftFragment leftFragment;
	private MainFragment mainFragment;
	
	/**
	 * 返回杂志数据列表
	 * @return
	 */
	public ArrayList<BookShelf> getMagDatas(){
		return mags;
	}
	
	public LeftFragment getLeftFragment(){
		return leftFragment;
	}
	
	public MainFragment getMainFragment(){
		return mainFragment;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.catalog);
		
		leftFragment = (LeftFragment) getSupportFragmentManager().findFragmentById(R.id.left_fragment);
		mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.main_fragment);
		handleJSON();
	}
	
	private void handleJSON(){
		try {
			InputStream in = getAssets().open(JSON_FILE);
			if (in != null) {
				String json = getStrFromStream(in);
				mags = parseJSON(json);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String getStrFromStream(InputStream in){
		ByteBuffer buff = ByteBuffer.allocateDirect(4096);
		ReadableByteChannel inChannel = Channels.newChannel(in);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		WritableByteChannel  outChannel = Channels.newChannel(out);
		try {
			while(inChannel.read(buff) != -1){
				buff.flip();
				outChannel.write(buff);
				buff.clear();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return new String(out.toByteArray());
	}
	
	/**
	 * 解析json
	 * @param json
	 * @return
	 */
	private ArrayList<BookShelf> parseJSON(String json){
		ArrayList<BookShelf> result = new ArrayList<BookShelf>();
		try {
			JSONObject jsonObject = new JSONObject(json);
			JSONArray  jsonArray = jsonObject.optJSONArray("magazines");
			if(jsonArray != null){
				int len = jsonArray.length();
				for(int i = 0; i < len; i++){
					JSONObject obj = jsonArray.optJSONObject(i);
					if(obj != null){
						BookShelf bookShelfData = new BookShelf();
						bookShelfData.setId(obj.optString("id"));
						bookShelfData.setIssue(obj.optString("issue"));
						bookShelfData.setMd5(obj.optString("md5"));
						bookShelfData.setBestTopicUrl(obj.optString("bestTopicUrl"));
						bookShelfData.setBookDownloadStatus(obj.optString("bookDownloadStatus"));
						bookShelfData.setCover(obj.optString("cover"));
						bookShelfData.setDeviceType(obj.optString("deviceType"));
						bookShelfData.setDir_hor(obj.optString("dir-hor"));
						bookShelfData.setDir_ver(obj.optString("dir-ver"));
						bookShelfData.setMagazine(obj.optString("magazine"));
						bookShelfData.setPublisher(obj.optString("publisher"));
						bookShelfData.setPublishTime(obj.optString("publishTime"));
						bookShelfData.setSize(obj.optString("size"));
						bookShelfData.setSummary(obj.optString("summary"));
						bookShelfData.setThumb(obj.optString("thumb"));
						bookShelfData.setUrl(obj.optString("url"));
						bookShelfData.setVolume(obj.optString("volume"));
						result.add(bookShelfData);
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public void doPositiveClick() {
	    // Do stuff here.
	    Log.i("FragmentAlertDialog", "Positive click!");
	}

	public void doNegativeClick() {
	    // Do stuff here.
	    Log.i("FragmentAlertDialog", "Negative click!");
	}
	
	
}
