package com.maxproj.android.dirplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

public class DialogPlayList  extends DialogFragment {

	int plTab;
	
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
	    Button plc_ibn_seq = (Button)v.findViewById(R.id.plc_ibn_seq);
	    switch(((DirPlayerActivity) getActivity()).getPlaySeq()){
	    	case LocalConst.play_seq_normal:
	    		plc_ibn_seq.setText(getResources().getText(R.string.play_seq_normal));
	    		break;
	    	case LocalConst.play_seq_random:
	    		plc_ibn_seq.setText(getResources().getText(R.string.play_seq_random));
	    		break;
	    	case LocalConst.play_seq_single:
	    		plc_ibn_seq.setText(getResources().getText(R.string.play_seq_single));
	    		break;
	    }
	    
	    plc_ibn_seq.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialogPlayListInterface.onDialogPlayListSeq();
				
				/**
				 * 点击时需要
				 * 	自身文字修改
				 * 	service中次序变量修改
				 *  notification文字修改
				 */
			    switch(((DirPlayerActivity) getActivity()).getPlaySeq()){
		    	case LocalConst.play_seq_normal:
		    		((Button) v).setText(getResources().getText(R.string.play_seq_normal));
		    		break;
		    	case LocalConst.play_seq_random:
		    		((Button) v).setText(getResources().getText(R.string.play_seq_random));
		    		break;
		    	case LocalConst.play_seq_single:
		    		((Button) v).setText(getResources().getText(R.string.play_seq_single));
		    		break;
		    }
		    
				
				
			}
		});
	    
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
