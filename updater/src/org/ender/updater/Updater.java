package org.ender.updater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.krush.helper.Helper;

public class Updater {

	public UpdaterConfig cfg;
	private final IUpdaterListener listener;

	public Updater(IUpdaterListener listener) {
		this.listener = listener;
		this.cfg = new UpdaterConfig();
	}

	public void update() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				boolean success = true;

				Updater.this.listener.log("Checking for updates...");
				List<UpdaterConfig.Item> update = new ArrayList<>();
				for (UpdaterConfig.Item item : Updater.this.cfg.items) {
					if (Updater.this.correct_platform(item)) {
						Updater.this.init(item);
						if (Updater.this.has_update(item)) {
							Updater.this.listener.log(String.format("Updates found for '%s'", new Object[]{item.file.getName()}));
							if (item.file.getName().equals(Main.JarName)) {
								success = false;
								Updater.this.listener.log(String.format("Please manually download this file"));
								Updater.this.listener.log(String.format("%s", new Object[]{item.link}));
								break;
							}

							Updater.this.download(item);
							if (item.extract != null) {
								Updater.this.extract(item);
							}
						} else {
							Updater.this.listener.log(String.format("No updates for '%s'", new Object[]{item.file.getName()}));
						}
					}
				}

				if (success) {
					Updater.this.listener.finished();
				}
			}
		});
		t.setDaemon(true);
		t.start();
	}

	private boolean correct_platform(UpdaterConfig.Item item) {
		String os = System.getProperty("os.name");
		String arch = System.getProperty("os.arch");
		return (os.contains(item.os)) && ((arch.equals(item.arch)) || (item.arch.length() == 0));
	}

	private void init(UpdaterConfig.Item item) {
		if (item.file.exists()) {
			item.date = item.file.lastModified();
			item.size = item.file.length();
		}
	}

	private boolean has_update(UpdaterConfig.Item item) {
		try {
			if (Main.TESTING && ("hafen.jar".equals(item.file.getName()) || "hafen-minion.jar".equals(item.file.getName()))) {
				return false;
			}

			URL url = new URL(item.link);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("HEAD");
			try {
				if (conn.getResponseCode() == 200) {
					Long oldSize = item.size;
					Long oldDate = item.date;
					Long size = conn.getContentLengthLong();
					Long date = conn.getLastModified();
					item.size = size;
					return oldSize != item.size || oldDate < date;
				}
			} catch (NumberFormatException localNumberFormatException) {
				conn.disconnect();
			}
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}
		return false;
	}

	private void download(UpdaterConfig.Item item) {
		this.listener.log(String.format("Downloading '%s' [%s]", new Object[]{item.file.getName(), Helper.readableFileSize(item.size)}));
		try {
			URL link = new URL(item.link);
			ReadableByteChannel rbc = Channels.newChannel(link.openStream());
			try (FileOutputStream fos = new FileOutputStream(item.file)) {
				long position = 0L;
				int step = 20480;
				this.listener.progress(position, item.size);
				while (position < item.size) {
					position += fos.getChannel().transferFrom(rbc, position, step);
					this.listener.progress(position, item.size);
				}
				this.listener.progressFinished();
			}
		} catch (MalformedURLException e) {
		} catch (IOException e) {
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
					try (FileOutputStream fos = new FileOutputStream(new File(item.extract, name))) {
						long position = 0L;
						long size = file.getSize();
						int step = 20480;
						while (position < size) {
							position += fos.getChannel().transferFrom(rbc, position, step);
						}
					}
				}
			}
		} catch (IOException e) {
		}

	}
}
