package com.maxproj.android.dirplayer;

import java.io.File;
import java.net.URLConnection;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.provider.BaseColumns;

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
	public static final String time_format = "yyyy-MM-dd";
	
	/**
	 * tab相关
	 */
	public static final int TAB_LEFT = 0;
	public static final int TAB_RIGHT = 1;
	public static final int TAB_BOOKMARK = 2;
	public static final int TAB_PLAYLIST = 3;
	
	
	/**
	 * 文件及文件夹类型
	 */
	public static final int TYPE_PARAENT = 0;
	public static final int TYPE_DIR = 1;
	public static final int TYPE_FILE = 2;

	public static final String PARAENT_NAME = "..";
	
	/**
	 * 数据库相关
	 */
	public static final String DB_NAME_BOOKMARK = "bookmark";
	public static final String DB_NAME_PLAYLIST_1 = "playlist_1";
	public static final String DB_NAME_PLAYLIST_2 = "playlist_2";
	public static final String DB_NAME_PLAYLIST_3 = "playlist_3";
	public static final String DB_NAME_PLAYLIST_4 = "playlist_4";
	public static final String DB_NAME_PLAYLIST_5 = "playlist_5";
	public static final String DB_NAME_PLAYLIST_TMP = "playlist_tmp";
	
	public static int dbSwitch = 1;//0:file, 1:database
	
	/**
	 * 保存list的文件
	 */
	public static final String bookmark_file = "bookmark";
	public static final String playlist_file_prefix = "playlist";
	
	/**
	 * 保存listview的adapter
	 */
	public static Bundle adapters;
	public static final int tabCount = 2;
	public static MyArrayAdapter[] myArrayAdapter_fragmentList = new MyArrayAdapter[tabCount];
	public static String[] currentPath_fragmentList = new String[tabCount];


	/**
	 * playlist tab相关
	 */
	public static final int[][] plViewId = {
		{R.id.fragment_playlist_path_1, R.id.fragment_playlist_1, R.id.current_play_1, R.id.pl_radio_1},
		{R.id.fragment_playlist_path_2, R.id.fragment_playlist_2, R.id.current_play_2, R.id.pl_radio_2},
		{R.id.fragment_playlist_path_3, R.id.fragment_playlist_3, R.id.current_play_3, R.id.pl_radio_3},
		{R.id.fragment_playlist_path_4, R.id.fragment_playlist_4, R.id.current_play_4, R.id.pl_radio_4},
		{R.id.fragment_playlist_path_5, R.id.fragment_playlist_5, R.id.current_play_5, R.id.pl_radio_5}
	};
	public static final int plCount = 5; //如果这个数字改变，fragment_playlist.xml里面的view数目必须同时改变
	public static final String PL_TAB_IN_PREF = "current_pl_tab";

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
    
    public static final int play_seq_normal = 0;
    public static final int play_seq_random = 1;
    public static final int play_seq_single = 2;

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
    
    
    /**
     * intent部分
     * 
     */
    
	
	public static final String REQUEST_FRAG_FILE_LIST_UPDATE = "com.maxproj.android.dirplayer.REQUEST_FRAG_FILE_LIST_UPDATE";
	public static final String REQUEST_FRAG_BOOKMARK_LIST_UPDATE = "com.maxproj.android.dirplayer.REQUEST_FRAG_BOOKMARK_LIST_UPDATE";
	public static final String REQUEST_FRAG_PLAY_LIST_UPDATE = "com.maxproj.android.dirplayer.REQUEST_FRAG_PLAY_LIST_UPDATE";
	
	
	/**
	 * notification发送给service的intent
	 */
	public static final String NOTIFICATION_ACTION = "com.maxproj.android.dirplayer.NOTIFICATION_ACTION";
	public static final String NOTIFICATION_OP = "OP";	
	public static final String NOTIFICATION_GOTO_LAST = "com.maxproj.android.dirplayer.NOTIFICATION_LAST";
	public static final String NOTIFICATION_GOTO_PLAY = "com.maxproj.android.dirplayer.NOTIFICATION_PLAY";
	public static final String NOTIFICATION_GOTO_PAUSE = "com.maxproj.android.dirplayer.NOTIFICATION_PAUSE";
	public static final String NOTIFICATION_GOTO_NEXT = "com.maxproj.android.dirplayer.NOTIFICATION_NEXT";
	
	public static final String NOTIFICATION_SEQ_SWITCH = "com.maxproj.android.dirplayer.NOTIFICATION_SEQ_SWITCH";
	
	public static final String BOTTOM_STATUS_TEXT = "com.maxproj.android.dirplayer.BOTTOM_STATUS_TEXT";
		//extra
		public static final String 	STATUS_TEXT = "STATUS_TEXT";
	
	/**
	 * Service发送的intent
	 */
	//Action
	public static final String BROADCAST_SERVICE_STATUS = "com.maxproj.android.dirplayer.BROADCAST_SERVICE_STATUS";
		//extra
		public static final String PLAY_TYPE = "PLAY_TYPE";
		public static final String PLAY_STATUS = "PLAY_STATUS";
		public static final String PLAY_PATH = "PLAY_PATH";
		public static final String PLAY_PL_TAB = "PLAY_PL_TAB";
	
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
    
    
    
    /**
     *	功能函数 
     */
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

    public static String getMimeByFileName(String name){
    	String mime = URLConnection.getFileNameMap().getContentTypeFor(name);
    	
    	if(mime == null){
    		/**
    		 * 补充mime和后缀名的对应表
    		 */
//    		String ext = getFileExt(name);
//    		if(ext != null){
//    			ext = ext.toLowerCase();
//	    		if(ext.equals("dat")){
//	    			mime = "video/";
//	    		}else if(ext.equals("f4v")){
//	    			mime = "video/";
//	    		}else if(ext.equals("flv")){
//	    			mime = "video/";
//	    		}else if(ext.equals("ape")){
//	    			mime = "audio/";
//	    		}else if(ext.equals("rmvb")){
//	    			mime = "video/";
//	    		}
//    		}
    	}
    	
    	return mime;
    }

    public static String getFileExt(String name) {

//        String separator = System.getProperty("file.separator");        
//        int indexOfLastSeparator = name.lastIndexOf(separator);
//        String filename = name.substring(indexOfLastSeparator + 1);
        
        int extensionIndex = name.lastIndexOf(".");
        if (extensionIndex == -1){
        	return null;
        }else{
        	return name.substring(extensionIndex + 1);        
        }
    }
    
    
}
