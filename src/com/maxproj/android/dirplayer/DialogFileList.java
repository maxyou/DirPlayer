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

public class DialogFileList  extends DialogFragment {

	int tab;
	
	public interface DialogFileListInterface{
        void onDialogFileListAdd(int tab);
        void onDialogFileListL2rCopy(int tab);
        void onDialogFileListL2rMove(int tab);
        void onDialogFileListR2lCopy(int tab);
        void onDialogFileListR2lMove(int tab);
        void onDialogFileListMkdir(int tab);
        void onDialogFileListDel(int tab);
        void onDialogFileListRename(int tab);
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
        	dialogFileListInterface = (DialogFileListInterface)activity;
            Log.d(LocalConst.DTAG,"activity implemented interface!");
        }catch(ClassCastException e){
            Log.d(LocalConst.DTAG,"onAttach() throw new ClassCastException!");
            throw new ClassCastException(activity.toString()+ " must implement "
                    +dialogFileListInterface.toString());
        }
    }
}
