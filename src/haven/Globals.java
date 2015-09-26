package haven;

import haven.Window.WindowCFG;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.List;
import java.util.Set;

public class Globals {

	public static String USERNAME = "";
	public static String SESSION_TIMESTAMP = "";

	public static void Setup() {
		Setup("");
	}

	public static void Setup(String username) {
		USERNAME = username;
		SESSION_TIMESTAMP = Utils.timestamp(true).replace(" ", "_").replace(":", "."); //ex. 2015-09-08_14.22.15
		try {
			System.setOut(new PrintStream(new FileOutputStream(LogFile("output.log"), true)));
		} catch (FileNotFoundException ex) {
		}
		try {
			System.setErr(new PrintStream(new FileOutputStream(LogFile("error.log"), true)));
		} catch (FileNotFoundException ex) {
		}
		CFG.loadConfig();
		WindowCFG.loadConfig();
		Config.loadConfig();
	}

	public static class Data {

		private static final Gson gson = (new GsonBuilder()).setPrettyPrinting().create();

		private static String CONFIG_JSON;
		private static Map<String, HashSet<String>> data;
		private static final Map<String, Set<DataObserver>> observers = new HashMap<>();

		static {
			loadConfig();
		}

		public static interface DataObserver {

			void dataUpdated(String name);
		}

		private static void loadConfig() {
			String configJson = Globals.DataFileString("data.json", true);
			Map<String, List<String>> tmp = new HashMap<>();
			try {
				Type type = new TypeToken<Map<String, List<String>>>() {
				}.getType();
				String json = Config.loadFile(configJson);
				if (json != null) {
					tmp = gson.fromJson(json, type);
				}
			} catch (Exception e) {
			}
			CONFIG_JSON = configJson;
			if (tmp == null) {
				tmp = new HashMap<>();
			}

			data = new HashMap<>();
			for (Entry<String, List<String>> entry : tmp.entrySet()) {
				data.put(entry.getKey(), new HashSet<>(entry.getValue()));
			}

			// now add the base data
			tmp = new HashMap<>();
			try {
				InputStream inputStream = Data.class.getResourceAsStream("/data.json");
				StringWriter writer = new StringWriter();
				IOUtils.copy(inputStream, writer, StandardCharsets.UTF_8);

				Type type = new TypeToken<Map<String, List<String>>>() {
				}.getType();
				String json = writer.toString();
				if (json != null) {
					tmp = gson.fromJson(json, type);
				}
			} catch (IOException | JsonSyntaxException e) {
			}
			try {
			} catch (Exception e) {
			}
			if (tmp == null) {
				tmp = new HashMap<>();
			}
			for (Entry<String, List<String>> entry : tmp.entrySet()) {
				HashSet<String> set = data.get(entry.getKey());
				if (set == null) {
					set = new HashSet<>();
				}
				set.addAll(entry.getValue());
				data.put(entry.getKey(), set);
			}
		}

		private static synchronized void saveConfig() {
			Map<String, List<String>> data2 = new HashMap<>();
			for (Entry<String, HashSet<String>> entry : data.entrySet()) {
				List<String> dList = new ArrayList<>(entry.getValue());
				Collections.sort(dList);
				data2.put(entry.getKey(), dList);
			}
			Config.saveFile(CONFIG_JSON, gson.toJson(data2));
		}

		public static synchronized void addObserver(String name, DataObserver observer) {
			if (observer == null) {
				return;
			}
			if (observers.get(name) == null) {
				observers.put(name, new HashSet<DataObserver>());
			}
			Set<DataObserver> obs = observers.get(name);
			obs.add(observer);
			observers.put(name, obs);
		}

		public static synchronized void remObserver(String name, DataObserver observer) {
			if (observer == null || observers.get(name) == null) {
				return;
			}
			Set<DataObserver> obs = observers.get(name);
			obs.remove(observer);
			observers.put(name, obs);
		}

		public static synchronized List<String> get(String name) {
			return get(name, "");
		}

