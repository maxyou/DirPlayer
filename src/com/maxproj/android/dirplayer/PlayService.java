/**
 * 
 */
package com.maxproj.android.dirplayer;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import android.view.View;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.RemoteViews;

/**
 * @author Max You
 * 
 */
public class PlayService extends Service implements MediaPlayerControl {

	private final IBinder mBinder = new LocalBinder();
	
	NotificationInforReceiver mNotificationInforReceiver;

	/**
	 * 播放列表
	 */
	// public int localPlTab = 0;
	LinkedList<LvRow>[] playListItemsService = new LinkedList[LocalConst.plCount];
	{
		for (int i = 0; i < LocalConst.plCount; i++) {
			playListItemsService[i] = new LinkedList<LvRow>();
		}
	}
	int playingType = LocalConst.NoPlay;// 播放fileList或playList？
	int playStatus = LocalConst.clear;
	int playingPlTab = 0;
	File playingFile = null;// 当前播放的文件
	static int playListItemIndex = 0; // 第一首
	int playSequence = LocalConst.play_seq_normal;
	
	int playListErrCount = 0;

	public void gotoNextMusic() {
		// updatePlayingFlag(playingType, LocalConst.clear,
		// playingFile.getPath(), playingPlTab);
		/**
		 * 找到下一首歌曲，并调用play()
		 */
		if (playListItemsService[playingPlTab].size() == 0) {
			// 没有曲目，停止播放
			return;
			
			/**
			 * 这里有个小bug
			 * 假设列表里面所有的曲目文件都是坏掉的，不能播放
			 * 那么会不断的调用gotoNextMusic()
			 */
		}

		clearLastMusicPlaying();
		
		calcNextItem();

		play(playListItemsService[playingPlTab].get(playListItemIndex)
				.getFile(), LocalConst.ListPlay);
	}

