package com.maxproj.android.dirplayer;

import java.util.List;
import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BookMarkArrayAdapter extends ArrayAdapter {

	int resource;
	final List<BookMarkRow> listItems;// = new List<LvRow>();

	public BookMarkArrayAdapter(Context context, int textViewResourceId,
			List<BookMarkRow> objects) {
		super(context, textViewResourceId, objects);
		resource = textViewResourceId;
		listItems = objects;
		Log.d(LocalConst.DTAG, "initialize BookMarkArrayAdapter...end");
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout fileView;
		// 1. using final var
		// final Integer p;
		// p = position;
		BookMarkRow bmr = (BookMarkRow) getItem(position);

		if (convertView == null) {
			Log.d(LocalConst.DTAG, "getView: new view, position: " + position);
			fileView = new LinearLayout(getContext());
			String inflater = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater li;
			li = (LayoutInflater) getContext().getSystemService(inflater);
			li.inflate(resource, fileView, true);
		} else {
			fileView = (LinearLayout) convertView;
			Log.d(LocalConst.DTAG, "getView: converView, position: " + position);
		}

		TextView path = (TextView) fileView.findViewById(R.id.bookmark);
		path.setText(bmr.getPath());
		Log.d(LocalConst.DTAG, "getView: path: " + path.getText());

		CheckBox cb = (CheckBox) fileView.findViewById(R.id.bm_checkbox);
		// 2. add tag to view
		cb.setTag(position); // sava position in view
		Log.d(LocalConst.DTAG, "getView: setTag " + cb.getTag());

		cb.setVisibility(cb.VISIBLE);
		cb.setChecked(listItems.get(position).getSelected()); // restore check
																// state
		Log.d(LocalConst.DTAG,
				"getView: listItems " + position + " is "
						+ listItems.get(position).getSelected());

		cb.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				CheckBox cb = (CheckBox) view;
				Log.d(LocalConst.DTAG,
						"getView: listItems getTag " + (Integer) view.getTag()
								+ " set " + cb.isChecked());
				listItems.get((Integer) view.getTag()).setSelected(
						cb.isChecked());
				// listItems.get(p).setSelected(cb.isChecked());
			}
		});

		Log.d(LocalConst.DTAG, "getView: " + bmr.getPath());

		// Log.d(TAG_DEBUG, "date: " + date.getText());

		return fileView;
	}

}
