package org.ender.updater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Updater {

	public UpdaterConfig cfg;
	private IUpdaterListener listener;

	public Updater(IUpdaterListener listener) {
		this.listener = listener;
		this.cfg = new UpdaterConfig();
	}

	public void update() {
		Thread t = new Thread(new Runnable() {
			public void run() {
				List<UpdaterConfig.Item> update = new ArrayList<UpdaterConfig.Item>();
				for (UpdaterConfig.Item item : Updater.this.cfg.items) {
					if (Updater.this.correct_platform(item)) {
						Updater.this.set_date(item);
						if (Updater.this.has_update(item)) {
							Updater.this.listener.log(String.format("Updates found for '%s'", new Object[]{item.file.getName()}));
							update.add(item);
						} else {
							Updater.this.listener.log(String.format("No updates for '%s'", new Object[]{item.file.getName()}));
						}
					}
				}
				for (UpdaterConfig.Item item : update) {
					Updater.this.download(item);
					if (item.extract != null) {
						Updater.this.extract(item);
					}
				}

				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				}

				Updater.this.listener.finished();
			}
		});
		t.setDaemon(true);
		t.start();
	}

	private boolean correct_platform(UpdaterConfig.Item item) {
		String os = System.getProperty("os.name");
		String arch = System.getProperty("os.arch");
		return (os.indexOf(item.os) >= 0) && ((arch.equals(item.arch)) || (item.arch.length() == 0));
	}

	private void set_date(UpdaterConfig.Item item) {
		if (item.file.exists()) {
			item.date = item.file.lastModified();
		}
	}

	private boolean has_update(UpdaterConfig.Item item) {
		try {
			URL url = new URL(item.link);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("HEAD");
			conn.setIfModifiedSince(item.date);
			try {
				if (conn.getResponseCode() == 200) {
					item.size = Long.parseLong(conn.getHeaderField("Content-Length"));
					return true;
				}
			} catch (NumberFormatException localNumberFormatException) {
				conn.disconnect();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private void download(UpdaterConfig.Item item) {
		this.listener.log(String.format("Downloading '%s'", new Object[]{item.file.getName()}));
		try {
			URL link = new URL(item.link);
			ReadableByteChannel rbc = Channels.newChannel(link.openStream());
			FileOutputStream fos = new FileOutputStream(item.file);
			long position = 0L;
			int step = 20480;
			this.listener.progress(position, item.size);
			while (position < item.size) {
				position += fos.getChannel().transferFrom(rbc, position, step);
				this.listener.progress(position, item.size);
			}
			this.listener.progress(0L, item.size);
			fos.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void extract(UpdaterConfig.Item item) {
		this.listener.log(String.format("Unpacking '%s'", new Object[]{item.file.getName()}));
		try {
			ZipFile zip = new ZipFile(item.file);
			Enumeration contents = zip.entries();
			while (contents.hasMoreElements()) {
				ZipEntry file = (ZipEntry) contents.nextElement();
				String name = file.getName();
				if (name.indexOf("META-INF") != 0) {
					this.listener.log("\t" + name);
					ReadableByteChannel rbc = Channels.newChannel(zip.getInputStream(file));
					FileOutputStream fos = new FileOutputStream(new File(item.extract, name));
					long position = 0L;
					long size = file.getSize();
					int step = 20480;
					while (position < size) {
						position += fos.getChannel().transferFrom(rbc, position, step);
					}
					fos.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
