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

public class DialogBookMark  extends DialogFragment {
	
	public interface DialogBookMarkInterface{
        void onDialogBookMarkAdd2PlayList();
        void onDialogBookMarkCopy2Left();
        void onDialogBookMarkCopy2Right();
        void onDialogBookMarkDel();
    }
	
    private DialogBookMarkInterface dialogBookMarkInterface = null;

	public static DialogBookMark newInstance() {
		DialogBookMark dialogFileList = new DialogBookMark();
		return dialogFileList;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();
	    final View v = inflater.inflate(R.layout.bookmarkcmd, null);
	    
	    Button bmc_ibn_add = (Button)v.findViewById(R.id.bmc_ibn_add);
	    bmc_ibn_add.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
				// TODO Auto-generated method stub
				dialogBookMarkInterface.onDialogBookMarkAdd2PlayList();
			}
		});
	    Button bmc_ibn_copy_2_left = (Button)v.findViewById(R.id.bmc_ibn_copy_2_left);
	    bmc_ibn_copy_2_left.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
				// TODO Auto-generated method stub
				dialogBookMarkInterface.onDialogBookMarkCopy2Left();
			}
		});
	    Button bmc_ibn_copy_2_right = (Button)v.findViewById(R.id.bmc_ibn_copy_2_right);
	    bmc_ibn_copy_2_right.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
				// TODO Auto-generated method stub
				dialogBookMarkInterface.onDialogBookMarkCopy2Right();
			}
		});
	    Button bmc_ibn_del = (Button)v.findViewById(R.id.bmc_ibn_del);
	    bmc_ibn_del.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
				// TODO Auto-generated method stub
				dialogBookMarkInterface.onDialogBookMarkDel();
			}
		});	    

	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    builder.setTitle(R.string.bookmark_prompt)
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
        	dialogBookMarkInterface = (DialogBookMarkInterface)activity;
            Log.d(LocalConst.DTAG,"activity implemented interface!");
        }catch(ClassCastException e){
            Log.d(LocalConst.DTAG,"onAttach() throw new ClassCastException!");
            throw new ClassCastException(activity.toString()+ " must implement "
                    +dialogBookMarkInterface.toString());
        }
    }
}
