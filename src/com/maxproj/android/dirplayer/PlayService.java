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
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
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
		try {
			playSingle(lr.getFile());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void playSingle(File f) throws IOException{
		Log.d(DTAG,"play in service: " + f.getPath());
		if (mediaPlayer != null){
			//mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
			Log.d(DTAG,"play in service: clear last");
		}		
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		Log.d(DTAG,"play in service 0");
		mediaPlayer.setDataSource(getApplicationContext(), Uri.fromFile(f));
		Log.d(DTAG,"play in service 1");
		mediaPlayer.prepare();			
		Log.d(DTAG,"play in service 2");
		//mediaPlayer.prepareAsync();										
		mediaPlayer.start();
		Log.d(DTAG,"play in service 3");
		
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
    
}
