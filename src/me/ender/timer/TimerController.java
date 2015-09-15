package me.ender.timer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import haven.Config;
import haven.Globals;
import haven.Utils;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TimerController {

	private static String CONFIG_JSON;
	private static final Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
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
		String configJson = Globals.SettingFileString(Globals.USERNAME + "/timers.json", true);
		Map<String, Object> tmp = new HashMap<String, Object>();
		try {
			Type type = new TypeToken<List<Timer>>() {
			}.getType();
			// first check if we have username config
			String json = Config.loadFile(configJson);
			if (json != null) {
				tmp = gson.fromJson(json, type);
			} else {
				// now check for default config
				configJson = Globals.SettingFileString("/timers.json", true);
				json = Config.loadFile(configJson);
				if (json != null) {
					tmp = gson.fromJson(json, type);
				}
			}
		} catch (Exception e) {
		}
		CONFIG_JSON = configJson;
		if (timers == null) {
			timers = new LinkedList<Timer>();
		}
	}

	public void save() {
		Config.saveFile(CONFIG_JSON, gson.toJson(timers));
	}
}
