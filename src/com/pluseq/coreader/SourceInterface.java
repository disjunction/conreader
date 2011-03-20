package com.pluseq.coreader;

import android.content.Context;
import android.view.View;

/**
 * File selector and reader. Main source implementations are SourceFileSystem and SourceWeb
 * @author or
 */
public interface SourceInterface {
	public View getSourceSelectorView(Context context);
	public String getFileId();
	public void setFilePath(String filePath);
	public void setEncoding(String encoding);
	public String getBookHash();
}
