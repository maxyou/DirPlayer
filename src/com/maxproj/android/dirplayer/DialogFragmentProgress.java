package com.maxproj.android.dirplayer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DialogFragmentProgress extends DialogFragment {
	final static String DTAG = "DirPlayer";

	View v;
	ProgressBar pb = null;
	TextView tv = null;

	String mMsg = null;
	int mProgress = -1;

	public void setMsg(String msg) {
		Log.d(DTAG, "ProgressBar: setMsg(" + msg + ")");

		mMsg = msg;

		if (tv != null) {
			Log.d(DTAG, "ProgressBar: (tv != null)");
			tv.setText(mMsg);
		} else {
			Log.d(DTAG, "ProgressBar: (tv == null)");
		}
	}

	public void setProgress(int progress) {
		mProgress = progress;

		Log.d(DTAG, "ProgressBar: setProgress(" + progress + ")");
		if (pb != null) {
			Log.d(DTAG, "ProgressBar: (pb != null)");
			pb.setProgress(mProgress);
			Log.d(DTAG, "ProgressBar: getProgress() return " + pb.getProgress());
		} else {
			Log.d(DTAG, "ProgressBar: (pb == null)");
		}
	}

	public static DialogFragmentProgress newInstance() {

		DialogFragmentProgress fragment = new DialogFragmentProgress();

		Log.d(DTAG, "ProgressBar: DialogFragmentProgress.newInstance()!");
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
				.setNegativeButton(R.string.negative, null);

		tv = (TextView) v.findViewById(R.id.progressbar_text);
		pb = (ProgressBar) v.findViewById(R.id.progressbar);

		if (mMsg != null) {
			tv.setText(mMsg);
		}
		pb.setMax(100);
		if (mProgress != -1) {
			pb.setProgress(mProgress);
		}

		Log.d(DTAG, "ProgressBar: DialogFragmentProgress.onCreateDialog()!");

		return builder.create();
	}

}
