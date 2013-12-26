package com.maxproj.android.dirplayer;

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
import com.maxproj.android.dirplayer.PlayService.LocalBinder;
import com.maxproj.android.dirplayer.PlayService.ServiceConstants;



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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.GestureDetectorCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.ShareActionProvider;
import android.widget.Toast;
import android.widget.VideoView;
import android.os.Environment;

public class DirPlayerActivity extends FragmentActivity implements
		ActionBar.TabListener, FragmentListview.FragmentListviewInterface,
		FragmentBookMark.FragmentBookMarkInterface,
		FragmentPlayList.FragmentPlayListInterface {

	final static String DTAG = "DirPlayer";

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

	final int CMD_COPY = 1;
	final int CMD_MOVE = 2;
	final int CMD_DELETE = 3;
	final int CMD_FRESH = 4;
	final int CMD_MKDIR = 5;
	final int CMD_PLAY = 6;
	
	int tabCount = 3;
	private static final String pathRoot = Environment
			.getExternalStorageDirectory().getPath();


	/**
	 * 音频播放
	 */
	//MediaPlayer mediaPlayer = null;
	MediaController mediaController = null;
	//这是媒体播放器MediaPlayer和媒体控制器MediaController之间的接口
	MediaPlayerControl mediaPlayerControl;
	
	/**
	 * 视频播放
	 */
	View vBottonControl;
	VideoView vv;
	//注意video和其控制器之间不需接口，而是直接关联
	MediaController videoController = null;
	
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
	FragmentListview[] fragmentListview = new FragmentListview[tabCount];
	MyArrayAdapter[] myArrayAdapter = new MyArrayAdapter[tabCount];
	LinkedList<LvRow>[] viewListItems = new LinkedList[tabCount];
	LinkedList<LvRow>[] selectedItems = new LinkedList[tabCount];
	LinkedList<File>[] dirList = new LinkedList[tabCount];
	LinkedList<File>[] fileList = new LinkedList[tabCount];
	String[] currentPath = new String[tabCount];
	String[] parentPath = new String[tabCount];
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

	int lastTab = 0; //最近使用的窗口，可能是右窗口，也可能是左窗口

	SharedPreferences sharedPref;
	SharedPreferences.Editor prefEditor;

	DialogFragmentProgress dfp = null; //进度条


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
				Log.d(DTAG, "get book mark from file" + line);
				// Toast.makeText(this, "您获取了书签： " + line, Toast.LENGTH_LONG)
				// .show();
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		Log.d(DTAG, "onFragmentBookMarkClicked " + i);

		updateDirInfor(bookMarkItems.get(i).getPath(), lastTab);
		mViewPager.setCurrentItem(lastTab);
	}

	public void onFragmentBookMarkButton1() {
		Log.d(DTAG, "onFragmentBookMarkButton1 ");
		// 全选
		for (BookMarkRow bmr : bookMarkItems) {
			bmr.setSelected(true);
		}
		bookMarkArrayAdapter.notifyDataSetChanged();
	}

	public void onFragmentBookMarkButton2() {
		Log.d(DTAG, "onFragmentBookMarkButton2 ");
		// 全清
		for (BookMarkRow bmr : bookMarkItems) {
			bmr.setSelected(false);
		}
		bookMarkArrayAdapter.notifyDataSetChanged();

	}

	public void onFragmentBookMarkButton3() {
		Log.d(DTAG, "onFragmentBookMarkButton3 ");
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
		Log.d(DTAG, "onFragmentBookMarkButton4 ");
		// 删除
		Iterator<BookMarkRow> iter = bookMarkItems.iterator();
		while (iter.hasNext()) {
			if (iter.next().getSelected() == true)
				iter.remove();
		}
		bookMarkArrayAdapter.notifyDataSetChanged();

		saveBookMark2File();
	}

	public void onFragmentBookMarkButton5() {
		Log.d(DTAG, "onFragmentBookMarkButton5 ");
		// 修改

	}

	private void updateBookMarkInfor() {
		// 本函数和bookMarkArrayAdapter.notifyDataSetChanged()有什么区别？回头想下能否去掉

		bookMarkArrayAdapter = new BookMarkArrayAdapter(this,
				R.layout.bookmark_row, bookMarkItems);
		Log.d(DTAG, "after new BookMarkArrayAdapter");

		if (fragmentBookMark != null) {
			fragmentBookMark.setListviewAdapter(bookMarkArrayAdapter);
			Log.d(DTAG,
					"fragmentBookMark.setListviewAdapter(bookMarkArrayAdapter)!");
		} else {
			Log.d(DTAG, "fragmentBookMark is null!");
		}
	}
	public void onFragmentListviewLongClicked(int i,int tab){
		File f = viewListItems[tab].get(i).getFile();

		if (f.isDirectory()) {
			Log.d(DTAG, "a dir is long clicked in tab: " + tab);

			// 发送这个目录
		} else {
			
			// 发送这个文件
			String mime = URLConnection.getFileNameMap().getContentTypeFor(f.getName());
			Log.d(DTAG, "a file is long clicked in tab " + tab + "which mime is "+ mime);
			Toast.makeText(this, "file type: " + mime,
					Toast.LENGTH_LONG).show();		
			
			// 根据后缀判定文件类型，并生产intent
			
			Intent dealIntent = new Intent();
			dealIntent.setAction(Intent.ACTION_SEND);
			dealIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
			dealIntent.setType(mime);

			startActivity(Intent.createChooser(dealIntent, 
					getResources().getText(R.string.dealby)));
			Log.d(DTAG, "onOptionsItemSelected: cmdList.size() " + cmdList.size());	
			
		}
	}
	public void onFragmentListviewClicked(int i, int tab) {
		LvRow lr = viewListItems[tab].get(i);
		File f = lr.getFile();

		if (lr.getType() != 2) {// 2是文件，1是目录，0是上一级目录
			Log.d(DTAG, "a dir is clicked in tab: " + tab);
			try {
				updateFileInfor(f, tab);
			} catch (Exception e) {
				if (e.getMessage().equals(pathRoot)) {
					updateDirInfor(pathRoot, tab);
				}
			}
		} else {
			Log.d(DTAG, "a file is clicked in tab" + tab);

			// 文件单击时直接打开
			
			String mime = lr.getMime();
			
			if (mime == null){
				// 不认识的格式
				Log.d(DTAG, "mediaPlayer: mime == null, unkown video format");
				return;
			}
			
			// 音频文件处理
			if (mime.startsWith("audio/"))				
			{
				Log.d(DTAG, "mediaPlayer: a MP3 file, play ......");
				mService.play(f, null);
				mediaController.show();
				Log.d(DTAG, "mediaPlayer: after start()");

			}
			else if (mime.startsWith("video/")){
				
				// 如果有音频或其他再播放，停止，防止声音冲突
				if (mService != null){					
					// stop first
					mService.pause();

				    Log.d(DTAG, "mediaPlayer: after clear mediaPlayer()");
				}
				Log.d(DTAG, "mediaPlayer: no mediaPlayer");
				// 方法一
				// 使用intent，发送文件给系统缺省播放器
				// Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
				// intent.setDataAndType(Uri.fromFile(f), mime);
				
				// 方法二
				// 使用VideoView
				vv.setVisibility(View.VISIBLE);
				vv.setVideoURI(Uri.fromFile(f));
				vv.start();
				Log.d(DTAG, "mediaPlayer: vv.start()");
				videoController.show();
				Log.d(DTAG, "mediaPlayer: after mediaController.show()");
			}
			
			// 文本文件直接查看
			// 网页文件用浏览器打开
		}
	}


	public void onFragmentButton1(int tab) {
		Log.d(DTAG, "Button1 clicked in fragment " + tab);
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
		Log.d(DTAG, "Button2 clicked in fragment " + tab);
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
		Log.d(DTAG, "Button3 clicked in fragment " + tab);
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
		Log.d(DTAG, "Button4 clicked in fragment " + tab);
		Toast.makeText(this, "您添加了书签： " + currentPath[tab], Toast.LENGTH_LONG)
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

	public void onFragmentButton5(int tab) {
		Log.d(DTAG, "Button5 clicked in fragment " + tab);
		// Toast.makeText(this, "Button5 clicked in fragment " + tab,
		// Toast.LENGTH_LONG).show();

		// 操作
		commandMenu(tab);
	}

	public void onFragmentButton6(int tab) {
		Log.d(DTAG, "Button6 clicked in fragment " + tab);
		// Toast.makeText(this, "Button5 clicked in fragment " + tab,
		// Toast.LENGTH_LONG).show();

		// 向上
		if ((currentPath[tab].equals(pathRoot))){
			Toast.makeText(this, "只能到这一层啦",
					Toast.LENGTH_LONG).show();			
		}
		else{// 如果已经到顶层了，什么也不做
			updateDirInfor(parentPath[tab], tab);
		}
	}

	private void commandMenu(int tab) { // operate selected files
		Log.d(DTAG, "show Operation Dialog");
		
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

		Log.d(DTAG, "after show Operation Dialogcon");

	}

	/**
	 * Handler顺序处理cmd list
	 * 用空白msg激活
	 */
	private class CmdHandler extends Handler {

		public void handleMessage(Message msg) {
			FileCmd fc = cmdList.pollFirst();
			if (fc != null) { // 如果还有命令没执行完
				Log.d(DTAG, "dir copy: Handler find a cmd: " + fc.cmd);
				try {
					switch (fc.cmd) {
					case CMD_COPY:
						copyFiles(fc.src, fc.desPath, fc.force);
						break;
					case CMD_MOVE:
						moveFiles(fc.src, fc.desPath, fc.force);
						break;
					case CMD_DELETE:
						deleteFiles(fc.src);
						break;
					case CMD_FRESH:
						Log.d(DTAG, "fresh window: " + fc.fresh);
						updateDirInfor(currentPath[fc.fresh], fc.fresh);
						break;
					case CMD_MKDIR:
						Log.d(DTAG, "mkdir at tab: " + fc.fresh);
						mkDir(fc.desPath, fc.force);
						break;
					case CMD_PLAY:
						Log.d(DTAG, "mkdir at tab: " + fc.fresh);
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
			        		Log.d(DTAG, "mkdir onClick()");
			        		
			        		EditText et = (EditText) v.findViewById(R.id.mkdirtext);
			        		Log.d(DTAG, "mkdir find EditText()");
			        		
			        		if(et == null){
			        			Log.d(DTAG, "mkdir find et is null");
			        			return;
			        		}
			        		String dirName = et.getText().toString();
			        		Log.d(DTAG, "mkdir get dir name"+dirName);
			        		
			        		File newDirFile = new File(newDirParantStr, dirName);
			        		try{
			        			newDirFile.mkdir();
			        			cmdHandler.sendEmptyMessage(1); // move Handler to next
			        		}catch(Exception e){
			        			Log.d(DTAG, "mkdir get exception");
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
			Log.d(DTAG, "dir copy: Handler in copyFileTask()");
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

			Log.d(DTAG,
					"dir copy: doInBackground(src: "
							+ files[0].getPath()
							+ " des:" + files[1].getPath() + ")");

			try {
				//如果out路径文件夹不存在，会异常？
				//尝试创建out路径文件夹？
				
				in = new BufferedInputStream(new FileInputStream(files[0]));
				out = new BufferedOutputStream(new FileOutputStream(files[1]));
				Log.d(DTAG, "dir copy: before while");

				while ((b = in.read()) != -1) {
					out.write(b);
					count++;
					countLoop++;
					if (countLoop > 1024) { //每拷贝1k字节更新一次进度条
						countLoop = 0;
						publishProgress((int) ((count / (float) length) * 100));
						Log.d(DTAG, "AsyncTask: publishProgress("
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
			Log.d(DTAG, "dir copy: onProgressUpdate(" + progress + ")");
			if (dfp != null) {
				dfp.setProgress(progress[0].intValue());
			}
		}

		protected void onPreExecute() {
			Log.d(DTAG, "dir copy: onPreExecute()");
			
			// 启动进度条
			dfp = DialogFragmentProgress.newInstance();
			dfp.show(getSupportFragmentManager(), "");
			dfp.setProgress(0);
			dfp.setMsg(msg);
		}

		protected void onPostExecute(Long result) {
			Log.d(DTAG, "dir copy: onPostExecute()");
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
			Log.d(DTAG, "Handler copyFile: validaity check failed: " + srcFile.getPath()
					+ " to " + desPathStr);
			
			/**
			 * 如果不合法，停止后续所有操作
			 */
			Log.d(DTAG, "dir copy: cmdList.clear() in copyFiles()"+ Thread.currentThread().getStackTrace()[1].getLineNumber());
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
				Log.d(DTAG,
						"Handler copyFile: delete file before copy");
				deleteFileOrDir(desFile);
			} else {
				Log.d(DTAG, "Handler copyFile: do nothing");
				
				//什么也不做，但是激活后面的操作
				cmdHandler.sendEmptyMessage(1);
				return;
			}
		}
		
		if (srcFile.isFile()) {// 一个文件
			Log.d(DTAG, "dir copy: srcFile.isFile()");

			//添加拷贝任务
			new copySingleFileAsyncTask(this, srcFile.getName())
			.execute(srcFile, desFile);
			
			// 在AsyncTask里面激活Handler，这里不用激活
			return;
		}
		
		if (srcFile.isDirectory()) {// 一个目录

			Log.d(DTAG, "dir copy: srcFile.isDirectory()");
			
			if(desFile.mkdirs()){
				// 这里拷贝下一级
				copyFilesInDir(srcFile, desFile.getPath(),force);
				
				cmdHandler.sendEmptyMessage(1);
				return;
				
			}else{
				//严重错误，停止后续全部操作				 
				Log.d(DTAG, "dir copy: cmdList.clear() in copyFiles() "
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
		
		Log.d(DTAG, "dir copy: copyFilesInDir()");
		
		File srcFiles[] = srcDirFile.listFiles();
		for (File f : srcFiles) {
			Log.d(DTAG, "dir copy: copyFilesInDir() add "+f.getName());
			cmdList.addFirst(new FileCmd(f, desDirStr, force, CMD_COPY, 1));
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
			Log.d(DTAG, "moveFiles: validaity check failed: " + srcFile.getPath()
					+ " to " + desPathStr);
			
			/**
			 * 如果不合法，停止后续所有操作
			 */
			//Log.d(DTAG, "dir copy: cmdList.clear() in moveFiles()"
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
				Log.d(DTAG,
						"moveFiles: delete file before copy");
				deleteFileOrDir(desFile);
			} else {
				Log.d(DTAG, "moveFiles: do nothing");
				
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
						+ sdf.format(f.lastModified()), f, false, 2, mime);
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
		Log.d(DTAG, "updatePlayListAdapter()");
		playListArrayAdapter = new MyArrayAdapter(this, R.layout.file_row,
				playListItems);

		if (fragmentPlayList != null) {
			fragmentPlayList.setListviewAdapter(playListArrayAdapter);
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
		Log.d(DTAG, "cmd: " + cmdPosition + ", path A: " + currentPath[0]
				+ ", path B: " + currentPath[1]);

		for (int i = 0; i < 2; i++) {// A是0，B是1

			selectedItems[i].clear();
			for (LvRow lr : viewListItems[i]) {
				if (lr.getSelected() == true) {
					selectedItems[i].add(lr);
					Log.d(DTAG,
							"operateMenu(): tab " + i + " selected: "
									+ lr.getName());
				}
			}
		}
		
		Log.d(DTAG, "dir copy: cmdList.clear() in addCmds()"
				+ Thread.currentThread().getStackTrace()[1].getLineNumber());
		
		cmdList.clear();
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
						CMD_COPY, 0));
			}
			cmdList.add(new FileCmd(null, null, true, CMD_FRESH, 1));
			break;
		case 1: // 从A移动到B
			for (LvRow lr : selectedItems[0]) {
				cmdList.add(new FileCmd(lr.getFile(), currentPath[1], true,
						CMD_MOVE, 0));
			}
			cmdList.add(new FileCmd(null, null, true, CMD_FRESH, 0));// 两边都刷新
			cmdList.add(new FileCmd(null, null, true, CMD_FRESH, 1));
			break;
		case 2: // 从B拷贝到A
			for (LvRow lr : selectedItems[1]) {
				cmdList.add(new FileCmd(lr.getFile(), currentPath[0], true,
						CMD_COPY, 0));
			}
			cmdList.add(new FileCmd(null, null, true, CMD_FRESH, 0));
			break;
		case 3: // 从B移动到A
			for (LvRow lr : selectedItems[1]) {
				cmdList.add(new FileCmd(lr.getFile(), currentPath[0], true,
						CMD_MOVE, 0));
			}
			cmdList.add(new FileCmd(null, null, true, CMD_FRESH, 0));
			cmdList.add(new FileCmd(null, null, true, CMD_FRESH, 1));
			break;
		case 4: // 删除当前窗口所选
			for (LvRow lr : selectedItems[tab]) {
				cmdList.add(new FileCmd(lr.getFile(), null, false, CMD_DELETE,
						0));
			}
			cmdList.add(new FileCmd(null, null, true, CMD_FRESH, tab));
			break;
		case 5: // 创建文件夹
			cmdList.add(new FileCmd(null, currentPath[tab], false, CMD_MKDIR, tab));
			cmdList.add(new FileCmd(null, null, true, CMD_FRESH, tab));
			Log.d(DTAG, "cmdList.size(): "+cmdList.size());
			break;
		case 6: // 添加到播放列表
			for (LvRow lr : selectedItems[tab]) {
				addToPlayList(lr.getFile());
			}
			savePlayList2File();
			updatePlayListAdapter();
			Log.d(DTAG, "cmdList.size(): "+cmdList.size());
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
				if (e.getMessage().equals(pathRoot)) {
					updateDirInfor(pathRoot, tab);
				}
			}
		} else {
			// NullPointerException
			Log.d(DTAG, "path null: " + path);
		}
	}

	private void updateFileInfor(File f, int tab) throws Exception {
		File files[];

		Log.d(DTAG, "goto this directory: " + f.getPath());
		try {
			files = f.listFiles();
			if (files == null) {
				// Log.d(DTAG, "sorry, can't update to this directory: " +
				// currentPath);
				Toast.makeText(this, "Failed to this directory!",
						Toast.LENGTH_SHORT).show();
				return;
			}
			// update to new directory
			currentPath[tab] = f.getPath();
			parentPath[tab] = f.getParent(); // 如果f是根目录会怎样？

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

			Log.d(DTAG, "check viewListItems[" + tab + "]:"
					+ viewListItems[tab].toString());
			myArrayAdapter[tab] = new MyArrayAdapter(this, R.layout.file_row,
					viewListItems[tab]);
			Log.d(DTAG, "after new MyArrayAdapter");

			if (fragmentListview[tab] != null) {
				fragmentListview[tab].setListviewAdapter(myArrayAdapter[tab],
						currentPath[tab]);
				
				//debug, should be deleted
				//fragmentPlayList.setListviewAdapter(myArrayAdapter[tab]);
				
				Log.d(DTAG,
						"fragmentListview.setListviewAdapter(myArrayAdapter)!");
			} else {
				Log.d(DTAG, "fragmentListview is null!");
			}
			// Log.d(TAG_DEBUG, "show path: " + parentPath + f.getName());
		} catch (Exception e) {
			Log.d(DTAG, "path: " + f.getPath() + "Exception e: " + e.toString());
			e.printStackTrace();

			throw new Exception(pathRoot);
			// return;
		} finally {
			Log.d(DTAG, "3. finally " + currentPath[tab]);
			// return;
		}

	}

	/**
	 *	集中所有的dir和file到对应list
	 *	对list里面的items排序
	 *	以后排序控制加到这里
	 */
	private void fillList(File[] files, int tab) {
		Log.d(DTAG, "in fillList, fils.length: " + files.length);
		dirList[tab].clear();
		fileList[tab].clear();
		for (File f : files) {
			if (f.isDirectory()) {
				Log.d(DTAG, "find directory: " + f.getName());
				dirList[tab].add(f);
			} else if (f.isFile()) {
				Log.d(DTAG, "find file: " + f.getName());
				fileList[tab].add(f);
			} else
				Log.d(DTAG, "File error~~~");

		}
		Log.d(DTAG, "fillList loop end 1");

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
		if (!currentPath[tab].equals(pathRoot)) {
			Log.d(DTAG, "add parentPath: " + parentPath[tab]);
			// viewListFiles.add(new File(parentPath));
			LvRow lr = new LvRow("/..", "", "", new File(parentPath[tab]), false, 0, null);
			viewListItems[tab].add(lr);
		}
		Log.d(DTAG, "fillList loop end 2");

		for (File f : dirList[tab]) {
			// viewListFiles.add(f);
			LvRow lr = new LvRow("/" + f.getName(), "", ""
					+ sdf.format(f.lastModified()), f, false, 1, null);
			Log.d(DTAG, "add directory: " + lr.getName());
			viewListItems[tab].add(lr);
		}
		for (File f : fileList[tab]) {
			// viewListFiles.add(f);
			LvRow lr = new LvRow("" + f.getName(), "" + f.length(), ""
					+ sdf.format(f.lastModified()), f, false, 2, 
					URLConnection.getFileNameMap().getContentTypeFor(f.getName()));
			Log.d(DTAG, "add file: " + lr.getName());
			viewListItems[tab].add(lr);
		}
		// System.out.println(viewListItems);
		Log.d(DTAG, "viewListItems is constructed!");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dir_player);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
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
		
		Log.d(DTAG, "fragment initial code begin...");
		for (int i = 0; i < tabCount; i++) {
			viewListItems[i] = new LinkedList<LvRow>();
			selectedItems[i] = new LinkedList<LvRow>();
			dirList[i] = new LinkedList<File>();
			fileList[i] = new LinkedList<File>();
			currentPath[i] = pathRoot;
			parentPath[i] = null;
			fragmentListview[i] = FragmentListview.newInstance(i);
			myArrayAdapter[i] = new MyArrayAdapter(this, R.layout.file_row,
					viewListItems[i]);
			fragmentListview[i].setListviewAdapter(myArrayAdapter[i],
					currentPath[i]);
		}
		Log.d(DTAG, "fragment initial code ended!");
		
		ImageView bottomIcon = (ImageView)findViewById(R.id.bottom_icon);
		bottomIcon.setImageResource(R.drawable.bottom);

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
		
		/**
		 * 初始化左右窗口
		 */
		sharedPref = getPreferences(Context.MODE_PRIVATE);
		prefEditor = sharedPref.edit();
		
		String lwp = sharedPref.getString(getString(R.string.left_window_path),
				pathRoot);
		String rwp = sharedPref.getString(
				getString(R.string.right_window_path), pathRoot);
		// Toast.makeText(this,
		// "lwp: " + lwp + " rwp:" + rwp + " pathRoot: " + pathRoot,
		// Toast.LENGTH_LONG).show();
		updateDirInfor(lwp, 0);
		updateDirInfor(rwp, 1);

		// updateDirInfor(pathRoot, 2);
		Log.d(DTAG, "updateDir....");

		/**
		 * 音视频媒体相关初始化
		 */
		vBottonControl = findViewById(R.id.main_window); // 停放controller的地方
		
		mediaPlayerControlInit(); // 生成音频控制接口		
		
		// 音频控制器初始化 
		mediaController = new MediaController(this);
		mediaController.setMediaPlayer(mediaPlayerControl);
		mediaController.setAnchorView(vBottonControl);
		mediaController.setEnabled(true);					

		// 视频控制器初始化
		vv = (VideoView)findViewById(R.id.videoview); // 视频播放窗口
		videoController = new MediaController(this);
		//videoController.setMediaPlayer(videoPlayerControl);
		//videoController.setAnchorView(vBottonControl);
		//videoController.setEnabled(true);
		vv.setMediaController(videoController);
		vv.setOnTouchListener(vvOnTouchListener);
		
		/**
		 * 播放列表
		 */
		getPlayList();
		updatePlayListAdapter();
		
		/**
		 * 启动音频播放service
		 */
		Intent intent = new Intent(this, PlayService.class);
		startService(intent);

		
		/**
		 * 通过广播接收service的播放信息，比如当前曲目
		 */
        // The filter's action is BROADCAST_ACTION
        IntentFilter mStatusIntentFilter = new IntentFilter(
        		ServiceConstants.BROADCAST_ACTION);
    
        // Adds a data filter for the HTTP scheme
        //mStatusIntentFilter.addDataScheme("http");
        ServiceInforReceiver mServiceInforReceiver =
                new ServiceInforReceiver();
        // Registers the DownloadStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(this).registerReceiver(
        		mServiceInforReceiver,
                mStatusIntentFilter);
	}

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, PlayService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
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
	            Log.d(DTAG,"videoview onDown"); 
	            return true;
	        }

			/* (non-Javadoc)
			 * @see android.view.GestureDetector.SimpleOnGestureListener#onSingleTapUp(android.view.MotionEvent)
			 */
			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				// TODO Auto-generated method stub
				videoController.show();
				return super.onSingleTapUp(e);
			}

			/* (non-Javadoc)
			 * @see android.view.GestureDetector.SimpleOnGestureListener#onSingleTapConfirmed(android.view.MotionEvent)
			 */
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				// TODO Auto-generated method stub
				
				return super.onSingleTapConfirmed(e);
			}

			/* (non-Javadoc)
			 * @see android.view.GestureDetector.SimpleOnGestureListener#onScroll(android.view.MotionEvent, android.view.MotionEvent, float, float)
			 */
			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2,
					float distanceX, float distanceY) {
				Log.d(DTAG,"videoview onScroll"); 
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

				Log.d(DTAG,"videoview onFling:"+(e2.getY() - e1.getY()));
				Log.d(DTAG,"videoview onFling:"+swipeMinDistance);
				Log.d(DTAG,"videoview onFling:"+Math.abs(velocityY));
				Log.d(DTAG,"videoview onFling:"+swipeThresholdVelocity);
				
				if (e2.getY() - e1.getY() > swipeMinDistance
						&& Math.abs(velocityY) > swipeThresholdVelocity) {
					Log.d(DTAG,"onFling is accepted");
					// 退出视频，并隐藏VideoView
					videoController.hide();
					vv.stopPlayback();
					vv.setVisibility(View.GONE);					
		            return true;
				}
				Log.d(DTAG,"diliver onFling()");
				return super.onFling(e1, e2, velocityX, velocityY);
			}
	    }
	 class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
	        
	        @Override
	        public boolean onDown(MotionEvent event) { 
	            Log.d(DTAG,"onDown: " + event.toString()); 
	            return true;
	        }

	        /* (non-Javadoc)
			 * @see android.view.GestureDetector.SimpleOnGestureListener#onScroll(android.view.MotionEvent, android.view.MotionEvent, float, float)
			 */
			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2,
					float distanceX, float distanceY) {
				
				//如果从底部滑出，并且媒体播放器存在的话，调用其show方法
				Log.d(DTAG,"onScroll().getY(): "+e1.getY()
						+ ", bottom is: "+ (widthHeightInPixels[1] - 20));
				
				if (e1.getY() > (widthHeightInPixels[1] - 20)) {
					Log.d(DTAG,"onScroll() find scroll from bottom!");
					if((mService!=null)&&(mediaController != null)){
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
	     Log.d(DTAG, "widthHeightInPixels[0]: " + widthHeightInPixels[0]
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
		lastTab = tab.getPosition();
		Log.d(DTAG, "last tab: " + lastTab);
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
				Log.d(DTAG, "getItem(" + position + ") return fragmentPlayList");
				return fragmentPlayList;
			}
			if (position == 2) { // 第三个tab为标签
				Log.d(DTAG, "getItem(" + position + ") return fragmentBookMark");
				return fragmentBookMark;
			} else { // 第一个tab为左窗口，第二个tab为右窗口
				Log.d(DTAG, "getItem(" + position + ") return fragmentListview");
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
	    	Log.d(DTAG, "back key found!");
	    	Toast.makeText(this, "连按两次回退键可以退出程序", Toast.LENGTH_SHORT).show();
	    	
	        if (backPressed == true) {
	        	Log.d(DTAG, "back key twice! close app!");
	            finish();
	        }
	        else
	        {
	        	Log.d(DTAG, "back key once! set flag.");
	            backPressed = true;
	            new Handler().postDelayed(new Runnable() {

	                @Override
	                public void run() {
	                	backPressed = false;   
	                	Log.d(DTAG, "clear back key once flag from runnable()!");;
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
				Log.d(DTAG, "mediaPlayer: pause()");
				mService.pause();
			}
		
			@Override
			public void seekTo(int arg0) {
				// TODO Auto-generated method stub
				mService.seekTo(arg0);
			}
		
			@Override
			public void start() {
				Log.d(DTAG, "mediaPlayer: start()");
				mService.start();
				// TODO Auto-generated method stub
				
			}
		};
    }


	
    
	public void onFragmentPlayListClicked(int i) {
		Log.d(DTAG, "onFragmentPlayListClicked: " + i);
		if(mService != null){
			mService.playList(i);
			mediaController.show();
		}
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
		// 删除
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
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
		String line;
		File playlist = new File(getFilesDir(),
				getString(R.string.playlist_file));

		playListItems.clear();
		try {
			BufferedReader br = new BufferedReader(new FileReader(playlist));

			while ((line = br.readLine()) != null) {
				File f = new File(line);
				LvRow lr = new LvRow("" + f.getName(), "" + f.length(), ""
						+ sdf.format(f.lastModified()), f, false, 2, 
						URLConnection.getFileNameMap().getContentTypeFor(f.getName()));
				playListItems.add(lr);
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 加在链表的末尾
	 * 
	 */
	private void appendPlayList2File(LvRow lr) {

		File playlist = new File(getFilesDir(),
				getString(R.string.playlist_file));

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(playlist,
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

		File playlist = new File(getFilesDir(),
				getString(R.string.playlist_file));

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(playlist));
			for (LvRow lr : playListItems) {
				bw.write(lr.getFile().getPath() + "\n");
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(mService != null)
			mService.updatePlayList();
	}

	private class ServiceInforReceiver extends BroadcastReceiver
	{
		private ServiceInforReceiver(){ // Prevents instantiation
		}
		
	    // Called when the BroadcastReceiver gets an Intent it's registered to receive
		@Override
	    public void onReceive(Context context, Intent intent) {
			String path = intent.getStringExtra(ServiceConstants.EXTENDED_DATA_STATUS);
			Log.d(DTAG, "ServiceInforReceiver() get: " + path);
			fragmentPlayList.setPathView(
					getResources().getString(R.string.pl_path) + path);
	    }
	}
	
}
