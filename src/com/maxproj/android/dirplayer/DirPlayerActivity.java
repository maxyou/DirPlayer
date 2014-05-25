/**
 * app name
 * 		DirPlayer
 * author
 * 		Max You 游红宇
 * 		hyyou@foxmail.com
 * 		dev@dirplayer.com
 * 功能简介
 * 		文件管理
 * 			文件/文件夹的拷贝、移动、改名、创建、删除、收藏
 * 		音频播放
 * 			在文件夹浏览窗口单曲播放
 * 			在播放列表连续播放
 * 			从底往上滑出控制器，从顶往下滑出控制面板
 * 		视频播放
 * 			在文件浏览窗口点击视频即可播放
 * 			单击视频调出控制面板
 * 			双击放大和缩小，下滑关闭视频播放
 * 主要部分完成时间
 * 		2013年10月~2014年3月
 */
package com.maxproj.android.dirplayer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;


import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RadioButton;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import com.maxproj.android.dirplayer.PlayService.LocalBinder;
//import android.widget.MediaController;

public class DirPlayerActivity extends FragmentActivity implements
		ActionBar.TabListener,
		FragmentListview.FragmentListviewInterface,
		FragmentBookMark.FragmentBookMarkInterface,
		FragmentPlayList.FragmentPlayListInterface,
		DialogFileList.DialogFileListInterface,
		DialogBookMark.DialogBookMarkInterface,
		DialogPlayList.DialogPlayListInterface
		{
	
	/**
	 * 系统控制
	 * 
	 * 现在存在一个bug：
	 * 	在本app由于种种原因隐藏后，比如home键，如果马上调回本app，无问题
	 * 	如果经历了很长时间，或者大量开启其他耗费内存的应用，其后再调回本app，则出现bug
	 * 	根据目前的调试判断，该bug是由于android系统帮助恢复fragment造成的
	 * 	因为内存紧缺时系统会删除fragment，在app调出时帮忙恢复这些被删除的fragment
	 * 	但是系统帮忙的恢复操作非常不好处理，app不知怎样接收这个fragment，隐患实在非常的多
	 * 	而且不知道如何阻止系统的这一鲁莽行径
	 * 	暂时的解决方法是，在attach里面判断，如果是系统帮忙创建的，则覆盖掉自己创建的
	 */
	int sysAttachFragment = 0;
	
	ActionBar actionBar = null;
	
	int controllerPromptCount = 0;
	 
	/**
	 * 设置
	 */
	SharedPreferences settingPref;
	/**
	 * 分享菜单 
	 *
	 */
	ShareActionProvider mShareActionProvider = null;
	
	/**
	 * 手势
	 */
	private GestureDetectorCompat mDetector;
	private GestureDetectorCompat mDetectorVideoView;
	/*
	 * 用户命令相关定义
	 */
	
	private class FileCmd {
		
		/*
		 * 目前命令
		 * 
		 */
		
		int cmd;
		File src;
		String desPath;
		Boolean force; // 如果目标存在，要不要强制替换
		int fresh;// 0:左窗口，1：右窗口，2：收藏，3：播放列表
		LinkedList<LvRow> ll;
		
		/**
		 * 补充信息
		 * 如果有多种类别，这里可以用联合
		 */
		int cmdSrcTabInAll;
		int listIndex;

		public FileCmd(int cmd, File src, String desPath, Boolean force, int fresh) {
			this.src = src;
			this.desPath = desPath;
			this.force = force;
			this.cmd = cmd;
			this.fresh = fresh;
			
			this.cmdSrcTabInAll = -1; 
		}
		
		
	}

	static LinkedList<FileCmd> cmdList = new LinkedList<FileCmd>();

	static Handler cmdHandler;

	/**
	 * 系统状态
	 */
	int dirPlayerState = LocalConst.STATE_FILE;

	/**
	 * 音频播放
	 */
	//MediaPlayer mediaPlayer = null;
	android.widget.MediaController mediaController = null;
	//这是媒体播放器MediaPlayer和媒体控制器MediaController之间的接口
	android.widget.MediaController.MediaPlayerControl mediaPlayerControl;
	
	/**
	 * 视频播放
	 */
	View mainWindow;
	VideoView vv;
	//注意video和其控制器之间不需接口，而是直接关联
	MediaController videoController = null;
	
	int orientation = Configuration.ORIENTATION_PORTRAIT;
	boolean fullScreen = false;

	//android.widget.LinearLayout.LayoutParams partVideoParamsBK = null;
	int partScreenHeight = 0;
	int partScreenWidth = 0;
	
	/**
	 * 播放列表PlayList
	 */
	FragmentPlayList fragmentPlayList = null;
	public int currentPlTab = 0;
	LinkedList<LvRow>[] playListItems = new LinkedList[LocalConst.plCount];
	MyArrayAdapter[] playListArrayAdapter = new MyArrayAdapter[LocalConst.plCount];
	int currentPlayingTab = 0;
	
	/**
	 * PlayService音频播放
	 */
    PlayService mService;
    boolean mBound = false;    

    int servicePlayType = 0; //播放类型
    int servicePlaying = 0; //播放状态
    String servicePlayPath; //列表播放的路径
    int servicePlayPlTab = 0;
    int servicePlayListIndex = 0;
//    int playStatus_fl_record = 0; //播放状态
//    int playListIndex = 0; //列表下标
//    int playListItemIndex = 0; //播放下标
//    String fileListPath; //文件播放的路径
    
//    int playSequence = LocalConst.play_seq_normal;
    
    /**
     * 下面两个，放在这里初始化，还是放在onCreate()更好？
     */
    
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
			if(mService != null){
				for(int i=0;i<LocalConst.plCount;i++){
    				mService.updatePlayList(i, playListItems[i]);
    				Log.d(LocalConst.DTAG, "playlist: mService != null");
    			}
    		}
            Log.d(LocalConst.DTAG, "playlist: mService = binder.getService();");
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }


    };

	View.OnTouchListener vvOnTouchListener = new View.OnTouchListener(){

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			
			mDetectorVideoView.onTouchEvent(event);
			return true;
		}
		
	};

	/**
	 * 书签窗口相关定义
	 */
	FragmentBookMark fragmentBookMark = null;
	LinkedList<LvRow> bookMarkItems = new LinkedList<LvRow>();
	LinkedList<LvRow> bookMarkSelectedItems = new LinkedList<LvRow>();
	MyArrayAdapter bookMarkArrayAdapter;// = new
												// BookMarkArrayAdapter(this,
												// R.layout.bookmark_row,
												// bookMarkItems);

	int widthHeightInPixels[/*2*/] = new int[2];
	/**
	 * 左右窗口相关定义
	 */
	FragmentListview[] fragmentListview = new FragmentListview[LocalConst.tabCount];
	MyArrayAdapter[] myArrayAdapter = new MyArrayAdapter[LocalConst.tabCount];
	LinkedList<LvRow>[] viewListItems = new LinkedList[LocalConst.tabCount];
	LinkedList<LvRow>[] selectedItems = new LinkedList[LocalConst.tabCount];
	LinkedList<File>[] dirList = new LinkedList[LocalConst.tabCount];
	LinkedList<File>[] fileList = new LinkedList[LocalConst.tabCount];
	String[] currentPath = new String[LocalConst.tabCount];
	String[] parentPath = new String[LocalConst.tabCount];
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	int currentPagerTab = 0;
	int lastWinTab = 0; //最近使用的窗口，可能是右窗口，也可能是左窗口
	SharedPreferences sharedPref;
	SharedPreferences.Editor prefEditor;
	DialogFragmentProgress dfp = null; //进度条
	boolean showCopyProcess;
	ImageView bottomIcon;
	TextView bottomText;
//	String bottomTextTab0;
//	String bottomTextTab1;
//	String bottomTextTab3;
	

	public void setShowCopyProcess(boolean enable){
		showCopyProcess = enable;
	}


	/**
	 * 书签用文本文件保存
	 * 每个书签占一行
	 */
	private void getBookMarkList() {
		bookMarkItems = LocalConst.getListFromFile(LocalConst.bookmark_file);
		for(LvRow lr: bookMarkItems){
			Log.d(LocalConst.DTAG, "database read getBookMarkList " + lr.getPath());
		}
	}



