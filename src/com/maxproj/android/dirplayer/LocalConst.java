package com.maxproj.android.dirplayer;

public class LocalConst {

	private LocalConst() {
		// TODO Auto-generated constructor stub
	}

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
	
	
    public static final int CMD_COPY = 1;
    public static final int CMD_MOVE = 2;
    public static final int CMD_DELETE = 3;
    public static final int CMD_FRESH = 4;
    public static final int CMD_MKDIR = 5;
    public static final int CMD_PLAY = 6;
}
