package com.maxproj.android.dirplayer;


import java.io.File;
import java.text.SimpleDateFormat;


public class LvRow{
	// 以下唯一确定一个文件或文件夹，相当于id
	private String path;
    private File file;
    
    // 以下可以根据id获取
    // 但获取比较耗时的，应该保存
    // 永久不变，不需更新的，也可以保存
    // 所以保存type、mime
    private String name;
    private String lengthStr;
    private String dateStr;
    private String mime;
    
    // 以下是自定义属性，需要保存
    private int type; // 0: paraent, 1: dir, 2: file
    private boolean selected;
    private int playingStatus;

    
	/**
	 * @return the playing
	 */
	public int getPlayingStatus() {
		return playingStatus;
	}
	/**
	 * @param playing the playing to set
	 */
	public void setPlayingStatus(int playingStatus) {
		this.playingStatus = playingStatus;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getPath(){
        return this.path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public String getName(){
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getLength() {
        return lengthStr;
    }

    public void setLength(String lengthStr) {
        this.lengthStr = lengthStr;
    }

    public String getDate() {
        return dateStr;
    }

    public void setDate(String dateStr) {
        this.dateStr = dateStr;
    }
    public File getFile() {
        return file;
    }
    public String getMime(){
        return this.mime;
    }

    public void setMime(String mime){
        this.mime = mime;
    }
    public void setFile(File file) {
        this.file = file;
    }
    public boolean getSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    /*
    public LvRow(String name, String length, String date, File file){
        this.name = name;
        this.length = length;
        this.date = date;
        this.file = file;
        this.selected = false;
    }
    */

    /**
     *	尽量用下面两个这个函数 
     */
    public LvRow(String path, 
    		boolean selected, int playingStatus){
    	
    	this.path = path;
    	File f = new File(path);
    	this.file = f;
    	
        this.name = f.getName();
        this.lengthStr = f.isDirectory()?"":"" + LocalConst.byteConvert(f.length());
        this.dateStr = "" + new SimpleDateFormat(LocalConst.time_format).format(f.lastModified());
        this.selected = selected;
        this.type = f.isDirectory()?1:2;
        this.mime = LocalConst.getMimeByFileName(f.getName());
        this.playingStatus = playingStatus;
    }
    public LvRow(File f, 
    		boolean selected, int playingStatus){

    	this.path = f.getPath();
    	this.file = f;
    	
        this.name = f.getName();
        this.lengthStr = f.isDirectory()?"":"" + LocalConst.byteConvert(f.length());
        this.dateStr = "" + new SimpleDateFormat(LocalConst.time_format).format(f.lastModified());
        this.selected = selected;
        this.type = f.isDirectory()?LocalConst.TYPE_DIR:LocalConst.TYPE_FILE;
        this.mime = LocalConst.getMimeByFileName(f.getName());
        this.playingStatus = playingStatus;
    }
    
    /**
     *	这个构造函数可以设置类型为0，也即特殊的“/..” 
     */
    public static LvRow lvRowParaent(String path, 
    		boolean selected, int playingStatus){
    	
    	LvRow lr = new LvRow(path, false, LocalConst.clear);
    	lr.type = LocalConst.TYPE_PARAENT;
    	lr.name = LocalConst.PARAENT_NAME;
    	lr.dateStr = "";
    	
    	return lr;
    }
}

