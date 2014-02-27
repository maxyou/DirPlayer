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
import android.widget.TextView;

public class FragmentListview extends Fragment {
    int tab = -1; // attach时如果为-1，说明是系统帮忙恢复的。正常初始化后会被设置为0和1
    String currentPath = "static_ini_code_null";
    MyArrayAdapter listAdapter = null;
    ListView listView = null;
    TextView show_path = null;
    View fragmentView;

    Button b1, b2, b3, b4, b5, b6;

    public interface FragmentListviewInterface{
        void onFragmentListviewLongClicked(int i,int tab);
        void onFragmentListviewClicked(int i,int tab);
        void onFragmentButton1(int tab);
        void onFragmentButton2(int tab);
        void onFragmentButton3(int tab);
        void onFragmentButton4(int tab);
        void onFragmentButton5(int tab);
        void onFragmentButton6(int tab);
        void sysAttachFragmentListviewLowMem(int tab, FragmentListview fragment);
    }
    private FragmentListviewInterface fragmentListviewInterface = null;

    /* 
    public FragmentListview(int tab){
        Log.d(LocalConst.DTAG,"fragment " + tab + " is initialized");
        this.tab = tab;
    }
    */
    public static FragmentListview newInstance(int tab) {    	
        
        FragmentListview fragment = new FragmentListview();
        fragment.tab = tab;

        Log.d(LocalConst.LIFECYCLE, "FragmentListview.newInstance() "+tab+" "+fragment);
        return fragment;
    }

    public void setListviewAdapter(MyArrayAdapter a, String newPath){
        listAdapter = a;
    	Log.d(LocalConst.LIFECYCLE, "FragmentListview.setListviewAdapter() "+tab+":"+currentPath+" "+this);
        currentPath = newPath;
        Log.d(LocalConst.LIFECYCLE, "FragmentListview.setListviewAdapter() "+tab+":"+currentPath+" "+this);
        
//        LocalConst.currentPath_fragmentList[tab] = currentPath;
//        LocalConst.myArrayAdapter_fragmentList[tab] = a;
        
        
		//show path at upper of listview
		//show_path = (TextView)fragmentView.findViewById(R.id.show_path);
        if(show_path !=null){
        	show_path.setText("当前路径是"+currentPath);
            Log.d(LocalConst.FRAGMENT_LIFE,"setListviewAdapter(): show_path is set to "+currentPath);
        }else{
            Log.d(LocalConst.FRAGMENT_LIFE,"setListviewAdapter(): show_path is null pointer!");
        }
		
        if (listView != null){
            listView.setAdapter(listAdapter);
            Log.d(LocalConst.FRAGMENT_LIFE,"setListviewAdapter(): adapter is set!");
        }else{
            Log.d(LocalConst.FRAGMENT_LIFE,"setListviewAdapter(): listView is null pointer!");
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
    	Log.d(LocalConst.LIFECYCLE, "FragmentListview.onCreateView() "+tab+":"+currentPath+" "+this);
        fragmentView =  inflater.inflate(R.layout.fragment_listview, container, false);
        
        b1 = (Button)fragmentView.findViewById(R.id.fl_b1);
        b1.setOnClickListener(new View.OnClickListener() { // 全选
            @Override
            public void onClick(View view) {
                fragmentListviewInterface.onFragmentButton1(tab);
            }
        });
        b2 = (Button)fragmentView.findViewById(R.id.fl_b2);
        b2.setOnClickListener(new View.OnClickListener() { // 清除
            @Override
            public void onClick(View view) {
                fragmentListviewInterface.onFragmentButton2(tab);
            }
        });
        b3 = (Button)fragmentView.findViewById(R.id.fl_b3);
        b3.setOnClickListener(new View.OnClickListener() { // 反选
            @Override
            public void onClick(View view) {
                fragmentListviewInterface.onFragmentButton3(tab);
            }
        });
        b4 = (Button)fragmentView.findViewById(R.id.fl_b4);
        b4.setOnClickListener(new View.OnClickListener() { // 向上
            @Override
            public void onClick(View view) {
            	Log.d(LocalConst.DTAG,"button test: b4 is clicked....");
                fragmentListviewInterface.onFragmentButton4(tab);
            }
        });
        b5 = (Button)fragmentView.findViewById(R.id.fl_b5);
        b5.setOnClickListener(new View.OnClickListener() { // 收藏
            @Override
            public void onClick(View view) {
            	Log.d(LocalConst.DTAG,"button test: b5 is clicked....");
                fragmentListviewInterface.onFragmentButton5(tab);
            }
        });
        b6 = (Button)fragmentView.findViewById(R.id.fl_b6);
        b6.setOnClickListener(new View.OnClickListener() { // 操作
            @Override
            public void onClick(View view) {
            	Log.d(LocalConst.DTAG,"button test: b6 is clicked....");
                fragmentListviewInterface.onFragmentButton6(tab);
            }
        });
        listView = (ListView)fragmentView.findViewById(R.id.fragment_listview);


        listView.setOnItemClickListener(new ItemClicklistener());
        Log.d(LocalConst.FRAGMENT_LIFE,"listView.setOnItemClickListener(new ItemClicklistener())!");

        listView.setOnItemLongClickListener(new ItemLongClickListener());
                
        
		//show path at upper of listview
		show_path = (TextView)fragmentView.findViewById(R.id.show_path);
//        if(show_path !=null){
//        	show_path.setText("当前路径是"+currentPath);
//            Log.d(LocalConst.DTAG,"setListviewAdapter(): show_path is set to "+currentPath);
//        }
		Log.d(LocalConst.FRAGMENT_LIFE, "fragment onCreateView("+tab+") end!");
        return fragmentView;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.d(LocalConst.LIFECYCLE, "FragmentListview.onActivityCreated() "+tab+":"+currentPath+" "+this);

//        if (savedInstanceState != null) {
//        	currentPath = savedInstanceState.getString("currentPath", LocalConst.pathRoot);
//        }
        if (currentPath == null){
//        	currentPath = LocalConst.currentPath_fragmentList[tab];
        }
        if (listAdapter == null){
//        	listAdapter = LocalConst.myArrayAdapter_fragmentList[tab];
        }
        
        if(show_path !=null){
        	show_path.setText("当前路径是"+currentPath);
            Log.d(LocalConst.DTAG,"setListviewAdapter(): show_path is "+tab+":"+currentPath);
        }
        
        if (listAdapter != null){
            listView.setAdapter(listAdapter);
            Log.d(LocalConst.DTAG,"(maa != null) and adapter is set!");
        }else{
            Log.d(LocalConst.DTAG,"maa is null pointer!");
        }
    }
    private class ItemLongClickListener implements AdapterView.OnItemLongClickListener{

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
            fragmentListviewInterface.onFragmentListviewLongClicked(arg2,tab);

            Log.d(LocalConst.DTAG,"ItemLongClickListener is called!");
			return false;
		}
    	
    }
    private class ItemClicklistener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            fragmentListviewInterface.onFragmentListviewClicked(i,tab);

