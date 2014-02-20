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


public class FragmentBookMark  extends Fragment {

    BookMarkArrayAdapter listAdapter = null;
    ListView listView = null;
    View fragmentView;

    Button b1, b2, b3, b4, b5;

    public interface FragmentBookMarkInterface{
        void onFragmentBookMarkClicked(int i);
        void onFragmentBookMarkButton1();
        void onFragmentBookMarkButton2();
        void onFragmentBookMarkButton3();
        void onFragmentBookMarkButton4();
        void onFragmentBookMarkButton5();
    }
    private FragmentBookMarkInterface fragmentBookMarkInterface = null;

    /* 
    public FragmentListview(int tab){
        Log.d(LocalConst.DTAG,"fragment " + tab + " is initialized");
        this.tab = tab;
    }
    */
    public static FragmentBookMark newInstance() {
    	FragmentBookMark fragment = new FragmentBookMark();        
    	Log.d(LocalConst.DTAG,"FragmentBookMark newInstance()");
        return fragment;
    }

    public void setListviewAdapter(BookMarkArrayAdapter a){
        listAdapter = a;

        if (listView != null){
            listView.setAdapter(listAdapter);
            Log.d(LocalConst.DTAG,"FragmentBookMark setListviewAdapter(): adapter is set!");
        }else{
            Log.d(LocalConst.DTAG,"FragmentBookMark setListviewAdapter(): listView is null pointer!");
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
        Log.d(LocalConst.DTAG,"FragmentBookMark onCreateView() is called!");
        fragmentView =  inflater.inflate(R.layout.fragment_bookmark, container, false);
        
        b1 = (Button)fragmentView.findViewById(R.id.bm_b1);
        b1.setOnClickListener(new View.OnClickListener() { // 全选
            @Override
            public void onClick(View view) {
            	fragmentBookMarkInterface.onFragmentBookMarkButton1();
            }
        });
        b2 = (Button)fragmentView.findViewById(R.id.bm_b2);
        b2.setOnClickListener(new View.OnClickListener() { // 清除
            @Override
            public void onClick(View view) {
            	fragmentBookMarkInterface.onFragmentBookMarkButton2();
            }
        });
        b3 = (Button)fragmentView.findViewById(R.id.bm_b3);
        b3.setOnClickListener(new View.OnClickListener() { // 反选
            @Override
            public void onClick(View view) {
            	fragmentBookMarkInterface.onFragmentBookMarkButton3();
            }
        });
        b4 = (Button)fragmentView.findViewById(R.id.bm_b4);
        b4.setOnClickListener(new View.OnClickListener() { // 删除
            @Override
            public void onClick(View view) {
            	fragmentBookMarkInterface.onFragmentBookMarkButton4();
            }
        });
        b5 = (Button)fragmentView.findViewById(R.id.bm_b5);
        b5.setOnClickListener(new View.OnClickListener() { // 修改
            @Override
            public void onClick(View view) {
            	fragmentBookMarkInterface.onFragmentBookMarkButton5();
            }
        });

        listView = (ListView)fragmentView.findViewById(R.id.fragment_bookmark);


        listView.setOnItemClickListener(new ItemClicklistener());
        Log.d(LocalConst.DTAG,"FragmentBookMark listView.setOnItemClickListener(new ItemClicklistener())!");

        return fragmentView;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.d(LocalConst.DTAG,"FragmentBookMark onActivityCreated() is called!");

        if (listAdapter != null){
            listView.setAdapter(listAdapter);
            Log.d(LocalConst.DTAG,"FragmentBookMark (maa != null) and adapter is set!");
        }else{
            Log.d(LocalConst.DTAG,"FragmentBookMark maa is null pointer!");
        }
    }
    private class ItemClicklistener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        	fragmentBookMarkInterface.onFragmentBookMarkClicked(i);

            Log.d(LocalConst.DTAG,"FragmentBookMark ItemClicklistener is called!");
        }
    }
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        Log.d(LocalConst.DTAG,"FragmentBookMark onAttach() is called!");
        try{
        	Log.d(LocalConst.DTAG,"FragmentBookMark check if activity implement interface....");
        	fragmentBookMarkInterface = (FragmentBookMarkInterface)activity;
            Log.d(LocalConst.DTAG,"activity implemented interface!");
        }catch(ClassCastException e){
            Log.d(LocalConst.DTAG,"FragmentBookMark onAttach() throw new ClassCastException!");
            throw new ClassCastException(activity.toString()+ " must implement "
                    +fragmentBookMarkInterface.toString());
        }
        Log.d(LocalConst.DTAG,"FragmentBookMark onAttach() is ended!");
    }

}
