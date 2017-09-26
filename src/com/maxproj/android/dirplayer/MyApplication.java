package com.maxproj.android.dirplayer;

import java.io.File;
import java.util.LinkedList;

import com.maxproj.android.dirplayer.LocalConst.RootsVirName;

import android.annotation.SuppressLint;

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
    	
    	LocalConst.roots = new LinkedList<RootsVirName>();

    	/**
    	 * 不可拆卸外存
    	 * 可拆卸外存(SD卡)
    	 * 只需要根目录
    	 */
    	LocalConst.roots.add(new RootsVirName(Environment.getExternalStorageDirectory(),
    			getResources().getString(R.string.sys_external_storage)));
    	
    	if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
        	File files[] = context.getExternalFilesDirs(null);
        	
        	/**
        	 * getExternalFilesDirs() return:
        	 * 	/storage/emulated/0/Android/data/com.maxproj.android.dirplayer/files
			 * 	/storage/sdcard1/Android/data/com.maxproj.android.dirplayer/files
			 * 从第二项开始是sd卡
        	 */
        	
        	if(files.length > 1){
        		
	            for(int i = 1; i < files.length; i++) {
	            	if(files[i] != null){
	            		if(files[i].getParentFile() != null){
	            			if(files[i].getParentFile().getParentFile() != null){
	            				if(files[i].getParentFile().getParentFile().getParentFile() != null){
	            					LocalConst.roots.add(new RootsVirName(files
	            	                		[i].getParentFile().getParentFile().getParentFile().getParentFile(),
	            	                		getResources().getString(R.string.sys_sdcard)+i
	            	                		));
	            				}
	            			}
	            		}
	            	}
	                
	            }
        	}
    	}
        
        /**
         * 公共媒体目录
         */
        LocalConst.roots.add(new RootsVirName(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
        		getResources().getString(R.string.sys_music)));
        LocalConst.roots.add(new RootsVirName(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
        		getResources().getString(R.string.sys_movie)));
//        
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
//	        /**
//	         * app媒体目录
//	         */
//	        File files2[] = context.getExternalFilesDirs(Environment.DIRECTORY_MUSIC);
//	        for(int i = 0; i < files2.length; i++) {
//	            LocalConst.roots.add(files2[i]);
//	        }
//	        File files3[] = context.getExternalFilesDirs(Environment.DIRECTORY_MOVIES);
//	        for(int i = 0; i < files3.length; i++) {
//	            LocalConst.roots.add(files2[i]);
//	        }
//        }
        
        for(RootsVirName r : LocalConst.roots){
        	Log.d(LocalConst.DTAG, "updateFileInfor: roots "+r.file.getAbsolutePath());
        }
    }
}
