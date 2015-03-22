package com.daliedu.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.daliedu.DownloadActivity;
import com.daliedu.R;
import com.daliedu.VideoActivity3;
import com.daliedu.entity.Course;
import com.daliedu.util.FileUtil;
import com.umeng.analytics.MobclickAgent;

public class MyCourseListAdapter2 extends BaseAdapter{
	private Context context;
	private List<Course> courses;
	private SharedPreferences settingfile;
	private String loginType;
	private String username;
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return courses.size();
	}
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return courses.get(position);
	}
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	public android.view.View getView(final int position, android.view.View v, android.view.ViewGroup parent) {
		ViewHolder holder = null;
		if(v == null)
		{
			holder = new ViewHolder();
			v = LayoutInflater.from(context).inflate(R.layout.courselist_layout, null);
			holder.name = (TextView) v.findViewById(R.id.text4);
			holder.isDown = (TextView) v.findViewById(R.id.Downprogresstext);
			holder.btn = (ImageButton) v.findViewById(R.id.playerBtn);
			v.setTag(holder);
		}else
		{
			holder = (ViewHolder) v.getTag();
		}
		holder.name.setText(courses.get(position).getCourseName());
		int state = courses.get(position).getState();
		if(state==0)
		{
			holder.isDown.setText("δ����");
			holder.isDown.setTextColor(context.getResources().getColor(R.color.grey));
		}else if(state ==1)
		{
			holder.isDown.setText("������");
			holder.isDown.setTextColor(context.getResources().getColor(R.color.red));
		}else if(state == 2)
		{
			holder.isDown.setText("������");
			holder.isDown.setTextColor(context.getResources().getColor(R.color.green));
		}
		holder.name.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if("local".equals(loginType))
				{
					Intent intent = new Intent(context,DownloadActivity.class);
					intent.putExtra("actionName", "outline");
					context.startActivity(intent);
					return;
				}
				//umeng��¼�¼�
				MobclickAgent.onEvent(context,"online_listen");
				//
				Course c = courses.get(position);
				Intent intent = new Intent(context,VideoActivity3.class);
				intent.putExtra("name", c.getCourseName());
				intent.putExtra("username", c.getUsername());
				intent.putExtra("courseid", c.getCourseId());
				//�������ص�״̬ 
				if(c.getState()==2)
				{
					intent.putExtra("url",c.getFilePath());
				}else
				{
					//����Ƿ�����2G/3G�²���
					Boolean wifiState = checkWifiNetworkInfo();
					Boolean isDownUse3G = settingfile.getBoolean("setPlayIsUse3G", true);
					if(wifiState==null)
					{
						print("������������");
						return;
					}
					if(wifiState==false&&isDownUse3G==false)//û��wifi,�ֲ�����3G����
					{
						print("��ǰ����Ϊ2G/3G,Ҫ�������޸����û���wifi");
						return;
					}
					intent.putExtra("url", c.getFileUrl());
				}
				intent.putExtra("httpUrl", c.getFileUrl());
				context.startActivity(intent);
			}
		});
		holder.btn.setOnClickListener(new ClickEvent(position));
		return v;
		
	};
	public MyCourseListAdapter2(Context context,List<Course> course,String loginType,String username) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.courses = course;
		this.loginType = loginType;
		this.settingfile = context.getSharedPreferences("settingfile", 0);
		this.username = username;
	}
	private class ClickEvent implements OnClickListener
	{
		private int position;
		public ClickEvent(int position) {
			this.position = position;
		}
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			//���sd���Ƿ����,
			//��ȡ�ļ��Ĵ�С
			//���sd�Ŀ��������Ƿ�
			if(!FileUtil.checkSDCard(0))
			{
				Toast.makeText(context, "�����SD��", Toast.LENGTH_SHORT).show();
				return;
			}
			if("local".equals(loginType))
			{
				Toast.makeText(context, "�����ߵ�¼", Toast.LENGTH_SHORT).show();
				return;
			}
			Boolean wifiState = checkWifiNetworkInfo();
			Boolean isDownUse3G = settingfile.getBoolean("setDownIsUse3G", true);
			if(wifiState==null)
			{
				print("������������");
				return;
			}
			if(wifiState==false&&isDownUse3G==false)//û��wifi,�ֲ�����3G����
			{
				print("��ǰ����Ϊ2G/3G,Ҫ�������޸����û���wifi");
				return;
			}
			int state = courses.get(position).getState();
			Intent intent = new Intent(context,DownloadActivity.class);
			intent.putExtra("name", courses.get(position).getCourseName());
			intent.putExtra("url", courses.get(position).getFileUrl());
			intent.putExtra("username", username);
			if(state == 2)
			{
				intent.putExtra("actionName", "outline");
			}
			context.startActivity(intent);
		}
	}
	private Boolean checkWifiNetworkInfo()
	{
		ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		 //mobile 3G Data Network
        State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        //wifi
        State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        if(wifi==State.CONNECTED||wifi==State.CONNECTING)
        {
        	return true;
        }
        if(mobile==State.CONNECTED||mobile==State.CONNECTING)
        {
        	return false;
        }
		return null;
	}
	private void print(String msg)
	{
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}
	static class ViewHolder
	{
		TextView name;
		TextView isDown;
		ImageButton btn;
	}
}