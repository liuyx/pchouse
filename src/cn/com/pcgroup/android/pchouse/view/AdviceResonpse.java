package cn.com.pcgroup.android.pchouse.view;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.pcgroup.android.pchouse.page.MainCatalogActivity;
import cn.com.pcgroup.android.pchouse.page.R;
import cn.com.pcgroup.common.android.ui.SimpleToast;

import com.imofan.android.basic.feedback.MFFeedback;
import com.imofan.android.basic.feedback.MFFeedbackReplyListener;
import com.imofan.android.basic.feedback.MFFeedbackService;
import com.imofan.android.basic.feedback.MFFeedbackSubmitListener;

public class AdviceResonpse{
	private static final int UPDATE_FEEDBACK = 0;
	private static final int SEND_FEEDBACK = 0x2;
	private static final int ADVICE_INIT_LENGTH = 150;
	private final MainCatalogActivity mainCatalogActivity;
	private final MainFragment mainFragment;
	private SetPageHeader header;
	private ViewGroup adviceRootView;
	// 意见内容
	private EditText advice;
	// 提交按钮
	private Button post;
	private TextView inputWarnings;
	private ListView adviceAndResponse;
	private FeedbackAdapter feedbackListViewAdapter;
	
	public AdviceResonpse(MainCatalogActivity mainCatalogActivity, SetPageHeader header){
		this.mainCatalogActivity = mainCatalogActivity;
		mainFragment = mainCatalogActivity.getMainFragment();
		this.header = header;
		initViews();
	}
	
	private void initViews() {
		adviceRootView = (ViewGroup) mainCatalogActivity.findViewById(R.id.advice_response_root);
		advice = (EditText) mainCatalogActivity.findViewById(R.id.advice_content);
		post = (Button) mainCatalogActivity.findViewById(R.id.post_btn);
		inputWarnings = (TextView) mainCatalogActivity.findViewById(R.id.input_warning);
		monitorAdviceLenAndChangePostState(); 
		setPostBtnClickListener(post);
		
		adviceAndResponse = (ListView) mainCatalogActivity.findViewById(R.id.advice_post_and_response);
		adviceAndResponse.setDivider(null);
		
		List<MFFeedback> feedbacks = new ArrayList<MFFeedback>();
		feedbackListViewAdapter = new FeedbackAdapter(mainCatalogActivity, feedbacks);
		adviceAndResponse.setAdapter(feedbackListViewAdapter);
		handler = new AdviceAndResponseHandler(mainCatalogActivity, feedbackListViewAdapter, adviceAndResponse);
		updateFeedback();
	}
	
	private void updateFeedback(){
		MFFeedbackService.update(mainCatalogActivity, new MFFeedbackReplyListener() {
			
			@Override
			public void onDetectedNothing() {
				handler.sendEmptyMessage(UPDATE_FEEDBACK);
			}
			
			@Override
			public void onDetectedNewReplies(List<MFFeedback> arg0) {
				handler.sendEmptyMessage(UPDATE_FEEDBACK);
			}
		});
	}
	
	private AdviceAndResponseHandler handler;
	
	private static class AdviceAndResponseHandler extends Handler{
		private Activity act;
		private FeedbackAdapter feedbackListViewAdapter;
		private ListView adviceAndResponseListView;
		
		public AdviceAndResponseHandler(Activity act, FeedbackAdapter feedbackListViewAdapter,ListView adviceAndResponseListView){
			this.act = act;
			this.feedbackListViewAdapter = feedbackListViewAdapter;
			this.adviceAndResponseListView = adviceAndResponseListView;
		}

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case UPDATE_FEEDBACK:
				updateFeedback();
				break;
			case SEND_FEEDBACK:
				showSubmitFeedback(msg);
				break;
			}
			
		}
		
		private void updateFeedback(){
			if (act != null) {
				List<MFFeedback> feedbacks = MFFeedbackService
						.getLocalAllReply(act);
				if (feedbacks != null) {
					feedbackListViewAdapter.setFeedbackMsgs(feedbacks);
					feedbackListViewAdapter.notifyDataSetChanged();
					adviceAndResponseListView.setSelection(feedbacks.size());
				}
			}
		}
		
		private void showSubmitFeedback(Message msg){
			String adviceStr = msg.getData().getString("advice");
			if(feedbackListViewAdapter != null){
				feedbackListViewAdapter.getFeedBacks().add(new MFFeedback(adviceStr));
				feedbackListViewAdapter.notifyDataSetChanged();
			}
			
			// 清空输入框
			EditText advice = (EditText) msg.obj;
			if (advice != null) {
				advice.setText("");
			}
		}
	}
	
	/**
	 * 监视输入建议的长度，并显示提交按钮的状态
	 */
	private void monitorAdviceLenAndChangePostState(){
		advice.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				if(s.length() > ADVICE_INIT_LENGTH){
					post.setClickable(false);
					int len = s.length() - ADVICE_INIT_LENGTH;
					inputWarnings.setTextColor(Color.RED);
					inputWarnings.setText("已超出" + len + "个字");
				}else{
					post.setClickable(true);
					int len = ADVICE_INIT_LENGTH - s.length();
					inputWarnings.setTextColor(Color.parseColor("#808080"));
					inputWarnings.setText("还可以输入" + len + "个字");
				}
			}
		});
	}
	
	private void setPostBtnClickListener(Button post){
		post.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				updateFeedback();
				final String feedbackStr = advice.getText().toString();
				MFFeedback feedback = new MFFeedback(feedbackStr);
				feedback.setUserType(MFFeedback.USER_NORMAL);
				feedback.setTimestamp(System.currentTimeMillis()/1000l+"");
				MFFeedbackService.submit(mainCatalogActivity, feedback, new MFFeedbackSubmitListener() {
					
					@Override
					public void onSubmitSucceeded(MFFeedback feedback) {
						Message msg = handler.obtainMessage();
						Bundle data = new Bundle();
						data.putString("advice", feedbackStr);
						msg.setData(data);
						msg.obj = advice;
						msg.what = SEND_FEEDBACK;
						handler.sendMessage(msg);
					}
					
					@Override
					public void onSubmitFailed() {
						SimpleToast.show(mainCatalogActivity, "提交反馈失败，请重试", Toast.LENGTH_SHORT);
					}
				});
			}
		});
	}
	
	
	public void show(){
		this.mainFragment.normalHeader.setVisibility(View.GONE);
		header.setTitle("意见反馈");
		header.show();
		this.mainFragment.gridLayout.setVisibility(View.GONE);
		adviceRootView.setVisibility(View.VISIBLE);
	}
	
	public void hide(){
		header.hide();
		adviceRootView.setVisibility(View.GONE);
	}
}