//	private void saveBookMark2File() {
//		LocalConst.saveList2File(bookMarkItems, LocalConst.bookmark_file);
//	}

	public void onFragmentBookMarkClicked(int i) {
		Log.d(LocalConst.DTAG, "onFragmentBookMarkClicked " + i);

		// 如果是目录。如果是文件呢？
		LvRow lr = bookMarkItems.get(i);
		File f = lr.getFile();
		if(f.exists() == false){
			Toast.makeText(this, "该路径不存在，可能被移走或删除了 " + pathTrim4Show(lr.getPath()), Toast.LENGTH_LONG).show();
			return;
		}
		
		if(lr.getType() == LocalConst.TYPE_DIR){
			updateDirInfor(lr.getPath(), lastWinTab);
		}else if(lr.getType() == LocalConst.TYPE_FILE){
			updateDirInfor(lr.getFile().getParent(), lastWinTab);
		}
		
		mViewPager.setCurrentItem(lastWinTab);
	}

	public void onFragmentBookMarkButton1() {
		Log.d(LocalConst.DTAG, "onFragmentBookMarkButton1 ");
		// 全选
		for (LvRow bmr : bookMarkItems) {
			bmr.setSelected(true);
		}
		bookMarkArrayAdapter.notifyDataSetChanged();
	}

	public void onFragmentBookMarkButton2() {
		Log.d(LocalConst.DTAG, "onFragmentBookMarkButton2 ");
		// 全清
		for (LvRow bmr : bookMarkItems) {
			bmr.setSelected(false);
		}
		bookMarkArrayAdapter.notifyDataSetChanged();

	}

	public void onFragmentBookMarkButton3() {
		Log.d(LocalConst.DTAG, "onFragmentBookMarkButton3 ");
		// 反选
		for (LvRow bmr : bookMarkItems) {
			if (bmr.getSelected() == true)
				bmr.setSelected(false);
			else
				bmr.setSelected(true);
		}
		bookMarkArrayAdapter.notifyDataSetChanged();

	}

	public void onFragmentBookMarkButton4() {
		Log.d(LocalConst.DTAG, "onFragmentBookMarkButton4 ");
		// 上移
		moveUpSelected(bookMarkItems);
		bookMarkArrayAdapter.notifyDataSetChanged();
		
		//改为在onStop时备份
//		saveBookMark2File();
		
	}

	public void onFragmentBookMarkButton5() {
		Log.d(LocalConst.DTAG, "onFragmentBookMarkButton5 ");
		// 下移
		moveDownSelected(bookMarkItems);
		bookMarkArrayAdapter.notifyDataSetChanged();
		
		//改为在onStop时备份
//		saveBookMark2File();

	}
	
	public void onFragmentBookMarkButton6() {
		Log.d(LocalConst.DTAG, "onFragmentBookMarkButton6 ");
		// 操作
		bookMarkMenu();
		
	}
	private void bookMarkMenu() { // operate selected files

		DialogBookMark.newInstance().show(getSupportFragmentManager(), "");
		
	}
	
	private void updateBookMarkInfor() {
		// 本函数和bookMarkArrayAdapter.notifyDataSetChanged()有什么区别？回头想下能否去掉
		// 总得设置一次adapter？

		bookMarkArrayAdapter = new MyArrayAdapter(this,
				R.layout.file_row, bookMarkItems, LocalConst.TAB_BOOKMARK);
		Log.d(LocalConst.DTAG, "after new BookMarkArrayAdapter");

		if (fragmentBookMark != null) {
			fragmentBookMark.setListviewAdapter(bookMarkArrayAdapter);
			Log.d(LocalConst.DTAG,
					"fragmentBookMark.setListviewAdapter(bookMarkArrayAdapter)!");
		} else {
			Log.d(LocalConst.DTAG, "fragmentBookMark is null!");
		}
	}
	public void onFragmentListviewLongClicked(int i,int tab){
		File f = viewListItems[tab].get(i).getFile();

		if (f.isDirectory()) {
			Log.d(LocalConst.DTAG, "a dir is long clicked in tab: " + tab);

			// 发送这个目录
		} else {
			
			// 发送这个文件
			// todo: mime信息应该一开始在列表里面就有，不用重复获取
			String mime = LocalConst.getMimeByFileName(f.getName());
			Log.d(LocalConst.DTAG, "a file is long clicked in tab " + tab + "which mime is "+ mime);
			Toast.makeText(this, "file type: " + mime,
					Toast.LENGTH_LONG).show();		
			
			// 根据后缀判定文件类型，并生产intent
			
			Intent dealIntent = new Intent();
			dealIntent.setAction(Intent.ACTION_SEND);
			dealIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
			dealIntent.setType(mime);

			startActivity(Intent.createChooser(dealIntent, 
					getResources().getText(R.string.dealby)));
			Log.d(LocalConst.DTAG, "onOptionsItemSelected: cmdList.size() " + cmdList.size());	
			
		}
	}
	public void onFragmentListviewClicked(int i, int tab) {
		LvRow lr = viewListItems[tab].get(i);
		File f = lr.getFile();

		if (lr.getType() != 2) {// 2是文件，1是目录，0是上一级目录
			Log.d(LocalConst.DTAG, "a dir is clicked in tab: " + tab);
			try {
				updateFileInfor(f, tab);
			} catch (Exception e) {
				if (e.getMessage().equals(LocalConst.pathRoot)) {
					updateDirInfor(LocalConst.pathRoot, tab);
				}
			}
		} else {
			Log.d(LocalConst.DTAG, "a file is clicked in tab" + tab);

			// 文件单击时直接打开
			
			String mime = lr.getMime();
			
			if (mime == null){
				// 不认识的格式
				Log.d(LocalConst.DTAG, "mediaPlayer: mime == null, unkown video format");
				Toast.makeText(this, "file mime is null !",
						Toast.LENGTH_SHORT).show();
				return;
			}
			
			// 音频文件处理
			if (mime.startsWith("audio/"))				
			{
				dirPlayerState = LocalConst.STATE_MUSIC;
				
				Log.d(LocalConst.DTAG, "audio/video: begin audio play......");
				/**
				 * 这里有个问题，Service何时能初始化完成？
				 */
				if(mService != null){
					// 退出视频，并隐藏VideoView
					clearVideoViewPlaying();
					mService.playFile(f, LocalConst.SinglePlay); // clearMusicPlaying() will be called
					Log.d(LocalConst.DTAG, "audio/video: after mService.play(f, null)");
					// mediaController.show(); // 开始播放时是否要显示一下控制面板？
					Log.d(LocalConst.DTAG, "audio/video: after mediaController.show()");
					
					if(controllerPromptCount != 0){
						controllerPromptCount--;
						Toast.makeText(this, getResources().getString(R.string.mediacontroller_prompt), 
							Toast.LENGTH_LONG).show();
					}
				}
			}
			else if (mime.startsWith("video/")){
				// 设置当前为视频播放状态
				dirPlayerState = LocalConst.STATE_VIDEO; 

				// 如果有音频或其他再播放，停止，防止声音冲突
				if (mService != null){					
					// stop first
					mService.quitMusicPlaying();
					Log.d(LocalConst.DTAG, "audio/video: after clear audio playing");
				}
				Log.d(LocalConst.DTAG, "audio/video: begin vv play");
				if (vv.isPlaying()){
					vv.stopPlayback();
					Log.d(LocalConst.DTAG, "audio/video: after clear last vv playing");
				}
				fullScreen = false; //打开时总是窗口显示
				setVideoScreen();
				vv.requestFocus();
				vv.setVideoURI(Uri.fromFile(f));
				Log.d(LocalConst.DTAG, "audio/video: after vv.setVideoURI(Uri.fromFile(f))");

				/* 这个高端玩法似乎不行
				vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
					@Override
					public void onPrepared(MediaPlayer mediaPlayer) {
						// optional need Vitamio 4.0
						//mediaPlayer.setPlaybackSpeed(1.0f);目前没修改过速度
						vv.start();
						videoController.show();
					}
				});
				*/
				//mediaPlayer.prepareAsync();
				vv.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){

					@Override
					public void onCompletion(MediaPlayer mp) {
						// TODO Auto-generated method stub
						mp.seekTo(0);
						mp.pause();
					}
					
				});
				Log.d(LocalConst.DTAG, "audio/video: after vv.setOnCompletionListener()");
				vv.setOnErrorListener(videoViewErrorListener);
				vv.start();
				Log.d(LocalConst.DTAG, "audio/video: after vv.start()");
			}
			
			// 文本文件直接查看
			// 网页文件用浏览器打开
		}
	}
	public OnErrorListener videoViewErrorListener = new OnErrorListener() {
		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			clearVideoViewPlaying();
			return true;
		}
	};
	public void setVideoScreen(){
	    if (fullScreen == true) {
	    	setVideoViewFullScreen();
	    } else {
	    	setVideoViewPartScreen();
	    }
	}
	public void setVideoViewFullScreen(){
		/**
		 * 设置全屏必须在横屏和竖屏时都管用，也即要跟横屏和竖屏无关
		 * 视频尽可能充满屏幕，不能填满的地方黑屏
		 * 不能改变长宽比
		 */
//		DisplayMetrics metrics = new DisplayMetrics();
//		getWindowManager().getDefaultDisplay().getMetrics(metrics);
//		android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) vv.getLayoutParams();
//		params.width =  metrics.widthPixels;
//		params.height = metrics.heightPixels;
//		params.leftMargin = 0;
//		vv.setLayoutParams(params);
		
		android.support.v4.view.ViewPager pager = (android.support.v4.view.ViewPager)findViewById(R.id.pager);
		LinearLayout bottom = (LinearLayout)findViewById(R.id.bottom);
		pager.setVisibility(View.GONE);
		bottom.setVisibility(View.GONE);
		
//		View decorView = getWindow().getDecorView();
//		int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
//		decorView.setSystemUiVisibility(uiOptions);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		actionBar.hide();
		mainWindow.setBackgroundColor(Color.BLACK);
		
		android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) vv.getLayoutParams();
		partScreenHeight = params.height;
		partScreenWidth = params.width;
		Log.d(LocalConst.DTAG, "screen: backup params " + partScreenHeight + " " + partScreenWidth);
		params.height = android.widget.LinearLayout.LayoutParams.MATCH_PARENT;
		params.width = android.widget.LinearLayout.LayoutParams.MATCH_PARENT;
		vv.setLayoutParams(params);
	}
	public void setVideoViewPartScreen(){
//		DisplayMetrics metrics = new DisplayMetrics();
//		getWindowManager().getDefaultDisplay().getMetrics(metrics);
//		android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) vv.getLayoutParams();
//		params.width = LayoutParams.WRAP_CONTENT;
//		params.height = (int) (200*metrics.density);
//		params.gravity = Gravity.CENTER;
//		vv.setLayoutParams(params);
		

		if(partScreenHeight != 0){
			android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) vv.getLayoutParams();
			params.height = partScreenHeight;
			params.width = partScreenWidth;
			Log.d(LocalConst.DTAG, "screen: backup params " + partScreenHeight + " " + partScreenWidth);
			vv.setLayoutParams(params);
		}
		vv.setVisibility(View.VISIBLE);
		android.support.v4.view.ViewPager pager = (android.support.v4.view.ViewPager)findViewById(R.id.pager);
		LinearLayout bottom = (LinearLayout)findViewById(R.id.bottom);
		pager.setVisibility(View.VISIBLE);
		bottom.setVisibility(View.VISIBLE);
		
