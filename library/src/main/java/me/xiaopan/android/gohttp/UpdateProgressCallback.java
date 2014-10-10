package me.xiaopan.android.gohttp;

public abstract class UpdateProgressCallback{
	private boolean markRead;
	
	public boolean isMarkRead() {
		return markRead;
	}

	public void setMarkRead(boolean markRead) {
		this.markRead = markRead;
	}
	
	public abstract void onUpdateProgress(long contentLength, long completedLength);
}