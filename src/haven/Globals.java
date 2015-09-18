package haven;

import haven.Window.WindowCFG;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

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

		public final static String[] actions = new String[]{
			"chop",
			"eat",
			"harvest",
			"pick",};
		public final static String[] boulders = new String[]{
			"basalt",
			"cassiterite",
			"chalcopyrite",
			"cinnabar",
			"dolomite",
			"feldspar",
			"flint",
			"gneiss",
			"granite",
			"hematite",
			"ilmenite",
			"limestone",
			"limonite",
			"magnetite",
			"malachite",
			"marble",
			"porphyry",
			"quartz",
			"ras",
			"sandstone",
			"schist",};
		public final static String[] bushes = new String[]{
			"arrowwood",
			"blackberrybush",
			"blackcurrant",
			"blackthorn",
			"bogmyrtle",
			"boxwood",
			"bsnightshade",
			"caprifole",
			"crampbark",
			"dogrose",
			"elderberrybush",
			"gooseberrybush",
			"hawthorn",
			"holly",
			"raspberrybush",
			"redcurrant",
			"sandthorn",
			"spindlebush",
			"teabush",
			"tibast",
			"tundrarose",
			"woodbine",};
		public final static String[] trees = new String[]{
			"alder",
			"juniper",
			"appletree",
			"ash",
			"aspen",
			"baywillow",
			"beech",
			"birch",
			"birdcherrytree",
			"buckthorn",
			"cedar",
			"cherry",
			"chestnuttree",
			"conkertree",
			"corkoak",
			"crabappletree",
			"cypress",
			"elm",
			"fir",
			"goldenchain",
			"hazel",
			"hornbeam",
			"kingsoak",
			"larch",
			"laurel",
			"linden",
			"maple",
			"mirkwood",
			"mulberry",
			"oak",
			"olivetree",
			"peartree",
			"pine",
			"planetree",
			"plumtree",
			"poplar",
			"rowan",
			"sallow",
			"spruce",
			"sweetgum",
			"walnuttree",
			"whitebeam",
			"willow",
			"yew",};
		public final static String[] icons = new String[]{
			"blueberry",
			"chantrelle",
			"chick",
			"chicken",
			"dandelion",
			"dragonfly",
			"rat",
			"spindlytaproot",
			"stingingnettle",};

		static {
			Arrays.sort(boulders);
			Arrays.sort(bushes);
			Arrays.sort(trees);
		}
	}

	private final static String chatFolder = "chat";
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