		public static synchronized List<String> get(String name, String startsWith) {
			return get(name, startsWith, false);
		}

		public static synchronized List<String> get(String name, String startsWith, boolean trimStartsWith) {
			return get(name, startsWith, trimStartsWith, false);
		}

		@SuppressWarnings("unchecked")
		public static synchronized List<String> get(String name, String startsWith, boolean trimStartsWith, boolean trimTrailingDigits) {
			if (data.get(name) == null) {
				return new ArrayList<>();
			}
			if (startsWith.isEmpty()) {
				return new ArrayList(data.get(name));
			}
			ArrayList<String> dataList = new ArrayList();
			for (String value : data.get(name)) {
				if (value.startsWith(startsWith)) {
					String s = trimStartsWith ? value.substring(startsWith.length()) : value;
					dataList.add(trimTrailingDigits ? s.replaceAll("\\d*$", "") : s);
				}
			}
			Collections.sort(dataList);
			return dataList;
		}

		public static synchronized void set(String name, String value) {
			try {
				value = value.toLowerCase();
				if (data.get(name) == null) {
					data.put(name, new HashSet<String>());
				}
				if (data.get(name).contains(value)) {
					return;
				}
				data.get(name).add(value);
				for (DataObserver observer : observers.get(name)) {
					observer.dataUpdated(name);
				}
				saveConfig();
			} catch (Exception ex) {
			}
		}
	}

	private final static String chatFolder = "chat";
	private final static String dataFolder = "data";
	private final static String logFolder = "log";
	private final static String mapFolder = "map";
	private final static String settingFolder = "setting";

	// Custom funtions
	private static File CustomFolder(String baseName) {
		return CustomFolder(baseName, false);
	}

	private static File CustomFolder(String baseName, boolean useDefault) {
		return CustomFolder(baseName, useDefault, SESSION_TIMESTAMP);
	}

	private static File CustomFolder(String baseName, boolean useDefault, String sessionTimestamp) {
		File file = new File(CustomFolderString(baseName, useDefault, sessionTimestamp));
		file.mkdirs();
		return file;
	}

	private static String CustomFolderString(String baseName) {
		return CustomFolderString(baseName, false);
	}

	private static String CustomFolderString(String baseName, boolean useDefault) {
		return CustomFolderString(baseName, useDefault, SESSION_TIMESTAMP);
	}

	private static String CustomFolderString(String baseName, boolean useDefault, String sessionTimestamp) {
		String folderStr;
		if (!useDefault && !"".equals(USERNAME)) {
			folderStr = String.format("./%s/%s/%s/", baseName, USERNAME, sessionTimestamp);
		} else {
			folderStr = String.format("./%s/", baseName);
		}
		return folderStr;
	}

	private static File CustomFile(String folderName, String fileName) {
		return CustomFile(folderName, fileName, false);
	}

	private static File CustomFile(String folderName, String fileName, boolean useDefault) {
		return CustomFile(folderName, fileName, useDefault, SESSION_TIMESTAMP);
	}

