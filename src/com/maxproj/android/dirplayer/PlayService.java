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
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
//import io.vov.vitamio.MediaPlayer;
//import io.vov.vitamio.MediaPlayer.OnCompletionListener;
//import io.vov.vitamio.MediaPlayer.OnPreparedListener;
//import io.vov.vitamio.Vitamio;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.MediaController.MediaPlayerControl;

/**
 * @author Max You
 * 
 */
public class PlayService extends Service implements MediaPlayerControl {
	String DTAG = "DirPlayer";

	private final IBinder mBinder = new LocalBinder();

	/**
	 * 播放列表
	 */
	LinkedList<LvRow> playListItemsService = new LinkedList<LvRow>();
	File playingFile;//当前播放的文件
	int playListItemIndex = 0; // 第一首
	

	public void lightenPlayList(){
		/**
		 * 需要发送item位置，如果有多个列表的话，需要发送列表编号
		 */
		Log.d(DTAG, "audio/video: path in playlist " + playingFile.getPath());
				Intent localIntent = new Intent(
						LocalConst.BROADCAST_ACTION)
				// Puts the status into the Intent
						.putExtra(LocalConst.PLAY_STATUS, LocalConst.playing)
						.putExtra(LocalConst.PLAY_TYPE, LocalConst.ListPlay)
						.putExtra(LocalConst.PLAYLIST_PATH, playingFile.getPath())
						.putExtra(LocalConst.PLAYLIST_INDEX, 0)//以后可能多列表
						.putExtra(LocalConst.PLAYLIST_ITEM_INDEX, playListItemIndex);
				// Broadcasts the Intent to receivers in this app.
				LocalBroadcastManager.getInstance(this).sendBroadcast(
						localIntent);
	}
	public void unLightenPlayList(){
				Intent localIntent = new Intent(
						LocalConst.BROADCAST_ACTION)
				// Puts the status into the Intent
						.putExtra(LocalConst.PLAY_STATUS, LocalConst.clear)
						.putExtra(LocalConst.PLAY_TYPE, LocalConst.ListPlay)
						.putExtra(LocalConst.PLAYLIST_PATH, playingFile.getPath())
						.putExtra(LocalConst.PLAYLIST_INDEX, 0)//以后可能多列表
						.putExtra(LocalConst.PLAYLIST_ITEM_INDEX, playListItemIndex);
				// Broadcasts the Intent to receivers in this app.
				LocalBroadcastManager.getInstance(this).sendBroadcast(
						localIntent);
	}
	public void lightenFileList(){
		/**
		 * 需要发送文件path
		 */
		Intent localIntent = new Intent(
				LocalConst.BROADCAST_ACTION)
		// Puts the status into the Intent
				.putExtra(LocalConst.PLAY_STATUS, LocalConst.playing)
				.putExtra(LocalConst.PLAY_TYPE, LocalConst.SinglePlay)
				.putExtra(LocalConst.FILELIST_PATH, playingFile.getPath());
		// Broadcasts the Intent to receivers in this app.
		LocalBroadcastManager.getInstance(this).sendBroadcast(
				localIntent);
	}
	public void unLightenFileList(){
		Intent localIntent = new Intent(
				LocalConst.BROADCAST_ACTION)
		// Puts the status into the Intent
				.putExtra(LocalConst.PLAY_STATUS, LocalConst.clear)
				.putExtra(LocalConst.PLAY_TYPE, LocalConst.SinglePlay)
				.putExtra(LocalConst.FILELIST_PATH, playingFile.getPath());
		// Broadcasts the Intent to receivers in this app.
		LocalBroadcastManager.getInstance(this).sendBroadcast(
				localIntent);
	}
	