            Log.d(LocalConst.DTAG,"ItemClicklistener is called!");
        }
    }
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        
        Log.d(LocalConst.LIFECYCLE, "FragmentListview.onAttach() "+tab+":"+currentPath+" "+this);
        try{
        	Log.d(LocalConst.DTAG,"check if activity implement interface....");
            fragmentListviewInterface = (FragmentListviewInterface)activity;
            Log.d(LocalConst.DTAG,"activity implemented interface!");
        }catch(ClassCastException e){
            Log.d(LocalConst.DTAG,"onAttach() throw new ClassCastException!");
            throw new ClassCastException(activity.toString()+ " must implement "
                    +fragmentListviewInterface.toString());
        }
        Log.d(LocalConst.FRAGMENT_LIFE,"onAttach() is ended!");
        
        /**
         * 有时候系统会帮我重建fragment，但是却不交给我指针
         * 这里试验一下能否自己更新指针
         * 
         * 首先判断，自己是app创建的，还是系统帮忙创建的
         */

        if (tab == -1){ // 系统帮忙创建的，更新到activity
        	tab = ((DirPlayerActivity) getActivity()).sysAttachFragment;
        	fragmentListviewInterface.sysAttachFragmentListviewLowMem(
        			tab, 
        			this);
        	((DirPlayerActivity) getActivity()).sysAttachFragment++;
        }
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.d(LocalConst.LIFECYCLE, "FragmentListview.onCreate() "+tab+":"+currentPath+" "+this);
		

	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.d(LocalConst.LIFECYCLE, "FragmentListview.onDestroy() "+tab+":"+currentPath+" "+this);
	}
	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		Log.d(LocalConst.LIFECYCLE, "FragmentListview.onDetach() "+tab+":"+currentPath+" "+this);
	}
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.d(LocalConst.LIFECYCLE, "FragmentListview.onPause() "+tab+":"+currentPath+" "+this);
	}
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d(LocalConst.LIFECYCLE, "FragmentListview.onResume() "+tab+":"+currentPath+" "+this);
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		Log.d(LocalConst.LIFECYCLE, "FragmentListview.onSaveInstanceState() "+tab+":"+currentPath+" "+this);
	}
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.d(LocalConst.LIFECYCLE, "FragmentListview.onStart() "+tab+":"+currentPath+" "+this);
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.d(LocalConst.LIFECYCLE, "FragmentListview.onStop() "+tab+":"+currentPath+" "+this);
	}

}
