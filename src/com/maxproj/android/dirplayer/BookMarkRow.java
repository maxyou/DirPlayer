package com.maxproj.android.dirplayer;

public class BookMarkRow {
	private String path;
	private boolean selected;

	public String getPath() {
		return this.path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean getSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public BookMarkRow(String path) {
		this.path = path;

		this.selected = false;
	}

	public BookMarkRow(String path, boolean selected) {
		this.path = path;

		this.selected = selected;
	}

}
