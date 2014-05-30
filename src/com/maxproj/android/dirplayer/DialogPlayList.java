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
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class DialogPlayList  extends DialogFragment {

	int plTab;
	
	MusicProgressAsyncTask mpat;
	TextView pl_playing_name;
	Button plc_ibn_pause;	
	Button plc_ibn_next;	
	Button plc_ibn_seq;	
    SeekBar plc_ibn_seekbar;
    
	public interface DialogPlayListInterface{
        void onDialogPlayListAdd(int plTab);
        void onDialogPlayListDel(int plTab);
        void onDialogPlayListSeq();
    }
    private DialogPlayListInterface dialogPlayListInterface = null;

	public static DialogPlayList newInstance(int plTab) {
		DialogPlayList dialogPlayList = new DialogPlayList();
		dialogPlayList.plTab = plTab;
		return dialogPlayList;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();
	    final View v = inflater.inflate(R.layout.playlistcmd, null);
	    
	    Button plc_ibn_add = (Button)v.findViewById(R.id.plc_ibn_add);
	    plc_ibn_add.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
				// TODO Auto-generated method stub
				dialogPlayListInterface.onDialogPlayListAdd(plTab);
			}
		});

	    Button plc_ibn_del = (Button)v.findViewById(R.id.plc_ibn_del);
	    plc_ibn_del.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
				// TODO Auto-generated method stub
				dialogPlayListInterface.onDialogPlayListDel(plTab);
			}
		});
	    
	    pl_playing_name = (TextView)v.findViewById(R.id.pl_playing_name);
	    plc_ibn_pause = (Button)v.findViewById(R.id.plc_ibn_pause);
	    plc_ibn_next = (Button)v.findViewById(R.id.plc_ibn_next);
	    plc_ibn_seq = (Button)v.findViewById(R.id.plc_ibn_seq);
	    plc_ibn_seekbar = (SeekBar)v.findViewById(R.id.plc_ibn_seekbar);
	    
	    int playStatus = ((DirPlayerActivity) getActivity()).servicePlaying;
	    int playType =  ((DirPlayerActivity) getActivity()).servicePlayType;
	    if((playType == LocalConst.ListPlay)
	    	&& (playStatus != LocalConst.clear)	)
	    {
	    	/**
	    	 * 显示曲目名
	    	 */
	    	String s = ((DirPlayerActivity) getActivity()).servicePlayPath;
	    	File f = new File(s);
	    	pl_playing_name.setText(f.getName());
	    	
	    	/**
	    	 * 播放按钮
	    	 * 		显示播放按钮
	    	 * 		添加监听函数
	    	 */
		    showMusicPlayButton(plc_ibn_pause, ((DirPlayerActivity) getActivity()).servicePlaying);
		    plc_ibn_pause.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.d(LocalConst.DTAG,"pressed button, check playing status: "
							+ ((DirPlayerActivity) getActivity()).servicePlaying);
				    switch(((DirPlayerActivity) getActivity()).servicePlaying){
					    case LocalConst.clear:
					    case(LocalConst.stopped):
					    	break;
					    case LocalConst.playing:
					    	((DirPlayerActivity) getActivity()).
								sendBroadcast(new Intent(LocalConst.NOTIFICATION_GOTO_PAUSE));
					    	break;
					    case(LocalConst.paused):
					    	((DirPlayerActivity) getActivity()).
								sendBroadcast(new Intent(LocalConst.NOTIFICATION_GOTO_PLAY));
					    	break;
				    }
			    }
			});

		    /**
		     * 下一首按钮
		     */
		    plc_ibn_next.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
			    	((DirPlayerActivity) getActivity()).
						sendBroadcast(new Intent(LocalConst.NOTIFICATION_GOTO_NEXT));
			    }
			});
		    
		    /**
		     * 播放次序按钮
		     * 		显示播放次序
		     * 		添加监听函数
		     */
		    showPlaySeqButton(plc_ibn_seq, ((DirPlayerActivity) getActivity()).getPlaySeq());
		    plc_ibn_seq.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					((DirPlayerActivity) getActivity()).
						sendBroadcast(new Intent(LocalConst.NOTIFICATION_SEQ_SWITCH));
				}
			});

		    /**
		     * 进度条
		     */
		    plc_ibn_seekbar.setMax(100);
		    plc_ibn_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				
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
			mpat = LocalConst.dirPlayerActivity.new MusicProgressAsyncTask(LocalConst.app, plc_ibn_seekbar);
			mpat.execute();
	    	
	    }else{
	    	pl_playing_name.setVisibility(View.GONE);
	    	plc_ibn_pause.setVisibility(View.GONE);
	    	plc_ibn_next.setVisibility(View.GONE);
	    	plc_ibn_seq.setVisibility(View.GONE);
	    	plc_ibn_seekbar.setVisibility(View.GONE);
	    } 
	
		IntentFilter mIntentFilter = new IntentFilter(LocalConst.BROADCAST_SERVICE_STATUS);
		DialogPlayListReceiver dialogPlayListReceiver = new DialogPlayListReceiver();
		/**
		 * 注意，使用LocalBroadcastManager注册的receiver只能接收本app内部的intent
		 * 但notification在本app之外 所以这里需要直接使用registerReceiver来注册
		 */
		LocalBroadcastManager.getInstance(LocalConst.dirPlayerActivity).registerReceiver(dialogPlayListReceiver, mIntentFilter);

	    
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
	private class DialogPlayListReceiver extends BroadcastReceiver {
		private DialogPlayListReceiver() { // Prevents instantiation
		}

		// Called when the BroadcastReceiver gets an Intent it's registered to
		// receive
		@Override
		public void onReceive(Context context, Intent intent) {

			/**
			 * 接收如下信息
			 * 		播放/暂停
			 * 		播放次序
			 * 		曲目
			 */

			String action = intent.getAction();
			if (LocalConst.BROADCAST_SERVICE_STATUS.equals(action)){
				int servicePlaying =  intent.getIntExtra(LocalConst.PLAY_STATUS, -1);
				int servicePlayType = intent.getIntExtra(LocalConst.PLAY_TYPE, -1);
				String servicePlayPath = intent.getStringExtra(LocalConst.PLAY_PATH);
				int servicePlaySeq = intent.getIntExtra(LocalConst.PLAY_SEQUENCE, -1);
				Log.d(LocalConst.DTAG, "play seq:" + servicePlaySeq);
				
				//播放/暂停
				if(servicePlayType == LocalConst.ListPlay){
					if(servicePlaying == LocalConst.playing){
						showMusicPlayButton(plc_ibn_pause, LocalConst.playing);
					}else if(servicePlaying == LocalConst.paused){
						showMusicPlayButton(plc_ibn_pause, LocalConst.paused);						
					}
				}
				//播放次序
				showPlaySeqButton(plc_ibn_seq, servicePlaySeq);
				//曲目
				if(servicePlayPath != null){
					File f = new File(servicePlayPath);
					if(f != null){
						pl_playing_name.setText(f.getName());
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
    private void showPlaySeqButton(Button v, int seq) {
		if(v == null)
			return;

		switch(seq){
	    	case LocalConst.play_seq_normal:
	    		v.setText(R.string.play_seq_normal);
	    		break;
	    	case LocalConst.play_seq_random:
	    		v.setText(R.string.play_seq_random);
	    		break;
	    	case LocalConst.play_seq_single:
	    		v.setText(R.string.play_seq_single);
	    		break;
	    }
		Log.d(LocalConst.DTAG, "play seq changed:" + seq);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        
        try{
        	Log.d(LocalConst.DTAG,"check if activity implement interface....");
        	dialogPlayListInterface = (DialogPlayListInterface)activity;
            Log.d(LocalConst.DTAG,"activity implemented interface!");
        }catch(ClassCastException e){
            Log.d(LocalConst.DTAG,"onAttach() throw new ClassCastException!");
            throw new ClassCastException(activity.toString()+ " must implement "
                    +dialogPlayListInterface.toString());
        }
    }
}
