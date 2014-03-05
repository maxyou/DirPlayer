package com.maxproj.android.dirplayer;

import java.io.File;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;

public class LocalConst {

	private LocalConst() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 系统相关
	 */
	public static Context app = null;
	public static Context dirPlayerActivity = null;
	
	public static final String pathRoot = Environment
			.getExternalStorageDirectory().getPath();

	/**
	 * 保存listview的adapter
	 */
	public static Bundle adapters;
	public static final int tabCount = 2;
	public static MyArrayAdapter[] myArrayAdapter_fragmentList = new MyArrayAdapter[tabCount];
	public static String[] currentPath_fragmentList = new String[tabCount];
	
	
	/**
	 * Log到文件
	 */
	public static final String logFileName = "dirplayer_log";
	public static File logFile = null;

	
	/**
	 * Log switch
	 */
	public static final String ACTIVITY_LIFE = "activity_life";
	public static final String FRAGMENT_LIFE = "fragment_life";
	public static final String LIFECYCLE = "lifecycle";
	public static final String FL = "fl";
	public static final String BM = "bm";
	public static final String PL = "pl";
	public static final String DTAG = "dirplayer";
	public static final String TMP = "tmp";
	
	/**
	 * 系统状态
	 */
    public static final int STATE_FILE       = 0;
    public static final int STATE_VIDEO       = 1;
    public static final int STATE_MUSIC       = 2;

	
	/**
	 * 音乐播放状态
	 */
//    public static final int NoPlay       = 0;
    public static final int SinglePlay       = 1;
    public static final int ListPlay      = 2;
    
    // status
    public static final int clear       = 0;
    public static final int playing       = 1;
    public static final int paused       = 2;
    public static final int stopped      = 3;
    
    // notification operation
    public static final int op_play       = 1;
    public static final int op_pause       = 2;
    public static final int op_stop      = 3;
    public static final int op_last      = 4;
    public static final int op_next      = 5;
    
    
	// 过滤器
	public static final String BROADCAST_ACTION = "com.maxproj.android.dirplayer.BROADCAST_ACTION";
	public static final String NOTIFICATION_ACTION = "com.maxproj.android.dirplayer.NOTIFICATION_ACTION";
	public static final String NOTIFICATION_OP = "OP";	
	public static final String NOTIFICATION_GOTO_LAST = "com.maxproj.android.dirplayer.NOTIFICATION_LAST";
	public static final String NOTIFICATION_GOTO_PLAY = "com.maxproj.android.dirplayer.NOTIFICATION_PLAY";
	public static final String NOTIFICATION_GOTO_PAUSE = "com.maxproj.android.dirplayer.NOTIFICATION_PAUSE";
	public static final String NOTIFICATION_GOTO_NEXT = "com.maxproj.android.dirplayer.NOTIFICATION_NEXT";
	// 消息类别
	public static final String 	PLAY_TYPE = "com.maxproj.android.dirplayer.PLAY_TYPE";
	// 消息动作
	public static final String 	PLAY_STATUS = "com.maxproj.android.dirplayer.PLAY_STATUS";
	// 列表播放信息
	public static final String PLAYLIST_INDEX = "com.maxproj.android.dirplayer.PLAYLIST_INDEX";
	public static final String PLAYLIST_ITEM_INDEX = "com.maxproj.android.dirplayer.PLAYLIST_ITEM_INDEX";
	public static final String PLAYLIST_PATH = "com.maxproj.android.dirplayer.PLAYLIST_PATH";
	// 文件播放信息
	public static final String FILELIST_PATH = "com.maxproj.android.dirplayer.FILELIST_PATH";
	
	/**
	 * 文件命令
	 */
    public static final int CMD_PLAY = 1;
    public static final int CMD_COPY = 2;
    public static final int CMD_MOVE = 3;
    public static final int CMD_DELETE = 4;
    public static final int CMD_FRESH = 5;
    public static final int CMD_MKDIR = 6;
    public static final int CMD_RENAME = 7;
    
    
    public static String byteConvert(long bytes){
    	return humanReadableByteCount(bytes, true);
    }
    //copy from http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