	private static File CustomFile(String folderName, String fileName, boolean useDefault, String sessionTimestamp) {
		File file = new File(CustomFileString(folderName, fileName, useDefault, sessionTimestamp));
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException ex) {
			}
		}
		return file;
	}

	private static String CustomFileString(String folderName, String fileName) {
		return CustomFileString(folderName, fileName, false);
	}

	private static String CustomFileString(String folderName, String fileName, boolean useDefault) {
		return CustomFileString(folderName, fileName, useDefault, SESSION_TIMESTAMP);
	}

	private static String CustomFileString(String folderName, String fileName, boolean useDefault, String sessionTimestamp) {
		String folderStr = CustomFolderString(folderName, useDefault, sessionTimestamp);
		String fileStr = Paths.get(folderStr, fileName).toString();
		return fileStr;
	}

	// Chat functions
	public static File ChatFolder() {
		return ChatFolder(false);
	}

	public static File ChatFolder(boolean useDefault) {
		return CustomFolder(chatFolder, useDefault);
	}

	public static String ChatFolderString() {
		return ChatFolderString(false);
	}

	public static String ChatFolderString(boolean useDefault) {
		return CustomFolderString(chatFolder, useDefault);
	}

	public static File ChatFile(String fileName) {
		return ChatFile(fileName, false);
	}

	public static File ChatFile(String fileName, boolean useDefault) {
		return CustomFile(chatFolder, fileName, useDefault);
	}

	public static String ChatFileString(String fileName) {
		return ChatFileString(fileName, false);
	}

	public static String ChatFileString(String fileName, boolean useDefault) {
		return CustomFileString(chatFolder, fileName, useDefault);
	}

	// Data functions
	public static File DataFolder() {
		return DataFolder(false);
	}

	public static File DataFolder(boolean useDefault) {
		return CustomFolder(dataFolder, useDefault);
	}

	public static String DataFolderString() {
		return DataFolderString(false);
	}

	public static String DataFolderString(boolean useDefault) {
		return CustomFolderString(dataFolder, useDefault);
	}

	public static File DataFile(String fileName) {
		return DataFile(fileName, false);
	}

	public static File DataFile(String fileName, boolean useDefault) {
		return CustomFile(dataFolder, fileName, useDefault);
	}

	public static String DataFileString(String fileName) {
		return DataFileString(fileName, false);
	}

	public static String DataFileString(String fileName, boolean useDefault) {
		return CustomFileString(dataFolder, fileName, useDefault);
	}

	// Log functions
	public static File LogFolder() {
		return LogFolder(false);
	}

	public static File LogFolder(boolean useDefault) {
		return CustomFolder(logFolder, useDefault);
	}

	public static String LogFolderString() {
		return LogFolderString(false);
	}

	public static String LogFolderString(boolean useDefault) {
		return CustomFolderString(logFolder, useDefault);
	}

	public static File LogFile(String fileName) {
		return LogFile(fileName, false);
	}

	public static File LogFile(String fileName, boolean useDefault) {
		return CustomFile(logFolder, fileName, useDefault);
	}

	public static String LogFileString(String fileName) {
		return LogFileString(fileName, false);
	}

	public static String LogFileString(String fileName, boolean useDefault) {
		return CustomFileString(logFolder, fileName, useDefault);
	}

	// Map functions
	public static File MapFolder() {
		return MapFolder(false);
	}

	public static File MapFolder(boolean useDefault) {
		return CustomFolder(mapFolder, useDefault);
	}

	public static String MapFolderString() {
		return MapFolderString(false);
	}

	public static String MapFolderString(boolean useDefault) {
		return CustomFolderString(mapFolder, useDefault);
	}

	public static File MapFile(String fileName) {
		return MapFile(fileName, false);
	}

	public static File MapFile(String fileName, boolean useDefault) {
		return CustomFile(mapFolder, fileName, useDefault);
	}

	public static String MapFileString(String fileName) {
		return MapFileString(fileName, false);
	}

	public static String MapFileString(String fileName, boolean useDefault) {
		return CustomFileString(mapFolder, fileName, useDefault);
	}

	// Setting functions
	public static File SettingFolder() {
		return SettingFolder(false);
	}

	public static File SettingFolder(boolean useDefault) {
		return CustomFolder(settingFolder, useDefault);
	}

	public static String SettingFolderString() {
		return SettingFolderString(false);
	}

	public static String SettingFolderString(boolean useDefault) {
		return CustomFolderString(settingFolder, useDefault);
	}

	public static File SettingFile(String fileName) {
		return SettingFile(fileName, false);
	}

	public static File SettingFile(String fileName, boolean useDefault) {
		return CustomFile(settingFolder, fileName, useDefault);
	}

	public static String SettingFileString(String fileName) {
		return SettingFileString(fileName, false);
	}

	public static String SettingFileString(String fileName, boolean useDefault) {
		return CustomFileString(settingFolder, fileName, useDefault);
	}
}