	OnCompletionListener listPlayListener = new OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mp) {
			unLightenPlayList();
			/**
			 * 找到下一首歌曲，并调用play()
			 */
			if (playListItemsService.size() == 0) {
				// 没有曲目，停止播放
				return;
			}

			playListItemIndex++;
			if (playListItemIndex >= playListItemsService.size()) {
				playListItemIndex = 0;
			}
			play(playListItemsService.get(playListItemIndex).getFile(), LocalConst.ListPlay);
		}
	};
	
	OnCompletionListener singlePlayListener = new OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mp) {
			unLightenFileList();
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
			// Return this instance of LocalService so clients can call public
			// methods
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

		Log.d(DTAG, "service: onCreate()");
		updatePlayList();
		
		// Vitamio.initialize(this);
	}

	@Override
	public void onDestroy() {
		Log.d(DTAG, "service: onDestroy()");
		if (mediaPlayer != null)
			mediaPlayer.release();
	}

	/**
	 * service通过文件获取播放列表
	 */
	public void updatePlayList() {
		getPlayList();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}

	/**
	 * 拷贝自DirPlayerActivity.java，以后重构合并 应该直接new一个list，读取数据后返回这个list
	 */
	private void getPlayList() {
		Log.d(DTAG, "getPlayList()");
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
						URLConnection.getFileNameMap().getContentTypeFor(
								f.getName()),false);
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
		Log.d(DTAG, "playList: " + i);

		LvRow lr = playListItemsService.get(i);
		if (lr == null)
			return;

		playListItemIndex = i; // 更新当前指针

		play(lr.getFile(), LocalConst.ListPlay);
	}

	public void play(File f, int type) {
		Log.d(DTAG, "audio/video: play in service: " + f.getPath());
		
		clearMusicPlaying();
		
		playingFile = f;
		
		mediaPlayer = new MediaPlayer();
		//mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		Log.d(DTAG, "audio/video: after new MediaPlayer(this)");

		try {
			mediaPlayer.setDataSource(getApplicationContext(), Uri.fromFile(f));
			Log.d(DTAG, "audio/video: after mediaPlayer.setDataSource()");
			
			/**
			 * 小心。
			 * 如果是同步的prepare，那么其后start
			 * 如果是异步的prepare，那么可能需要在prepare之前设置监听？！
			 */
			
			mediaPlayer.prepare();
			mediaPlayer.start();
			
			/*
			mediaPlayer.setOnPreparedListener(new OnPreparedListener() { 
		        @Override
		        public void onPrepared(MediaPlayer mp) {
		            mp.start();
		        }
		    });
			Log.d(DTAG, "audio/video: after mediaPlayer.setOnPreparedListener()");
			mediaPlayer.prepareAsync();
			Log.d(DTAG, "audio/video: after mediaPlayer.prepare()");
			*/
			if(type == LocalConst.ListPlay){
				mediaPlayer.setOnCompletionListener(listPlayListener);
				lightenPlayList();
				// 发送到notification
				sendNotification();
			}else if(type == LocalConst.SinglePlay){
				mediaPlayer.setOnCompletionListener(singlePlayListener);
				lightenFileList();
			}
			
			Log.d(DTAG, "audio/video: after sendNotification()");
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void sendNotification() {
		String songName;
		if (playListItemsService != null) {
			LvRow lr = playListItemsService.get(playListItemIndex);
			if (lr != null) {
				NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
						.setSmallIcon(R.drawable.bottom)
						.setContentTitle(lr.getName())
						.setContentText("click to back!");
				
				Intent resultIntent = new Intent(this, DirPlayerActivity.class);
				PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
						resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

				mBuilder.setContentIntent(resultPendingIntent);
				// Sets an ID for the notification
				int mNotificationId = 001;
				// Gets an instance of the NotificationManager service
				NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				// Builds the notification and issues it.
				mNotifyMgr.notify(mNotificationId, mBuilder.build());
			}
		}

	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		if (mediaPlayer != null)
			mediaPlayer.start();
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		if (mediaPlayer != null)
			mediaPlayer.pause();
	}

	public void stop() {
		// TODO Auto-generated method stub
		if (mediaPlayer != null)
			mediaPlayer.stop();
	}

	public void clearMusicPlaying() {
		// TODO Auto-generated method stub
		Log.d(DTAG, "audio/video: begin clear music playing");
		if (mediaPlayer != null){
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
		}
		Log.d(DTAG, "audio/video: after clear music playing");
	}
	
	@Override
	public int getDuration() {
		// TODO Auto-generated method stub
		if (mediaPlayer != null)
			return (int) mediaPlayer.getDuration();
		else
			return 0;
	}

	@Override
	public int getCurrentPosition() {
		// TODO Auto-generated method stub
		if (mediaPlayer != null)
			return (int) mediaPlayer.getCurrentPosition();
		else
			return 0;
	}

	@Override
	public void seekTo(int pos) {
		// TODO Auto-generated method stub
		if (mediaPlayer != null)
			mediaPlayer.seekTo(pos);
	}

	@Override
	public boolean isPlaying() {
		// TODO Auto-generated method stub
		if (mediaPlayer != null)
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

}
