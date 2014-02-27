package com.maxproj.android.dirplayer;



import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.TextView;


public class FragmentPlayList  extends Fragment {

    MyArrayAdapter listAdapter = null;
    ListView listView = null;
    View fragmentView = null;

    Button b1, b2, b3, b4, b5, b6;
    TextView pathView = null;
    
    public interface FragmentPlayListInterface{
        void onFragmentPlayListClicked(int i);
        void onFragmentPlayListButton1();
        void onFragmentPlayListButton2();
        void onFragmentPlayListButton3();
        void onFragmentPlayListButton4();
        void onFragmentPlayListButton5();
        void onFragmentPlayListButton6();
        void onFragmentPlayListButton7();
        PlayService getServiceConnection();
    }
    private FragmentPlayListInterface FragmentPlayListInterface = null;

    /**
     * play service control
     */
    PlayService mService = null;
    
    /* 
    public FragmentListview(int tab){
        Log.d(LocalConst.DTAG,"fragment " + tab + " is initialized");
        this.tab = tab;
    }
    */
    public static FragmentPlayList newInstance() {
    	FragmentPlayList fragment = new FragmentPlayList();        
    	Log.d(LocalConst.PL, "FragmentPlayList.newInstance()");
        return fragment;
    }

    public void setListviewAdapter(MyArrayAdapter a){
        listAdapter = a;
        Log.d(LocalConst.PL, "FragmentPlayList.setListviewAdapter() "+listAdapter);
        if (listView != null){
            listView.setAdapter(listAdapter);
            Log.d(LocalConst.DTAG,"FragmentPlayList setListviewAdapter(): adapter is set!");
        }else{
            Log.d(LocalConst.DTAG,"FragmentPlayList setListviewAdapter(): listView is null pointer!");
        }
    }
    public View getItemView(int position){

        if (position > listView.getCount())
            return null;
        else
            return listView.getChildAt(position);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
    	Log.d(LocalConst.PL, "FragmentPlayList.onCreateView() "+listAdapter);
        fragmentView =  inflater.inflate(R.layout.fragment_playlist, container, false);
        
        b1 = (Button)fragmentView.findViewById(R.id.pl_b1);
        b1.setOnClickListener(new View.OnClickListener() { // 全选
            @Override
            public void onClick(View view) {
            	FragmentPlayListInterface.onFragmentPlayListButton1();
            }
        });
        b2 = (Button)fragmentView.findViewById(R.id.pl_b2);
        b2.setOnClickListener(new View.OnClickListener() { // 清除
            @Override
            public void onClick(View view) {
            	FragmentPlayListInterface.onFragmentPlayListButton2();
            }
        });
        b3 = (Button)fragmentView.findViewById(R.id.pl_b3);
        b3.setOnClickListener(new View.OnClickListener() { // 反选
            @Override
            public void onClick(View view) {
            	FragmentPlayListInterface.onFragmentPlayListButton3();
            }
        });
        b4 = (Button)fragmentView.findViewById(R.id.pl_b4);
        b4.setOnClickListener(new View.OnClickListener() { // 上移
            @Override
            public void onClick(View view) {
            	FragmentPlayListInterface.onFragmentPlayListButton4();
            }
        });
        b5 = (Button)fragmentView.findViewById(R.id.pl_b5);
        b5.setOnClickListener(new View.OnClickListener() { // 下移
            @Override
            public void onClick(View view) {
            	FragmentPlayListInterface.onFragmentPlayListButton5();
            }
        });
        b6 = (Button)fragmentView.findViewById(R.id.pl_b6);
        b6.setOnClickListener(new View.OnClickListener() { // 删除
            @Override
            public void onClick(View view) {
            	FragmentPlayListInterface.onFragmentPlayListButton6();
            }
        });


        listView = (ListView)fragmentView.findViewById(R.id.fragment_playlist);


        listView.setOnItemClickListener(new ItemClicklistener());
        Log.d(LocalConst.DTAG,"FragmentPlayList listView.setOnItemClickListener(new ItemClicklistener())!");

        pathView = (TextView)fragmentView.findViewById(R.id.current_play);

        return fragmentView;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.d(LocalConst.PL, "FragmentPlayList.onActivityCreated() "+listAdapter);

        if (listAdapter != null){
            listView.setAdapter(listAdapter);
            Log.d(LocalConst.DTAG,"FragmentPlayList (maa != null) and adapter is set!");
        }else{
            Log.d(LocalConst.DTAG,"FragmentPlayList maa is null pointer!");
        }
        
        mService = FragmentPlayListInterface.getServiceConnection();
		
    }
    public void setPathView(String path){
    	/**
    	 * 这里有个问题。
    	 * 刚开机，还没有切换到播放tab，也即这个tab还没有初始化，此刻设置pathView导致空指针异常
    	 */
    	if(pathView != null)
    		pathView.setText(path);
    }
    private class ItemClicklistener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        	FragmentPlayListInterface.onFragmentPlayListClicked(i);

            Log.d(LocalConst.DTAG,"FragmentPlayList ItemClicklistener is called!");
        }
    }
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        Log.d(LocalConst.PL, "FragmentPlayList.onAttach() "+listAdapter);
        try{
        	Log.d(LocalConst.DTAG,"FragmentPlayList check if activity implement interface....");
        	FragmentPlayListInterface = (FragmentPlayListInterface)activity;
            Log.d(LocalConst.DTAG,"activity implemented interface!");
        }catch(ClassCastException e){
            Log.d(LocalConst.DTAG,"FragmentPlayList onAttach() throw new ClassCastException!");
            throw new ClassCastException(activity.toString()+ " must implement "
                    +FragmentPlayListInterface.toString());
        }
        Log.d(LocalConst.DTAG,"FragmentPlayList onAttach() is ended!");
    }

}
