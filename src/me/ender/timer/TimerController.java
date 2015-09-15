package me.ender.timer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import haven.Config;
import haven.Utils;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class TimerController {

	private static File config;
	private double delta = 0;
	public List<Timer> timers;
	final public Object lock = new Object();

	public TimerController() {
		load();
	}

	public void update(double dt) {
		delta += dt;
		if (delta > 1) {
			synchronized (lock) {
				for (Timer timer : timers) {
					if ((timer.isWorking()) && (timer.update())) {
						timer.stop();
					}
				}
			}
			delta = 0;
		}
	}

	public void add(Timer timer) {
		synchronized (lock) {
			timers.add(timer);
			save();
		}
	}

	public void remove(Timer timer) {
		synchronized (lock) {
			timers.remove(timer);
		}
	}

	private void load() {
		config = Config.getFile("timers.cfg");
		try {
			Gson gson = new GsonBuilder().create();
			InputStream is = new FileInputStream(config);
			timers = gson.fromJson(Utils.stream2str(is), new TypeToken<List<Timer>>() {
			}.getType());
		} catch (Exception ignored) {
		}
		if (timers == null) {
			timers = new LinkedList<Timer>();
		}
	}

	public void save() {
		Gson gson = new GsonBuilder().create();
		String data = gson.toJson(timers);
		boolean exists = config.exists();
		if (!exists) {
			try {
				//noinspection ResultOfMethodCallIgnored
				new File(config.getParent()).mkdirs();
				exists = config.createNewFile();
			} catch (IOException ignored) {
			}
		}
		if (exists && config.canWrite()) {
			PrintWriter out = null;
			try {
				out = new PrintWriter(config);
				out.print(data);
			} catch (FileNotFoundException ignored) {
			} finally {
				if (out != null) {
					out.close();
				}
			}
		}
	}
}
