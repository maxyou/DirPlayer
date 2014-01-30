package com.maxproj.android.dirplayer;


import java.io.File;
import java.net.URLConnection;


public class LvRow{
    private String name;
    private String length;
    private String date;
    private File file;
    private boolean selected;
    private int type; // 0: paraent, 1: dir, 2: file
    private String mime;
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
	public String getName(){
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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
    
    public LvRow(String name, String length, String date, File file, 
    		boolean selected, int type, String mime, int playingStatus){
        this.name = name;
        this.length = length;
        this.date = date;
        this.file = file;
        this.selected = selected;
        this.type = type;
        this.mime = mime;
        this.playingStatus = playingStatus;
    }

}

