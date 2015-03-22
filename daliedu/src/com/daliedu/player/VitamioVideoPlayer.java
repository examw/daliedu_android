package com.daliedu.player;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.widget.VideoView;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.daliedu.VideoActivity3;


public class VitamioVideoPlayer implements OnBufferingUpdateListener,
		OnCompletionListener, OnPreparedListener,
		OnInfoListener {
	
	private static final String TAG = "VitamioVideoView";
	private VideoView mVideoView;
	private SurfaceHolder surfaceHolder;
	private SeekBar skbProgress;
	private TextView currentTime;
	private TextView totalTime;
	private RelativeLayout loadLayout;
	private long duration;
	private int recordTime;
	private VideoActivity3 v3;
	private Timer mTimer ;
	
	public VitamioVideoPlayer(VideoActivity3 v3, VideoView videoView,
			SeekBar skbProgress, TextView currentTime, TextView totalTime,
			int recordTime, RelativeLayout loadLayout,String url) {
		// TODO Auto-generated constructor stub
		this.v3 = v3;
		this.mVideoView = videoView;
		this.skbProgress = skbProgress;
		this.currentTime = currentTime;
		this.totalTime = totalTime;
		this.recordTime = recordTime;
		this.loadLayout = loadLayout;
//		String url2 = "http://www.youeclass.com:8090/2013yjssssjj2-1.flv";
		System.out.println("���ŵ�ַΪ:url="+url);
		mVideoView.setVideoURI(Uri.parse(url));
		mVideoView.requestFocus();
		mVideoView.setOnBufferingUpdateListener(this);
		mVideoView.setOnCompletionListener(this);
		mVideoView.setOnPreparedListener(this);
	}
	
	TimerTask mTimerTask = new MyTask(); 
	
	class MyTask extends TimerTask{
		@Override
		public void run() {
			try {
				if (mVideoView.isPlaying() && skbProgress.isPressed() == false) {
					handleProgress.sendEmptyMessage(0);
				}
			} catch (NullPointerException e) {
				Log.e(TAG, "��������û�д���");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	Handler handleProgress = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				try {
					int position = (int) mVideoView.getCurrentPosition();
					// int duration = mediaPlayer.getDuration();
					if (duration > 0) {
						long pos = skbProgress.getMax() * position / duration;
						skbProgress.setProgress((int) pos);
						currentTime.setText(getTime(position / 1000));
					}
					if (duration == position) {

					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		};
	};
	private String getTime(long count) {
		long m = count / 60;
		long s = count % 60;
		String ms = m < 10 ? "0" + m : m + "";
		return ms + ":" + (s < 10 ? "0" + s : s + "");
	}
	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		skbProgress.setSecondaryProgress(percent);
	}
	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		Log.e("mediaPlayer", "onCompletion �������");
		skbProgress.setProgress(0);
		currentTime.setText("00:00");
		mVideoView.seekTo(0);
		mVideoView.pause();
		mTimer.cancel();
		mTimer = null;
		v3.onCompletion(mp);
	}
	private boolean needResume;
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		switch (what) {
        case MediaPlayer.MEDIA_INFO_BUFFERING_START:
            //��ʼ���棬��ͣ����
            if (isPlaying()) {
            	mVideoView.pause();
                needResume = true;
            }
            loadLayout.setVisibility(View.VISIBLE);
            break;
        case MediaPlayer.MEDIA_INFO_BUFFERING_END:
            //������ɣ���������
            if (needResume)
            	mVideoView.start();
            loadLayout.setVisibility(View.GONE);
            break;
        case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
            //��ʾ �����ٶ�
            Log.e(TAG,"download rate:" + extra);
            break;
        }
		return true;
	};
	public void onPrepared(MediaPlayer mp) {
		Log.e(TAG,"videoview OnPrepared");
		loadLayout.setVisibility(View.GONE);
		skbProgress.setOnSeekBarChangeListener(new SeekBarChangeEvent());
		duration = mp.getDuration();
		totalTime.setText(getTime(duration / 1000));
		//skbProgress.setMax((int) duration);
		if (recordTime > 0 && recordTime< duration-10) {
			skbProgress
					.setProgress((int) (recordTime * skbProgress.getMax() / duration));
			mp.seekTo(recordTime);
		}
		mp.setPlaybackSpeed(1.0f);
		if(!mp.isPlaying())
		{
			mp.start();
			if(mTimer == null)
			{
				mTimer = new Timer();
				mTimer.schedule(mTimerTask, 0, 1000);
			}
		}
	};
	class SeekBarChangeEvent implements SeekBar.OnSeekBarChangeListener {
		int progress;

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// ԭ����(progress/seekBar.getMax())*player.mediaPlayer.getDuration()
			this.progress = (int) (progress * mVideoView.getDuration() / seekBar
					.getMax());
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// seekTo()�Ĳ����������ӰƬʱ������֣���������seekBar.getMax()��Ե�����
			mVideoView.seekTo(progress);
			// ����ʱ��
			currentTime.setText(getTime(progress / 1000));
		}
	}

	public void play()
	{
		Log.e(TAG,"videoview����");
		mVideoView.start();
		if(mTimer == null)
		{
			mTimer = new Timer();
			mTimerTask = new MyTask();
			mTimer.schedule(mTimerTask, 0, 1000);
		}
	}
	public void pause()
	{
		Log.e(TAG,"videoview��ͣ");
		if(mVideoView.isPlaying())
		{
			mVideoView.pause();
			if(mTimer!=null)
				mTimer.cancel();
			mTimer = null;
		}
	}
	public void stop()
	{
		Log.e(TAG,"videoView ֹͣ����");
		if(mVideoView != null)
			mVideoView.stopPlayback();
		if(mTimer!=null)
			mTimer.cancel();
		mTimer = null;
		//����ʱ�ļ�ɾ��
		File f = new File(Environment.getExternalStorageDirectory()+File.separator+"eschool"+"temp_"+File.separator+"current.flv");
		if(f.exists()){
			f.delete();
		}
	}
	public long getCurrentTime()
	{
		recordTime = (int) mVideoView.getCurrentPosition();
		return mVideoView.getCurrentPosition();
	}
	public void setBack() {
		long ct = mVideoView.getCurrentPosition();
		if (ct - 5000 > 0) {
			ct = ct - 5000;
			mVideoView.seekTo(ct);
			skbProgress
					.setProgress((int) (ct * skbProgress.getMax() / duration));
			currentTime.setText(getTime(ct / 1000));
		}
	}
	public void setForward() {
		long ct = mVideoView.getCurrentPosition();
		if (ct + 5000 < mVideoView.getDuration()) {
			ct = ct + 5000;
			mVideoView.seekTo(ct);
			skbProgress
					.setProgress((int) (ct * skbProgress.getMax() / duration));
			currentTime.setText(getTime(ct / 1000));
		}
	}
	public boolean isPlaying() {
		return mVideoView.isPlaying();
	}

}