package org.ender.updater;

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
import javax.swing.UnsupportedLookAndFeelException;

public class Main extends JFrame
				implements IUpdaterListener {

	private static final int PROGRESS_MAX = 1024;
	private static final long serialVersionUID = 1L;
	private static Updater updater;
	private FileOutputStream log;
	private final JTextArea logbox;
	private final JProgressBar progress;

	public static boolean TESTING = false;

	public static void main(String[] args) {
		if (args.length > 0) {
			TESTING = true;
		}

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException localException) {
		}
		Main gui = new Main();
		gui.setVisible(true);
		gui.setSize(500, 500);
		gui.log(String.format("OS: '%s', arch: '%s'", new Object[]{System.getProperty("os.name"), System.getProperty("os.arch")}));

		updater = new Updater(gui);
		updater.update();
	}

	public Main() {
		super("Haven & Hearth - Minion Client Updater");
		try {
			if (!UpdaterConfig.dir.exists()) {
				UpdaterConfig.dir.mkdirs();
			}
			this.log = new FileOutputStream(new File(UpdaterConfig.dir, "updater.log"));
		} catch (FileNotFoundException e) {
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

	@Override
	public void log(String message) {
		message = message.concat("\n");
		this.logbox.append(message);
		try {
			if (this.log != null) {
				this.log.write(message.getBytes());
			}
		} catch (IOException e) {
		}

	}

	@Override
	public void finished() {
		log("Starting client...");
		String libs = String.format("-Djava.library.path=\"%%PATH%%\"%s.", new Object[]{File.pathSeparator});
		UpdaterConfig cfg = updater.cfg;
		ProcessBuilder pb = new ProcessBuilder(new String[]{"java", "-Xmx" + cfg.mem, libs, "-jar", cfg.jar, "-U", cfg.res, cfg.server});
		pb.directory(UpdaterConfig.dir.getAbsoluteFile());
		try {
			pb.start();
		} catch (IOException e) {
		}
		try {
			if (this.log != null) {
				this.log.flush();
				this.log.close();
			}
		} catch (IOException e) {
		}
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
		}
		System.exit(0);
	}

	@Override
	public void progress(long position, long size) {
		this.progress.setValue((int) (1024.0F * ((float) position / (float) size)));
	}
}
