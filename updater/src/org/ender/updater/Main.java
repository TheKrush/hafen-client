package org.ender.updater;

import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.UIManager;

public class Main extends JFrame
				implements IUpdaterListener {

	private static final int PROGRESS_MAX = 1024;
	private static final long serialVersionUID = 1L;
	private static Updater updater;
	private FileOutputStream log;
	private JTextArea logbox;
	private JProgressBar progress;

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception localException) {
		}
		Main gui = new Main();
		gui.setVisible(true);
		gui.setSize(350, 450);
		gui.log(String.format("OS: '%s', arch: '%s'", new Object[]{System.getProperty("os.name"), System.getProperty("os.arch")}));
		gui.log("Checking for updates...");

		updater = new Updater(gui);
		updater.update();
	}

	public Main() {
		super("HnH updater");
		try {
			if (!UpdaterConfig.dir.exists()) {
				UpdaterConfig.dir.mkdirs();
			}
			this.log = new FileOutputStream(new File(UpdaterConfig.dir, "updater.log"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		setDefaultCloseOperation(3);
		JPanel p;
		add(p = new JPanel());
		p.setLayout(new BoxLayout(p, 3));

		p.add(this.logbox = new JTextArea());
		this.logbox.setEditable(false);
		this.logbox.setFont(this.logbox.getFont().deriveFont(10.0F));

		p.add(this.progress = new JProgressBar());
		this.progress.setMinimum(0);
		this.progress.setMaximum(1024);
		pack();
	}

	public void log(String message) {
		message = message.concat("\n");
		this.logbox.append(message);
		try {
			if (this.log != null) {
				this.log.write(message.getBytes());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void finished() {
		log("Starting client...");
		String libs = String.format("-Djava.library.path=\"%%PATH%%\"%s.", new Object[]{File.pathSeparator});
		UpdaterConfig cfg = updater.cfg;
		ProcessBuilder pb = new ProcessBuilder(new String[]{"java", "-Xmx" + cfg.mem, libs, "-jar", cfg.jar, "-U", cfg.res, cfg.server});
		pb.directory(UpdaterConfig.dir.getAbsoluteFile());
		try {
			pb.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			if (this.log != null) {
				this.log.flush();
				this.log.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public void progress(long position, long size) {
		this.progress.setValue((int) (1024.0F * ((float) position / (float) size)));
	}
}
