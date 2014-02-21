package com.maxproj.android.dirplayer;

//import io.vov.vitamio.MediaPlayer;
//import io.vov.vitamio.widget.VideoView;
//import io.vov.vitamio.widget.MediaController;
//import io.vov.vitamio.widget.MediaController.MediaPlayerControl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

import android.R.color;
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
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.Preference;
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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.LinearLayout;
//import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.ShareActionProvider;
import android.widget.Toast;
import android.widget.VideoView;

import com.maxproj.android.dirplayer.PlayService.LocalBinder;

public class DirPlayerActivity extends FragmentActivity implements
		ActionBar.TabListener, FragmentListview.FragmentListviewInterface,
		FragmentBookMark.FragmentBookMarkInterface,
		FragmentPlayList.FragmentPlayListInterface
		{

	
	ActionBar actionBar = null;
	
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
		int cmd;
		File src;
		String desPath;
		Boolean force; // 如果目标存在，要不要强制替换
		int fresh;// 0:不刷新，1：刷新右边，2：刷新左边，3：两边都刷新

		public FileCmd(File src, String desPath, Boolean force, int cmd,
				int fresh) {
			this.src = src;
			this.desPath = desPath;
			this.force = force;
			this.cmd = cmd;
			this.fresh = fresh;
		}
	}

	static LinkedList<FileCmd> cmdList = new LinkedList<FileCmd>();

	static Handler cmdHandler;

//	final int CMD_COPY = 1;
//	final int CMD_MOVE = 2;
//	final int CMD_DELETE = 3;
//	final int CMD_FRESH = 4;
//	final int CMD_MKDIR = 5;
//	final int CMD_PLAY = 6;
	