	OnCompletionListener listPlayListener = new OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mp) {
			gotoNextMusic();
		}
	};

	public void calcNextItem() {
		switch (playSequence) {
		case LocalConst.play_seq_normal:
			playListItemIndex++;
			if (playListItemIndex >= playListItemsService[playingPlTab].size()) {
				playListItemIndex = 0;
			}
			break;
		case LocalConst.play_seq_random:
			playListItemIndex = new Random()
					.nextInt(playListItemsService[playingPlTab].size());
			break;
		case LocalConst.play_seq_single:
			break;

		default:

		}

		return;
	}

	OnCompletionListener singlePlayListener = new OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mp) {
			
			/**
			 * 倒回到进度0
			 * 处于暂停状态
			 * 更新按钮为“按我播放”
			 */
			
			if (mediaPlayer != null)
				mediaPlayer.seekTo(0);

			playingType = LocalConst.SinglePlay;
			playStatus = LocalConst.paused;
//			playingFile = null;
			
			updatePlayingFlag(playingType, LocalConst.paused,
					playingFile.getPath(), playingPlTab);
			
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

		Log.d(LocalConst.DTAG, "service: onCreate()");
		for (int i = 0; i < LocalConst.plCount; i++) {
			// updatePlayList(i);
		}

		/**
		 * notification上面的按键将发送intent给service
		 * notification上面其他部分被点击后发送pendingIntent给activity
		 */
		IntentFilter mNotificationIntentFilter = new IntentFilter();
		mNotificationIntentFilter.addAction(LocalConst.NOTIFICATION_GOTO_LAST);
		mNotificationIntentFilter.addAction(LocalConst.NOTIFICATION_GOTO_NEXT);
		mNotificationIntentFilter.addAction(LocalConst.NOTIFICATION_GOTO_PLAY);
		mNotificationIntentFilter.addAction(LocalConst.NOTIFICATION_GOTO_PAUSE);
		mNotificationIntentFilter.addAction(LocalConst.NOTIFICATION_SEQ_SWITCH);

		mNotificationInforReceiver = new NotificationInforReceiver();
		/**
		 * 注意，使用LocalBroadcastManager注册的receiver只能接收本app内部的intent
		 * 但notification在本app之外 所以这里需要直接使用registerReceiver来注册
		 */
		registerReceiver(mNotificationInforReceiver, mNotificationIntentFilter);
		// Vitamio.initialize(this);
	}

	@Override
	public void onDestroy() {
		Log.d(LocalConst.DTAG, "service: onDestroy()");
		if (mediaPlayer != null) {
			mediaPlayer.release();
			playStatus = LocalConst.clear;
		}

		unregisterReceiver(mNotificationInforReceiver);
	}

	/**
	 * service通过文件获取播放列表
	 */
	public void updatePlayList(int plTab, LinkedList<LvRow> ll) {
		// getPlayListFromFile(plTab);

		playListItemsService[plTab] = ll;

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
//	private void getPlayListFromFile(int plTab) {
//		playListItemsService[plTab].clear();
//		playListItemsService[plTab] = LocalConst
//				.getListFromFile(LocalConst.playlist_file_prefix + plTab);
//		Log.d(LocalConst.DTAG, "play service getPlayListFromFile() " + plTab
//				+ " size " + playListItemsService[plTab].size());
//	}

	/**
	 * 从playlist中获取一首歌曲，并开始播放
	 */
	public void playList(int i, int plTab) {
		Log.d(LocalConst.DTAG, "playList: " + i);

		if (i < playListItemsService[plTab].size()) {

			LvRow lr = playListItemsService[plTab].get(i);
			if (lr == null)
				return;

			clearLastMusicPlaying();
			
			playListItemIndex = i; // 更新当前指针
			playingPlTab = plTab;

			play(lr.getFile(), LocalConst.ListPlay);
		}
	}

	public void mediaPlayerErrorProcess() {
		Log.d(LocalConst.DTAG, "play file null trace 10");
		
		if (playingType == LocalConst.ListPlay) {
			/**
			 * 在ListPlay状态，如果连续发现三次文件错误，则停止播放
			 * 否则会拽死cpu并死机
			 */
			playListErrCount++;
			if(playListErrCount < 3){
				gotoNextMusic();
			}
		} else if (playingType == LocalConst.SinglePlay) {
			updatePlayingFlag(playingType, LocalConst.clear,
					playingFile.getPath(), playingPlTab);
			
			//clear record
			playingType = LocalConst.NoPlay;
			playStatus = LocalConst.clear;
//			playingFile = null;
		}
	}

	public OnErrorListener mediaPlayerErrorListener = new OnErrorListener() {
		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			Log.d(LocalConst.DTAG, "mediaPlayerErrorListener()");

			mediaPlayerErrorProcess();
			return true;
		}
	};
	public void playFile(File f, int type) {
		clearLastMusicPlaying();
		play(f, type);
	}
	private void play(File f, int type) {
		Log.d(LocalConst.DTAG, "audio/video: play in service: " + f.getPath());

//		clearLastMusicPlaying();

		playingType = type;
		playingFile = f;

		mediaPlayer = new MediaPlayer();
		// mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		Log.d(LocalConst.DTAG, "audio/video: after new MediaPlayer(this)");

		try {
			if(!f.exists()){
				Log.d(LocalConst.DTAG, "play file null trace 1");
				throw new IOException();
			}
			mediaPlayer.setDataSource(getApplicationContext(), Uri.fromFile(f));
			Log.d(LocalConst.DTAG,
					"audio/video: after mediaPlayer.setDataSource()");

			if (playingType == LocalConst.ListPlay) {
				mediaPlayer.setOnCompletionListener(listPlayListener);
			} else if (playingType == LocalConst.SinglePlay) {
				mediaPlayer.setOnCompletionListener(singlePlayListener);
			}

			mediaPlayer.setOnErrorListener(mediaPlayerErrorListener);

			/**
			 * 小心。 如果是同步的prepare，那么其后start 如果是异步的prepare，那么可能需要在prepare之前设置监听？！
			 */
			mediaPlayer.prepare();
			mediaPlayer.start();
			playStatus = LocalConst.playing;

			/*
			 * mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
			 * 
			 * @Override public void onPrepared(MediaPlayer mp) { mp.start(); }
			 * }); Log.d(LocalConst.DTAG,
			 * "audio/video: after mediaPlayer.setOnPreparedListener()");
			 * mediaPlayer.prepareAsync(); Log.d(LocalConst.DTAG,
			 * "audio/video: after mediaPlayer.prepare()");
			 */

			updatePlayingFlag(playingType, LocalConst.playing,
					playingFile.getPath(), playingPlTab);
			sendNotification();

			if(mediaPlayer.isPlaying()){
				playListErrCount = 0;
			}
			
			Log.d(LocalConst.DTAG, "audio/video: after sendNotification()");
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d(LocalConst.DTAG, "play file null trace 2");
			mediaPlayerErrorProcess();
		}

	}

	public void sendNotification() {

		if (playingFile != null) {

			/**
			 * 给notification构建remoteView
			 */
			RemoteViews notifyView = new RemoteViews(this.getPackageName(),
					R.layout.notification);

			/**
			 * 设置当前播放曲目
			 */
			notifyView.setTextViewText(R.id.notification_name,
					playingFile.getName());
			notifyView.setImageViewResource(R.id.notification_icon,
					R.drawable.icon);


			/**
			 * 设置播放和暂停按钮
			 */
			if (playStatus == LocalConst.playing) {
				notifyView.setInt(R.id.notification_goto_pause, "setText",
						R.string.notification_goto_pause);
				notifyView.setOnClickPendingIntent(R.id.notification_goto_pause,
						PendingIntent.getBroadcast(this, 0, new Intent(
								LocalConst.NOTIFICATION_GOTO_PAUSE),
								PendingIntent.FLAG_UPDATE_CURRENT));			
			} else {
				notifyView.setInt(R.id.notification_goto_pause, "setText",
						R.string.notification_goto_play);
				notifyView.setOnClickPendingIntent(R.id.notification_goto_pause,
						PendingIntent.getBroadcast(this, 0, new Intent(
								LocalConst.NOTIFICATION_GOTO_PLAY),
								PendingIntent.FLAG_UPDATE_CURRENT));			
			}
			
			/**
			 * 设置下一首
			 */
			if (playingType == LocalConst.ListPlay) {
				notifyView.setOnClickPendingIntent(R.id.notification_goto_next,
						PendingIntent.getBroadcast(this, 0, new Intent(
								LocalConst.NOTIFICATION_GOTO_NEXT),
								PendingIntent.FLAG_UPDATE_CURRENT));
				
				notifyView.setViewVisibility(R.id.notification_goto_next, View.VISIBLE);
			} else {
				notifyView.setViewVisibility(R.id.notification_goto_next, View.GONE);
			}//是否要考虑playingType == LocalConst.NoPlay？

			/**
			 * 设置播放次序按钮
			 */
			if (playingType == LocalConst.ListPlay){
				switch (playSequence) {
				case LocalConst.play_seq_normal:
					notifyView.setInt(R.id.notification_seq, "setText",
							R.string.play_seq_normal);
					break;
				case LocalConst.play_seq_random:
					notifyView.setInt(R.id.notification_seq, "setText",
							R.string.play_seq_random);
					break;
				case LocalConst.play_seq_single:
					notifyView.setInt(R.id.notification_seq, "setText",
							R.string.play_seq_single);
					break;
				default:
					notifyView.setInt(R.id.notification_seq, "setText",
							R.string.play_seq_normal);
				}
				notifyView.setOnClickPendingIntent(R.id.notification_seq,
						PendingIntent.getBroadcast(this, 0, new Intent(
								LocalConst.NOTIFICATION_SEQ_SWITCH),
								PendingIntent.FLAG_UPDATE_CURRENT));

				notifyView.setViewVisibility(R.id.notification_seq, View.VISIBLE);
			}else{
				notifyView.setViewVisibility(R.id.notification_seq, View.GONE);
			}


			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
					this)

					.setSmallIcon(R.drawable.bottom)
					.setContentTitle(playingFile.getName())
					.setContentText(
							getResources().getString(
									R.string.notification_back_msg))

					/**
					 * 非常奇怪，上面三个set不能省略，否则notification就不显示
					 * 但上面三项并不显示，因为使用了remoteView及其布局
					 */

					.setContent(notifyView);

			Intent resultIntent = new Intent(this, DirPlayerActivity.class);
			PendingIntent resultPendingIntent = PendingIntent.getActivity(this,
					0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			mBuilder.setContentIntent(resultPendingIntent);
			// Sets an ID for the notification
			int mNotificationId = 001;
			// Gets an instance of the NotificationManager service
			NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			// Builds the notification and issues it.
			mNotifyMgr.notify(mNotificationId, mBuilder.build());

			// Log.d(LocalConst.TMP, "end of sendNotification()");
			
			/**
			* send to widget
			*/
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(LocalConst.app);
			ComponentName thisWidget = new ComponentName(LocalConst.app, 
					com.maxproj.android.dirplayer.ControllerAppWidgetProvider.class);
			int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
			final int N = appWidgetIds.length;
			
			Intent intent = new Intent(LocalConst.app, DirPlayerActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(LocalConst.app, 0,
					intent, 0);
			notifyView.setOnClickPendingIntent(R.id.notification_icon, pendingIntent);

			
			for(int i = 0; i < N; i++){
				int appWidgetId = appWidgetIds[i]; 
				appWidgetManager.updateAppWidget(appWidgetId, notifyView);
			}
		}
	}

	public void cancelNotification() {
		// Gets an instance of the NotificationManager service
		NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// Builds the notification and issues it.
		mNotifyMgr.cancelAll();
	}

	private class NotificationInforReceiver extends BroadcastReceiver {
		private NotificationInforReceiver() { // Prevents instantiation
		}

		// Called when the BroadcastReceiver gets an Intent it's registered to
		// receive
		@Override
		public void onReceive(Context context, Intent intent) {

			Log.d(LocalConst.TMP, "service: onReceive()");

			/**
			 * 接收如下信息 播放、暂停、上一首、下一首
			 */
			if (mediaPlayer == null)
				return;

			String action = intent.getAction();

			if (LocalConst.NOTIFICATION_GOTO_LAST.equals(action)) {
				Log.d(LocalConst.TMP, "Pressed last");

			} else if (LocalConst.NOTIFICATION_GOTO_NEXT.equals(action)) {
				Log.d(LocalConst.TMP, "Pressed next");
				mediaPlayer.seekTo(mediaPlayer.getDuration());
			} else if (LocalConst.NOTIFICATION_GOTO_PLAY.equals(action)) {
				Log.d(LocalConst.TMP, "Pressed play");
				mediaPlayer.start();
				playStatus = LocalConst.playing;
				sendNotification();
				pleaseUpdatePlayingFlag();
			} else if (LocalConst.NOTIFICATION_GOTO_PAUSE.equals(action)) {
				Log.d(LocalConst.TMP, "Pressed pause");
				mediaPlayer.pause();
				playStatus = LocalConst.paused;
				sendNotification();
				pleaseUpdatePlayingFlag();
			} else if (LocalConst.NOTIFICATION_SEQ_SWITCH.equals(action)) {
				Log.d(LocalConst.TMP, "play sequence switch");
				playSeqSwitch();
				sendNotification();
			}

		}
	}

	public void playSeqSwitch() {
		playSequence++;
		if (playSequence > LocalConst.play_seq_single) {
			playSequence = LocalConst.play_seq_normal;
		}
	}

	public int getPlaySeq(){
		return playSequence;
	}
	@Override
	public void start() {
		// TODO Auto-generated method stub
		if (mediaPlayer != null){
			mediaPlayer.start();
		}
		playStatus = LocalConst.playing;
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		if (mediaPlayer != null){
			mediaPlayer.pause();
		}
		playStatus = LocalConst.paused;
	}

	public void stop() {
		// TODO Auto-generated method stub
		clearLastMusicPlaying();
		playStatus = LocalConst.stopped;
	}
	public void pleaseUpdatePlayingFlag(){
		if(playingFile != null){
			updatePlayingFlag(playingType, playStatus, playingFile.getPath(),
				playingPlTab);
		}else{
			updatePlayingFlag(playingType, playStatus, null,
					playingPlTab);
		}
		
	}
	/**
	 * 这里不太好 只需要发送： 路径是什么 点亮什么 熄灭什么
	 */
	public void updatePlayingFlag(int playType, int playing, String playPath,
			int playPlTab) {
		/**
		 * 需要发送item位置，如果有多个列表的话，需要发送列表编号
		 */
		Intent localIntent = new Intent(LocalConst.BROADCAST_SERVICE_STATUS)
				// Puts the status into the Intent
				.putExtra(LocalConst.PLAY_TYPE, playType)
				.putExtra(LocalConst.PLAY_STATUS, playing)
				.putExtra(LocalConst.PLAY_PATH, playPath)
				.putExtra(LocalConst.PLAY_LIST_INDEX, playListItemIndex)
				.putExtra(LocalConst.PLAY_PL_TAB, playPlTab);
		
		// Broadcasts the Intent to receivers in this app.
		LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
	}

	public void quitMusicPlaying(){
		clearLastMusicPlaying();
		
		//clear record
		playingType = LocalConst.NoPlay;
		playStatus = LocalConst.clear;
//		playingFile = null;
	}
	private void clearLastMusicPlaying() {
		// TODO Auto-generated method stub
		Log.d(LocalConst.DTAG, "audio/video: begin clear music playing");
		if (mediaPlayer != null) {
			if (playingFile != null) {
				updatePlayingFlag(playingType, LocalConst.clear,
						playingFile.getPath(), playingPlTab);
			}
			cancelNotification();

			try {
				mediaPlayer.stop();
				mediaPlayer.release();
			} catch (Exception e) {
				e.printStackTrace();
			}
			mediaPlayer = null;
			playStatus = LocalConst.clear;
		}
		Log.d(LocalConst.DTAG, "audio/video: after clear music playing");
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

	public int getProgressPercentage(){
		int duration = getDuration();
		int current = getCurrentPosition(); 
		
		if(duration == 0){
			return 0;
		}else if(current <= duration){
			return (current * 100)/duration;
		}else{
			return 0;
		}
		
	}
	
	@Override
	public void seekTo(int pos) {
		// TODO Auto-generated method stub
		if (mediaPlayer != null)
			mediaPlayer.seekTo(pos);
	}
	
	public void seekToPercentage(int perc) {
		int duration = getDuration();
		
		if (mediaPlayer != null)
			mediaPlayer.seekTo((perc * duration)/100);
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
