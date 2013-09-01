package com.androidmontreal.weddingvideoguestbook.db;


public class DBItem {
	protected String thumbnailImagePath;
	protected long id;
	protected String filename;

	public DBItem(String thumbnailImagePath, long id, String filename) {
		super();
		this.thumbnailImagePath = thumbnailImagePath;
		this.id = id;
		this.filename = filename;
	}

	public String getThumbnailImagePath() {
		return thumbnailImagePath;
	}

	public void setImage(String thumbnailImagePath) {
		this.thumbnailImagePath = thumbnailImagePath;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	
}