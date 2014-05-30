package com.maxproj.android.dirplayer;

import java.io.File;

import com.maxproj.android.dirplayer.DirPlayerActivity.MusicProgressAsyncTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class DialogFileList  extends DialogFragment {

	int tab;

	MusicProgressAsyncTask mpat;
	TextView fl_playing_name;
	Button flc_ibn_pause;	
    SeekBar flc_ibn_seekbar;
    
	public interface DialogFileListInterface{
        void onDialogFileListAdd(int tab);
        void onDialogFileListL2rCopy(int tab);
        void onDialogFileListL2rMove(int tab);
        void onDialogFileListR2lCopy(int tab);
        void onDialogFileListR2lMove(int tab);
        void onDialogFileListMkdir(int tab);
        void onDialogFileListDel(int tab);
        void onDialogFileListRename(int tab);
        void onDialogFileListPause(int tab);
        void onDialogFileListPlay(int tab);
    }
    private DialogFileListInterface dialogFileListInterface = null;

	public static DialogFileList newInstance(int tab) {
		DialogFileList dialogFileList = new DialogFileList();
		dialogFileList.tab = tab;
		return dialogFileList;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();
	    final View v = inflater.inflate(R.layout.filelistcmd, null);
	    
	    Button flc_ibn_add = (Button)v.findViewById(R.id.flc_ibn_add);
	    flc_ibn_add.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
				// TODO Auto-generated method stub
				dialogFileListInterface.onDialogFileListAdd(tab);
			}
		});
	    Button flc_ibn_l2r_copy = (Button)v.findViewById(R.id.flc_ibn_l2r_copy);
	    flc_ibn_l2r_copy.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
				// TODO Auto-generated method stub
				dialogFileListInterface.onDialogFileListL2rCopy(tab);
			}
		});
	    Button flc_ibn_r2l_copy = (Button)v.findViewById(R.id.flc_ibn_r2l_copy);
	    flc_ibn_r2l_copy.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
				// TODO Auto-generated method stub
				dialogFileListInterface.onDialogFileListR2lCopy(tab);
			}
		});
	    Button flc_ibn_l2r_move = (Button)v.findViewById(R.id.flc_ibn_l2r_move);
	    flc_ibn_l2r_move.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
				// TODO Auto-generated method stub
				dialogFileListInterface.onDialogFileListL2rMove(tab);
			}
		});
	    Button flc_ibn_r2l_move = (Button)v.findViewById(R.id.flc_ibn_r2l_move);
	    flc_ibn_r2l_move.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
				// TODO Auto-generated method stub
				dialogFileListInterface.onDialogFileListR2lMove(tab);
			}
		});
	    Button flc_ibn_rename = (Button)v.findViewById(R.id.flc_ibn_rename);
	    flc_ibn_rename.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
				// TODO Auto-generated method stub
				dialogFileListInterface.onDialogFileListRename(tab);
			}
		});
	    Button flc_ibn_del = (Button)v.findViewById(R.id.flc_ibn_del);
	    flc_ibn_del.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
				// TODO Auto-generated method stub
				dialogFileListInterface.onDialogFileListDel(tab);
			}
		});
	    Button flc_ibn_mkdir = (Button)v.findViewById(R.id.flc_ibn_mkdir);
	    flc_ibn_mkdir.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
				// TODO Auto-generated method stub
				dialogFileListInterface.onDialogFileListMkdir(tab);
			}
		});

	    fl_playing_name = (TextView)v.findViewById(R.id.fl_playing_name);
	    flc_ibn_pause = (Button)v.findViewById(R.id.flc_ibn_pause);
	    flc_ibn_seekbar = (SeekBar)v.findViewById(R.id.flc_ibn_seekbar);
	    
	    int playStatus = ((DirPlayerActivity) getActivity()).servicePlaying;
	    int playType =  ((DirPlayerActivity) getActivity()).servicePlayType;
	    
	    if((playType == LocalConst.SinglePlay)
	    	&& (playStatus != LocalConst.clear)	)
	    {
	    	String s = ((DirPlayerActivity) getActivity()).servicePlayPath;
	    	File f = new File(s);
	    	fl_playing_name.setText(f.getName());
	    	
		    showMusicPlayButton(flc_ibn_pause, ((DirPlayerActivity) getActivity()).servicePlaying);
		    
		    flc_ibn_pause.setOnClickListener(new OnClickListener() {
			
				@Override
				public void onClick(View v) {
					Log.d(LocalConst.DTAG,"pressed button, check playing status: "
							+ ((DirPlayerActivity) getActivity()).servicePlaying);
				    switch(((DirPlayerActivity) getActivity()).servicePlaying){
					    case LocalConst.clear:
					    case(LocalConst.stopped):
					    	// 什么也不做
					    	break;
					    case LocalConst.playing:
//							LocalBroadcastManager.getInstance(LocalConst.app).
					    	((DirPlayerActivity) getActivity()).
								sendBroadcast(new Intent(LocalConst.NOTIFICATION_GOTO_PAUSE));
//					    	showMusicPlayButton((Button)v, LocalConst.paused);
							Log.d(LocalConst.DTAG,"send pressed pause");
//					    	dialogFileListInterface.onDialogFileListPause(tab);
					    	break;
					    case(LocalConst.paused):
//							LocalBroadcastManager.getInstance(LocalConst.app).
					    	((DirPlayerActivity) getActivity()).
								sendBroadcast(new Intent(LocalConst.NOTIFICATION_GOTO_PLAY));
//					    	showMusicPlayButton((Button)v, LocalConst.playing);
					    	Log.d(LocalConst.DTAG,"send pressed play");
//					    	dialogFileListInterface.onDialogFileListPlay(tab);
					    	break;
				    }
			    }
			});
		    
		    flc_ibn_seekbar.setMax(100);
		    flc_ibn_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
					if(mpat != null){
						mpat.usrAdjust = false;
					}
				}
				
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
					if(mpat != null){
						mpat.usrAdjust = true;
					}
				}
				
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					// TODO Auto-generated method stub
					if(fromUser == true){
						if(((DirPlayerActivity) getActivity()).mService != null){
							((DirPlayerActivity) getActivity()).mService.seekToPercentage(progress);
							seekBar.setProgress(progress);
						}
					}
				}
			});
		    
			Log.d(LocalConst.DTAG, "AsyncTask: fragment - new MusicProgressAsyncTask()");
			mpat = LocalConst.dirPlayerActivity.new MusicProgressAsyncTask(LocalConst.app, flc_ibn_seekbar);
			mpat.execute();
	    	
	    }else{
	    	fl_playing_name.setVisibility(View.GONE);
	    	flc_ibn_pause.setVisibility(View.GONE);
	    	flc_ibn_seekbar.setVisibility(View.GONE);
	    } 
	
		IntentFilter mIntentFilter = new IntentFilter(LocalConst.BROADCAST_SERVICE_STATUS);
		DialogFileListReceiver dialogFileListReceiver = new DialogFileListReceiver();
		/**
		 * 注意，使用LocalBroadcastManager注册的receiver只能接收本app内部的intent
		 * 但notification在本app之外 所以这里需要直接使用registerReceiver来注册
		 */
		LocalBroadcastManager.getInstance(LocalConst.dirPlayerActivity).registerReceiver(dialogFileListReceiver, mIntentFilter);
	    
	    
	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    builder.setTitle(R.string.prompt)
	    	.setView(v)
	        .setNegativeButton(R.string.negative, new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
	        	
	        });
	    return builder.create();
	}


	/**
	 * 接收控制媒体播放的Intent，并做相应显示变动
	 */
	private class DialogFileListReceiver extends BroadcastReceiver {
		private DialogFileListReceiver() { // Prevents instantiation
		}

		// Called when the BroadcastReceiver gets an Intent it's registered to
		// receive
		@Override
		public void onReceive(Context context, Intent intent) {

			/**
			 * 接收如下信息 播放、暂停
			 */

			String action = intent.getAction();
			if (LocalConst.BROADCAST_SERVICE_STATUS.equals(action)){
				int servicePlaying =  intent.getIntExtra(LocalConst.PLAY_STATUS, -1);
				int servicePlayType = intent.getIntExtra(LocalConst.PLAY_TYPE, -1);
				String servicePlayPath = intent.getStringExtra(LocalConst.PLAY_PATH);
				
				if(servicePlayType == LocalConst.SinglePlay){
					if(servicePlaying == LocalConst.playing){
						showMusicPlayButton(flc_ibn_pause, LocalConst.playing);
					}else if(servicePlaying == LocalConst.paused){
						showMusicPlayButton(flc_ibn_pause, LocalConst.paused);						
					}
				}
				if(servicePlayPath != null){
					File f = new File(servicePlayPath);
					if(f != null){
						fl_playing_name.setText(f.getName());
					}
				}
			}
		}
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(mpat != null){
			Log.d(LocalConst.DTAG, "AsyncTask: fragment - onDestroy()");
			mpat.cancel(true);
		}
		
	}

	private void showMusicPlayButton(Button v, int playing){
		if(v == null)
			return;
		
	    switch(playing){
		    case LocalConst.clear:
		    case(LocalConst.stopped):
		    	// 不显示控件
		    	v.setVisibility(View.GONE);
		    	break;
		    case LocalConst.playing:
	    		v.setText(R.string.flc_ibn_pause);
		    	break;
		    case(LocalConst.paused):
	    		v.setText(R.string.flc_ibn_play);
		    	break;
	    }
	}
	
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        
        try{
        	Log.d(LocalConst.DTAG,"check if activity implement interface....");
        	dialogFileListInterface = (DialogFileListInterface)activity;
            Log.d(LocalConst.DTAG,"activity implemented interface!");
        }catch(ClassCastException e){
            Log.d(LocalConst.DTAG,"onAttach() throw new ClassCastException!");
            throw new ClassCastException(activity.toString()+ " must implement "
                    +dialogFileListInterface.toString());
        }
    }
}
