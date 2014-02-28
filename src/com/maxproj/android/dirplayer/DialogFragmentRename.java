package com.maxproj.android.dirplayer;

import java.io.File;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class DialogFragmentRename extends DialogFragment {

	File mf;
	
	public static DialogFragmentRename newInstance(File f) {
		DialogFragmentRename dialogFragment = new DialogFragmentRename();
		dialogFragment.mf = f;
		return dialogFragment;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();
	    final View v = inflater.inflate(R.layout.rename, null);
	    
		TextView tv = (TextView) v.findViewById(R.id.rename_origin);
		tv.setText(mf.getName());

	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    builder.setTitle(R.string.rename_prompt)
	    	.setView(v)
	        .setNegativeButton(R.string.negative, new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					((DirPlayerActivity) getActivity()).activateNextCmd();//继续处理下一条Cmd
				}
	        	
	        })
	        .setNeutralButton(R.string.rename_neutral, new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					/**
					 * 取消后续所有改名操作
					 */
					((DirPlayerActivity) getActivity()).cancelRename();
					((DirPlayerActivity) getActivity()).activateNextCmd();//继续处理下一条Cmd
				}
	        	
	        })
	        .setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
	        	@Override
	            public void onClick(DialogInterface dialog, int id) {
	        		Log.d(LocalConst.DTAG, "rename onClick()");
	        		
	        		EditText et = (EditText) v.findViewById(R.id.rename_new);
	        		Log.d(LocalConst.DTAG, "rename find EditText()");

	        		String newName = et.getText().toString();
	        		Log.d(LocalConst.DTAG, "rename get new name"+newName);
	        		
	        		if(!mf.renameTo(new File(mf.getParentFile(), newName))){
	        			Toast.makeText(
	    	        			getActivity(),
	    	        			"由于某种原因，"+mf.getName()+"改名失败",
	    	        			Toast.LENGTH_LONG)
	    	        			.show();
	        		}
					((DirPlayerActivity) getActivity()).activateNextCmd();//继续处理下一条Cmd
	               }
	           });      
	    return builder.create();
	}
	
	
}