//		View decorView = getWindow().getDecorView();
//		int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
//		decorView.setSystemUiVisibility(uiOptions);
		getWindow().setFlags(~WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		actionBar.show();
		mainWindow.setBackgroundColor(Color.WHITE);

	}
	public void setVideoViewGone(){
		setVideoViewPartScreen();
		vv.setVisibility(View.GONE);
	}

	public void onFragmentButton1(int tab) {
		Log.d(LocalConst.DTAG, "Button1 clicked in fragment " + tab);
		// Toast.makeText(this, "Button1 clicked in fragment " + tab,
		// Toast.LENGTH_LONG).show();

		// 全选
		for (LvRow lr : viewListItems[tab]) {
			if (lr.getType() != LocalConst.TYPE_PARAENT){
				lr.setSelected(true);
			}
		}
		myArrayAdapter[tab].notifyDataSetChanged();

	}

	public void onFragmentButton2(int tab) {
		Log.d(LocalConst.DTAG, "Button2 clicked in fragment " + tab);
		// Toast.makeText(this, "Button2 clicked in fragment " + tab,
		// Toast.LENGTH_LONG).show();

		// 全清
		for (LvRow lr : viewListItems[tab]) {
			if (lr.getType() != LocalConst.TYPE_PARAENT){
				lr.setSelected(false);
			}
		}
		myArrayAdapter[tab].notifyDataSetChanged();
	}

	public void onFragmentButton3(int tab) {
		Log.d(LocalConst.DTAG, "Button3 clicked in fragment " + tab);
		// Toast.makeText(this, "Button3 clicked in fragment " + tab,
		// Toast.LENGTH_LONG).show();

		// 反选
		for (LvRow lr : viewListItems[tab]) {
			if (lr.getType() != LocalConst.TYPE_PARAENT){
				if (lr.getSelected() == true)
					lr.setSelected(false);
				else
					lr.setSelected(true);
			}
		}
		myArrayAdapter[tab].notifyDataSetChanged();
	}

	public void onFragmentButton4(int tab) {
		Log.d(LocalConst.DTAG, "Button4 clicked in fragment " + tab);
		// 向上
		if ((currentPath[tab].equals(LocalConst.pathRoot))){
			Toast.makeText(this, "只能到这一层啦",
					Toast.LENGTH_LONG).show();			
		}
		else{// 如果已经到顶层了，什么也不做
			updateDirInfor(parentPath[tab], tab);
		}
	}
	public boolean checkFileInLvRowList(String path, LinkedList<LvRow> list){
		for (LvRow lr : list) {
			if (lr.getPath().equals(path)){
				return true;
			}
		}
		return false;		
	}
	
	public void onFragmentButton5(int tab) {
		Log.d(LocalConst.DTAG, "Button5 clicked in fragment " + tab);

		// 收藏
		
		/**
		 * 如果有文件夹或文件被选择，添加这些
		 * 如果没有任何东西被选择，添加当前目录
		 */
		
//		calcSelectItems(tab);
		selectedItems[tab] = generateSelectItems(viewListItems[tab]);
		if(selectedItems[tab].size() == 0){
			/**
			 * 没有条目被选择，添加当前目录到收藏
			 */
			Log.d(LocalConst.DTAG, "Button5 add current directory");
			
			Toast.makeText(this, "您添加了收藏： " + pathTrim4Show(currentPath[tab]), Toast.LENGTH_LONG).show();
			
//			File f = new File(currentPath[tab]);
			
			// 判断当前目录是否已经收藏
//			if(checkFileInLvRowList(currentPath[tab], bookMarkItems))
//				return;
			
			LvRow lr = new LvRow(currentPath[tab], 
					true, //设置为被选择状态，以利后续操作
					LocalConst.clear);// 没有播放状态
			bookMarkItems.add(lr);
		}else{
			/**
			 * 有条目被选择，添加被选择的条目到书签
			 */
			Log.d(LocalConst.DTAG, "Button5 add a dir or a file");
			for (LvRow lr : selectedItems[tab]) {
				
				// 判断是否已经收藏
//				if(checkFileInLvRowList(lr.getPath(), bookMarkItems)){
//					continue;
//				}else{
					bookMarkItems.add(lr);
//				}
			}
			
			Toast.makeText(this, "您添加了收藏： " + pathTrim4Show(selectedItems[tab].getFirst().getPath())+"等等", Toast.LENGTH_LONG).show();

		}
		Log.d(LocalConst.DTAG, "total bookMarkItems: " + bookMarkItems.size());
		bookMarkArrayAdapter.notifyDataSetChanged();
		
		// 改在onStop时备份收藏列表
//		LocalConst.saveList2File(bookMarkItems, LocalConst.bookmark_file);
	}

	public void onFragmentButton6(int tab) {
		Log.d(LocalConst.DTAG, "Button6 clicked in fragment " + tab);
		// Toast.makeText(this, "Button5 clicked in fragment " + tab,
		// Toast.LENGTH_LONG).show();

		// 操作
		commandMenu(tab);
	}

	private void commandMenu(int tab) { // operate selected files
		Log.d(LocalConst.DTAG, "show Operation Dialog");
		
		final int currentTab = tab;
		//mCmdOptions.clear();

		DialogFileList.newInstance(tab).show(getSupportFragmentManager(), "");
		
//		new DialogFragment() {
//
//			@Override
//			public Dialog onCreateDialog(Bundle savedInstanceState) {
//				AlertDialog.Builder builder = new AlertDialog.Builder(
//						getActivity());
//				builder.setTitle(R.string.prompt)
//						.setItems(R.array.cmdList,
//								new DialogInterface.OnClickListener() {
//									public void onClick(DialogInterface dialog,
//											int cmdIndex) {
//										addCmds(cmdIndex, currentTab);
//									}
//								})
//							.setNegativeButton(R.string.negative, null);
//				return builder.create();
//			}
//		}.show(getSupportFragmentManager(), "");

		Log.d(LocalConst.DTAG, "after show Operation Dialogcon");

	}

	/**
	 * Handler顺序处理cmd list
	 * 用空白msg激活
	 */
	private class CmdHandler extends Handler {

		public void handleMessage(Message msg) {
			FileCmd fc = cmdList.pollFirst();
			if (fc != null) { // 如果还有命令没执行完
				Log.d(LocalConst.DTAG, "dir copy: Handler find a cmd: " + fc.cmd);
				try {
					switch (fc.cmd) {
					case LocalConst.CMD_COPY:
						copyFiles(fc.src, fc.desPath, fc.force);
						break;
					case LocalConst.CMD_MOVE:
						/**
						 * 如果move命令来自文件浏览窗口的操作命令，无其他操作
						 * 如果move命令来自收藏窗口的操作命令，需要修改原始收藏窗口的list
						 * 使其指向新的地方
						 */
						boolean result = moveFiles(fc.src, fc.desPath, fc.force);
						if(result == true){
							if (fc.cmdSrcTabInAll == LocalConst.TAB_BOOKMARK){
								LvRow bookMarkLr = bookMarkItems.get(fc.listIndex);
								String bookMarkPath = bookMarkLr.getPath();
								if(bookMarkPath.equals(fc.src.getPath())){
									/**
									 * 如果moveTo成功、发自收藏窗口、path吻合
									 */
									LvRow updatedLr = new LvRow(new File(fc.desPath, bookMarkLr.getName()), true, LocalConst.clear);
									bookMarkItems.set(fc.listIndex, updatedLr);
									Log.d(LocalConst.DTAG, "bookmark move, origin index: " + fc.listIndex);
								}
							}
						}
						
						break;
					case LocalConst.CMD_DELETE:
						deleteFiles(fc.src);
						break;
					case LocalConst.CMD_FRESH:
						/**
						 * 由于Cmd结构设计得比较早，没有想到后面很大的变动
						 * 所以比较丑陋，暂时将就，以后重构
						 */
						switch(fc.fresh){
							case 0:
							case 1:
								updateDirInfor(currentPath[fc.fresh], fc.fresh);
								cmdHandler.sendEmptyMessage(1);//其他命令的结束都有这个激活next指令
								break;
							case 2:
								break;
							case 3:
							case 4:
							case 5:
							case 6:
							case 7:
//								savePlayList2File(fc.fresh - 3);
								//更新adapter。注意这个操作不能少，否则系统提示adapter被异常修改而退出
								updatePlayListAdapter(fc.fresh - LocalConst.TAB_PLAYLIST);
								break;
						}
						
						
						break;
					case LocalConst.CMD_MKDIR:
						mkDir(fc.desPath, fc.force);
						break;
					case LocalConst.CMD_ADD_PLAY:
						Log.d(LocalConst.DTAG, "add to playlit");
						new addToPlayListAsyncTask(new LvRow(fc.src, true, LocalConst.clear), fc.fresh - LocalConst.TAB_PLAYLIST)// 01是左右窗口，2是收藏，3开始是5个播放列表
							.execute();
						break;
					case LocalConst.CMD_RENAME:
						rename(fc.src, fc.force);
						break;
					default:
					}

				} catch (IOException e) {
					e.printStackTrace();
					new DialogFragment() {
						@Override
						public Dialog onCreateDialog(Bundle savedInstanceState) {
							AlertDialog.Builder builder = new AlertDialog.Builder(
									getActivity());
							builder.setTitle("文件操作过程中好像发生了异常")
									.setPositiveButton("知道了", null);
							return builder.create();
						}
					}.show(getSupportFragmentManager(), "");
					
					updateDirInfor(currentPath[0], 0);
					updateDirInfor(currentPath[1], 1);
					} finally {

				}
			}
		}
	}

	private void mkDir(final String newDirParantStr, boolean force){
		
		new DialogFragment() {

			@Override
			public Dialog onCreateDialog(Bundle savedInstanceState) {
			    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			    // Get the layout inflater
			    LayoutInflater inflater = getActivity().getLayoutInflater();
			    final View v = inflater.inflate(R.layout.mkdir, null);
			    // Inflate and set the layout for the dialog
			    // Pass null as the parent view because its going in the dialog layout
			    builder.setTitle(R.string.mkdir_prompt)
			    	.setView(v)
			        .setNegativeButton(R.string.negative, null)
			        .setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
			        	@Override
			            public void onClick(DialogInterface dialog, int id) {
			               // mkdir here
			        		Log.d(LocalConst.DTAG, "mkdir onClick()");
			        		
			        		EditText et = (EditText) v.findViewById(R.id.mkdirtext);
			        		Log.d(LocalConst.DTAG, "mkdir find EditText()");
			        		
			        		if(et == null){
			        			Log.d(LocalConst.DTAG, "mkdir find et is null");
			        			return;
			        		}
			        		String dirName = et.getText().toString();
			        		Log.d(LocalConst.DTAG, "mkdir get dir name"+dirName);
			        		
			        		File newDirFile = new File(newDirParantStr, dirName);
			        		try{
			        			newDirFile.mkdir();
			        			cmdHandler.sendEmptyMessage(1); // move Handler to next
			        		}catch(Exception e){
			        			Log.d(LocalConst.DTAG, "mkdir get exception");
			        			Toast.makeText(
			        			getActivity(),
			        			"由于某种原因，比如目录名不合法，本次创建文件夹失败",
			        			Toast.LENGTH_LONG)
			        			.show();
			        		}
			               }
			           });      
			    return builder.create();
			}
		}.show(getSupportFragmentManager(), "");

		
	}
	
	/**
	 * 单个文件的具体操作，异步进行，假定之前已经做了合法性检查
	 */
	private class copySingleFileAsyncTask extends
			AsyncTask<File, Integer, Long> {
		String msg;
		Context context;

		public copySingleFileAsyncTask(Context context, String msg) {
			Log.d(LocalConst.DTAG, "dir copy: Handler in copyFileTask()");
			this.msg = msg;
			this.context = context;

		}

		protected Long doInBackground(File... files) {

			long count = 0;
			long countLoop = 0;
			int b;
			long length = files[0].length();

			InputStream in;
			OutputStream out;

			Log.d(LocalConst.DTAG,
					"dir copy: doInBackground(src: "
							+ files[0].getPath()
							+ " des:" + files[1].getPath() + ")");

			try {
				//如果out路径文件夹不存在，会异常？
				//尝试创建out路径文件夹？
				
				in = new BufferedInputStream(new FileInputStream(files[0]));
				out = new BufferedOutputStream(new FileOutputStream(files[1]));
				Log.d(LocalConst.DTAG, "dir copy: before while");

				while ((b = in.read()) != -1) {
					out.write(b);
					count++;
					countLoop++;
					if (countLoop > 1024) { //每拷贝1k字节更新一次进度条
						countLoop = 0;
						publishProgress((int) ((count / (float) length) * 100));
						Log.d(LocalConst.DTAG, "AsyncTask: publishProgress("
								+ ((int) ((count / (float) length) * 100))
								+ ")");
					}
				}
				out.flush();
				in.close();
				out.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {

			}

			return count;
		}

		protected void onProgressUpdate(Integer... progress) {
			Log.d(LocalConst.DTAG, "dir copy: onProgressUpdate(" + progress + ")");
			if (dfp != null) {
				dfp.setProgress(progress[0].intValue());
			}
		}

		protected void onPreExecute() {
			Log.d(LocalConst.DTAG, "dir copy: onPreExecute()");
			
			// 启动进度条
			if (showCopyProcess == true) {
				dfp = DialogFragmentProgress.newInstance();
				dfp.show(getSupportFragmentManager(), "");
				dfp.setProgress(0);
				dfp.setMsg(msg);
			}else{
				dfp = null;
			}
		}

		protected void onPostExecute(Long result) {
			Log.d(LocalConst.DTAG, "dir copy: onPostExecute()");
			if (dfp != null) {
				dfp.dismiss();
			}
			// 重新启动Handler
			cmdHandler.sendEmptyMessage(1);
		}
	}

	/**
	 * 添加音乐文件到播放列表
	 */
	private class addToPlayListAsyncTask extends
			AsyncTask<Void, Integer, Long> {
		LvRow lr;
		int plTab;
//		Context context;

		public addToPlayListAsyncTask(LvRow lr, int plTab) {
			this.lr = lr;
			this.plTab = plTab;
//			this.context = context;
			Log.d(LocalConst.DTAG, "addToPlayListAsyncTask() constructed");
		}

		protected Long doInBackground(Void... vs) {
			long count = 0;
			addToPlayList(lr, plTab);

			return count;
		}

		protected void onProgressUpdate(Integer... progress) {
//			if (dfp != null) {
//				dfp.setProgress(progress[0].intValue());
//			}
		}

		protected void onPreExecute() {
			// 启动进度条
//			if (showCopyProcess == true) {
//				dfp = DialogFragmentProgress.newInstance();
//				dfp.show(getSupportFragmentManager(), "");
//				dfp.setProgress(0);
//				dfp.setMsg(msg);
//			}else{
//				dfp = null;
//			}
		}

		protected void onPostExecute(Long result) {
//			if (dfp != null) {
//				dfp.dismiss();
//			}
			// 重新启动Handler
			cmdHandler.sendEmptyMessage(1);
			Log.d(LocalConst.DTAG, "addToPlayListAsyncTask().onPostExecute()");
		}
	}
	/*
	 * 移动的合法性检查
	 * 
	 * 	如果源是文件，目标路径不能等同源路径
	 *  如果源是文件夹，目标路径不能等同也不能包含源路径
	 *  
	 *  源路径
	 *  
	 *  不合法的：
	 *  move /a/b to /a 目标等于父目录
	 *  move /a/b to /a/b 目标等于自己
	 *  move /a/b to /a/b/c 源包含了目标
	 *  
	 *  合法：
	 *  move /a/b/c/d to /a/b
	 *  move /a/b to /a/c
	 *  
	 *  归纳：
	 *  	跟copy检查一样
	 *  
	 */	
	private Boolean moveFilesValidity(File src, String des){
		return copyFilesValidity(src, des);
	}
	/*
	 * 拷贝的合法性检查
	 * 
	 * 	如果源是文件，目标路径不能等同源路径
	 *  如果源是文件夹，目标路径不能等同也不能包含源路径
	 *  
	 *  源路径
	 *  
	 *  不合法的：
	 *  copy /a/b to /a 目标等于父目录
	 *  copy /a/b to /a/b 目标等于自己
	 *  copy /a/b to /a/b/c 源包含了目标
	 *  
	 *  合法：
	 *  copy /a/b/c/d to /a/b
	 *  copy /a/b to /a/c
	 *  
	 *  归纳：
	 *  	父目录不能等于目标，否则相当于在原地拷贝
	 *  	源不能包含目标
	 *  	源不能等于目标
	 *  
	 *  是否穷尽了？？？
	 *  
	 */	
	private Boolean copyFilesValidity(File src, String des){
		if(!src.exists()){
			Log.d(LocalConst.DTAG, "copy null trace 31");
			return false;
		}
		
		if (src.getParent().equals(des)){ //父目录不能等于目标，否则相当于在原地拷贝
			Log.d(LocalConst.DTAG, "copy null trace 5");
			return false;
		}else if(des.startsWith(src.getPath())){//源包含了目标
			Log.d(LocalConst.DTAG, "copy null trace 6");
			return false;
		}else if(src.getPath().equals(des)){
			Log.d(LocalConst.DTAG, "copy null trace 7");
			return false;
		}
		Log.d(LocalConst.DTAG, "copy null trace 8");
		return true;
	}
	/**
	 * 被Handler调用
	 * 
	 * 合法性检查
	 * 如果是单个文件
	 *  	 直接拷贝
	 * 如果是文件夹
	 * 		 创建目标文件夹
	 * 		 分解为多个下一级文件拷贝
	 * 		 并且插入队列
	 * 
	 * 重要！！！
	 * 		如果直接return，则Handler不再处理后续cmd
	 * 		所以，如果正常进行，要发送msg来驱动Handler处理下一条cmd
	 * 		或者启动AsyncTash，该任务结束时会发送msg
	 */
	private void copyFiles(File srcFile, String desPathStr, Boolean force)
			throws IOException {
		Log.d(LocalConst.DTAG, "copy null trace 1");
		if(!copyFilesValidity(srcFile,desPathStr)){
			Log.d(LocalConst.DTAG, "Handler copyFile: validaity check failed: " + srcFile.getPath()
					+ " to " + desPathStr);
			
			/**
			 * 如果不合法，停止后续所有操作
			 */
			cmdList.clear();
			Log.d(LocalConst.DTAG, "copy null trace 2");
			return;
		}
		
		Log.d(LocalConst.DTAG, "copy null trace 3");
		/**
		 * 如果存在同名的File，并且force被设置，直接删除同名的File
		 * 否则停止本File的拷贝，但是继续后面的拷贝
		 */
		File desFile = new File(desPathStr, srcFile.getName());
		Log.d(LocalConst.DTAG, "copy null trace 10");
		if (desFile.exists()) {
			if (force == true) {// 覆盖同名文件
				deleteFileOrDir(desFile);
			} else {
				Log.d(LocalConst.DTAG, "Handler copyFile: do nothing");
				
				//什么也不做，但是激活后面的操作
				cmdHandler.sendEmptyMessage(1);
				return;
			}
		}
		Log.d(LocalConst.DTAG, "copy null trace 11");
		if (srcFile.isFile()) {// 一个文件
			Log.d(LocalConst.DTAG, "dir copy: srcFile.isFile()");

			//添加拷贝任务
			new copySingleFileAsyncTask(this, srcFile.getName())
			.execute(srcFile, desFile);
			
			// 在AsyncTask里面激活Handler，这里不用激活
			return;
		}
		
		if (srcFile.isDirectory()) {// 一个目录

			Log.d(LocalConst.DTAG, "dir copy: srcFile.isDirectory()");
			
			if(desFile.mkdirs()){
				// 这里拷贝下一级
				copyFilesInDir(srcFile, desFile.getPath(),force);
				
				cmdHandler.sendEmptyMessage(1);
				return;
				
			}else{
				//严重错误，停止后续全部操作				 
				Log.d(LocalConst.DTAG, "dir copy: cmdList.clear() in copyFiles() "
						+ Thread.currentThread().getStackTrace()[1].getLineNumber());
				cmdList.clear();
				return;
			}
		}
		Log.d(LocalConst.DTAG, "copy null trace 20");
	}

	/**
	 * 拷贝目录里面的各项子文件或子目录 
	 */
	public void copyFilesInDir(File srcDirFile, String desDirStr, boolean force){
		
		Log.d(LocalConst.DTAG, "dir copy: copyFilesInDir()");
		
		File srcFiles[] = srcDirFile.listFiles();
		for (File f : srcFiles) {
			Log.d(LocalConst.DTAG, "dir copy: copyFilesInDir() add "+f.getName());
			cmdList.addFirst(new FileCmd(LocalConst.CMD_COPY, f, desDirStr, force, 1));
		}

	}
	
	private void rename(File f , boolean force){

		DialogFragmentRename dfr = DialogFragmentRename.newInstance(f);
		dfr.show(getSupportFragmentManager(), "");
		
		// 注意不能在这里激活handler，要在本次对话操作完成之后
//		cmdHandler.sendEmptyMessage(1);
		
	}
	public void cancelRename(){
		/**
		 * rename之外的其他命令继续执行，包括frash
		 * 似乎也只有frash
		 */
		Iterator<FileCmd> iter = cmdList.iterator();
		while (iter.hasNext()) {
			if (iter.next().cmd == LocalConst.CMD_RENAME)
				iter.remove();
		}
	}
	

	public void activateNextCmd(){
		cmdHandler.sendEmptyMessage(1);
	}
	
	public void cancelCopy(){
		/**
		 * copy之外的其他命令继续执行，包括frash
		 * 似乎也只有frash
		 */
		Iterator<FileCmd> iter = cmdList.iterator();
		while (iter.hasNext()) {
			if (iter.next().cmd == LocalConst.CMD_COPY)
				iter.remove();
		}
	}
	/**
	 * 被Handler调用
	 * 
	 * 无论是单个文件还是文件夹
	 *  	合法性检查
	 *  	 直接移动
	 *  
	 * 重要！！！
	 * 		如果直接return，则Handler不再处理后续cmd
	 * 		所以，如果正常进行，要发送msg来驱动Handler处理下一条cmd
	 * 		或者启动AsyncTash，该任务结束时会发送msg
	 */
	
	private boolean moveFiles(File srcFile, String desPathStr, Boolean force)
			throws IOException {
		
		if(!moveFilesValidity(srcFile,desPathStr)){
			Log.d(LocalConst.DTAG, "moveFiles: validaity check failed: " + srcFile.getPath()
					+ " to " + desPathStr);
			
			/**
			 * 目前的处理是，如果不合法，停止后续所有操作
			 * 这似乎不合理，应该允许后续的合法操作
			 */
			//Log.d(LocalConst.DTAG, "dir copy: cmdList.clear() in moveFiles()"
			//		+ Thread.currentThread().getStackTrace()[1].getLineNumber());
			cmdList.clear();
			return false;
		}
		
		
		/**
		 * 如果存在同名的File，并且force被设置，直接删除同名的File
		 * 否则停止本File的移动，但是继续后面的移动
		 */
		File desFile = new File(desPathStr, srcFile.getName());
		if (desFile.exists()) {
			if (force == true) {// 覆盖同名文件
				Log.d(LocalConst.DTAG,
						"moveFiles: delete file before copy");
				deleteFileOrDir(desFile);
			} else {
				Log.d(LocalConst.DTAG, "moveFiles: do nothing");
				
				//什么也不做，但是激活后面的操作
				cmdHandler.sendEmptyMessage(1);
				return false;
			}
		}
		
		boolean result = srcFile.renameTo(desFile);
		cmdHandler.sendEmptyMessage(1); // let Handler move to next
										// cmd
//		if (result != true) {
//			throw new IOException();
//		}
		return result;
	}

	/**
	 * 被Handler调用
	 * 
	 * 重要！！！
	 * 		如果直接return，则Handler不再处理后续cmd
	 * 		所以，如果正常进行，要发送msg来驱动Handler处理下一条cmd
	 * 		或者启动AsyncTash，该任务结束时会发送msg
	 * 
	 */
	private void deleteFiles(File file) {
		// TODO Auto-generated method stub
		deleteFileOrDir(file);
		
		cmdHandler.sendEmptyMessage(1);
	}

	private boolean deleteFileOrDir(File file) {

			if (file.isFile()){
				return file.delete();
			}
			
			if(file.isDirectory()){
				File files[] = file.listFiles();
				for (File f : files) {
					if(!deleteFileOrDir(f)){
						return false;
					}					
				}
				return file.delete();
			}
			return false;
	}
	
	/**
	 * 添加一个LvRow到播放列表
	 */
	private void addToPlayList(LvRow lr, int plTab) {
		
		if (lr.getType() == LocalConst.TYPE_FILE){//file
			//直接添加;
			String mime = lr.getMime();
			/**
			 * 只加音乐文件
			 */
			if (mime != null){
				if(mime.startsWith("audio/")){	
					playListItems[plTab].add(lr);
				}
			}
		}else if(lr.getType() == LocalConst.TYPE_DIR){//dir
			File files[] = lr.getFile().listFiles();
			if(files != null){
				for (File subFile : files) {
					addToPlayList(new LvRow(subFile, false, LocalConst.clear), plTab);
				}
			}
		}
		
	}
	private void updatePlayListAdapter(int plTab)
	{
		Log.d(LocalConst.LIFECYCLE, "pl updatePlayListAdapter("+playListItems[plTab].size()+") in plTab " + plTab);

		
		playListArrayAdapter[plTab] = new MyArrayAdapter(this, R.layout.file_row,
				playListItems[plTab], LocalConst.TAB_PLAYLIST + plTab);

		if (fragmentPlayList != null) {
			fragmentPlayList.setListviewAdapter(playListArrayAdapter[plTab], plTab);
		}
		
		// 先修改，再通知？
		playListArrayAdapter[plTab].notifyDataSetChanged();
	}
	private void updatePlayListAdapterAll(){
		for(int i=0;i<LocalConst.plCount;i++){
			updatePlayListAdapter(i);
		}
	}
	


	
	public LinkedList<LvRow> generateSelectItems(LinkedList<LvRow> list){
		LinkedList<LvRow> selectedItems = new LinkedList<LvRow>();
		
		int i = 0;
		for (LvRow lr : list) {
			if (lr.getSelected() == true) {
				LvRow selectedLr = new LvRow(lr.getFile(), true, LocalConst.clear);
				selectedLr.setOriginIndex(i);
				selectedItems.add(selectedLr);
			}
			i++;
		}
		return selectedItems;
	}
	
//	public void calcSelectItems(int tab){
//		selectedItems[tab].clear();
//		for (LvRow lr : viewListItems[tab]) {
//			if (lr.getSelected() == true) {
//				selectedItems[tab].add(lr);
//				Log.d(LocalConst.DTAG,
//						"operateMenu(): tab " + tab + " selected: "
//							+ lr.getName());
//			}
//		}
//	}
	/*
	 * 将用户命令添加到list 
	 */
	private void addCmds(int cmdPosition, int tab) {

		cmdList.clear();
		
		switch (cmdPosition) {
		/**
		 * 注意移动时需要刷新两边
		 */
		case 0: // 添加到播放列表
			
			/**
			 * 必须走message+handler流程，因为selectedItems[tab]很快会被改变
			 * 需要将selectedItems[tab]先保存到message队列中
			 */
			for (LvRow lr : selectedItems[tab]) {
				//addToPlayList(lr);
				cmdList.add(new FileCmd(LocalConst.CMD_ADD_PLAY, lr.getFile(), null, true,
						LocalConst.TAB_PLAYLIST + currentPlTab)); //第一个播放列表在总表的索引是3.收藏表是2，左右窗口和0和1
			}
			// savePlayList2File(currentPlTab);
			// updatePlayListAdapter(currentPlTab);
			
			/**
			 * 2014年4月1日
			 * 由于之前的FileCmd结构设计不合理，这里是凑合的补丁
			 * 在下面的刷新操作里面判断是左右窗口刷新，或者是收藏和播放列表的刷新
			 * 在播放列表刷新时插入savePlayList2File(currentPlTab)操作
			 */
			
			cmdList.add(new FileCmd(LocalConst.CMD_FRESH, null, null, true, LocalConst.TAB_PLAYLIST + currentPlTab));
			break;
		case 1: // 从A拷贝到B
			setShowCopyProcess(true);
			for (LvRow lr : selectedItems[0]) {
				cmdList.add(new FileCmd(LocalConst.CMD_COPY, lr.getFile(), currentPath[1], true,
						0));
			}
			cmdList.add(new FileCmd(LocalConst.CMD_FRESH, null, null, true, LocalConst.TAB_RIGHT));
			break;
		case 2: // 从A移动到B
			for (LvRow lr : selectedItems[0]) {
				cmdList.add(new FileCmd(LocalConst.CMD_MOVE, lr.getFile(), currentPath[1], true,
						0));
			}
			cmdList.add(new FileCmd(LocalConst.CMD_FRESH, null, null, true, LocalConst.TAB_LEFT));// 两边都刷新
			cmdList.add(new FileCmd(LocalConst.CMD_FRESH, null, null, true, LocalConst.TAB_RIGHT));
			break;
		case 3: // 从B拷贝到A
			setShowCopyProcess(true);
			for (LvRow lr : selectedItems[1]) {
				cmdList.add(new FileCmd(LocalConst.CMD_COPY, lr.getFile(), currentPath[0], true,
						0));
			}
			cmdList.add(new FileCmd(LocalConst.CMD_FRESH, null, null, true, LocalConst.TAB_LEFT));
			break;
		case 4: // 从B移动到A
			for (LvRow lr : selectedItems[1]) {
				cmdList.add(new FileCmd(LocalConst.CMD_MOVE, lr.getFile(), currentPath[0], true,
						0));
			}
			cmdList.add(new FileCmd(LocalConst.CMD_FRESH, null, null, true, LocalConst.TAB_LEFT));
			cmdList.add(new FileCmd(LocalConst.CMD_FRESH, null, null, true, LocalConst.TAB_RIGHT));
			break;
		case 5: // 删除当前窗口所选
			for (LvRow lr : selectedItems[tab]) {
				cmdList.add(new FileCmd(LocalConst.CMD_DELETE,lr.getFile(), null, false, 
						0));
			}
			cmdList.add(new FileCmd(LocalConst.CMD_FRESH, null, null, true, tab));
			break;
		case 6: // 创建文件夹
			cmdList.add(new FileCmd(LocalConst.CMD_MKDIR, null, currentPath[tab], false, tab));
			cmdList.add(new FileCmd(LocalConst.CMD_FRESH, null, null, true, tab));
			Log.d(LocalConst.DTAG, "cmdList.size(): "+cmdList.size());
			break;
		case 7: // 改名
			for (LvRow lr : selectedItems[tab]) {
				cmdList.add(new FileCmd(LocalConst.CMD_RENAME,lr.getFile(), null, false, 
						0));
			}
			cmdList.add(new FileCmd(LocalConst.CMD_FRESH, null, null, true, tab));
			break;
		case 8: // 从收藏添加到播放列表
			for (LvRow lr : bookMarkSelectedItems) {
				//addToPlayList(lr);
				cmdList.add(new FileCmd(LocalConst.CMD_ADD_PLAY, lr.getFile(), null, true,
						LocalConst.TAB_PLAYLIST + currentPlTab)); //第一个播放列表在总表的索引是3.收藏表是2，左右窗口和0和1
			}
			cmdList.add(new FileCmd(LocalConst.CMD_FRESH, null, null, true, LocalConst.TAB_PLAYLIST + currentPlTab));
			break;
		case 9: // 从收藏拷贝到左窗口
//			Toast.makeText(this, "从收藏拷贝到左窗口!",
//					Toast.LENGTH_LONG).show();
			setShowCopyProcess(true);
			for (LvRow lr : bookMarkSelectedItems) {
				cmdList.add(new FileCmd(LocalConst.CMD_COPY, lr.getFile(), currentPath[LocalConst.TAB_LEFT], true, 0));
			}
			cmdList.add(new FileCmd(LocalConst.CMD_FRESH, null, null, true, LocalConst.TAB_LEFT));
			break;
		case 10: // 从收藏拷贝到右窗口
//			Toast.makeText(this, "从收藏拷贝到右窗口!",
//					Toast.LENGTH_LONG).show();
			setShowCopyProcess(true);
			for (LvRow lr : bookMarkSelectedItems) {
				cmdList.add(new FileCmd(LocalConst.CMD_COPY, lr.getFile(), currentPath[LocalConst.TAB_RIGHT], true, 0));
			}
			cmdList.add(new FileCmd(LocalConst.CMD_FRESH, null, null, true, LocalConst.TAB_RIGHT));
			break;
		case 11: // 从收藏移动到左窗口
			for (LvRow lr : bookMarkSelectedItems) {
				FileCmd fc = new FileCmd(LocalConst.CMD_MOVE, lr.getFile(), currentPath[LocalConst.TAB_LEFT], true, 0);
				fc.cmdSrcTabInAll = LocalConst.TAB_BOOKMARK;
				fc.listIndex = lr.getOriginIndex();
				cmdList.add(fc);
			}
			cmdList.add(new FileCmd(LocalConst.CMD_FRESH, null, null, true, LocalConst.TAB_LEFT));
			cmdList.add(new FileCmd(LocalConst.CMD_FRESH, null, null, true, LocalConst.TAB_RIGHT));
			break;
		case 12: // 从收藏移动到右窗口
			setShowCopyProcess(true);
			for (LvRow lr : bookMarkSelectedItems) {
				FileCmd fc = new FileCmd(LocalConst.CMD_MOVE, lr.getFile(), currentPath[LocalConst.TAB_RIGHT], true, 0);
				fc.cmdSrcTabInAll = LocalConst.TAB_BOOKMARK;
				fc.listIndex = lr.getOriginIndex();
				cmdList.add(fc);
			}
			cmdList.add(new FileCmd(LocalConst.CMD_FRESH, null, null, true, LocalConst.TAB_LEFT));
			cmdList.add(new FileCmd(LocalConst.CMD_FRESH, null, null, true, LocalConst.TAB_RIGHT));
			break;
		default:
			break;
		}
		/*
		 * } catch (java.io.IOException e) { // 提示一下，文件操作过程中可能出现什么异常了 new
		 * DialogFragment() {
		 * 
		 * @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
		 * AlertDialog.Builder builder = new AlertDialog.Builder(
		 * getActivity());
		 * builder.setTitle("文件操作过程中好像发生了异常").setPositiveButton("知道了", null);
		 * return builder.create(); } }.show(getSupportFragmentManager(), ""); }
		 * finally { //updateDirInfor(currentPath[0], 0);
		 * //updateDirInfor(currentPath[1], 1); }
		 */

		cmdHandler.sendEmptyMessage(1);

	}

	private void updateDirInfor(String path, int tab) {
		// Log.d(TAG_DEBUG, "------1------");
		if (path != null) {
			File f = new File(path);
			try {
				updateFileInfor(f, tab);
			} catch (Exception e) {
				if (e.getMessage().equals(LocalConst.pathRoot)) {
					updateDirInfor(LocalConst.pathRoot, tab);
				}
			}
		} else {
			// NullPointerException
			Log.d(LocalConst.DTAG, "path null: " + path);
		}
	}

	private void updateFileInfor(File f, int tab) throws Exception {
		File files[];

		Log.d(LocalConst.DTAG, "xtab "+tab+" goto this directory: " + f.getPath());
		try {
			files = f.listFiles();
			if (files == null) {
				Log.d(LocalConst.DTAG, "sorry, xtab "+tab+" can't update to this directory: " +
				 currentPath);
				Toast.makeText(this, "Failed to this directory!",
						Toast.LENGTH_SHORT).show();
				return;
			}
			// update to new directory
			Log.d(LocalConst.DTAG, "xtab "+tab+" get currentPath and praentPath");
			currentPath[tab] = f.getPath();
			parentPath[tab] = f.getParent(); // 如果f是根目录会怎样？

			Log.d(LocalConst.DTAG, "xtab "+tab+" save currentPath");
			if (tab == 0) {
				prefEditor.putString(getString(R.string.left_window_path),
						currentPath[tab]);
				prefEditor.commit();
			} else if (tab == 1) {
				prefEditor.putString(getString(R.string.right_window_path),
						currentPath[tab]);
				prefEditor.commit();
			}

			fillList(files, tab);

			constructViewList(tab);

			myArrayAdapter[tab] = new MyArrayAdapter(this, R.layout.file_row,
					viewListItems[tab], LocalConst.TAB_LEFT + tab);
			Log.d(LocalConst.FRAGMENT_LIFE, "after new MyArrayAdapter");

			if (fragmentListview[tab] != null) {
				fragmentListview[tab].setListviewAdapter(myArrayAdapter[tab],
						currentPath[tab]);
				
				if(tab == currentPagerTab){
					updateBottomStatus(pathTrim4Show(currentPath[tab]));
				}
				Log.d(LocalConst.LIFECYCLE,
						"DirPlayerActivity.updateFileInfor()!"+" "+fragmentListview[tab]+" "+myArrayAdapter);
			} else {
				Log.d(LocalConst.FRAGMENT_LIFE, "fragmentListview is null!");
			}
		} catch (Exception e) {
			Log.d(LocalConst.FRAGMENT_LIFE, "xtab path: " + f.getPath() + "Exception e: " + e.toString());
			e.printStackTrace();

			throw new Exception(LocalConst.pathRoot);
			// return;
		} finally {
			Log.d(LocalConst.FRAGMENT_LIFE, "xtab " + tab + " finally " + currentPath[tab]);
			// return;
		}

	}
	private String pathTrim4Show(String path) {
		
		/**
		 *	/mnt/sdcard --> /
		 *	/mnt/sdcard/MyDoc --> /MyDoc
		 *
		 *	如果path等于rootPath，返回“/”
		 *	否则去掉pathRoot
		 */
		if(path == null){//可能在初始状态会得到null
			Log.d(LocalConst.DTAG, "pathTrim4Show() get path: " + path);
			return "/";
		}
		if(path.equals(LocalConst.pathRoot)){
			return "/";
		}else{
			return path.substring(LocalConst.pathRoot.length());
		}
	}
	
	/**
	 *	集中所有的dir和file到对应list
	 *	对list里面的items排序
	 *	以后排序控制加到这里
	 */
	private void fillList(File[] files, int tab) {
		Log.d(LocalConst.DTAG, "in fillList, fils.length: " + files.length);
		dirList[tab].clear();
		fileList[tab].clear();
		for (File f : files) {
			if (f.isDirectory()) {
//				Log.d(LocalConst.DTAG, "find directory: " + f.getName());
				dirList[tab].add(f);
			} else if (f.isFile()) {
//				Log.d(LocalConst.DTAG, "find file: " + f.getName());
				fileList[tab].add(f);
			} else
				Log.d(LocalConst.DTAG, "File error~~~");

		}
		Log.d(LocalConst.DTAG, "fillList loop end 1");

		Collections.sort(dirList[tab]);
		Collections.sort(fileList[tab]);

	}

	/**
	 * 初始化左右窗口的view list items
	 */
	private void constructViewList(int tab) {
		SimpleDateFormat sdf = new SimpleDateFormat(LocalConst.time_format);
		String date;

		// viewListFiles.clear();
		viewListItems[tab].clear();
		if (!currentPath[tab].equals(LocalConst.pathRoot)) {
			Log.d(LocalConst.DTAG, "add parentPath: " + parentPath[tab]);
			// viewListFiles.add(new File(parentPath));
			LvRow lr = LvRow.lvRowParaent(parentPath[tab], false, LocalConst.clear);
			viewListItems[tab].add(lr);
		}
		Log.d(LocalConst.DTAG, "fillList loop end 2");

		for (File f : dirList[tab]) {
			// viewListFiles.add(f);
			LvRow lr = new LvRow(f, false, LocalConst.clear);
//			Log.d(LocalConst.DTAG, "add directory: " + lr.getName());
			viewListItems[tab].add(lr);
		}
		for (File f : fileList[tab]) {
			
			/**
			 * 添加正在播放标记
			 * 这个信息放在这里是否合适？
			 */
			int playFlag = LocalConst.clear;
			if(
					(servicePlayType == LocalConst.SinglePlay)
					&&(f.getPath().equals(servicePlayPath))
					)
			{
				playFlag = servicePlaying;
			}
			
			// viewListFiles.add(f);
			LvRow lr = new LvRow(f, false,playFlag);
			
//			Log.d(LocalConst.DTAG, "mime test: " + lr.getName()+" --- "+ LocalConst.getMimeByFileName(f.getName()));
			
//			Log.d(LocalConst.DTAG, "mime test: " + lr.getName()+" --- " +
//			MimeTypeMap.getSingleton().getMimeTypeFromExtension(
//					FilenameUtils.getExtension(f.getName()))
//			);
			
//			try {
//				Log.d(LocalConst.DTAG, "mime test: " + lr.getName()+" --- "+ Files. probeContentType(f.toPath()));
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			viewListItems[tab].add(lr);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dir_player);
		
		Log.d(LocalConst.LIFECYCLE, "DirPlayerActivity.onCreate()");
		{
			/**
			 * 保存上下文
			 */
			LocalConst.app = getApplicationContext();
			LocalConst.dirPlayerActivity = this;

		}
		// Set up the action bar.
		actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);		
		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		
		/**
		 * 初始化文件浏览数据结构
		 */
		{
			for (int i = 0; i < LocalConst.plCount; i++) {
				playListItems[i] = new LinkedList<LvRow>();
				playListArrayAdapter[i] = new MyArrayAdapter(this, R.layout.fragment_playlist, null, LocalConst.TAB_PLAYLIST+i);
			}
			for (int i = 0; i < LocalConst.tabCount; i++) {
				viewListItems[i] = new LinkedList<LvRow>();
				selectedItems[i] = new LinkedList<LvRow>();
				dirList[i] = new LinkedList<File>();
				fileList[i] = new LinkedList<File>();
				currentPath[i] = LocalConst.pathRoot;
				parentPath[i] = null;
				if(sysAttachFragment == 0){ // 正常启动，而不是系统帮忙恢复
					fragmentListview[i] = FragmentListview.newInstance(i);
				}
				Log.d(LocalConst.LIFECYCLE, "DirPlayerActivity.StaticCodeIniFragment{} "+i+" "+fragmentListview[i]);
			}
			if(fragmentPlayList == null){
				fragmentPlayList = FragmentPlayList.newInstance();
			}
			if(fragmentBookMark == null){
				fragmentBookMark = FragmentBookMark.newInstance();
			}
		}
		
		/**
		 * 初始化setting
		 */
		
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		settingPref = PreferenceManager.getDefaultSharedPreferences(this);
		
		/**
		 * 初始化手势
		 * 
		 */
		getScreenSizePixels(widthHeightInPixels);
		mDetector = new GestureDetectorCompat(this, new MyGestureListener());
		mDetectorVideoView = new GestureDetectorCompat(this, new VideoViewGestureListener());

		bottomIcon = (ImageView)findViewById(R.id.bottom_icon);
		bottomText = (TextView)findViewById(R.id.bottom_about);
		bottomIcon.setImageResource(R.drawable.bottom);
		Log.d(LocalConst.FRAGMENT_LIFE, "activity onCreate() end!"); // 要检查这个，这样才知道是否初始化全部完成
	}

    @Override
    protected void onStart() {
        super.onStart();
        
        Log.d(LocalConst.LIFECYCLE, "DirPlayerActivity.onStart()");
        
        //是否显示顶部标题条
        boolean b = settingPref.getBoolean(getString(R.string.setting1_key), false);
        if(b == true){
        	actionBar.setDisplayOptions(
        			actionBar.DISPLAY_SHOW_CUSTOM|actionBar.DISPLAY_SHOW_HOME|actionBar.DISPLAY_SHOW_TITLE,
        			actionBar.DISPLAY_SHOW_CUSTOM|actionBar.DISPLAY_SHOW_HOME|actionBar.DISPLAY_SHOW_TITLE);
        	actionBar.setTitle(R.string.app_name);
        }else{
        	actionBar.setDisplayOptions(
        			0,
        			actionBar.DISPLAY_SHOW_CUSTOM|actionBar.DISPLAY_SHOW_HOME|actionBar.DISPLAY_SHOW_TITLE);
        }

       

		/**
		 * 初始化命令处理handler
		 */
		cmdHandler = new CmdHandler();

		/**
		 * 初始化书签相关
		 */
		getBookMarkList();
		updateBookMarkInfor();
		// bookMarkArrayAdapter = new BookMarkArrayAdapter(this,
		// R.layout.bookmark_row, bookMarkItems);
		// bookMarkArrayAdapter.notifyDataSetChanged();
		Log.d(LocalConst.FRAGMENT_LIFE, "activity onStart() after updateBookMarkInfor()!");
		
		/**
		 * 初始化左右窗口
		 */
		sharedPref = getPreferences(Context.MODE_PRIVATE);
		prefEditor = sharedPref.edit();
		
		String lwp = sharedPref.getString(getString(R.string.left_window_path),
				LocalConst.pathRoot);
		String rwp = sharedPref.getString(
				getString(R.string.right_window_path), LocalConst.pathRoot);
		Log.d(LocalConst.FRAGMENT_LIFE,
				"lwp: " + lwp + " rwp:" + rwp + " LocalConst.pathRoot: " + LocalConst.pathRoot);
		updateDirInfor(lwp, 0);
		updateDirInfor(rwp, 1);

		Log.d(LocalConst.FRAGMENT_LIFE, "activity onStart() after updateDirInfor()!");

		/**
		 * 读取文件中的播放列表
		 */
		currentPlTab = sharedPref.getInt(LocalConst.PL_TAB_IN_PREF, 0);
		for(int i=0;i<LocalConst.plCount;i++){
			getPlayList(i);
		}
//		updatePlayListAdapterAll();
//		Log.d(LocalConst.FRAGMENT_LIFE, "activity onStart() after updatePlayListAdapter()!");

		/**
		 * 音视频媒体相关初始化
		 */
		mainWindow = findViewById(R.id.main_window); // 停放controller的地方
		
		mediaPlayerControlInit(); // 生成音频控制接口		
		
		// 音频控制器使用android自带的
		mediaController = new android.widget.MediaController(this);
		mediaController.setMediaPlayer(mediaPlayerControl);
		mediaController.setAnchorView(mainWindow);
		mediaController.setEnabled(true);					

		// 视频控制器使用videoview内含的
		vv = (VideoView)findViewById(R.id.videoview); // 视频播放窗口
		videoController = new MediaController(this);
		//videoController.setMediaPlayer(videoPlayerControl);
		//videoController.setAnchorView(mainWindow);
		//videoController.setEnabled(true);
		vv.setMediaController(videoController);
		vv.setOnTouchListener(vvOnTouchListener);
		Log.d(LocalConst.FRAGMENT_LIFE, "activity onStart() after vv.setOnTouchListener()!");
		
		/**
		 * 启动音频播放service
		 */
        // Bind to LocalService
        bindService(new Intent(this, PlayService.class), mConnection, Context.BIND_AUTO_CREATE);
		startService(new Intent(this, PlayService.class));
		
		/**
		 * 接收广播信息
		 * 		来自service的播放信息
		 * 		fragment的更新请求
		 * 		目录更新？
		 */
        IntentFilter mainActivityIntentFilter = new IntentFilter();
        mainActivityIntentFilter.addAction(LocalConst.REQUEST_FRAG_BOOKMARK_LIST_UPDATE);
        mainActivityIntentFilter.addAction(LocalConst.REQUEST_FRAG_FILE_LIST_UPDATE);
        mainActivityIntentFilter.addAction(LocalConst.REQUEST_FRAG_PLAY_LIST_UPDATE);
        mainActivityIntentFilter.addAction(LocalConst.BROADCAST_SERVICE_STATUS);
        mainActivityIntentFilter.addAction(LocalConst.BOTTOM_STATUS_TEXT);
   
        /**
         * 注意，使用LocalBroadcastManager注册的receiver只能接收本app内部的intent
         * 但notification在本app之外
         * 所以这里需要直接使用registerReceiver来注册
         */
        LocalBroadcastManager.getInstance(this).registerReceiver(
        		new MainActivityReceiver(),
        		mainActivityIntentFilter);
        
        Log.d(LocalConst.FRAGMENT_LIFE, "activity onStart() end!"); // 要检查这个，这样才知道是否初始化全部完成
    }

    @Override
    protected void onStop() {
        super.onStop();
        
        Log.d(LocalConst.LIFECYCLE, "DirPlayerActivity.onStop()");
        
        /**
         * 在退出时备份各种列表
         */
        LocalConst.saveList2File(bookMarkItems, LocalConst.bookmark_file);
		for(int i=0;i<LocalConst.plCount;i++){
			LocalConst.saveList2File(playListItems[i], LocalConst.playlist_file_prefix + i);
		}
        
        
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.d(LocalConst.LIFECYCLE, "DirPlayerActivity.onDestroy()");
		
		/**
		 * 退出app时关掉notification
		 */
		if(mService != null){
			mService.stop();
			mService.cancelNotification();
			mService.stopSelf();
		}
		
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d(LocalConst.LIFECYCLE, "DirPlayerActivity.onResume()");
		
		controllerPromptCount = 2;
		
		if(mService != null){
			mService.pleaseUpdatePlayingFlag();
		}
	}
	@Override
	protected void onResumeFragments() {
		// TODO Auto-generated method stub
		super.onResumeFragments();
		Log.d(LocalConst.LIFECYCLE, "DirPlayerActivity.onResumeFragments()");
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		Log.d(LocalConst.LIFECYCLE, "DirPlayerActivity.onSaveInstanceState()");
	}
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		Log.d(LocalConst.LIFECYCLE, "DirPlayerActivity.onRestart()");
	}
	@Override
	public void recreate() {
		// TODO Auto-generated method stub
		super.recreate();
		Log.d(LocalConst.LIFECYCLE, "DirPlayerActivity.recreate()");
	}

	 class VideoViewGestureListener extends GestureDetector.SimpleOnGestureListener {
	        
	        @Override
	        public boolean onDown(MotionEvent event) { 
	            Log.d(LocalConst.DTAG,"videoview onDown"); 
	            return true;
	        }

			/* (non-Javadoc)
			 * @see android.view.GestureDetector.SimpleOnGestureListener#onSingleTapUp(android.view.MotionEvent)
			 */
			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				// TODO Auto-generated method stub
				return super.onSingleTapUp(e);
			}

			/* (non-Javadoc)
			 * @see android.view.GestureDetector.SimpleOnGestureListener#onSingleTapConfirmed(android.view.MotionEvent)
			 */
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				// TODO Auto-generated method stub
				videoController.show();
				return super.onSingleTapConfirmed(e);
			}

			/* (non-Javadoc)
			 * @see android.view.GestureDetector.SimpleOnGestureListener#onDoubleTap(android.view.MotionEvent)
			 */
			@Override
			public boolean onDoubleTap(MotionEvent e) {
				// TODO Auto-generated method stub

				/**
				 * 怎样实现全屏播放？
				 * 简单起见，根据横竖屏来确定
				 * 横屏：全屏播放
				 * 竖屏：小窗口播放
				 * 所以不需要用双击来控制
				 */
				if(fullScreen == false)
					fullScreen = true;
				else
					fullScreen = false;
				
				setVideoScreen();
				
				return super.onDoubleTap(e);
			}

			/* (non-Javadoc)
			 * @see android.view.GestureDetector.SimpleOnGestureListener#onScroll(android.view.MotionEvent, android.view.MotionEvent, float, float)
			 */
			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2,
					float distanceX, float distanceY) {
				Log.d(LocalConst.DTAG,"videoview onScroll"); 
				// TODO Auto-generated method stub
				return super.onScroll(e1, e2, distanceX, distanceY);
			}

			/* (non-Javadoc)
			 * @see android.view.GestureDetector.SimpleOnGestureListener#onFling(android.view.MotionEvent, android.view.MotionEvent, float, float)
			 */
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2,
					float velocityX, float velocityY) {
				
				// TODO Auto-generated method stub
				// http://stackoverflow.com/questions/937313/android-basic-gesture-detection
				final ViewConfiguration vc = ViewConfiguration.get(getBaseContext());
				final int swipeMinDistance = vc.getScaledPagingTouchSlop();
				final int swipeThresholdVelocity = vc.getScaledMinimumFlingVelocity();
				final int swipeMaxOffPath = vc.getScaledTouchSlop();

				Log.d(LocalConst.DTAG,"videoview onFling:"+(e2.getY() - e1.getY()));
				Log.d(LocalConst.DTAG,"videoview onFling:"+swipeMinDistance);
				Log.d(LocalConst.DTAG,"videoview onFling:"+Math.abs(velocityY));
				Log.d(LocalConst.DTAG,"videoview onFling:"+swipeThresholdVelocity);
				
				if (e2.getY() - e1.getY() > swipeMinDistance
						&& Math.abs(velocityY) > swipeThresholdVelocity) {
					Log.d(LocalConst.DTAG,"onFling is accepted");
					// 退出视频，并隐藏VideoView
					clearVideoViewPlaying();					
		            return true;
				}
				Log.d(LocalConst.DTAG,"diliver onFling()");
				return super.onFling(e1, e2, velocityX, velocityY);
			}
	    }
	 class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
	        
	        @Override
	        public boolean onDown(MotionEvent event) { 
	            Log.d(LocalConst.DTAG,"onDown: " + event.toString()); 
	            return true;
	        }

	        /* (non-Javadoc)
			 * @see android.view.GestureDetector.SimpleOnGestureListener#onScroll(android.view.MotionEvent, android.view.MotionEvent, float, float)
			 */
			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2,
					float distanceX, float distanceY) {
				
				//如果从底部滑出，并且媒体播放器存在的话，调用其show方法
//				Log.d(LocalConst.DTAG,"onScroll(): "
//						+e1.getX()+"/"+(widthHeightInPixels[0] - 20)+" "
//						+e1.getY()+"/"+(widthHeightInPixels[1] - 20));
				
				if(
						(dirPlayerState == LocalConst.STATE_MUSIC)
						||(mService.isPlaying())
						)
				{
					
				    if (
				    		(orientation == Configuration.ORIENTATION_PORTRAIT)?
		    				(e1.getY() > (widthHeightInPixels[1] - 20)):
		    					(e1.getY() > (widthHeightInPixels[0] - 20))
				    	)
					{
						Log.d(LocalConst.DTAG,"onScroll() find scroll from bottom!");
						if(mediaController != null){
							mediaController.show();
						}else{
							Toast.makeText(LocalConst.dirPlayerActivity, "mediaController is null!", Toast.LENGTH_SHORT).show();
						}
						return true;
				    }
				}
				// TODO Auto-generated method stub
				return super.onScroll(e1, e2, distanceX, distanceY);
			}


	    }

	 /** 
	  * http://stackoverflow.com/questions/17481341/how-to-get-android-screen-size-programmatically-once-and-for-all
	  * get screen size in "pixels", i.e. touchevent/view units.
	  * on my droid 4, this is 360x640 or 540x960
	  * depending on whether the app is in screen compatibility mode
	  * (i.e. targetSdkVersion<=10 in the manifest) or not. */
	 public void getScreenSizePixels(int widthHeightInPixels[/*2*/])
	 {
	     Resources resources = getResources();
	     Configuration config = resources.getConfiguration();
	     DisplayMetrics dm = resources.getDisplayMetrics();
	     // Note, screenHeightDp isn't reliable
	     // (it seems to be too small by the height of the status bar),
	     // but we assume screenWidthDp is reliable.
	     // Note also, dm.widthPixels,dm.heightPixels aren't reliably pixels
	     // (they get confused when in screen compatibility mode, it seems),
	     // but we assume their ratio is correct.
	     double screenWidthInPixels = (double)config.screenWidthDp * dm.density;
	     double screenHeightInPixels = screenWidthInPixels * dm.heightPixels / dm.widthPixels;
	     widthHeightInPixels[0] = (int)(screenWidthInPixels + .5);
	     widthHeightInPixels[1] = (int)(screenHeightInPixels + .5);
	     Log.d(LocalConst.DTAG, "widthHeightInPixels[0]: " + widthHeightInPixels[0]
	    		 + "widthHeightInPixels[1]: " + widthHeightInPixels[1]);
	 }
