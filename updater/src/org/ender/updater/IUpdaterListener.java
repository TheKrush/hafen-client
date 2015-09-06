package org.ender.updater;

public abstract interface IUpdaterListener {

	public abstract void log(String paramString);

	public abstract void finished();

	public abstract void progress(long paramLong1, long paramLong2);
}
