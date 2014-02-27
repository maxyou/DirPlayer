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
    
    public static final int clear       = 0;
    public static final int playing       = 1;
    public static final int pause       = 2;
    public static final int stop      = 3;
    
    
	// 过滤器
	public static final String BROADCAST_ACTION = "com.maxproj.android.dirplayer.BROADCAST";
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
    public static final int CMD_COPY = 1;
    public static final int CMD_MOVE = 2;
    public static final int CMD_DELETE = 3;
    public static final int CMD_FRESH = 4;
    public static final int CMD_MKDIR = 5;
    public static final int CMD_PLAY = 6;
}
