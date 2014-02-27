package com.maxproj.android.dirplayer;

import java.util.List;



import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyArrayAdapter extends ArrayAdapter<LvRow>{

    int resource;
    final List<LvRow> listItems;// = new List<LvRow>();

    public MyArrayAdapter(Context context, int textViewResourceId, List<LvRow> objects) {
        super(context, textViewResourceId, objects);
        resource = textViewResourceId;
        listItems = objects;
        Log.d(LocalConst.LIFECYCLE,"MyArrayAdapter.MyArrayAdapter()");
    }


    public View getView (int position, View convertView, ViewGroup parent){
        LinearLayout fileView;
        //1. using final var
        //final Integer p;
        //p = position;
        LvRow lr = (LvRow)getItem(position);

        if (convertView == null){
//            Log.d(LocalConst.DTAG, "getView: new view, position: " + position);
            fileView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater li;
            li = (LayoutInflater)getContext().getSystemService(inflater);
            li.inflate(resource, fileView, true);
        }else{
            fileView = (LinearLayout)convertView;
//            Log.d(LocalConst.DTAG, "getView: converView, position: " + position);
        }

        TextView name = (TextView)fileView.findViewById(R.id.text1);
        String fileName = lr.getName(); 
        name.setText(fileName);
//        Log.d(LocalConst.DTAG, "getView: name: " + name.getText());

        ImageView iv = (ImageView)fileView.findViewById(R.id.fileicon);
        if (lr.getType() == 0){        	
        	iv.setImageResource(R.drawable.up);
        }else if (lr.getType() == 1){
        	iv.setImageResource(R.drawable.dir);
        }else if (lr.getType() == 2){
        	if(lr.getMime() == null){
        		iv.setImageResource(R.drawable.unkown);
        	}else if(lr.getMime().startsWith("audio/")){
        		iv.setImageResource(R.drawable.music);
        	}else if(lr.getMime().startsWith("video/")){
        		iv.setImageResource(R.drawable.video);
        	}else if(lr.getMime().startsWith("text/")){
        		iv.setImageResource(R.drawable.txt);
        	}else if(lr.getMime().startsWith("text/")){
        		iv.setImageResource(R.drawable.txt);
        	}else if(lr.getMime().startsWith("image/")){
        		iv.setImageResource(R.drawable.pic);
        	}else{
        		iv.setImageResource(R.drawable.unkown);
        	}
        }
        //iv.setImageResource(resId)

        ImageView fpi = (ImageView)fileView.findViewById(R.id.fileplayingicon);
        if ((lr.getPlayingStatus() == LocalConst.playing)
        		|| (lr.getPlayingStatus() == LocalConst.pause))
        {
        	fpi.setVisibility(View.VISIBLE);
        	fpi.setImageResource(R.drawable.playingmusic);
        }else if ((lr.getPlayingStatus() == LocalConst.clear)
        		|| (lr.getPlayingStatus() == LocalConst.stop))
        {
        	fpi.setVisibility(View.GONE);
        }

        TextView length = (TextView)fileView.findViewById(R.id.text2);
        length.setText(lr.getLength());
        
        TextView date = (TextView)fileView.findViewById(R.id.text3);
        date.setText(lr.getDate());

        CheckBox cb = (CheckBox)fileView.findViewById(R.id.checkbox);
        // 2. add tag to view
        cb.setTag(position); // sava position in view
//        Log.d(LocalConst.DTAG, "getView: setTag " + cb.getTag());


        //if (lr.getName().charAt(0) == '/') // can't choose a directory
        if (fileName.equals("/..")) // can't choose parent directory
        {
            cb.setVisibility(cb.INVISIBLE);
//            Log.d(LocalConst.DTAG,"getView: " + lr.getName());
        }else{
            cb.setVisibility(cb.VISIBLE);
            cb.setChecked(listItems.get(position).getSelected()); // restore check state
//            Log.d(LocalConst.DTAG,"getView: listItems "+ position +" is "+ listItems.get(position).getSelected());

            cb.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    CheckBox cb = (CheckBox)view;
//                    Log.d(LocalConst.DTAG, "getView: listItems getTag " + (Integer)view.getTag() + " set " + cb.isChecked());
                    listItems.get((Integer)view.getTag()).setSelected(cb.isChecked());
                    // listItems.get(p).setSelected(cb.isChecked());
                }
            });

//            Log.d(LocalConst.DTAG,"getView: " + lr.getName());
        }

        //Log.d(TAG_DEBUG, "date: " + date.getText());

        return fileView;
    }
}

