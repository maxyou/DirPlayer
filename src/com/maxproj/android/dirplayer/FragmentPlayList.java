package com.maxproj.android.dirplayer;



import java.util.LinkedList;

import org.apache.http.impl.io.ChunkedOutputStream;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.RadioButton;
import android.widget.TextView;


public class FragmentPlayList  extends Fragment {

    View fragmentView = null;
    Button b1, b2, b3, b4, b5, b6;
    Button[] plChooser = new Button[LocalConst.plCount];
//    Button t1, t2, t3, t4, t5;
    
    int localPlTab = 0;
    int currentPlayingTab = 0;
    
    /**
     * 由于要保存数据，所以atapter有多个，这个很自然
     * 但是listview只要一个，还是设为多个？这里比较迷惑
     * 一个的话节省资源，但是切换的时候需要保存多个view状态，比如scroll到什么位置了，等等，比较麻烦
     * 进度原因，这里先设置多个view，非当前的view先隐藏
     */
    class PlayListTabGroup{
    	int tabViewId; 
    	LinearLayout tabView; 
    	
    	int listViewId;
    	ListView listView;
    	
    	int pathId;
    	TextView pathView;
    	String path;
    	
    	MyArrayAdapter listAdapter;

    	int radioId;
    	
    	public PlayListTabGroup(
    			int tabViewId,
    			LinearLayout tabView,
    	    	int listViewId,
    	    	ListView listView,    	    	
    	    	int pathId,
    	    	TextView pathView,
    	    	String path,    	    	
    	    	MyArrayAdapter listAdapter,
    	    	int radioId
    			){
    		this.tabViewId = tabViewId;
    		this.tabView = tabView;
	    	this.listViewId = listViewId;
	    	this.listView= listView;	    	
	    	this.pathId = pathId;
	    	this.pathView = pathView;
	    	this.path = path;	    	
	    	this.listAdapter = listAdapter;
	    	this.radioId = radioId;
    	}
    }
    PlayListTabGroup[] playListTabGroup = new PlayListTabGroup[LocalConst.plCount];
    
