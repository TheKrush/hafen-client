/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.krush.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.plaf.ProgressBarUI;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.text.DefaultCaret;
import org.ender.updater.IUpdaterListener;
import org.ender.updater.Main;
import org.ender.updater.UpdaterConfig;
import org.krush.helper.Helper;

/**
 *
 * @author Keith
 */
public class MainGui extends javax.swing.JFrame implements IUpdaterListener {

	/**
	 * Creates new form MainGui
	 */
	public MainGui() {
		super(Main.TITLE);
		try {
			new File(Main.LOG_FOLDER).mkdirs();
			this.log = new FileOutputStream(new File(Main.LOG_FOLDER + "updater.log"), true);
		} catch (FileNotFoundException e) {
		}

		initComponents();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);

		((DefaultCaret) jTextArea1.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		jTextArea1.setFont(new java.awt.Font("Courier New", 0, 10));

		final Font font = jTextArea1.getFont();
		final Color foregroundColor = jTextArea1.getForeground();
		final Color backgroundColor = jTextArea1.getBackground();

		ProgressBarUI ui = new BasicProgressBarUI() {
			@Override
			protected Color getSelectionForeground() {
				return backgroundColor;
			}

			@Override
			protected Color getSelectionBackground() {
				return foregroundColor;
			}
		};
		jProgressBar1.setUI(ui);
		jProgressBar1.setFont(font);
		jProgressBar1.setForeground(foregroundColor);
		jProgressBar1.setBackground(backgroundColor);
		jProgressBar1.setStringPainted(true);
		jProgressBar1.setString("");
		jProgressBar1.setMaximum(PROGRESS_BAR_MAX);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jScrollPane1 = new javax.swing.JScrollPane();
    jTextArea1 = new javax.swing.JTextArea();
    jProgressBar1 = new javax.swing.JProgressBar();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setPreferredSize(new java.awt.Dimension(500, 500));
    setResizable(false);

    jTextArea1.setEditable(false);
    jTextArea1.setColumns(20);
    jTextArea1.setRows(5);
    jTextArea1.setMargin(new java.awt.Insets(5, 5, 5, 5));
    jScrollPane1.setViewportView(jTextArea1);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jScrollPane1)
          .addComponent(jProgressBar1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap())
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
		//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
		 * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
		 */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(MainGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(MainGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(MainGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(MainGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		//</editor-fold>

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new MainGui().setVisible(true);
			}
		});
	}

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JProgressBar jProgressBar1;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JTextArea jTextArea1;
  // End of variables declaration//GEN-END:variables

	private static final int PROGRESS_BAR_MAX = 1024;
	private FileOutputStream log;

	@Override
	public void log() {
		log("");
	}

	@Override
	public void log(String message) {
		message = message.concat("\n");
		this.jTextArea1.append(message);
		try {
			if (this.log != null) {
				this.log.write(message.getBytes());
			}
		} catch (IOException e) {
		}
	}

	@Override
	public void progress(long paramLong1, long paramLong2) {
		jProgressBar1.setString(String.format("%s / %s", new Object[]{Helper.readableFileSize(paramLong1), Helper.readableFileSize(paramLong2)}));

		float percent = (float) paramLong1 / (float) paramLong2;
		jProgressBar1.setValue((int) (percent * PROGRESS_BAR_MAX));
	}

	@Override
	public void progressFinished() {
		jProgressBar1.setString("");
		jProgressBar1.setValue(0);
	}

	@Override
	public void finished() {
		log("Starting client...");
		String libs = String.format("-Djava.library.path=\"%%PATH%%\"%slib", new Object[]{File.pathSeparator});
		UpdaterConfig cfg = Main.updater.cfg;
		ProcessBuilder pb = new ProcessBuilder(new String[]{"java", "-XX:ErrorFile=" + cfg.errorFile, "-Xms" + cfg.smem, "-Xmx" + cfg.mem, libs, "-jar", cfg.jar, "-U", cfg.res, cfg.server});
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
		log();
		System.exit(0);
	}
}
