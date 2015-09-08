package haven;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public enum CFG {
	
	CONFIG_VERSION("config.version", 0),

	CAMERA_BRIGHT("camera.bright", 0f),
	DISPLAY_KINNAMES("display.kinnames", true),
	DISPLAY_FLAVOR("display.flavor", true),
	FREE_CAMERA_ROTATION("general.freecamera", false),
	STORE_MAP("general.storemap", false),
	STUDY_LOCK("ui.studylock", false),
	SHOW_CHAT_TIMESTAMP("ui.chat.timestamp", true),
	UI_MINIMAP_PLAYERS("ui.minimap.players", true),
	UI_MINIMAP_BOULDERS("ui.minimap.boulders", true),
	UI_ITEM_METER_COUNTDOWN("ui.item.meter.countdown", false),
	UI_ITEM_METER_RED("ui.item.meter.red", 1f),
	UI_ITEM_METER_GREEN("ui.item.meter.green", 1f),
	UI_ITEM_METER_BLUE("ui.item.meter.blue", 1f),
	UI_ITEM_METER_ALPHA("ui.item.meter.alpha", 0.25f),
	Q_SHOW_MODS("ui.q.showmods", 1),
	Q_MAX_SINGLE("ui.q.maxsingle", false),
	Q_SHOW_MODS_ONKEY("ui.q.allmods", 7);

	private static final String CONFIG_JSON = MainFrame.SETTINGS_FOLDER + "config.json";
	private static final int configVersion = 2;
	private static final Map<String, Object> cfg;
	private static final Map<String, Object> cache = new HashMap<String, Object>();
	private static final Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
	private final String path;
	public final Object def;

	static {
		Map<String, Object> tmp = new HashMap<String, Object>();
		try {
			Type type = new TypeToken<Map<Object, Object>>() {
			}.getType();
			String json = Config.loadFile(CONFIG_JSON);
			if (json != null) {
				tmp = gson.fromJson(json, type);
			}
		} catch (Exception e) {
		}
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
