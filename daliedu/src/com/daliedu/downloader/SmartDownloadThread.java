package com.daliedu.downloader;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

import com.daliedu.entity.DownloadItem;
import com.daliedu.util.Constant;


public class SmartDownloadThread extends Thread {
	private static final String TAG = "SmartDownloadThread";
	private File saveFile;
	private URL downUrl;
	/* *���ؿ�ʼλ��  */
	private int threadId = -1;	
	private int downLength;
	private boolean finish = false;
	private SmartFileDownloader downloader;
	private DownloadItem l;
	
	public SmartDownloadThread(SmartFileDownloader downloader,URL downUrl,DownloadItem l,File saveFile)
	{
		this.downloader = downloader;
		this.downUrl = downUrl;
		this.l = l;
		this.threadId = l.getThreadId();
		this.downLength = l.getCompleteSize();
		this.saveFile = saveFile;
	}
	@Override
	public void run() {
		if((l.getEndPos()-l.getStartPos())>l.getCompleteSize())
		{
			try {
			HttpURLConnection http = (HttpURLConnection) downUrl.openConnection();
			http.setConnectTimeout(5 * 1000);
			http.setRequestMethod("GET");
			http.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
			http.setRequestProperty("Accept-Language", "zh-CN");
			http.setRequestProperty("Referer", downUrl.toString()); 
			http.setRequestProperty("Charset", "UTF-8");
			//Range�����д
			http.setRequestProperty("RANGE", "bytes=" + (l.getStartPos()+l.getCompleteSize())+ "-"+ l.getEndPos());//���û�ȡʵ�����ݵķ�Χ
			http.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
			http.setRequestProperty("Connection", "Keep-Alive");
			InputStream inStream = http.getInputStream();
			byte[] buffer = new byte[1024];
			int offset = 0;
			print("Thread " + this.threadId + " start download from position "+ (l.getStartPos()+l.getCompleteSize()));
			RandomAccessFile threadfile = new RandomAccessFile(this.saveFile, "rwd");
			threadfile.seek((l.getStartPos()+l.getCompleteSize()) + Constant.OFFSET.getBytes().length);		//�޸�λ��+Constant.OFFSET.getBytes().length
			while (SmartFileDownloader.flagMap.get(downUrl.toString())&&(offset = inStream.read(buffer, 0, 1024)) != -1) {
				threadfile.write(buffer, 0, offset);
				downLength += offset;
				l.setCompleteSize(downLength);
//				if(downLength%5120==0)
//				{
//					System.out.println("Thread "+this.getId()+"�������ݿ�");
//					downloader.update(l);	//�������ݿ�
//				}
				downloader.append(offset);
			}
			downloader.update(l);//�˳���ѭ���ٴ�һ��
			threadfile.close();
			inStream.close();
			if(downLength==(l.getEndPos()-l.getStartPos()+1))
			{
				print("Thread " + this.threadId + " download finish");
				this.finish = true;
			}
		} catch (Exception e) {
			this.downLength = -1;
			print("Thread "+ this.threadId+ ":"+ e);
			e.printStackTrace();
		}
		}	
		print("Thread "+this.threadId+"ֹͣ������");
	}
	private static void print(String msg){
		Log.i(TAG, msg);
	}
	/**
	 * �����Ƿ����
	 * @return
	 */
	public boolean isFinish() {
		return finish;
	}
	/**
	 * �Ѿ����ص����ݴ�С
	 * @return �������ֵΪ-1,��������ʧ��
	 */
	public long getDownLength() {
		return downLength;
	}
}