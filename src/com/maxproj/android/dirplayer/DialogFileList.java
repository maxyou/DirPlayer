package com.maxproj.android.dirplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
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

public class DialogFileList  extends DialogFragment {

	int tab;


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

	    Button flc_ibn_pause = (Button)v.findViewById(R.id.flc_ibn_pause);
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
//						LocalBroadcastManager.getInstance(LocalConst.app).
				    	((DirPlayerActivity) getActivity()).
							sendBroadcast(new Intent(LocalConst.NOTIFICATION_GOTO_PAUSE));
				    	showMusicPlayButton((Button)v, LocalConst.paused);
						Log.d(LocalConst.DTAG,"send pressed pause");
//				    	dialogFileListInterface.onDialogFileListPause(tab);
				    	break;
				    case(LocalConst.paused):
//						LocalBroadcastManager.getInstance(LocalConst.app).
				    	((DirPlayerActivity) getActivity()).
							sendBroadcast(new Intent(LocalConst.NOTIFICATION_GOTO_PLAY));
				    	showMusicPlayButton((Button)v, LocalConst.playing);
				    	Log.d(LocalConst.DTAG,"send pressed play");
//				    	dialogFileListInterface.onDialogFileListPlay(tab);
				    	break;
			    }
		    }
		});
	    
	    
	    flc_ibn_seekbar = (SeekBar)v.findViewById(R.id.flc_ibn_seekbar);
	    flc_ibn_seekbar.setMax(100);
	    
//	    flc_ibn_seekbar.setProgress(80);
 
//	    new LooperThread().start();
	    
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
//	class LooperThread extends Thread {
//	      public Handler mHandler;
//	      int mProgressStatus = 0;
//	      {
//	    	  Log.d(LocalConst.DTAG,"while (mProgressStatus < 100) in LooperThread()");
//	      }
//	      
//	      public void run() {
//	          Looper.prepare();
//	          Log.d(LocalConst.DTAG,"while (mProgressStatus < 100) in run()");
//	          mHandler = new Handler() {
//	              public void handleMessage(Message msg) {
//	                  // process incoming messages here
//	              }
//	          };
//
//	          while (mProgressStatus < 100) {
//              	Log.d(LocalConst.DTAG,"while (mProgressStatus < 100)");
//              	try {
//						Thread.sleep(1000);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//                  // Update the progress bar
//                  mHandler.postDelayed((new Runnable() {
//                      public void run() {
//                      	if(
//                      			(((DirPlayerActivity) getActivity()).servicePlaying != LocalConst.clear)
//                      			&& (((DirPlayerActivity) getActivity()).mService != null)
//                      			
//                      			)
//                      	{
//                      		mProgressStatus = ((DirPlayerActivity) getActivity()).mService.getProgress100();
//                      		flc_ibn_seekbar.setProgress(mProgressStatus);
//                      		Log.d(LocalConst.DTAG,"mProgressStatus: " + mProgressStatus);
//                      	}
//                      }
//                  }), 100);
//              }
//	          Log.d(LocalConst.DTAG,"while (mProgressStatus < 100) before loop: " + mProgressStatus);
//	          Looper.loop();
//	          Log.d(LocalConst.DTAG,"while (mProgressStatus < 100) after loop: " + mProgressStatus);
//
//	      }
//	  }
	private void showMusicPlayButton(Button v, int playing){
	    switch(playing){
		    case LocalConst.clear:
		    case(LocalConst.stopped):
		    	// 不显示控件
		    	v.setVisibility(View.GONE);
		    	break;
		    case LocalConst.playing:
	    		v.setText(getResources().getText(R.string.flc_ibn_pause));
		    	break;
		    case(LocalConst.paused):
	    		v.setText(getResources().getText(R.string.flc_ibn_play));
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
