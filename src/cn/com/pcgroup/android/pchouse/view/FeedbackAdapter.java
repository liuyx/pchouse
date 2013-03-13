package cn.com.pcgroup.android.pchouse.view;

import java.util.List;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.pcgroup.android.pchouse.page.R;

import com.imofan.android.basic.feedback.MFFeedback;

public class FeedbackAdapter extends BaseAdapter {
	private final Context context;
	private List<MFFeedback> feedbackMsgs;
	private SparseArray<ViewHolder> viewHolders = new SparseArray<ViewHolder>();
	
	public FeedbackAdapter(Context context,List<MFFeedback> feedbackMsgs){
		this.context = context;
		this.feedbackMsgs = feedbackMsgs;
	}
	
	public void setFeedbackMsgs(List<MFFeedback> feedbackMsgs){
		this.feedbackMsgs = feedbackMsgs;
	}
	
	public List<MFFeedback> getFeedBacks(){
		return feedbackMsgs;
	}
	@Override
	public int getCount() {
		return feedbackMsgs != null ? feedbackMsgs.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return viewHolders.size() > position ? viewHolders.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.feedback_adapter, null);
			holder.feedbackImg = (ImageView) convertView.findViewById(R.id.feedback_push_img);
			holder.feedbackTxtView = (TextView)convertView.findViewById(R.id.feedback_txt);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		// 显示建议和反馈信息
		showAdviceAndResponse(position, holder);
		
		viewHolders.put(position, holder);
		
		return convertView;
	}
	
	private void showAdviceAndResponse(int position,ViewHolder holder){
		if(feedbackMsgs != null && feedbackMsgs.size() > position){
			MFFeedback feedback = feedbackMsgs.get(position);
			if(feedback != null){
				int userType = feedback.getUserType();
				final String advice = feedback.getFeedback();
				if(advice != null){
					holder.feedbackTxtView.setText(advice);
				}
				
				if(userType == MFFeedback.USER_NORMAL){
					holder.feedbackImg.setVisibility(View.VISIBLE);
					holder.feedbackTxtView.setBackgroundResource(R.drawable.saved_advice);
				}else if(userType == MFFeedback.USER_DEVELOPER){
					holder.feedbackImg.setVisibility(View.INVISIBLE);
					holder.feedbackTxtView.setBackgroundResource(0);
					holder.feedbackTxtView.setBackgroundResource(R.drawable.save_advice_response);
				}
			}
		}
	}
	
	static final class ViewHolder{
		ImageView feedbackImg;
		TextView feedbackTxtView;
	}

}