//	@Override
	public boolean _onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
//		inflater.inflate(R.menu.dir_player, menu);
		inflater.inflate(R.menu.dir_player_mini, menu);
		

		/* 测试代码
		
	    // 得到菜单上的share按钮
	    MenuItem item = menu.findItem(R.id.menu_item_share);

	    // 获得item关联的ShareActionProvider对象
	    mShareActionProvider = (ShareActionProvider) item.getActionProvider();


		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
		sendIntent.setType("text/plain");
		
		mShareActionProvider.setShareIntent(sendIntent);
		
		
		Intent shareIntent2 = new Intent();
		shareIntent2.setAction(Intent.ACTION_SEND);
		shareIntent2.putExtra(Intent.EXTRA_STREAM, "This is my text to send.");
		shareIntent2.setType("image/jpeg");
		
		mShareActionProvider.setShareIntent(shareIntent2);

		*/
		
		return super.onCreateOptionsMenu(menu);

	}




	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.action_settings:
            startActivity(new Intent(this,SettingsActivity.class));	
			return true;
		case R.id.about:
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		currentPagerTab = tab.getPosition();
		mViewPager.setCurrentItem(currentPagerTab);
		Log.d(LocalConst.DTAG, "MainActivityReceiver " + currentPagerTab); 
		if((currentPagerTab == LocalConst.TAB_LEFT)
			||(currentPagerTab == LocalConst.TAB_RIGHT)){//左右窗口
			//更新当前路径
			updateBottomStatus(pathTrim4Show(currentPath[currentPagerTab]));
		}else if(currentPagerTab == LocalConst.TAB_BOOKMARK){//收藏窗口
			updateBottomStatus(getResources().getString(R.string.bottom_about));
		}else if(currentPagerTab == LocalConst.TAB_PLAYLIST){//播放列表。当前曲目，或者空白标记
			//更新为正在播放曲目
//		    int servicePlayType = 0; //播放类型
//		    int servicePlaying = 0; //播放状态
//		    String servicePlayPath; //列表播放的路径
			if((servicePlaying == LocalConst.playing)
				&&(servicePlayPath != null)){
				updateBottomStatus(new File(servicePlayPath).getName());
			}else{
				updateBottomStatus(getResources().getString(R.string.bottom_about));
			}
		}
		
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		int lastPosition = tab.getPosition();
		if(lastPosition < 2) {// 左窗口0，右窗口1
			lastWinTab = lastPosition;
		}
		Log.d(LocalConst.DTAG, "last tab: " + lastWinTab);
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.

			if (position == LocalConst.TAB_PLAYLIST) { // 第四个tab为播放列表
//				Log.d(LocalConst.FRAGMENT_LIFE, "getItem(" + position + ") return fragmentPlayList");
				Log.d(LocalConst.PL,
						"DirPlayerActivity.SectionsPagerAdapter.getItem()"
						+" "+position+" "+fragmentPlayList);

				return fragmentPlayList;
			}
			if (position == LocalConst.TAB_BOOKMARK) { // 第三个tab为标签
//				Log.d(LocalConst.FRAGMENT_LIFE, "getItem(" + position + ") return fragmentBookMark");
				Log.d(LocalConst.BM,
						"DirPlayerActivity.SectionsPagerAdapter.getItem()"
						+" "+position+" "+fragmentBookMark);

				return fragmentBookMark;
			} else { // 第一个tab为左窗口，第二个tab为右窗口
//				Log.d(LocalConst.FRAGMENT_LIFE, "getItem(" + position + ") return fragmentListview");
				Log.d(LocalConst.LIFECYCLE,
						"DirPlayerActivity.SectionsPagerAdapter.getItem()"
						+" "+position+" "+fragmentListview[position]);
				return fragmentListview[position];
			}

			/*
			 * Fragment fragment = new DummySectionFragment(); Bundle args = new
			 * Bundle(); args.putInt(DummySectionFragment.ARG_SECTION_NUMBER,
			 * position + 1); fragment.setArguments(args); return fragment;
			 */
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return LocalConst.TAB_PLAYLIST + 1;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			case 3:
				return getString(R.string.title_section4).toUpperCase(l);
			}
			return null;
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.d(LocalConst.LIFECYCLE, "DirPlayerActivity.onPause()");
	}
	
	private boolean backPressed = false;
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	Log.d(LocalConst.DTAG, "back key found!");
	    	Toast.makeText(this, "连按两次回退键可以退出程序", Toast.LENGTH_SHORT).show();
	    	
	        if (backPressed == true) {
	        	Log.d(LocalConst.DTAG, "back key twice! close app!");
	            finish();
	        }
	        else
	        {
	        	Log.d(LocalConst.DTAG, "back key once! set flag.");
	            backPressed = true;
	            new Handler().postDelayed(new Runnable() {

	                @Override
	                public void run() {
	                	backPressed = false;   
	                	Log.d(LocalConst.DTAG, "clear back key once flag from runnable()!");;
	                }
	            }, 2000);
	            
	        }
	        return true; // 让系统不再处理这次回退键，因为系统收到回退键后会立即把app关了
	    }
	    return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {

		super.dispatchKeyEvent(event);
		//if(event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
		//	mediaController.hide();
		//}
		return false;
	}
	
    @Override 
    public boolean onTouchEvent(MotionEvent event){ 
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

	 
	
	/**
	 * MediaController interface
	 */
    public void mediaPlayerControlInit(){
		mediaPlayerControl = new MediaPlayerControl(){ 
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
		
			@Override
			public int getCurrentPosition() {
				// TODO Auto-generated method stub
				return mService.getCurrentPosition();
			}
		
			@Override
			public int getDuration() {
				// TODO Auto-generated method stub
				return mService.getDuration();
			}
		
			@Override
			public boolean isPlaying() {
				// TODO Auto-generated method stub
				return mService.isPlaying();
			}
		
			@Override
			public void pause() {
				// TODO Auto-generated method stub
				Log.d(LocalConst.DTAG, "mediaPlayer: pause()");
				mService.pause();
			}
		
			@Override
			public void seekTo(int arg0) {
				// TODO Auto-generated method stub
				mService.seekTo(arg0);
			}
		
			@Override
			public void start() {
				Log.d(LocalConst.DTAG, "mediaPlayer: start()");
				mService.start();
				// TODO Auto-generated method stub
				
			}
		};
    }

	public void onFragmentPlayListClicked(int i, int plTab) {
		Log.d(LocalConst.DTAG, "onFragmentPlayListClicked: " + i);
		
		dirPlayerState = LocalConst.STATE_MUSIC;
		
		if(mService != null){
			clearVideoViewPlaying();
			mService.playList(i, plTab);
			//mediaController.show();
			currentPlayingTab = plTab;
			if(controllerPromptCount != 0){
				controllerPromptCount--;
				Toast.makeText(this, getResources().getString(R.string.mediacontroller_prompt), 
					Toast.LENGTH_LONG).show();
			}
			
		}
	}

	/**
	 * 怎样干净地关掉视频播放呢？
	 * 如果使用android自带的播放系统
	 * 		似乎没有问题？
	 * 如果使用Vitamio播放视频
	 * 		在视频和音频切换时经常导致死机或退出
	 */
	public void clearVideoViewPlaying(){
		Log.d(LocalConst.DTAG, "audio/video: begin clearVideoViewPlaying()");
		//if((vv!=null)&&(vv.isPlaying())){
		if(vv != null){ // 即便vv并非正在播放，只要vv存在，就需要清除
			if(videoController!=null)
				videoController.hide();
			vv.stopPlayback();
			setVideoViewGone();
			//vv.pause();
		}
		Log.d(LocalConst.DTAG, "audio/video: after clearVideoViewPlaying()");
	}
	public void onFragmentPlayListButton1() {
		// 全选
		for (LvRow lr : playListItems[currentPlTab]) {
			lr.setSelected(true);
		}
		playListArrayAdapter[currentPlTab].notifyDataSetChanged();
	}

	public void onFragmentPlayListButton2() {
		// 全清
		for (LvRow lr : playListItems[currentPlTab]) {
			lr.setSelected(false);
		}
		playListArrayAdapter[currentPlTab].notifyDataSetChanged();

	}

	public void onFragmentPlayListButton3() {
		// 反选
		for (LvRow lr : playListItems[currentPlTab]) {
			if (lr.getSelected() == true)
				lr.setSelected(false);
			else
				lr.setSelected(true);
		}
		playListArrayAdapter[currentPlTab].notifyDataSetChanged();

	}
	public void moveUpSelected(LinkedList<LvRow> list){
		Log.d(LocalConst.DTAG, "move up ....");
		if (list.size() < 2)
			return;
		
		for(int i = 0;i < list.size() - 1 ;i++){
			Log.d(LocalConst.DTAG, "compare in pair: " + i);
			// 用冒泡的方式来实现被选择项上移
			// 首先获取一对
			LvRow lr_first = list.get(i);
			if (lr_first.getSelected()){
				Log.d(LocalConst.DTAG, "the first in pair is selected");
				continue; // 如果第一个被选择了，无论第二个是什么，什么也不做
			}
			
			LvRow lr_second = list.get(i+1);
			if (!lr_second.getSelected()){
				Log.d(LocalConst.DTAG, "the second in pair is not selected");
				continue; // 由于第一个没有被选择，第二个如果也没被选择，那么什么也不做
			}
			
			// 第二个被选择了，所以要交换
			LvRow lr = list.remove(i+1);
			list.add(i,lr);
			
			Log.d(LocalConst.DTAG, "move up in pair: " + i);
		}
		
	}
	public void onFragmentPlayListButton4() {
		// 上移
		moveUpSelected(playListItems[currentPlTab]);
		playListArrayAdapter[currentPlTab].notifyDataSetChanged();

//		savePlayList2File(currentPlTab);		
		
	}
	
	public void moveDownSelected(LinkedList<LvRow> list){
		Log.d(LocalConst.DTAG, "move down ....");
		if (list.size() < 2)
			return;
		
		for(int i = list.size() - 1; i > 0;i--){
			Log.d(LocalConst.DTAG, "compare in pair: " + i);
			// 用冒泡的方式来实现被选择项上移
			// 首先获取一对
			LvRow lr_second = list.get(i);
			if (lr_second.getSelected()){
				Log.d(LocalConst.DTAG, "the second in pair is selected");
				continue; // 如果第二个被选择了，无论第一个是什么，什么也不做
			}

			LvRow lr_first = list.get(i-1);
			if (!lr_first.getSelected()){
				Log.d(LocalConst.DTAG, "the first in pair is not selected");
				continue; // 由于第二个没有被选择，第一个如果也没被选择，那么什么也不做 
			}
			
			
			// 第一个被选择了，所以要交换
			LvRow lr = list.remove(i-1);
			list.add(i,lr);
			
			Log.d(LocalConst.DTAG, "move down in pair: " + i);
		}
	}
	public void onFragmentPlayListButton5() {
		// 下移
		moveDownSelected(playListItems[currentPlTab]);
		playListArrayAdapter[currentPlTab].notifyDataSetChanged();
//		savePlayList2File(currentPlTab);
	}
	public void onFragmentPlayListButton6(int plTab) {
		// 操作
		playListMenu(plTab);
	}

	private void playListMenu(int plTab) { // operate selected files

		DialogPlayList.newInstance(plTab).show(getSupportFragmentManager(), "");
	}


	
    public PlayService getServiceConnection(){
    	return mService;
    }
    
	/**
	 * 播放目录用什么格式保存呢？
	 */
	private void getPlayList(int plTab) {
		playListItems[plTab] = LocalConst.getListFromFile(LocalConst.playlist_file_prefix + plTab);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);		
		Log.d(LocalConst.DTAG,"rotate and onConfigurationChanged() is called");

		//保存一下
		orientation = newConfig.orientation;
		
	    // Checks the orientation of the screen
	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	        //Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();

	    	
	    	
	    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
	        //Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
	    	
	    	
	    	
	    }
	}

    /**
     * 有时候系统会帮我重建fragment，但是却不交给我指针
     * 这里试验一下能否自己更新指针
     */
	public void sysAttachFragmentListviewLowMem(int tab, FragmentListview fragment){
//		if(fragmentListview[tab] != null)
		Log.d(LocalConst.LIFECYCLE, "DirPlayerActivity.sysAttachFragmentListviewLowMem() "+tab+":"+"before "+fragmentListview[tab]);
			fragmentListview[tab] = fragment;
			Log.d(LocalConst.LIFECYCLE, "DirPlayerActivity.sysAttachFragmentListviewLowMem() "+tab+":"+"after "+fragment);
	}
	public void sysAttachFragmentPlayListLowMem(FragmentPlayList fragment){
//		if(fragmentListview[tab] != null)
		Log.d(LocalConst.LIFECYCLE, "DirPlayerActivity.sysAttachFragmentPlayListLowMem():"+"before "+fragmentPlayList);
			fragmentPlayList = fragment;
			Log.d(LocalConst.LIFECYCLE, "DirPlayerActivity.sysAttachFragmentPlayListLowMem():"+"after "+fragment);
	}
	public void sysAttachFragmentBookMarkLowMem(FragmentBookMark fragment){
//		if(fragmentListview[tab] != null)
		Log.d(LocalConst.LIFECYCLE, "DirPlayerActivity.sysAttachFragmentBookMarkLowMem():"+"before "+fragmentBookMark);
			fragmentBookMark = fragment;
			Log.d(LocalConst.LIFECYCLE, "DirPlayerActivity.sysAttachFragmentBookMarkLowMem():"+"after "+fragment);
	}


	private void updatePlayingFlag(Intent intent){
		/**
		 * 1、service需要发送当前播放状态
		 * 2、activity需要记录当前播放状态
		 * 3、activity需要更新fragment的显示
		 * 4、fragment切换时需要查询记录的当前播放状态
		 * 
		 * 播放状态
		 * 		文件播放
		 * 			播放路径
		 * 		列表播放
		 * 			播放路径
		 * 			列表编号
		 * 
		 */
		servicePlaying =  intent.getIntExtra(LocalConst.PLAY_STATUS, -1);
		Log.d(LocalConst.DTAG,"status when pressed: " + servicePlaying);
		servicePlayType = intent.getIntExtra(LocalConst.PLAY_TYPE, -1);
		servicePlayPath = intent.getStringExtra(LocalConst.PLAY_PATH);
		servicePlayPlTab = intent.getIntExtra(LocalConst.PLAY_PL_TAB, -1);
		servicePlayListIndex = intent.getIntExtra(LocalConst.PLAY_LIST_INDEX, -1);
		
		/**
		 * 三种可能：
		 * 	noPlay
		 * 	ListPlay
		 * 	SinglePlay
		 */
		if(servicePlayType == LocalConst.SinglePlay){
			/**
			 * 点击，并且播放后，在这里动态改变播放标记
			 * 循环查找非常耗时，但似乎没有优化的途径
			 * 
			 * 注意singlePlay时同一个tab里面没有多个path相同的问题
			 */
			for (int i = 0; i < LocalConst.tabCount; i++) {
				for(LvRow lr:viewListItems[i]){
					if(lr.getPath().equals(servicePlayPath)){
						//注意，下面一行也能清除播放标记
						lr.setPlayingStatus(servicePlaying);
					}
				}
				if(myArrayAdapter[i] != null){
					myArrayAdapter[i].notifyDataSetChanged();
				}
			}
		}else if(servicePlayType == LocalConst.ListPlay){
			/**
			 * 点击，并且播放后，在这里动态改变播放标记
			 */

			
			// 要点在于，播放列表可能已经改变
			Log.d(LocalConst.DTAG,"playlist index: " + servicePlayListIndex);
			
			boolean found = false;
			
			//首先根据index查找
			if(servicePlayListIndex < playListItems[servicePlayPlTab].size()){//防止越界异常
				if(playListItems[servicePlayPlTab].get(servicePlayListIndex).getPath().equals(servicePlayPath)){//看path是否一致
					playListItems[servicePlayPlTab].get(servicePlayListIndex).setPlayingStatus(servicePlaying);
					found = true;
				}
			}
			
			//没找到，只好遍历了。这通常是播放列表中途被修改导致的
			if(found == false){
				/**
				 * 此刻还要考虑列表中有相同曲目的情形
				 * 		如果是clear标志，必须循环查找并clear，否则以后没机会clear
				 * 		如果是set标志，可能导致path一致的曲目都被set了，比较难看，更麻烦的是，以后没法clear，所以，只能都不set
				 * 		但这种情况很少，因为开始播放之后intent就发过来了，这个时间很短，不太可能列表有改变
				 * 		所以先不处理 
				 */
				if(servicePlaying == LocalConst.clear){
					for(LvRow lr:playListItems[servicePlayPlTab]){
						if(lr.getPath().equals(servicePlayPath)){
							lr.setPlayingStatus(servicePlaying);
						}
					}
				}
			}
			
			if(playListArrayAdapter[servicePlayPlTab] != null){
				playListArrayAdapter[servicePlayPlTab].notifyDataSetChanged();
			}
			
		}//如果servicePlayType == LocalConst.noPlay
		
		if(currentPagerTab == LocalConst.TAB_PLAYLIST){
			/**
			 * 在播放列表显示时，即便播放的是文件列表的曲目，也显示这个曲目名
			 */
			if(servicePlaying == LocalConst.playing){
				if(servicePlayPath != null){
					updateBottomStatus(new File(servicePlayPath).getName());
				}
			}else{
				updateBottomStatus(getResources().getString(R.string.bottom_about));
			}
		}
	}
	private class MainActivityReceiver extends BroadcastReceiver
	{
		private MainActivityReceiver(){ // Prevents instantiation
		}
		
	    // Called when the BroadcastReceiver gets an Intent it's registered to receive
		@Override
	    public void onReceive(Context context, Intent intent) {
			
			/**
			 * 接收如下信息
			 * 		请求更新file list窗口
			 * 		请求更新play list窗口
			 * 		请求更新收藏窗口
			 *		在底部状态栏显示当前路径-----------需要
			 *		在底部状态栏显示当前播放曲目-----可选
			 */			

			String action = intent.getAction();
			Log.d(LocalConst.LIFECYCLE, "MainActivityReceiver get: "+action);			
			
		    if(LocalConst.BROADCAST_SERVICE_STATUS.equals(action)) {
		    	/**
		    	 * 更新播放状态
		    	 */
		    	updatePlayingFlag(intent);
		    }else if(LocalConst.BOTTOM_STATUS_TEXT.equals(action)) {
		        /**
		         * 请求更新底部状态栏
		         */
		    	String statusText = intent.getStringExtra(LocalConst.STATUS_TEXT);
		    	if(bottomText != null){
		    		bottomText.setText(statusText);
		    	}
		    }else if(LocalConst.REQUEST_FRAG_BOOKMARK_LIST_UPDATE.equals(action)) {
		    	/**
		    	 * 请求更新收藏窗口adapter
		    	 */
		        fragmentBookMark.setListviewAdapter(bookMarkArrayAdapter);		        
		    }else if(LocalConst.REQUEST_FRAG_FILE_LIST_UPDATE.equals(action)) {
		        /**
		         * 请求更新左右窗口adapter
		         */
		        for(int i=0;i<LocalConst.tabCount;i++){
		        	fragmentListview[i].setListviewAdapter(myArrayAdapter[i],
		        			currentPath[i]);
		        }
		    }else if(LocalConst.REQUEST_FRAG_PLAY_LIST_UPDATE.equals(action)) {
		    	/**
		    	 * 请求更新播放窗口adapter
		    	 */
		        Log.d(LocalConst.LIFECYCLE,"FRAG_PLAY_LIST_UPDATE_ACTION");				
				updatePlayListAdapterAll();
		    }	

		    
	    }
	}


	
	public void updateBottomStatus(String text){
		Log.d(LocalConst.DTAG,"MainActivityReceiver "+text);
		LocalBroadcastManager.getInstance(this).sendBroadcast(
				new Intent(LocalConst.BOTTOM_STATUS_TEXT)
				.putExtra(LocalConst.STATUS_TEXT, text));
	}


	@Override
	public void onDialogFileListAdd(int tab) {
		// TODO Auto-generated method stub
		
		selectedItems[tab] = generateSelectItems(viewListItems[tab]);
		
		addCmds(0, tab);
	}


	@Override
	public void onDialogFileListL2rCopy(int tab) {
		
		/**
		 * 注意形参tab说明命令发出的地点是左或右窗口
		 * 我们只计算拷贝源地址的selected items就行
		 */

		selectedItems[LocalConst.TAB_LEFT] = generateSelectItems(viewListItems[LocalConst.TAB_LEFT]);
		addCmds(1, tab);
	}


	@Override
	public void onDialogFileListL2rMove(int tab) {
		selectedItems[LocalConst.TAB_LEFT] = generateSelectItems(viewListItems[LocalConst.TAB_LEFT]);
		addCmds(2, tab);
	}


	@Override
	public void onDialogFileListR2lCopy(int tab) {
		selectedItems[LocalConst.TAB_RIGHT] = generateSelectItems(viewListItems[LocalConst.TAB_RIGHT]);
		addCmds(3, tab);
	}


	@Override
	public void onDialogFileListR2lMove(int tab) {
		selectedItems[LocalConst.TAB_RIGHT] = generateSelectItems(viewListItems[LocalConst.TAB_RIGHT]);
		addCmds(4, tab);
	}


	@Override
	public void onDialogFileListMkdir(int tab) {
		// TODO Auto-generated method stub
		addCmds(6, tab);
	}


	@Override
	public void onDialogFileListDel(int tab) {
		selectedItems[tab] = generateSelectItems(viewListItems[tab]);
		addCmds(5, tab);
	}


	@Override
	public void onDialogFileListRename(int tab) {
		selectedItems[tab] = generateSelectItems(viewListItems[tab]);
		addCmds(7, tab);
	}

	@Override
	public void onDialogFileListPause(int tab) {
		selectedItems[tab] = generateSelectItems(viewListItems[tab]);
		addCmds(7, tab);
	}
	@Override
	public void onDialogFileListPlay(int tab) {
		selectedItems[tab] = generateSelectItems(viewListItems[tab]);
		addCmds(7, tab);
	}

	@Override
	public void onDialogBookMarkAdd2PlayList() {
		/**
		 * 收藏的文件或文件夹可能已经被删除了，但是这样不用检测这个错误
		 * 因为在ListPlay状态连续检测到三次文件错误就会停止播放
		 */
		bookMarkSelectedItems = generateSelectItems(bookMarkItems);
		addCmds(8, LocalConst.TAB_BOOKMARK);
	}


	@Override
	public void onDialogBookMarkCopy2Left() {
		/**
		 * 收藏的文件或文件夹可能已经被删除了，但是这样不用检测这个错误
		 * 因为copyFilesValidity()里面会检测
		 */
		bookMarkSelectedItems = generateSelectItems(bookMarkItems);
		addCmds(9, LocalConst.TAB_BOOKMARK);
		Toast.makeText(this, "从收藏拷贝到左窗口!",
				Toast.LENGTH_LONG).show();
	}


	@Override
	public void onDialogBookMarkCopy2Right() {
		bookMarkSelectedItems = generateSelectItems(bookMarkItems);
		addCmds(10, LocalConst.TAB_BOOKMARK);
		Toast.makeText(this, "从收藏拷贝到右窗口!",
				Toast.LENGTH_LONG).show();
	}

	@Override
	public void onDialogBookMarkMove2Left() {
		bookMarkSelectedItems = generateSelectItems(bookMarkItems);
		addCmds(11, LocalConst.TAB_BOOKMARK);
	}


	@Override
	public void onDialogBookMarkMove2Right() {
		bookMarkSelectedItems = generateSelectItems(bookMarkItems);
		addCmds(12, LocalConst.TAB_BOOKMARK);
	}

	@Override
	public void onDialogBookMarkDel() {
		// TODO Auto-generated method stub
		Iterator<LvRow> iter = bookMarkItems.iterator();
		while (iter.hasNext()) {
			if (iter.next().getSelected() == true)
				iter.remove();
		}
		bookMarkArrayAdapter.notifyDataSetChanged();
		
	}


	@Override
	public void onDialogPlayListAdd(int plTab) {
		LinkedList<LvRow> selectedItems = generateSelectItems(playListItems[plTab]);

		if(selectedItems.size() == 0)
			return;
		
		for (LvRow lr : selectedItems) {			
			bookMarkItems.add(lr);			
		}
		
		Toast.makeText(this, "您添加了收藏： " + pathTrim4Show(selectedItems.getFirst().getPath())+"等等", Toast.LENGTH_LONG).show();
		
		bookMarkArrayAdapter.notifyDataSetChanged();		
	}

	@Override
	public void onDialogPlayListDel(int plTab) {
		// TODO Auto-generated method stub
		Iterator<LvRow> iter = playListItems[plTab].iterator();
		while (iter.hasNext()) {
			if (iter.next().getSelected() == true)
				iter.remove();
		}
		playListArrayAdapter[plTab].notifyDataSetChanged();
		
	}

	@Override
	public void onDialogPlayListSeq() {
		// TODO Auto-generated method stub
		mService.playSeqSwitch();
		mService.sendNotification();
	}
	public int getPlaySeq(){
		return mService.getPlaySeq();
	}


}
