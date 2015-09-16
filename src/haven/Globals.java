package haven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Globals {

	public static String USERNAME = "";
	public static String SESSION_TIMESTAMP = "";

	private static File CustomFolder(String baseName) {
		return CustomFolder(baseName, false);
	}

	private static File CustomFolder(String baseName, boolean useDefault) {
		File file;
		if (!useDefault && !"".equals(USERNAME)) {
			file = new File(String.format("./%s/%s/%s/", baseName, USERNAME, SESSION_TIMESTAMP));
		} else {
			file = new File(String.format("./%s/", baseName));
		}
		file.mkdirs();
		return file;
	}

	private static File CustomFile(String folderName, String fileName) {
		return CustomFile(folderName, fileName, false);
	}

	private static File CustomFile(String folderName, String fileName, boolean useDefault) {
		File folder = CustomFolder(folderName, useDefault);
		File file = new File(folder, fileName);
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

	// Chat
	public static File ChatFolder() {
		return CustomFolder("chat");
	}

	public static File ChatFolder(boolean useDefault) {
		return CustomFolder("chat", useDefault);
	}

	public static String ChatFolderString() {
		return ChatFolder().getPath();
	}

	public static String ChatFolderString(boolean useDefault) {
		return ChatFolder(useDefault).getPath();
	}

	public static File ChatFile(String fileName) {
		return CustomFile("chat", fileName);
	}

	public static File ChatFile(String fileName, boolean useDefault) {
		return CustomFile("chat", fileName, useDefault);
	}

	public static String ChatFileString(String fileName) {
		return ChatFile(fileName).getPath();
	}

	public static String ChatFileString(String fileName, boolean useDefault) {
		return ChatFile(fileName, useDefault).getPath();
	}

	// Log
	public static File LogFolder() {
		return CustomFolder("log");
	}

	public static File LogFolder(boolean useDefault) {
		return CustomFolder("log", useDefault);
	}

	public static String LogFolderString() {
		return LogFolder().getPath();
	}

	public static String LogFolderString(boolean useDefault) {
		return LogFolder(useDefault).getPath();
	}

	public static File LogFile(String fileName) {
		return CustomFile("log", fileName);
	}

	public static File LogFile(String fileName, boolean useDefault) {
		return CustomFile("log", fileName, useDefault);
	}

	public static String LogFileString(String fileName) {
		return LogFile(fileName).getPath();
	}

	public static String LogFileString(String fileName, boolean useDefault) {
		return LogFile(fileName, useDefault).getPath();
	}

	// Map
	public static File MapFolder() {
		return CustomFolder("map");
	}

	public static File MapFolder(boolean useDefault) {
		return CustomFolder("map", useDefault);
	}

	public static String MapFolderString() {
		return MapFolder().getPath();
	}

	public static String MapFolderString(boolean useDefault) {
		return MapFolder(useDefault).getPath();
	}

	public static File MapFile(String fileName) {
		return CustomFile("map", fileName);
	}

	public static File MapFile(String fileName, boolean useDefault) {
		return CustomFile("map", fileName, useDefault);
	}

	public static String MapFileString(String fileName) {
		return MapFile(fileName).getPath();
	}

	public static String MapFileString(String fileName, boolean useDefault) {
		return MapFile(fileName, useDefault).getPath();
	}

	// Setting
	public static File SettingFolder() {
		return CustomFolder("setting");
	}

	public static File SettingFolder(boolean useDefault) {
		return CustomFolder("setting", useDefault);
	}

	public static String SettingFolderString() {
		return SettingFolder().getPath();
	}

	public static String SettingFolderString(boolean useDefault) {
		return SettingFolder(useDefault).getPath();
	}

	public static File SettingFile(String fileName) {
		return CustomFile("setting", fileName);
	}

	public static File SettingFile(String fileName, boolean useDefault) {
		return CustomFile("setting", fileName, useDefault);
	}

	public static String SettingFileString(String fileName) {
		return SettingFile(fileName).getPath();
	}

	public static String SettingFileString(String fileName, boolean useDefault) {
		return SettingFile(fileName, useDefault).getPath();
	}

	public static void Setup() {
		SESSION_TIMESTAMP = Utils.timestamp(true).replace(" ", "_").replace(":", "."); //ex. 2015-09-08_14.22.15
		try {
			System.setOut(new PrintStream(new FileOutputStream(LogFile("output.log"), true)));
		} catch (FileNotFoundException ex) {
		}
		try {
			System.setErr(new PrintStream(new FileOutputStream(LogFile("error.log"), true)));
		} catch (FileNotFoundException ex) {
		}
	}
}