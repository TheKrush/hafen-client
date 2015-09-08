package org.ender.updater;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import javax.swing.JFrame;
import org.krush.gui.MainGui;

public class Main extends JFrame {

	private static final long serialVersionUID = 1L;
	public static Updater updater;

	private static final MainGui gui;
	public static boolean TESTING = false;
	public static String JarName = "";

	public static String VERSION;
	public static final String TITLE = "Haven & Hearth - Minion Client Updater by Ender & Krush";
	public static final String LOG_FOLDER = "./logs/" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "/";

	public static void main(String[] args) {
		if (args.length > 0) {
			TESTING = true;
		}

		try {
			JarName = new java.io.File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
		} catch (Exception ex) {
		}

		gui.show();

		gui.log(TITLE);
		gui.log(String.format("Jar: '%s' | Version: %s", new Object[]{JarName, VERSION}));
		gui.log(String.format("OS: '%s' | Arch: '%s'", new Object[]{System.getProperty("os.name"), System.getProperty("os.arch")}));
		gui.log();

		updater = new Updater(gui);
		updater.update();
	}

	static {
		loadBuildVersion();
		gui = new MainGui();
	}

	private static void loadBuildVersion() {
		InputStream in = Main.class.getResourceAsStream("/buildinfo");
		try {
			try {
				if (in != null) {
					Properties info = new Properties();
					info.load(in);

					VERSION = info.getProperty("version");
				}
			} finally {
				if (in != null) {
					in.close();
				}
			}
		} catch (IOException e) {
			throw (new Error(e));
		}
	}
}
