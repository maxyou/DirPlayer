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
    int tab;
    String currentPath = "test null path";
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
        return fragment;
    }

    public void setListviewAdapter(MyArrayAdapter a, String currentPath){
        listAdapter = a;
        this.currentPath = currentPath;

		//show path at upper of listview
		//show_path = (TextView)fragmentView.findViewById(R.id.show_path);
        if(show_path !=null){
        	show_path.setText("当前路径是"+this.currentPath);
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
        Log.d(LocalConst.FRAGMENT_LIFE,"onCreateView() is called!");
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
        b4.setOnClickListener(new View.OnClickListener() { // 书签
            @Override
            public void onClick(View view) {
            	Log.d(LocalConst.DTAG,"button test: b4 is clicked....");
                fragmentListviewInterface.onFragmentButton4(tab);
            }
        });
        b5 = (Button)fragmentView.findViewById(R.id.fl_b5);
        b5.setOnClickListener(new View.OnClickListener() { // 操作
            @Override
            public void onClick(View view) {
            	Log.d(LocalConst.DTAG,"button test: b5 is clicked....");
                fragmentListviewInterface.onFragmentButton5(tab);
            }
        });
        b6 = (Button)fragmentView.findViewById(R.id.fl_b6);
        b6.setOnClickListener(new View.OnClickListener() { // 向上
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
        if(show_path !=null){
        	show_path.setText("当前路径是"+currentPath);
            Log.d(LocalConst.DTAG,"setListviewAdapter(): show_path is set to "+currentPath);
        }
		Log.d(LocalConst.FRAGMENT_LIFE, "fragment onCreateView("+tab+") end!");
        return fragmentView;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.d(LocalConst.FRAGMENT_LIFE,"onActivityCreated() is called!");

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
        Log.d(LocalConst.FRAGMENT_LIFE,"onAttach() is called!");
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
    }
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		// 关掉本tab关联的媒体？
		
		
	}

}