    public interface FragmentPlayListInterface{
        void onFragmentPlayListClicked(int i, int plTab);
        void onFragmentPlayListButton1();
        void onFragmentPlayListButton2();
        void onFragmentPlayListButton3();
        void onFragmentPlayListButton4();
        void onFragmentPlayListButton5();
        void onFragmentPlayListButton6();
        void onFragmentPlayListButton7();
        PlayService getServiceConnection();
        void sysAttachFragmentPlayListLowMem(FragmentPlayList fragment);
    }
    private FragmentPlayListInterface fragmentPlayListInterface = null;

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
    	Log.d(LocalConst.LIFECYCLE, "FragmentPlayList.newInstance()");
        return fragment;
    }

    public void setListviewAdapter(MyArrayAdapter a, int plTab){
    	if(playListTabGroup[plTab] != null){//这个要及早创建----------------------应该有个机制来保证次序
	        playListTabGroup[plTab].listAdapter = a;
	        if (playListTabGroup[plTab].listView != null){
	            playListTabGroup[plTab].listView.setAdapter(playListTabGroup[plTab].listAdapter);
	            Log.d(LocalConst.LIFECYCLE, "pl setListviewAdapter()");
	        }else{
	        	Log.d(LocalConst.LIFECYCLE, "pl setListviewAdapter() when null");
	        }
    	}
    }
    public View getItemView(int position){

        if (position > playListTabGroup[localPlTab].listView.getCount())
            return null;
        else
            return playListTabGroup[localPlTab].listView.getChildAt(position);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	Log.d(LocalConst.LIFECYCLE, "pl onCreateView()");
        //return super.onCreateView(inflater, container, savedInstanceState);
        fragmentView =  inflater.inflate(R.layout.fragment_playlist, container, false);
        
        b1 = (Button)fragmentView.findViewById(R.id.pl_b1);
        b1.setOnClickListener(new View.OnClickListener() { // 全选
            @Override
            public void onClick(View view) {
            	fragmentPlayListInterface.onFragmentPlayListButton1();
            }
        });
        b2 = (Button)fragmentView.findViewById(R.id.pl_b2);
        b2.setOnClickListener(new View.OnClickListener() { // 清除
            @Override
            public void onClick(View view) {
            	fragmentPlayListInterface.onFragmentPlayListButton2();
            }
        });
        b3 = (Button)fragmentView.findViewById(R.id.pl_b3);
        b3.setOnClickListener(new View.OnClickListener() { // 反选
            @Override
            public void onClick(View view) {
            	fragmentPlayListInterface.onFragmentPlayListButton3();
            }
        });
        b4 = (Button)fragmentView.findViewById(R.id.pl_b4);
        b4.setOnClickListener(new View.OnClickListener() { // 上移
            @Override
            public void onClick(View view) {
            	fragmentPlayListInterface.onFragmentPlayListButton4();
            }
        });
        b5 = (Button)fragmentView.findViewById(R.id.pl_b5);
        b5.setOnClickListener(new View.OnClickListener() { // 下移
            @Override
            public void onClick(View view) {
            	fragmentPlayListInterface.onFragmentPlayListButton5();
            }
        });
        b6 = (Button)fragmentView.findViewById(R.id.pl_b6);
        b6.setOnClickListener(new View.OnClickListener() { // 删除
            @Override
            public void onClick(View view) {
            	fragmentPlayListInterface.onFragmentPlayListButton6();
            }
        });

		for (int i = 0; i < LocalConst.plCount; i++) {
			playListTabGroup[i] = new PlayListTabGroup(
	    			LocalConst.plViewId[i][0],//tabViewId
	    			(LinearLayout)fragmentView.findViewById(LocalConst.plViewId[i][0]),//tabView
	    			LocalConst.plViewId[i][1],//listview id
	    	    	(ListView)fragmentView.findViewById(LocalConst.plViewId[i][1]),//listview    	    	
	    	    	LocalConst.plViewId[i][2],//path view id
	    	    	(TextView)fragmentView.findViewById(LocalConst.plViewId[i][2]),//path view
	    	    	null,//path
	    	    	null,//adapter
	    	    	LocalConst.plViewId[i][3]//radio id
					);
			playListTabGroup[i].listView.setOnItemClickListener(new ItemClicklistener(i));
		}

		//更新主activity的plTab到本fragment
		//或者更往前一点，放在attached的时候？
		localPlTab = ((DirPlayerActivity)getActivity()).currentPlTab;
		currentPlayingTab = ((DirPlayerActivity)getActivity()).currentPlayingTab;
		
		for(int i=0;i<LocalConst.plCount;i++){
			plChooser[i] = (Button)fragmentView.findViewById(tab2RadioId(i));
			plChooser[i].setOnClickListener(new PlChooser(i));
		}
        return fragmentView;
    }
    
    class PlChooser implements View.OnClickListener{

    	int choosed;
    	PlChooser(int i){
    		choosed = i;
    	}
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			((DirPlayerActivity)getActivity()).currentPlTab = choosed;
			localPlTab = choosed;
			showPlayListView();
			showPlayListChooser();
		}
    	
    }
    
    public void showPlayListChooser(){
    	for (int i=0;i<LocalConst.plCount;i++){
        	/**
        	 * 设置“当前操作”标志
        	 * 设置“当前播放”标志
        	 */    		
//    		plChooser[i].setPressed((i==localPlTab)?true:false);
//    		plChooser[i].setBackgroundColor((i==currentPlayingTab)?0:1);
    		
    		String bs = (String) (plChooser[i].getText().subSequence(0, 1));
    		plChooser[i].setText(bs+((i==localPlTab)?"@":"")+((i==currentPlayingTab)?"#":""));
    	}
    }
    public void showPlayListView(){
    	Log.d(LocalConst.LIFECYCLE, "pl showPlayListView("+localPlTab+")");
		for(int i=0;i<LocalConst.plCount;i++){
			if(i == localPlTab){
				playListTabGroup[i].tabView.setVisibility(View.VISIBLE);
			}else{
				playListTabGroup[i].tabView.setVisibility(View.INVISIBLE);				
			}
		}
    }
    
    
    public int tab2RadioId(int plTab){
		switch(plTab){
			case 0: return R.id.pl_radio_1;
			case 1: return R.id.pl_radio_2;
			case 2: return R.id.pl_radio_3;
			case 3: return R.id.pl_radio_4;
			case 4: return R.id.pl_radio_5;
		}
		return R.id.pl_radio_1;
    }
    public int radioId2Tab(int id){
		switch(id){
			case R.id.pl_radio_1: return 0;
			case R.id.pl_radio_2: return 1;
			case R.id.pl_radio_3: return 2;
			case R.id.pl_radio_4: return 3;
			case R.id.pl_radio_5: return 4;
		}
    	return 0; 
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

//        for(int i=0;i<LocalConst.plCount;i++){
//        	if(playListTabGroup[localPlTab] != null){
//        		
//        		if (playListTabGroup[localPlTab].listAdapter != null){
//		            playListTabGroup[localPlTab].listView.setAdapter(playListTabGroup[localPlTab].listAdapter);
//		            Log.d(LocalConst.LIFECYCLE, "pl onActivityCreated() set adapter "+i);
//		        }else{
//		
//		        }
//        		
//        		playListTabGroup[localPlTab].pathView.setText(playListTabGroup[localPlTab].path);
//	        }
//        }
        
        mService = fragmentPlayListInterface.getServiceConnection();
        Log.d(LocalConst.LIFECYCLE, "pl onActivityCreated()");
    }
    public void setPathView(String path, int plTab){
    	/**
    	 * 这里有个问题。
    	 * 刚开机，还没有切换到播放tab，也即这个tab还没有初始化，此刻设置pathView导致空指针异常
    	 */
    	if(playListTabGroup[plTab] != null){
    		playListTabGroup[plTab].path = path;
    		if(playListTabGroup[plTab].pathView != null){
    			playListTabGroup[plTab].pathView.setText(path);
    		}
    	}
    }
    private class ItemClicklistener implements AdapterView.OnItemClickListener {
    	
    	int inner_plTab;
    	
    	public ItemClicklistener(int plTab){
    		inner_plTab = plTab;
    	}
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        	fragmentPlayListInterface.onFragmentPlayListClicked(i, inner_plTab);
        	
        	currentPlayingTab = inner_plTab;
        	showPlayListChooser();
            Log.d(LocalConst.DTAG,"FragmentPlayList ItemClicklistener is called!");
        }
    }
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try{
        	fragmentPlayListInterface = (FragmentPlayListInterface)activity;
        }catch(ClassCastException e){
            throw new ClassCastException(activity.toString()+ " must implement "
                    +fragmentPlayListInterface.toString());
        }
        
        fragmentPlayListInterface.sysAttachFragmentPlayListLowMem(this);
        
        Log.d(LocalConst.LIFECYCLE, "pl onAttach()");
    }

    @Override
    public void onResume(){
		super.onResume();
		
		showPlayListChooser();
		showPlayListView();//显示播放列表
		
		((DirPlayerActivity)getActivity()).updateFragmentLight();//考虑改成消息驱动模式
		
		Intent intent = new Intent(
				LocalConst.FRAG_PLAY_LIST_UPDATE_ACTION);
		LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
				intent);
		Log.d(LocalConst.LIFECYCLE, "pl onResume()");
    }

}
