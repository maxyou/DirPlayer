/**
 * 
 */
package com.maxproj.android.dirplayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Random;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.MediaController.MediaPlayerControl;

/**
 * @author Max You
 *
 */
public class PlayService extends Service implements MediaPlayerControl  {
	String DTAG = "DirPlayer";
	
    private final IBinder mBinder = new LocalBinder();

    /**
     * 播放列表 
     */    
	LinkedList<LvRow> playListItemsService = new LinkedList<LvRow>();
	int currentPlay = 0; //第一首
	OnCompletionListener listener = new OnCompletionListener() {		
		@Override
		public void onCompletion(MediaPlayer mp) {
			// TODO Auto-generated method stub
			/**
			 * 找到下一首歌曲，并调用play()
			 */
			if(playListItemsService.size() == 0){
				//没有曲目，停止播放
				return;
			}
			
			currentPlay++;
			if (currentPlay >= playListItemsService.size()){
				currentPlay = 0;
			}
			play(playListItemsService.get(currentPlay).getFile(), listener);
		}
	};

    /**
     * 音频播放
     */
    static MediaPlayer mediaPlayer = null;
    
    /**
	 * 不知道这个是否可以去掉 
	 */
	public PlayService() {
		// TODO Auto-generated constructor stub
	}
	
	public class LocalBinder extends Binder {
    	PlayService getService() {
            // Return this instance of LocalService so clients can call public methods
            return PlayService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		Log.d(DTAG,"service: onCreate()");
		updatePlayList();
	}
	@Override
	public void onDestroy() {
		Log.d(DTAG,"service: onDestroy()");
		if (mediaPlayer != null) 
			mediaPlayer.release();
	}
	
	/**
	 * service通过文件获取播放列表
	 */
	public void updatePlayList(){
		getPlayList();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}


	/**
	 * 拷贝自DirPlayerActivity.java，以后重构合并
	 * 应该直接new一个list，读取数据后返回这个list
	 */
	private void getPlayList() {
		Log.d(DTAG,"getPlayList()");
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
		String line;
		File playlist = new File(getFilesDir(),
				getString(R.string.playlist_file));

		playListItemsService.clear();
		try {
			BufferedReader br = new BufferedReader(new FileReader(playlist));

			while ((line = br.readLine()) != null) {
				File f = new File(line);
				LvRow lr = new LvRow("" + f.getName(), "" + f.length(), ""
						+ sdf.format(f.lastModified()), f, false, 2, 
						URLConnection.getFileNameMap().getContentTypeFor(f.getName()));
				playListItemsService.add(lr);
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 从playlist中获取一首歌曲，并开始播放
	 */
	public void playList(int i) {
		Log.d(DTAG,"playList: " + i);
		
		LvRow lr = playListItemsService.get(i);
		if (lr == null)
			return;
		
		currentPlay = i; //更新当前指针
		
		play(lr.getFile(), listener);
	}
	

	public void play(File f, OnCompletionListener listener) {
		Log.d(DTAG,"play in service: " + f.getPath());
		String mime = URLConnection.getFileNameMap().getContentTypeFor(f.getName());
		if (!mime.startsWith("audio/"))				
		{
			if(listener == null){
				//如果单曲播放，不产生任何影响
				return;
			}else{
				//如果是list播放，跳到下一曲
				//假定list中全是非音乐文件，这里会导致嵌套死循环！！
				//简单的处理方法是，禁止非音乐文件加入这个list
				listener.onCompletion(null);
				return;
			}
		}
		if (mediaPlayer != null){
			//mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
			Log.d(DTAG,"play in service: clear last");
		}		
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		Log.d(DTAG,"play in service 0");
		
		try {
			mediaPlayer.setDataSource(getApplicationContext(), Uri.fromFile(f));
			Log.d(DTAG,"play in service 1");
			mediaPlayer.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
		Log.d(DTAG,"play in service 2");
		//mediaPlayer.prepareAsync();										
		mediaPlayer.start();
		Log.d(DTAG,"play in service 3");
		
		if (listener != null){
			/**
			 * 如果listener监听器不为空，说明是list播放
			 * 此时将此监听器挂入
			 * 同时对外发送播放信息
			 */
			mediaPlayer.setOnCompletionListener(listener);
			broadcastInfor();
		}
		
		
//		String songName;
//		// assign the song name to songName
//		PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
//		                new Intent(getApplicationContext(), DirPlayerActivity.class),
//		                PendingIntent.FLAG_UPDATE_CURRENT);
//		Notification notification = new Notification();
//		notification.tickerText = text;
//		notification.icon = R.drawable.play0;
//		notification.flags |= Notification.FLAG_ONGOING_EVENT;
//		notification.setEventInfo(getApplicationContext(), "MusicPlayerSample",
//		                "Playing: " + songName, pi);
//		startForeground(NOTIFICATION_ID, notification);
	}


	@Override
	public void start() {
		// TODO Auto-generated method stub
		if(mediaPlayer != null)
			mediaPlayer.start();
	}


	@Override
	public void pause() {
		// TODO Auto-generated method stub
		if(mediaPlayer != null)
			mediaPlayer.pause();
	}


	@Override
	public int getDuration() {
		// TODO Auto-generated method stub
		if(mediaPlayer != null)			
			return mediaPlayer.getDuration();
		else
			return 0;
	}


	@Override
	public int getCurrentPosition() {
		// TODO Auto-generated method stub
		if(mediaPlayer != null)
			return mediaPlayer.getCurrentPosition();
		else
			return 0;
	}


	@Override
	public void seekTo(int pos) {
		// TODO Auto-generated method stub
		if(mediaPlayer != null)
			mediaPlayer.seekTo(pos);
	}


	@Override
	public boolean isPlaying() {
		// TODO Auto-generated method stub
		if(mediaPlayer != null)
			return mediaPlayer.isPlaying();
		else
			return false;
	}


	@Override
	public boolean canPause() {
		// TODO Auto-generated method stub
		return true;
	}


	@Override
	public boolean canSeekBackward() {
		// TODO Auto-generated method stub
		return true;
	}


	@Override
	public boolean canSeekForward() {
		// TODO Auto-generated method stub
		return true;
	}


	@Override
	public int getAudioSessionId() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public int getBufferPercentage() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public final class ServiceConstants {
	    // Defines a custom Intent action
	    public static final String BROADCAST_ACTION =
	        "com.maxproj.android.dirplayer.BROADCAST";
	    // Defines the key for the status "extra" in an Intent
	    public static final String EXTENDED_DATA_STATUS =
	        "com.maxproj.android.dirplayer.STATUS";
	}
	
	/**
	 * 通过广播发送当前播放曲目的路径和名称
	 */
	public void broadcastInfor(){
		if(playListItemsService != null){
			LvRow lr = playListItemsService.get(currentPlay);
			if (lr != null){
			
				Intent localIntent =
		            new Intent(ServiceConstants.BROADCAST_ACTION)
		            // Puts the status into the Intent
		            .putExtra(ServiceConstants.EXTENDED_DATA_STATUS, 
		            		lr.getFile().getPath());
				// Broadcasts the Intent to receivers in this app.
				LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
			}
		}
	}
	
	/**
	 * 通过notification发送消息，并设置service为Foreground
	 */
	
}