//	int tabCount = 2; // 左右两个窗口

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
	LinkedList<LvRow> playListItems = new LinkedList<LvRow>();
	FragmentPlayList fragmentPlayList = FragmentPlayList.newInstance();
	MyArrayAdapter playListArrayAdapter;
	
	
	/**
	 * PlayService音频播放
	 */
    PlayService mService;
    boolean mBound = false;    

    int playStatus = 0; //播放状态
    int playType = 0; //播放类型
    int playListIndex = 0; //列表下标
    int playListItemIndex = 0; //播放下标
    String playListPath; //列表播放的路径
    String fileListPath; //文件播放的路径
	
	/**
	 * 书签窗口相关定义
	 */
	FragmentBookMark fragmentBookMark = FragmentBookMark.newInstance();
	LinkedList<BookMarkRow> bookMarkItems = new LinkedList<BookMarkRow>();
	BookMarkArrayAdapter bookMarkArrayAdapter;// = new
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

	int lastWinTab = 0; //最近使用的窗口，可能是右窗口，也可能是左窗口

	SharedPreferences sharedPref;
	SharedPreferences.Editor prefEditor;

	DialogFragmentProgress dfp = null; //进度条

	boolean showCopyProcess;
	public void setShowCopyProcess(boolean enable){
		showCopyProcess = enable;
	}

	/**
	 * 书签用文本文件保存
	 * 每个书签占一行
	 */
	private void getBookMarkList() {
		String line;
		File bookmark = new File(getFilesDir(),
				getString(R.string.bookmark_file));

		bookMarkItems.clear();
		try {
			BufferedReader br = new BufferedReader(new FileReader(bookmark));

			while ((line = br.readLine()) != null) {
				bookMarkItems.add(new BookMarkRow(line, false));
				Log.d(LocalConst.DTAG, "get book mark from file" + line);
				// Toast.makeText(this, "您获取了书签： " + line, Toast.LENGTH_LONG)
				// .show();
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (Exception e){
			Log.d(LocalConst.FRAGMENT_LIFE, "getBookMarkList: "+e.toString());
		}
	}

	private void appendBookMark2File(BookMarkRow bmr) {

		File bookmark = new File(getFilesDir(),
				getString(R.string.bookmark_file));

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(bookmark,
					true));
			bw.write(bmr.getPath() + "\n");
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void saveBookMark2File() {

		File bookmark = new File(getFilesDir(),
				getString(R.string.bookmark_file));

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(bookmark));
			for (BookMarkRow bmr : bookMarkItems) {
				bw.write(bmr.getPath() + "\n");
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void onFragmentBookMarkClicked(int i) {
		Log.d(LocalConst.DTAG, "onFragmentBookMarkClicked " + i);

		updateDirInfor(bookMarkItems.get(i).getPath(), lastWinTab);
		mViewPager.setCurrentItem(lastWinTab);
	}

	public void onFragmentBookMarkButton1() {
		Log.d(LocalConst.DTAG, "onFragmentBookMarkButton1 ");
		// 全选
		for (BookMarkRow bmr : bookMarkItems) {
			bmr.setSelected(true);
		}
		bookMarkArrayAdapter.notifyDataSetChanged();
	}

	public void onFragmentBookMarkButton2() {
		Log.d(LocalConst.DTAG, "onFragmentBookMarkButton2 ");
		// 全清
		for (BookMarkRow bmr : bookMarkItems) {
			bmr.setSelected(false);
		}
		bookMarkArrayAdapter.notifyDataSetChanged();

	}

	public void onFragmentBookMarkButton3() {
		Log.d(LocalConst.DTAG, "onFragmentBookMarkButton3 ");
		// 反选
		for (BookMarkRow bmr : bookMarkItems) {
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
		
	}

	public void onFragmentBookMarkButton5() {
		Log.d(LocalConst.DTAG, "onFragmentBookMarkButton5 ");
		// 下移
		

	}
	
	public void onFragmentBookMarkButton6() {
		Log.d(LocalConst.DTAG, "onFragmentBookMarkButton6 ");
		// 删除
		Iterator<BookMarkRow> iter = bookMarkItems.iterator();
		while (iter.hasNext()) {
			if (iter.next().getSelected() == true)
				iter.remove();
		}
		bookMarkArrayAdapter.notifyDataSetChanged();

		saveBookMark2File();
	}
	private void updateBookMarkInfor() {
		// 本函数和bookMarkArrayAdapter.notifyDataSetChanged()有什么区别？回头想下能否去掉

		bookMarkArrayAdapter = new BookMarkArrayAdapter(this,
				R.layout.bookmark_row, bookMarkItems);
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
			String mime = URLConnection.getFileNameMap().getContentTypeFor(f.getName());
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
					mService.play(f, LocalConst.SinglePlay); // clearMusicPlaying() will be called
					Log.d(LocalConst.DTAG, "audio/video: after mService.play(f, null)");
					// mediaController.show(); // 开始播放时是否要显示一下控制面板？
					Log.d(LocalConst.DTAG, "audio/video: after mediaController.show()");
				}
			}
			else if (mime.startsWith("video/")){
				// 设置当前为视频播放状态
				dirPlayerState = LocalConst.STATE_VIDEO; 

				// 如果有音频或其他再播放，停止，防止声音冲突
				if (mService != null){					
					// stop first
					mService.clearMusicPlaying();
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
				
				vv.start();
				Log.d(LocalConst.DTAG, "audio/video: after vv.start()");
			}
			
			// 文本文件直接查看
			// 网页文件用浏览器打开
		}
	}

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
			if (!lr.getName().equals("/.."))
				lr.setSelected(true);
		}
		myArrayAdapter[tab].notifyDataSetChanged();

	}

	public void onFragmentButton2(int tab) {
		Log.d(LocalConst.DTAG, "Button2 clicked in fragment " + tab);
		// Toast.makeText(this, "Button2 clicked in fragment " + tab,
		// Toast.LENGTH_LONG).show();

		// 全清
		for (LvRow lr : viewListItems[tab]) {
			if (!lr.getName().equals("/.."))
				lr.setSelected(false);
		}
		myArrayAdapter[tab].notifyDataSetChanged();
	}

	public void onFragmentButton3(int tab) {
		Log.d(LocalConst.DTAG, "Button3 clicked in fragment " + tab);
		// Toast.makeText(this, "Button3 clicked in fragment " + tab,
		// Toast.LENGTH_LONG).show();

		// 反选
		for (LvRow lr : viewListItems[tab]) {
			if (!lr.getName().equals("/..")){
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

	public void onFragmentButton5(int tab) {
		Log.d(LocalConst.DTAG, "Button5 clicked in fragment " + tab);
		Toast.makeText(this, "您添加了收藏： " + currentPath[tab], Toast.LENGTH_LONG)
		.show();

		// 书签
		for (BookMarkRow bmr : bookMarkItems) {
			if (bmr.getPath().equals(currentPath[tab]))
				return;
		}

		BookMarkRow bmr = new BookMarkRow(currentPath[tab]);
		bookMarkItems.add(bmr);
		appendBookMark2File(bmr);
		updateBookMarkInfor();

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

		new DialogFragment() {

			@Override
			public Dialog onCreateDialog(Bundle savedInstanceState) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						getActivity());
				builder.setTitle(R.string.prompt)
						.setItems(R.array.cmdList,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int cmdIndex) {
										addCmds(cmdIndex, currentTab);
									}
								})
							.setNegativeButton(R.string.negative, null);
				return builder.create();
			}
		}.show(getSupportFragmentManager(), "");

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
						moveFiles(fc.src, fc.desPath, fc.force);
						break;
					case LocalConst.CMD_DELETE:
						deleteFiles(fc.src);
						break;
					case LocalConst.CMD_FRESH:
						Log.d(LocalConst.DTAG, "fresh window: " + fc.fresh);
						updateDirInfor(currentPath[fc.fresh], fc.fresh);
						cmdHandler.sendEmptyMessage(1);//其他命令的结束都有这个激活next指令
						break;
					case LocalConst.CMD_MKDIR:
						Log.d(LocalConst.DTAG, "mkdir at tab: " + fc.fresh);
						mkDir(fc.desPath, fc.force);
						break;
					case LocalConst.CMD_PLAY:
						Log.d(LocalConst.DTAG, "mkdir at tab: " + fc.fresh);
						//servicePlay();
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
		if (src.getParent().equals(des)){ //父目录不能等于目标，否则相当于在原地拷贝
			return false;
		}else if(des.startsWith(src.getPath())){//源包含了目标
			return false;
		}else if(src.getPath().equals(des)){
			return false;
		}
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
		
		if(!copyFilesValidity(srcFile,desPathStr)){
			Log.d(LocalConst.DTAG, "Handler copyFile: validaity check failed: " + srcFile.getPath()
					+ " to " + desPathStr);
			
			/**
			 * 如果不合法，停止后续所有操作
			 */
			Log.d(LocalConst.DTAG, "dir copy: cmdList.clear() in copyFiles()"+ Thread.currentThread().getStackTrace()[1].getLineNumber());
			cmdList.clear();
			return;
		}
		
		
		/**
		 * 如果存在同名的File，并且force被设置，直接删除同名的File
		 * 否则停止本File的拷贝，但是继续后面的拷贝
		 */
		File desFile = new File(desPathStr, srcFile.getName());
		if (desFile.exists()) {
			if (force == true) {// 覆盖同名文件
				Log.d(LocalConst.DTAG,
						"Handler copyFile: delete file before copy");
				deleteFileOrDir(desFile);
			} else {
				Log.d(LocalConst.DTAG, "Handler copyFile: do nothing");
				
				//什么也不做，但是激活后面的操作
				cmdHandler.sendEmptyMessage(1);
				return;
			}
		}
		
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
	}

	/**
	 * 拷贝目录里面的各项子文件或子目录 
	 */
	public void copyFilesInDir(File srcDirFile, String desDirStr, boolean force){
		
		Log.d(LocalConst.DTAG, "dir copy: copyFilesInDir()");
		
		File srcFiles[] = srcDirFile.listFiles();
		for (File f : srcFiles) {
			Log.d(LocalConst.DTAG, "dir copy: copyFilesInDir() add "+f.getName());
			cmdList.addFirst(new FileCmd(f, desDirStr, force, LocalConst.CMD_COPY, 1));
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
	
	private void moveFiles(File srcFile, String desPathStr, Boolean force)
			throws IOException {
		
		if(!moveFilesValidity(srcFile,desPathStr)){
			Log.d(LocalConst.DTAG, "moveFiles: validaity check failed: " + srcFile.getPath()
					+ " to " + desPathStr);
			
			/**
			 * 如果不合法，停止后续所有操作
			 */
			//Log.d(LocalConst.DTAG, "dir copy: cmdList.clear() in moveFiles()"
			//		+ Thread.currentThread().getStackTrace()[1].getLineNumber());
			cmdList.clear();
			return;
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
				return;
			}
		}
		
		boolean result = srcFile.renameTo(desFile);
		cmdHandler.sendEmptyMessage(1); // let Handler move to next
										// cmd
		if (result != true) {
			throw new IOException();
		}
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
	 * 添加一个File到播放列表
	 * @param file
	 */
	private void addToPlayList(File f) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
		
		if (f.isFile()){//file
			//直接添加;
			String mime = URLConnection.getFileNameMap().getContentTypeFor(f.getName());
			/**
			 * 只加音乐文件
			 */
			if (mime !=null){
				if(mime.startsWith("audio/")){	
					LvRow lr = new LvRow("" + f.getName(), "" + f.length(), ""
						+ sdf.format(f.lastModified()), f, false, 2, mime, LocalConst.clear);
					playListItems.add(lr);
				}
			}
		}else if(f.isDirectory()){//dir
			File files[] = f.listFiles();
			for (File subFile : files) {
				addToPlayList(subFile);
			}
		}
	}
	
	private void updatePlayListAdapter()
	{
		Log.d(LocalConst.DTAG, "updatePlayListAdapter()");
		playListArrayAdapter = new MyArrayAdapter(this, R.layout.file_row,
				playListItems);

		if (fragmentPlayList != null) {
			fragmentPlayList.setListviewAdapter(playListArrayAdapter);
		}
	}
	
	/**
	 * 这里做每一次cmd操作之前的准备
	 */
	public void cmdPrepare(){
		/**
		 * 先读取配置文件
		 */
		
		
		setShowCopyProcess(true);
	}
	public void cancelCopy(){

		Iterator<FileCmd> iter = cmdList.iterator();
		while (iter.hasNext()) {
			if (iter.next().cmd == LocalConst.CMD_COPY)
				iter.remove();
		}
	}
	/*
	 * 将用户命令添加到list 
	 */
	private void addCmds(int cmdPosition, int tab) {
		// Toast.makeText(
		// this,
		// "cmd: " + cmdIndex + ", path A: " + currentPath[0]
		// + ", path B: " + currentPath[1], Toast.LENGTH_LONG)
		// .show();
		Log.d(LocalConst.DTAG, "cmd: " + cmdPosition + ", path A: " + currentPath[0]
				+ ", path B: " + currentPath[1]);

		for (int i = 0; i < 2; i++) {// A是0，B是1

			selectedItems[i].clear();
			for (LvRow lr : viewListItems[i]) {
				if (lr.getSelected() == true) {
					selectedItems[i].add(lr);
					Log.d(LocalConst.DTAG,
							"operateMenu(): tab " + i + " selected: "
									+ lr.getName());
				}
			}
		}
		
		Log.d(LocalConst.DTAG, "dir copy: cmdList.clear() in addCmds()"
				+ Thread.currentThread().getStackTrace()[1].getLineNumber());
		
		cmdList.clear();
		cmdPrepare();
		
		// try {
		switch (cmdPosition) {
		/**
		 * 左窗口 == A窗口 == 数组下标0 == tab 0
		 * 右窗口 == B窗口 == 数组下边1 == tab 1
		 * 注意移动时需要刷新两边
		 */
		case 0: // 从A拷贝到B
			for (LvRow lr : selectedItems[0]) {
				cmdList.add(new FileCmd(lr.getFile(), currentPath[1], true,
						LocalConst.CMD_COPY, 0));
			}
			cmdList.add(new FileCmd(null, null, true, LocalConst.CMD_FRESH, 1));
			break;
		case 1: // 从A移动到B
			for (LvRow lr : selectedItems[0]) {
				cmdList.add(new FileCmd(lr.getFile(), currentPath[1], true,
						LocalConst.CMD_MOVE, 0));
			}
			cmdList.add(new FileCmd(null, null, true, LocalConst.CMD_FRESH, 0));// 两边都刷新
			cmdList.add(new FileCmd(null, null, true, LocalConst.CMD_FRESH, 1));
			break;
		case 2: // 从B拷贝到A
			for (LvRow lr : selectedItems[1]) {
				cmdList.add(new FileCmd(lr.getFile(), currentPath[0], true,
						LocalConst.CMD_COPY, 0));
			}
			cmdList.add(new FileCmd(null, null, true, LocalConst.CMD_FRESH, 0));
			break;
		case 3: // 从B移动到A
			for (LvRow lr : selectedItems[1]) {
				cmdList.add(new FileCmd(lr.getFile(), currentPath[0], true,
						LocalConst.CMD_MOVE, 0));
			}
			cmdList.add(new FileCmd(null, null, true, LocalConst.CMD_FRESH, 0));
			cmdList.add(new FileCmd(null, null, true, LocalConst.CMD_FRESH, 1));
			break;
		case 4: // 删除当前窗口所选
			for (LvRow lr : selectedItems[tab]) {
				cmdList.add(new FileCmd(lr.getFile(), null, false, LocalConst.CMD_DELETE,
						0));
			}
			cmdList.add(new FileCmd(null, null, true, LocalConst.CMD_FRESH, tab));
			break;
		case 5: // 创建文件夹
			cmdList.add(new FileCmd(null, currentPath[tab], false, LocalConst.CMD_MKDIR, tab));
			cmdList.add(new FileCmd(null, null, true, LocalConst.CMD_FRESH, tab));
			Log.d(LocalConst.DTAG, "cmdList.size(): "+cmdList.size());
			break;
		case 6: // 添加到播放列表
			for (LvRow lr : selectedItems[tab]) {
				addToPlayList(lr.getFile());
			}
			savePlayList2File();
			updatePlayListAdapter();
			Log.d(LocalConst.DTAG, "cmdList.size(): "+cmdList.size());
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

			// Log.d(TAG_DEBUG, "3.2 should not come here if Exception");
			constructViewList(tab);

//			Log.d(LocalConst.FRAGMENT_LIFE, "check viewListItems[" + tab + "]:"
//					+ viewListItems[tab].toString());
			myArrayAdapter[tab] = new MyArrayAdapter(this, R.layout.file_row,
					viewListItems[tab]);
			Log.d(LocalConst.FRAGMENT_LIFE, "after new MyArrayAdapter");

			if (fragmentListview[tab] != null) {
				fragmentListview[tab].setListviewAdapter(myArrayAdapter[tab],
						currentPath[tab]);
				
				//debug, should be deleted
				//fragmentPlayList.setListviewAdapter(myArrayAdapter[tab]);
				
				Log.d(LocalConst.FRAGMENT_LIFE,
						"fragmentListview.setListviewAdapter(myArrayAdapter)!");
			} else {
				Log.d(LocalConst.FRAGMENT_LIFE, "fragmentListview is null!");
			}
			// Log.d(TAG_DEBUG, "show path: " + parentPath + f.getName());
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
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
		String date;

		// viewListFiles.clear();
		viewListItems[tab].clear();
		if (!currentPath[tab].equals(LocalConst.pathRoot)) {
			Log.d(LocalConst.DTAG, "add parentPath: " + parentPath[tab]);
			// viewListFiles.add(new File(parentPath));
			LvRow lr = new LvRow("/..", "", "", new File(parentPath[tab]), false, 0, null, LocalConst.clear);
			viewListItems[tab].add(lr);
		}
		Log.d(LocalConst.DTAG, "fillList loop end 2");

		for (File f : dirList[tab]) {
			// viewListFiles.add(f);
			LvRow lr = new LvRow("/" + f.getName(), "", ""
					+ sdf.format(f.lastModified()), f, false, 1, null, LocalConst.clear);
//			Log.d(LocalConst.DTAG, "add directory: " + lr.getName());
			viewListItems[tab].add(lr);
		}
		for (File f : fileList[tab]) {
			// viewListFiles.add(f);
			LvRow lr = new LvRow("" + f.getName(), "" + f.length(), ""
					+ sdf.format(f.lastModified()), f, false, 2, 
					URLConnection.getFileNameMap().getContentTypeFor(f.getName()), LocalConst.clear);
//			Log.d(LocalConst.DTAG, "add file: " + lr.getName());
			viewListItems[tab].add(lr);
		}
		// System.out.println(viewListItems);
		Log.d(LocalConst.DTAG, "viewListItems is constructed!");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dir_player);
		
		Log.d(LocalConst.FRAGMENT_LIFE, "activity onCreate() begin!");
		
		/**
		 * 保存上下文
		 */
		LocalConst.app = getApplicationContext();
		LocalConst.dirPlayerActivity = this;
		
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

		/**
		 * 初始化所有窗口的fragment/adapter/listview
		 * 好像多初始化了一个fragment，以后改
		 */
		
		Log.d(LocalConst.FRAGMENT_LIFE, "fragment initial code begin...");
		for (int i = 0; i < LocalConst.tabCount; i++) {
			viewListItems[i] = new LinkedList<LvRow>();
			selectedItems[i] = new LinkedList<LvRow>();
			dirList[i] = new LinkedList<File>();
			fileList[i] = new LinkedList<File>();
			currentPath[i] = LocalConst.pathRoot;
			parentPath[i] = null;
			fragmentListview[i] = FragmentListview.newInstance(i);
			
			/**
			 * 以下设置adapter似乎没有必要
			 */
//			myArrayAdapter[i] = new MyArrayAdapter(this, R.layout.file_row,
//					viewListItems[i]);
//			Log.d(LocalConst.FRAGMENT_LIFE, "currentPath["+i+"]" + currentPath[i]);
//			fragmentListview[i].setListviewAdapter(myArrayAdapter[i],
//					currentPath[i]);
		}
		Log.d(LocalConst.FRAGMENT_LIFE, "fragment initial code ended!");
		
		ImageView bottomIcon = (ImageView)findViewById(R.id.bottom_icon);
		bottomIcon.setImageResource(R.drawable.bottom);
		Log.d(LocalConst.FRAGMENT_LIFE, "activity onCreate() end!"); // 要检查这个，这样才知道是否初始化全部完成
	}

    @Override
    protected void onStart() {
        super.onStart();
        
        Log.d(LocalConst.FRAGMENT_LIFE, "activity onStart() begin!");
        
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
		 * 音视频媒体相关初始化
		 */
		mainWindow = findViewById(R.id.main_window); // 停放controller的地方
		
		mediaPlayerControlInit(); // 生成音频控制接口		
		
		// 音频控制器使用android自带的
		mediaController = new android.widget.MediaController(this);
		mediaController.setMediaPlayer(mediaPlayerControl);
		mediaController.setAnchorView(mainWindow);
		mediaController.setEnabled(true);					

		// 视频控制器使用vitamio内含的
		vv = (VideoView)findViewById(R.id.videoview); // 视频播放窗口
		videoController = new MediaController(this);
		//videoController.setMediaPlayer(videoPlayerControl);
		//videoController.setAnchorView(mainWindow);
		//videoController.setEnabled(true);
		vv.setMediaController(videoController);
		vv.setOnTouchListener(vvOnTouchListener);
		Log.d(LocalConst.FRAGMENT_LIFE, "activity onStart() after vv.setOnTouchListener()!");
		
		/**
		 * 播放列表
		 */
		getPlayList();
		updatePlayListAdapter();
		Log.d(LocalConst.FRAGMENT_LIFE, "activity onStart() after updatePlayListAdapter()!");
		/**
		 * 启动音频播放service
		 */
        // Bind to LocalService
        bindService(new Intent(this, PlayService.class), mConnection, Context.BIND_AUTO_CREATE);
		startService(new Intent(this, PlayService.class));
		
		/**
		 * 通过广播接收service的播放信息，比如当前曲目
		 */
        // The filter's action is BROADCAST_ACTION
        IntentFilter mStatusIntentFilter = new IntentFilter(
        		LocalConst.BROADCAST_ACTION);
    
        // Adds a data filter for the HTTP scheme
        //mStatusIntentFilter.addDataScheme("http");
        ServiceInforReceiver mServiceInforReceiver =
                new ServiceInforReceiver();
        // Registers the DownloadStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(this).registerReceiver(
        		mServiceInforReceiver,
                mStatusIntentFilter);
        
        Log.d(LocalConst.FRAGMENT_LIFE, "activity onStart() end!"); // 要检查这个，这样才知道是否初始化全部完成
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }
	
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
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
				Log.d(LocalConst.DTAG,"onScroll().getY(): "+e1.getY()
						+ ", bottom is: "+ (widthHeightInPixels[1] - 20));
				
				if (e1.getY() > (widthHeightInPixels[1] - 20)) {
					Log.d(LocalConst.DTAG,"onScroll() find scroll from bottom!");
					if(
							(dirPlayerState == LocalConst.STATE_MUSIC)
							&&(mService!=null)
//							&&(mService.isPlaying())
							&&(mediaController != null)){
						mediaController.show();
					}
					return true;
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
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.dir_player, menu);
		

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
		mViewPager.setCurrentItem(tab.getPosition());
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

			if (position == 3) { // 第四个tab为播放列表
				Log.d(LocalConst.FRAGMENT_LIFE, "getItem(" + position + ") return fragmentPlayList");
				return fragmentPlayList;
			}
			if (position == 2) { // 第三个tab为标签
				Log.d(LocalConst.FRAGMENT_LIFE, "getItem(" + position + ") return fragmentBookMark");
				return fragmentBookMark;
			} else { // 第一个tab为左窗口，第二个tab为右窗口
				Log.d(LocalConst.FRAGMENT_LIFE, "getItem(" + position + ") return fragmentListview");
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
			return 4;
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

	public void onFragmentPlayListClicked(int i) {
		Log.d(LocalConst.DTAG, "onFragmentPlayListClicked: " + i);
		
		dirPlayerState = LocalConst.STATE_MUSIC;
		
		if(mService != null){
			clearVideoViewPlaying();
			mService.playList(i);
			//mediaController.show();
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
		for (LvRow lr : playListItems) {
			lr.setSelected(true);
		}
		playListArrayAdapter.notifyDataSetChanged();
	}

	public void onFragmentPlayListButton2() {
		// 全清
		for (LvRow lr : playListItems) {
			lr.setSelected(false);
		}
		playListArrayAdapter.notifyDataSetChanged();

	}

	public void onFragmentPlayListButton3() {
		// 反选
		for (LvRow lr : playListItems) {
			if (lr.getSelected() == true)
				lr.setSelected(false);
			else
				lr.setSelected(true);
		}
		playListArrayAdapter.notifyDataSetChanged();

	}

	public void onFragmentPlayListButton4() {
		// 上移
		Log.d(LocalConst.DTAG, "move up ....");
		if (playListItems.size() < 2)
			return;
		
		for(int i = 0;i < playListItems.size() - 1 ;i++){
			Log.d(LocalConst.DTAG, "compare in pair: " + i);
			// 用冒泡的方式来实现被选择项上移
			// 首先获取一对
			LvRow lr_first = playListItems.get(i);
			if (lr_first.getSelected()){
				Log.d(LocalConst.DTAG, "the first in pair is selected");
				continue; // 如果第一个被选择了，无论第二个是什么，什么也不做
			}
			
			LvRow lr_second = playListItems.get(i+1);
			if (!lr_second.getSelected()){
				Log.d(LocalConst.DTAG, "the second in pair is not selected");
				continue; // 由于第一个没有被选择，第二个如果也没被选择，那么什么也不做
			}
			
			// 第二个被选择了，所以要交换
			LvRow lr = playListItems.remove(i+1);
			playListItems.add(i,lr);
			
			Log.d(LocalConst.DTAG, "move up in pair: " + i);
		}
		
		playListArrayAdapter.notifyDataSetChanged();

		savePlayList2File();
	}
	public void onFragmentPlayListButton5() {
		// 下移
		Log.d(LocalConst.DTAG, "move down ....");
		if (playListItems.size() < 2)
			return;
		
		for(int i = playListItems.size() - 1; i > 0;i--){
			Log.d(LocalConst.DTAG, "compare in pair: " + i);
			// 用冒泡的方式来实现被选择项上移
			// 首先获取一对
			LvRow lr_second = playListItems.get(i);
			if (lr_second.getSelected()){
				Log.d(LocalConst.DTAG, "the second in pair is selected");
				continue; // 如果第二个被选择了，无论第一个是什么，什么也不做
			}

			LvRow lr_first = playListItems.get(i-1);
			if (!lr_first.getSelected()){
				Log.d(LocalConst.DTAG, "the first in pair is not selected");
				continue; // 由于第二个没有被选择，第一个如果也没被选择，那么什么也不做 
			}
			
			
			// 第一个被选择了，所以要交换
			LvRow lr = playListItems.remove(i-1);
			playListItems.add(i,lr);
			
			Log.d(LocalConst.DTAG, "move down in pair: " + i);
		}
		playListArrayAdapter.notifyDataSetChanged();

		savePlayList2File();
	}
	public void onFragmentPlayListButton6() {
		// 删除
		Iterator<LvRow> iter = playListItems.iterator();
		while (iter.hasNext()) {
			if (iter.next().getSelected() == true)
				iter.remove();
		}
		playListArrayAdapter.notifyDataSetChanged();

		savePlayList2File();

	}
	public void onFragmentPlayListButton7() {
		// 操作
		Iterator<LvRow> iter = playListItems.iterator();
		while (iter.hasNext()) {
			if (iter.next().getSelected() == true)
				iter.remove();
		}
		playListArrayAdapter.notifyDataSetChanged();

		savePlayList2File();
	}
    public PlayService getServiceConnection(){
    	return mService;
    }
    
	/**
	 * 播放目录用什么格式保存呢？
	 */
	private void getPlayList() {
		getListFromFile(playListItems, getString(R.string.playlist_file));
	}
	
	private void getListFromFile(LinkedList<LvRow> list, String fileName) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
		String line;
		File listFile = new File(getFilesDir(),fileName);

		list.clear();
		try {
			BufferedReader br = new BufferedReader(new FileReader(listFile));

			while ((line = br.readLine()) != null) {
				File f = new File(line);
				LvRow lr = new LvRow("" + f.getName(), "" + f.length(), ""
						+ sdf.format(f.lastModified()), f, false, 2, 
						URLConnection.getFileNameMap().getContentTypeFor(f.getName()), LocalConst.clear);
				list.add(lr);
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (Exception e){
			Log.d(LocalConst.FRAGMENT_LIFE, "listItems:" + e.toString());
		}
	}

	/**
	 * 加在链表的末尾
	 * 
	 */
	private void appendPlayList2File(LvRow lr) {
		appendList2File(lr, getString(R.string.playlist_file));
	}
	private void appendList2File(LvRow lr, String fileName) {
		File listFile = new File(getFilesDir(),fileName);

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(listFile,
					true));
			bw.write(lr.getFile().getPath() + "\n");
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 暂时每次都存
	 * 以后改成onStop的时候存
	 */
	private void savePlayList2File() {
		saveList2File(playListItems, getString(R.string.playlist_file));
		
		if(mService != null)
			mService.updatePlayList();
	}
	private void saveList2File(LinkedList<LvRow> list, String fileName) {
		File listFile = new File(getFilesDir(),fileName);

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(listFile));
			for (LvRow lr : list) {
				bw.write(lr.getFile().getPath() + "\n");
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private class ServiceInforReceiver extends BroadcastReceiver
	{
		private ServiceInforReceiver(){ // Prevents instantiation
		}
		
	    // Called when the BroadcastReceiver gets an Intent it's registered to receive
		@Override
	    public void onReceive(Context context, Intent intent) {
			/**
			 * 接收如下信息
			 * 		播放状态：正在播放、暂停、停止、清除
			 * 		播放类型：文件播放、列表播放
			 * 		播放路径：被播放的路径
			 * 		播放列表：以后可能多个列表
			 * 		播放下标：播放列表中的下标
			 */
			playStatus =  intent.getIntExtra(LocalConst.PLAY_STATUS, -1);
			playType = intent.getIntExtra(LocalConst.PLAY_TYPE, -1);
			if(playType == LocalConst.SinglePlay){
				fileListPath = intent.getStringExtra(LocalConst.FILELIST_PATH);
				
				/**
				 * 更新左右窗口的文件列表
				 * 也即更新listview的adapter
				 * 这里应该主动更新
				 */
				for (int i = 0; i < LocalConst.tabCount; i++) {
					for(LvRow lr:viewListItems[i]){
						if(lr.getFile().getPath().equals(fileListPath)){
							lr.setPlayingStatus(playStatus);
						}else{
							lr.setPlayingStatus(LocalConst.clear);
						}
					}
					/**
					 * 有两种更新方法，这里需要测试一下
					 * 其一是notifyDataSetChanged()-----发现这个可以
					 * 其二是重新设置adapter
					 */
					
					myArrayAdapter[i].notifyDataSetChanged();
				}
				Log.d(LocalConst.DTAG, "audio/video single: " + playStatus + " " + playType + " " + fileListPath);
				return;
			}else if(playType == LocalConst.ListPlay){
				playListPath = intent.getStringExtra(LocalConst.PLAYLIST_PATH);
				playListIndex = intent.getIntExtra(LocalConst.PLAYLIST_INDEX, 0);
				playListItemIndex = intent.getIntExtra(LocalConst.PLAYLIST_ITEM_INDEX, 0);

				Log.d(LocalConst.DTAG, "audio/video list: " + playStatus + " " + playType + " " + playListPath + " - " + playListIndex + " - " + playListItemIndex);
				/**
				 * 更新播放列表
				 * 更新播放路径
				 * 主动更新
				 */
				if(fragmentPlayList != null){
					fragmentPlayList.setPathView(
						getResources().getString(R.string.pl_path) + playListPath);
				}
				for (LvRow lr : playListItems) {
					if(lr.getFile().getPath().equals(playListPath)){
						lr.setPlayingStatus(playStatus);
					}else{
						lr.setPlayingStatus(LocalConst.clear);
					}
				}
				playListArrayAdapter.notifyDataSetChanged();
				return;
			}
	    }
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

	

}
