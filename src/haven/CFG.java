package haven;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public enum CFG {

	VERSION("version", ""),
	CAMERA_BRIGHTNESS("camera.brightness", 0f),
	CAMERA_TYPE("camera.type", 0), // default
	CONFIG_VERSION("config.version", 0),
	DISPLAY_FLAVOR("display.flavor", true),
	DISPLAY_KINNAMES("display.kinnames", true),
	GENERAL_STOREMAP("general.storemap", true),
	HOTKEY_ITEM_QUALITY("hotkey.item.quality", 1), // SHIFT
	HOTKEY_ITEM_TRANSFER_IN("hotkey.item.transfer.in", 4), // ALT
	HOTKEY_ITEM_TRANSFER_OUT("hotkey.item.transfer.out", 2), // CTRL
	UI_ACTION_PROGRESS_PERCENTAGE("ui.action.progress.percentage", true),
	UI_CHAT_LOGS("ui.chat.logs", true),
	UI_CHAT_TIMESTAMP("ui.chat.timestamp", true),
	UI_ITEM_METER_COUNTDOWN("ui.item.meter.countdown", false),
	UI_ITEM_METER_PROGRESSBAR("ui.item.meter.progressbar", false),
	UI_ITEM_METER_RED("ui.item.meter.red", 1f),
	UI_ITEM_METER_GREEN("ui.item.meter.green", 1f),
	UI_ITEM_METER_BLUE("ui.item.meter.blue", 1f),
	UI_ITEM_METER_ALPHA("ui.item.meter.alpha", 0.25f),
	UI_ITEM_QUALITY_SHOW("ui.item.quality.show", 1), // Show single
	UI_ITEM_QUALITY_SINGLEASMAX("ui.item.quality.singleasmax", false),
	UI_MINIMAP_BOULDERS("ui.minimap.boulders", true),
	UI_MINIMAP_FLOATING("ui.minimap.floating", false),
	UI_MINIMAP_PLAYERS("ui.minimap.players", true),
	UI_STUDYLOCK("ui.studylock", false);

	private static String CONFIG_JSON;
	private static final int configVersion = 4;
	private static Map<String, Object> cfg = new HashMap<String, Object>();
	private static final Map<String, Object> cache = new HashMap<String, Object>();
	private static final Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
	private final String path;
	public final Object def;
	private Observer observer;

	public static interface Observer {

		void updated(CFG cfg);
	}

	public static void loadConfig() {
		String configJson = Globals.SettingFileString(Globals.USERNAME + "/config.json", true);
		Map<String, Object> tmp = new HashMap<String, Object>();
		try {
			Type type = new TypeToken<Map<Object, Object>>() {
			}.getType();
			// first check if we have username config
			String json = Config.loadFile(configJson);
			if (json != null) {
				tmp = gson.fromJson(json, type);
			} else {
				// now check for default config
				configJson = Globals.SettingFileString("/config.json", true);
				json = Config.loadFile(configJson);
				if (json != null) {
					tmp = gson.fromJson(json, type);
				}
			}
		} catch (Exception e) {
		}
		CONFIG_JSON = configJson;
		// check config version
		int version = ((Number) CFG.get(CFG.CONFIG_VERSION, tmp)).intValue();
		if (version != configVersion) {
			System.out.println("Config version mismatch... reseting config");
			tmp = new HashMap<String, Object>();
		}
		cfg = tmp;
		CFG.CONFIG_VERSION.set(configVersion);
	}

	CFG(String path, Object def) {
		this.path = path;
		this.def = def;
	}

	public Object val() {
		return CFG.get(this);
	}

	public boolean valb() {
		return CFG.getb(this);
	}

	public int vali() {
		return CFG.geti(this);
	}

	public float valf() {
		return CFG.getf(this);
	}

	public void set(Object value) {
		CFG.set(this, value);
		if (observer != null) {
			observer.updated(this);
		}
	}

	public void set(Object value, boolean observe) {
		set(value);
		if (observe && observer != null) {
			observer.updated(this);
		}
	}

	public void setObserver(Observer observer) {
		this.observer = observer;
	}

	public static synchronized Object get(CFG name) {
		return get(name, cfg);
	}

	private static synchronized Object get(CFG name, Object configMap) {
		if (cache.containsKey(name.path)) {
			return cache.get(name.path);
		} else {
			Object value = retrieve(name, configMap);
			cache.put(name.path, value);
			return value;
		}
	}

	public static boolean getb(CFG name) {
		return (Boolean) get(name);
	}

	public static int geti(CFG name) {
		return ((Number) get(name)).intValue();
	}

	public static float getf(CFG name) {
		return ((Number) get(name)).floatValue();
	}

	public static synchronized void set(CFG name, Object value) {
		set(name, value, cfg);
	}

	@SuppressWarnings("unchecked")
	private static synchronized void set(CFG name, Object value, Object configMap) {
		cache.put(name.path, value);
		String[] parts = name.path.split("\\.");
		int i;
		Object cur = configMap;
		for (i = 0; i < parts.length - 1; i++) {
			String part = parts[i];
			if (cur instanceof Map) {
				Map<Object, Object> map = (Map<Object, Object>) cur;
				if (map.containsKey(part)) {
					cur = map.get(part);
				} else {
					cur = new HashMap<String, Object>();
					map.put(part, cur);
				}
			}
		}
		if (cur instanceof Map) {
			Map<Object, Object> map = (Map) cur;
			map.put(parts[parts.length - 1], value);
		}
		store();
	}

	private static synchronized void store() {
		try {
			Config.saveFile(CONFIG_JSON, gson.toJson(cfg));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private static Object retrieve(CFG name) {
		return retrieve(name, cfg);
	}

	private static Object retrieve(CFG name, Object configMap) {
		String[] parts = name.path.split("\\.");
		Object cur = configMap;
		for (String part : parts) {
			if (cur instanceof Map) {
				Map map = (Map) cur;
				if (map.containsKey(part)) {
					cur = map.get(part);
				} else {
					return name.def;
				}
			} else {
				return name.def;
			}
		}
		return cur;
	}
}
