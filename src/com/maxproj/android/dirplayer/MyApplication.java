package com.maxproj.android.dirplayer;

import java.io.File;
import java.util.LinkedList;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        initRoots();
        
    }
    
    @SuppressLint("NewApi") public void initRoots(){
    	
    	Context context = getApplicationContext();
    	
    	LocalConst.roots = new LinkedList<File>();

    	/**
    	 * 不可拆卸外存
    	 * 可拆卸外存(SD卡)
    	 */
    	if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
        	File files[] = context.getExternalFilesDirs(null);
            for(int i = 0; i < files.length; i++) {
                LocalConst.roots.add(files[i]);
            }
    	}else{
    		LocalConst.roots.add(context.getExternalFilesDir(null));
    	}
        
        /**
         * 公共媒体目录
         */
        LocalConst.roots.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
        LocalConst.roots.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES));
        
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
	        /**
	         * app媒体目录
	         */
	        File files2[] = context.getExternalFilesDirs(Environment.DIRECTORY_MUSIC);
	        for(int i = 0; i < files2.length; i++) {
	            LocalConst.roots.add(files2[i]);
	        }
	        File files3[] = context.getExternalFilesDirs(Environment.DIRECTORY_MOVIES);
	        for(int i = 0; i < files3.length; i++) {
	            LocalConst.roots.add(files2[i]);
	        }
        }
    }
}
