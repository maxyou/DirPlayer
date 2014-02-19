package com.maxproj.android.dirplayer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DialogFragmentProgress extends DialogFragment {

	View v;
	ProgressBar pb = null;
	TextView tv = null;

	String mMsg = null;
	int mProgress = -1;

	public void setMsg(String msg) {
		Log.d(LocalConst.DTAG, "ProgressBar: setMsg(" + msg + ")");

		mMsg = msg;

		if (tv != null) {
			Log.d(LocalConst.DTAG, "ProgressBar: (tv != null)");
			tv.setText(mMsg);
		} else {
			Log.d(LocalConst.DTAG, "ProgressBar: (tv == null)");
		}
	}

	public void setProgress(int progress) {
		mProgress = progress;

		Log.d(LocalConst.DTAG, "ProgressBar: setProgress(" + progress + ")");
		if (pb != null) {
			Log.d(LocalConst.DTAG, "ProgressBar: (pb != null)");
			pb.setProgress(mProgress);
			Log.d(LocalConst.DTAG, "ProgressBar: getProgress() return " + pb.getProgress());
		} else {
			Log.d(LocalConst.DTAG, "ProgressBar: (pb == null)");
		}
	}

	public static DialogFragmentProgress newInstance() {

		DialogFragmentProgress fragment = new DialogFragmentProgress();

		Log.d(LocalConst.DTAG, "ProgressBar: DialogFragmentProgress.newInstance()!");
		return fragment;
	}

	public DialogFragmentProgress() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();
		v = inflater.inflate(R.layout.progressbar, null);

		builder.setView(v).setTitle(R.string.progress_prompt)
				.setNegativeButton(R.string.cancel_copy, new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						((DirPlayerActivity) getActivity()).cancelCopy();
					}
				})
				.setPositiveButton(R.string.background_copy, new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						/**
						 * 设置本次序列的拷贝为背景拷贝
						 */
						
						((DirPlayerActivity) getActivity()).setShowCopyProcess(false);
					}
				});

		tv = (TextView) v.findViewById(R.id.progressbar_text);
		pb = (ProgressBar) v.findViewById(R.id.progressbar);

		if (mMsg != null) {
			tv.setText(mMsg);
		}
		pb.setMax(100);
		if (mProgress != -1) {
			pb.setProgress(mProgress);
		}

		Log.d(LocalConst.DTAG, "ProgressBar: DialogFragmentProgress.onCreateDialog()!");

		return builder.create();
	}

}
