package haven;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum CFG {

	VERSION("version", ""),
	CAMERA_TYPE("camera.type", 0), // default
	CONFIG_VERSION("config.version", 0),
	DISPLAY_BRIGHTNESS("display.brightness", 0f),
	DISPLAY_CROPS_SIMPLE("display.crops.simple", false),
	DISPLAY_FLAVOR("display.flavor", true),
	DISPLAY_GRID("display.grid", false),
	DISPLAY_KIN_NAMES("display.kin.names", true),
	DISPLAY_OBJECT_DAMAGE("display.object.health", false),
	DISPLAY_PLANT_GROWTH("display.plant.growth", false),
	DISPLAY_WEATHER("display.weather", true),
	GENERAL_CHAT_SAVE("general.chat.save", true),
	GENERAL_DATA_SAVE("general.data.save", false),
	GENERAL_MAP_SAVE("general.map.save", true),
	HOTKEY_ITEM_QUALITY("hotkey.item.quality", 1), // SHIFT
	HOTKEY_ITEM_TRANSFER_IN("hotkey.item.transfer.in", 4), // ALT
	HOTKEY_ITEM_TRANSFER_OUT("hotkey.item.transfer.out", 2), // CTRL
	HOTKEY_MOUSE_FOLLOW("hotkey.mouse.follow", true),
	MINIMAP_FLOATING("ui.minimap.floating", false),
	MINIMAP_BUMLINGS("ui.minimap.bumlings", new HashMap<String, Boolean>()),
	MINIMAP_BUSHES("ui.minimap.bushes", new HashMap<String, Boolean>()),
	MINIMAP_TREES("ui.minimap.trees", new HashMap<String, Boolean>()),
	MINIMAP_PLAYERS("ui.minimap.players", true),
	UI_ACTION_PROGRESS_PERCENTAGE("ui.action.progress.percentage", true),
	UI_CHAT_TIMESTAMP("ui.chat.timestamp", true),
	UI_ITEM_METER_COUNTDOWN("ui.item.meter.countdown", false),
	UI_ITEM_METER_PROGRESSBAR("ui.item.meter.progressbar", false),
	UI_ITEM_METER_RED("ui.item.meter.red", 1f),
	UI_ITEM_METER_GREEN("ui.item.meter.green", 1f),
	UI_ITEM_METER_BLUE("ui.item.meter.blue", 1f),
	UI_ITEM_METER_ALPHA("ui.item.meter.alpha", 0.25f),
	UI_ITEM_QUALITY_SHOW("ui.item.quality.show", 1), // show single average
	UI_KIN_STATUS("ui.kin.status", true),
	UI_MENU_FLOWER_CLICK_AUTO("ui.menu.flower.click.auto", new HashMap<String, Boolean>()),
	UI_MENU_FLOWER_CLICK_SINGLE("ui.menu.flower.click.single", true),
	UI_STUDYLOCK("ui.studylock", false),
	UI_TOOLTIP_LONG("ui.item.tooltip.long", false), // force long tooltip
	;

	private static String CONFIG_JSON;
	private static final int configVersion = 6;
	private static Map<String, Object> cfg = new HashMap<>();
	private static final Map<String, Object> cache = new HashMap<>();
	private static final Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
	private final String path;
	public final Object def;
	private List<CFGObserver> observers;

	static {
		loadConfig();
	}

	public static interface CFGObserver {

		void cfgUpdated(CFG cfg);
	}

	public static void loadConfig() {
		String configJson = Globals.SettingFileString(Globals.USERNAME + "/config.json", true);
		Map<String, Object> tmp = new HashMap<>();
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
		cache.clear();
		System.out.println("Using setting file: " + CONFIG_JSON);
		if (tmp == null) {
			tmp = new HashMap<>();
		}
		// check config version
		int version = ((Number) CFG.get(CFG.CONFIG_VERSION, tmp)).intValue();
		if (version != configVersion) {
			System.out.println("Config version mismatch... reseting config");
			tmp = new HashMap<>();
		}
		cfg = tmp;
		CFG.CONFIG_VERSION.set(configVersion);
	}

	private static synchronized void saveConfig() {
		try {
			Config.saveFile(CONFIG_JSON, gson.toJson(cfg));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	CFG(String path, Object def) {
		this.path = path.toLowerCase();
		this.def = def;
		observers = new ArrayList<>();
	}

	public Object val() {
		return CFG.get(this);
	}

	@SuppressWarnings("unchecked")
	public <T extends Object> T valo() {
		return (T) CFG.get(this);
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
		set(value, false);
	}

	public void set(Object value, boolean observe) {
		CFG.set(this, value);
		if (observe) {
			for (CFGObserver observer : observers) {
				observer.cfgUpdated(this);
			}
		}
	}

	public synchronized void addObserver(CFGObserver observer) {
		if (observer == null) {
			return;
		}
		observers.add(observer);
	}

	public synchronized void remObserver(CFGObserver observer) {
		if (observer == null) {
			return;
		}
		observers.remove(observer);
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
					cur = new HashMap<>();
					map.put(part, cur);
				}
			}
		}
		if (cur instanceof Map) {
			Map<Object, Object> map = (Map) cur;
			map.put(parts[parts.length - 1], value);
		}
		saveConfig();
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
