
/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */
package haven;

import static haven.UI.mapSaver;

public class OptWnd extends Window {

	public static final Coord PANEL_POS = new Coord(220, 30);
	public final Panel panelMain;
	public final Panel panelAudio, panelCamera, panelDisplay, panelGeneral, panelHotkey, panelUI, panelVideo;
	public Panel current;

	public void chpanel(Panel p) {
		if (current != null) {
			current.hide();
		}

		(current = p).show();

		pack();
	}

	public class PButton extends Button {

		public final Panel tgt;
		public final int key;

		public PButton(int w, String title, int key, Panel tgt) {
			super(w, title);
			this.tgt = tgt;
			this.key = key;
		}

		public void click() {
			chpanel(tgt);
		}

		public boolean type(char key, java.awt.event.KeyEvent ev) {
			if ((this.key != -1) && (key == this.key)) {
				click();
				return (true);
			}
			return (false);
		}
	}

	public class Panel extends Widget {

		public Panel() {
			visible = false;
			c = Coord.z;
		}
	}

	public class VideoPanel extends Panel {

		public VideoPanel(Panel back) {
			super();
			add(new PButton(200, "Back", 27, back), new Coord(0, 180));
			pack();
		}

		public class CPanel extends Widget {

			public final GLSettings cf;

			public CPanel(GLSettings gcf) {
				this.cf = gcf;
				int y = 0;
				add(new CheckBox("Per-fragment lighting") {
					{
						a = cf.flight.val;
					}

					public void set(boolean val) {
						if (val) {
							try {
								cf.flight.set(true);
							} catch (GLSettings.SettingException e) {
								getparent(GameUI.class).error(e.getMessage());
								return;
							}
						} else {
							cf.flight.set(false);
						}
						a = val;
						cf.dirty = true;
					}
				}, new Coord(0, y));
				y += 25;
				add(new CheckBox("Render shadows") {
					{
						a = cf.lshadow.val;
					}

					public void set(boolean val) {
						if (val) {
							try {
								cf.lshadow.set(true);
							} catch (GLSettings.SettingException e) {
								getparent(GameUI.class).error(e.getMessage());
								return;
							}
						} else {
							cf.lshadow.set(false);
						}
						a = val;
						cf.dirty = true;
					}
				}, new Coord(0, y));
				y += 25;
				add(new CheckBox("Antialiasing") {
					{
						a = cf.fsaa.val;
					}

					public void set(boolean val) {
						try {
							cf.fsaa.set(val);
						} catch (GLSettings.SettingException e) {
							getparent(GameUI.class).error(e.getMessage());
							return;
						}
						a = val;
						cf.dirty = true;
					}
				}, new Coord(0, y));
				y += 25;
				add(new Label("Anisotropic filtering"), new Coord(0, y));
				if (cf.anisotex.max() <= 1) {
					add(new Label("(Not supported)"), new Coord(15, y + 15));
				} else {
					final Label dpy = add(new Label(""), new Coord(165, y + 15));
					add(new HSlider(160, (int) (cf.anisotex.min() * 2), (int) (cf.anisotex.max() * 2), (int) (cf.anisotex.val * 2)) {
						protected void added() {
							dpy();
							this.c.y = dpy.c.y + ((dpy.sz.y - this.sz.y) / 2);
						}

						void dpy() {
							if (val < 2) {
								dpy.settext("Off");
							} else {
								dpy.settext(String.format("%.1f\u00d7", (val / 2.0)));
							}
						}

						public void changed() {
							try {
								cf.anisotex.set(val / 2.0f);
							} catch (GLSettings.SettingException e) {
								getparent(GameUI.class).error(e.getMessage());
								return;
							}
							dpy();
							cf.dirty = true;
						}
					}, new Coord(0, y + 15));
				}
				y += 35;
				add(new Button(200, "Reset to defaults") {
					public void click() {
						cf.cfg.resetprefs();
						curcf.destroy();
						curcf = null;
					}
				}, new Coord(0, 150));
				pack();
			}
		}

		private CPanel curcf = null;

		public void draw(GOut g) {
			if ((curcf == null) || (g.gc.pref != curcf.cf)) {
				if (curcf != null) {
					curcf.destroy();
				}
				curcf = add(new CPanel(g.gc.pref), Coord.z);
			}
			super.draw(g);
		}
	}

	public OptWnd(boolean gopts) {
		super(Coord.z, "Options", true);

		panelMain = add(new Panel());

		panelAudio = add(new Panel());
		panelVideo = add(new VideoPanel(panelMain));

		panelCamera = add(new Panel());
		panelDisplay = add(new Panel());
		panelGeneral = add(new Panel());
		panelHotkey = add(new Panel());
		panelUI = add(new Panel());

		int y = 0;
		initAudioPanel(0, y);
		initVideoPanel(1, y);
		y += 1;
		initGeneralPanel(0, y);
		initHotkeyPanel(1, y);
		y += 1;
		initDisplayPanel(0, y);
		initCameraPanel(1, y);
		y += 1;
		initUIPanel(.5, y);
		y += 2;
		if (gopts) {
			panelMain.add(new Button(200, "Switch character") {
				@Override
				public void click() {
					getparent(GameUI.class).act("lo", "cs");
				}
			}, getPanelButtonCoord(0, y));
			panelMain.add(new Button(200, "Log out") {
				@Override
				public void click() {
					getparent(GameUI.class).act("lo");
				}
			}, getPanelButtonCoord(1, y));
			y += 1;
		}
		panelMain.add(new Button(200, "Close") {
			@Override
			public void click() {
				OptWnd.this.hide();
			}
		}, getPanelButtonCoord(.5, y));

		panelMain.pack();
		chpanel(panelMain);
	}

	private Coord getPanelButtonCoord(double x, double y) {
		return new Coord((int) ((double) PANEL_POS.x * x), (int) ((double) PANEL_POS.y * y));
	}

	private void addPanelButton(String name, char key, Panel panel, double x, double y) {
		panelMain.add(new PButton(200, name, key, panel), getPanelButtonCoord(x, y));
	}

	private void initAudioPanel(double buttonX, double buttonY) {
		int x = 0, y = 0, mx = 0, my = 0;
		addPanelButton("Audio Settings", 'a', panelAudio, buttonX, buttonY);

		panelAudio.add(new Label("Master audio volume"), new Coord(x, y));
		y += 15;
		panelAudio.add(new HSlider(200, 0, 1000, (int) (Audio.volume * 1000)) {
			@Override
			public void changed() {
				Audio.setvolume(val / 1000.0);
			}
		}, new Coord(0, y));
		y += 30;
		panelAudio.add(new Label("In-game event volume"), new Coord(x, y));
		y += 15;
		panelAudio.add(new HSlider(200, 0, 1000, 0) {
			@Override
			protected void attach(UI ui) {
				super.attach(ui);
				val = (int) (ui.audio.pos.volume * 1000);
			}

			@Override
			public void changed() {
				ui.audio.pos.setvolume(val / 1000.0);
			}
		}, new Coord(x, y));
		y += 20;
		panelAudio.add(new Label("Ambient volume"), new Coord(x, y));
		y += 15;
		panelAudio.add(new HSlider(200, 0, 1000, 0) {
			@Override
			protected void attach(UI ui) {
				super.attach(ui);
				val = (int) (ui.audio.pos.volume * 1000);
			}

			@Override
			public void changed() {
				ui.audio.amb.setvolume(val / 1000.0);
			}
		}, new Coord(x, y));

		my = Math.max(my, y);

		panelAudio.add(new PButton(200, "Back", 27, panelMain), new Coord(x, my + 35));
		panelAudio.pack();
	}

	private void initCameraPanel(double buttonX, double buttonY) {
		int x = 0, y = 0, mx = 0, my = 0;
		addPanelButton("Camera Settings", 'c', panelCamera, buttonX, buttonY);

		panelCamera.add(new Label("Brighten view"), new Coord(x, y));
		y += 15;
		panelCamera.add(new CFGHSlider(null, CFG.CAMERA_BRIGHT) {
			@Override
			public void changed() {
				super.changed();
				if (ui.sess != null && ui.sess.glob != null) {
					ui.sess.glob.brighten();
				}
			}
		}, new Coord(x, y));
		y += 25;
		panelCamera.add(new CFGCheckBox("Free camera rotation", CFG.CAMERA_FREEROTATION), new Coord(x, y));

		my = Math.max(my, y);

		panelCamera.add(new PButton(200, "Back", 27, panelMain), new Coord(x, my + 35));
		panelCamera.pack();
	}

	private void initDisplayPanel(double buttonX, double buttonY) {
		int x = 0, y = 0, mx = 0, my = 0;
		addPanelButton("Display Settings", 'd', panelDisplay, buttonX, buttonY);

		panelDisplay.add(new CFGCheckBox("Show flavor objects", CFG.DISPLAY_FLAVOR), new Coord(x, y));
		y += 25;
		panelDisplay.add(new CFGCheckBox("Always show kin names", CFG.DISPLAY_KINNAMES), new Coord(x, y));

		my = Math.max(my, y);

		panelDisplay.add(new PButton(200, "Back", 27, panelMain), new Coord(x, my + 35));
		panelDisplay.pack();
	}

	private void initGeneralPanel(double buttonX, double buttonY) {
		int x = 0, y = 0, mx = 0, my = 0;
		addPanelButton("General Settings", 'g', panelGeneral, buttonX, buttonY);

		panelGeneral.add(new CFGCheckBox("Store minimap tiles", CFG.GENERAL_STOREMAP) {
			@Override
			public void changed(boolean val) {
				super.changed(val);
				if (val && UI.mapSaver == null) {
					mapSaver = new MapSaver(ui);
				}
			}
		}, new Coord(x, y));

		my = Math.max(my, y);

		panelGeneral.add(new PButton(200, "Back", 27, panelMain), new Coord(x, my + 35));
		panelGeneral.pack();
	}

	private void initHotkeyPanel(double buttonX, double buttonY) {
		int x = 0, y = 0, mx = 0, my = 0;
		addPanelButton("Hotkey Settings", 'h', panelHotkey, buttonX, buttonY);

		panelHotkey.add(new CFGLabel("Show all qualities",
						"Multiple selections means ANY key must be pressed to activate."), new Coord(x, y));
		y += 15;
		panelHotkey.add(new CFGCheckBox("SHIFT", CFG.HOTKEY_ITEM_QUALITY) {
			@Override
			protected void defval() {
				a = Utils.checkbit(cfg.vali(), 0);
			}

			@Override
			public void set(boolean a) {
				this.a = a;
				cfg.set(Utils.setbit(cfg.vali(), 0, a));

			}
		}, new Coord(x, y));
		y += 15;
		panelHotkey.add(new CFGCheckBox("CTRL", CFG.HOTKEY_ITEM_QUALITY) {
			@Override
			protected void defval() {
				a = Utils.checkbit(cfg.vali(), 1);
			}

			@Override
			public void set(boolean a) {
				this.a = a;
				cfg.set(Utils.setbit(cfg.vali(), 1, a));

			}
		}, new Coord(x, y));
		y += 15;
		panelHotkey.add(new CFGCheckBox("ALT", CFG.HOTKEY_ITEM_QUALITY) {
			@Override
			protected void defval() {
				a = Utils.checkbit(cfg.vali(), 2);
			}

			@Override
			public void set(boolean a) {
				this.a = a;
				cfg.set(Utils.setbit(cfg.vali(), 2, a));

			}
		}, new Coord(x, y));
		y += 25;
		panelHotkey.add(new CFGLabel("Transfer items / Stockpile transfer items in",
						"Multiple selections means ALL keys must be pressed to activate."), new Coord(x, y));
		y += 15;
		panelHotkey.add(new CFGCheckBox("SHIFT", CFG.HOTKEY_ITEM_TRANSFER_IN) {
			@Override
			protected void defval() {
				a = Utils.checkbit(cfg.vali(), 0);
			}

			@Override
			public void set(boolean a) {
				this.a = a;
				cfg.set(Utils.setbit(cfg.vali(), 0, a));

			}
		}, new Coord(x, y));
		y += 15;
		panelHotkey.add(new CFGCheckBox("CTRL", CFG.HOTKEY_ITEM_TRANSFER_IN) {
			@Override
			protected void defval() {
				a = Utils.checkbit(cfg.vali(), 1);
			}

			@Override
			public void set(boolean a) {
				this.a = a;
				cfg.set(Utils.setbit(cfg.vali(), 1, a));

			}
		}, new Coord(x, y));
		y += 15;
		panelHotkey.add(new CFGCheckBox("ALT", CFG.HOTKEY_ITEM_TRANSFER_IN) {
			@Override
			protected void defval() {
				a = Utils.checkbit(cfg.vali(), 2);
			}

			@Override
			public void set(boolean a) {
				this.a = a;
				cfg.set(Utils.setbit(cfg.vali(), 2, a));

			}
		}, new Coord(x, y));
		y += 25;
		panelHotkey.add(new CFGLabel("Drop items / Stockpile transfer items out",
						"Multiple selections means ALL keys must be pressed to activate."), new Coord(x, y));
		y += 15;
		panelHotkey.add(new CFGCheckBox("SHIFT", CFG.HOTKEY_ITEM_TRANSFER_OUT) {
			@Override
			protected void defval() {
				a = Utils.checkbit(cfg.vali(), 0);
			}

			@Override
			public void set(boolean a) {
				this.a = a;
				cfg.set(Utils.setbit(cfg.vali(), 0, a));

			}
		}, new Coord(x, y));
		y += 15;
		panelHotkey.add(new CFGCheckBox("CTRL", CFG.HOTKEY_ITEM_TRANSFER_OUT) {
			@Override
			protected void defval() {
				a = Utils.checkbit(cfg.vali(), 1);
			}

			@Override
			public void set(boolean a) {
				this.a = a;
				cfg.set(Utils.setbit(cfg.vali(), 1, a));

			}
		}, new Coord(x, y));
		y += 15;
		panelHotkey.add(new CFGCheckBox("ALT", CFG.HOTKEY_ITEM_TRANSFER_OUT) {
			@Override
			protected void defval() {
				a = Utils.checkbit(cfg.vali(), 2);
			}

			@Override
			public void set(boolean a) {
				this.a = a;
				cfg.set(Utils.setbit(cfg.vali(), 2, a));

			}
		}, new Coord(x, y));

		my = Math.max(my, y);

		panelHotkey.add(new PButton(200, "Back", 27, panelMain), new Coord(x, my + 35));
		panelHotkey.pack();
	}

	private void initUIPanel(double buttonX, double buttonY) {
		int x = 0, y = 0, mx = 0, my = 0;
		addPanelButton("UI Settings", 'u', panelUI, buttonX, buttonY);

		//panelUI.add(new CFGCheckBox("Show timestamps in chat", CFG.UI_CHAT_TIMESTAMP), new Coord(x, y));
		//y += 25;
		panelUI.add(new CFGCheckBox("Item meter countdown", CFG.UI_ITEM_METER_COUNTDOWN, "If checked all item progress meters will start full and empty over time."), new Coord(x, y));
		y += 25;
		panelUI.add(new Label("Item meter"), new Coord(x, y));
		y += 15;
		panelUI.add(new CFGHSlider("R", CFG.UI_ITEM_METER_RED), new Coord(x, y));
		y += 15;
		panelUI.add(new CFGHSlider("G", CFG.UI_ITEM_METER_GREEN), new Coord(x, y));
		y += 15;
		panelUI.add(new CFGHSlider("B", CFG.UI_ITEM_METER_BLUE), new Coord(x, y));
		y += 15;
		panelUI.add(new CFGHSlider("A", CFG.UI_ITEM_METER_ALPHA), new Coord(x, y));
		y += 25;
		panelUI.add(new CFGCheckBox("Show boulders on minimap", CFG.UI_MINIMAP_BOULDERS), new Coord(x, y));
		y += 25;
		panelUI.add(new CFGCheckBox("Show players on minimap", CFG.UI_MINIMAP_PLAYERS), new Coord(x, y));
		//y += 25;
		//panelUI.add(new CFGCheckBox("Study lock", CFG.UI_STUDYLOCK), new Coord(x, y));
		y += 25;
		CFGRadioGroup qualityRadioGroup = new CFGRadioGroup(panelUI);
		int qualityRadioGroupCheckedIndex = 0;
		switch (CFG.UI_ITEM_QUALITY_SHOW.vali()) {
			case 0:
				qualityRadioGroupCheckedIndex = 0;
				break;
			case 1:
				qualityRadioGroupCheckedIndex = 1;
				break;
			case 2:
				qualityRadioGroupCheckedIndex = 2;
				break;
		}
		qualityRadioGroup.add("Do not show quality", CFG.UI_ITEM_QUALITY_SHOW, 0, new Coord(x, y));
		y += 15;
		qualityRadioGroup.add("Show single quality", CFG.UI_ITEM_QUALITY_SHOW, 1, new Coord(x, y));
		y += 15;
		qualityRadioGroup.add("Show all qualities", CFG.UI_ITEM_QUALITY_SHOW, 2, new Coord(x, y));
		qualityRadioGroup.check(qualityRadioGroupCheckedIndex);
		y += 25;
		panelUI.add(new CFGCheckBox("Show single quality as max", CFG.UI_ITEM_QUALITY_SINGLEASMAX,
						"If checked will show single value quality as maximum of all qualities, instead of average"), new Coord(x, y));

		my = Math.max(my, y);

		panelUI.add(new PButton(200, "Back", 27, panelMain), new Coord(x, my + 35));
		panelUI.pack();
	}

	private void initVideoPanel(double buttonX, double buttonY) {
		addPanelButton("Video Settings", 'v', panelVideo, buttonX, buttonY);
	}

	public OptWnd() {
		this(true);
	}

	public void wdgmsg(Widget sender, String msg, Object... args) {
		if ((sender == this) && (msg == "close")) {
			hide();
		} else {
			super.wdgmsg(sender, msg, args);
		}
	}

	public void show() {
		chpanel(panelMain);
		super.show();
	}

	private class CFGCheckBox extends CheckBox {

		protected final CFG cfg;

		public CFGCheckBox(String lbl, CFG cfg) {
			this(lbl, cfg, null);
		}

		public CFGCheckBox(String lbl, CFG cfg, String tip) {
			super(lbl);

			this.cfg = cfg;
			defval();
			if (tip != null) {
				tooltip = Text.render(tip).tex();
			}
		}

		protected void defval() {
			super.set(cfg.valb());
		}

		@Override
		public void set(boolean a) {
			super.set(a);
			cfg.set(a);
		}
	}

	private class CFGHSlider extends HSlider {

		protected final CFG cfg;
		Text lbl;

		public CFGHSlider(String lbl, CFG cfg) {
			this(lbl, cfg, null);
		}

		public CFGHSlider(String lbl, CFG cfg, String tip) {
			this(lbl, cfg, tip, 200, 0, 1000, 0);
		}

		public CFGHSlider(String lbl, CFG cfg, String tip, int w, int min, int max, int val) {
			super(w, min, max, val);

			this.cfg = cfg;
			defval();
			if (lbl != null) {
				this.lbl = Text.std.render(lbl, java.awt.Color.WHITE);
			}
			if (tip != null) {
				tooltip = Text.render(tip).tex();
			}
		}

		protected void defval() {
			val = (int) (1000 * cfg.valf());
		}

		@Override
		public void changed() {
			super.changed();
			cfg.set(val / 1000.0f);
		}

		@Override
		public void draw(GOut g) {
			if (lbl != null) {
				g.image(lbl.tex(), new Coord());

				int offset = Math.max(10, lbl.tex().sz().x);
				int szX = sz.x - offset;
				int cy = (sflarp.sz().y - schain.sz().y) / 2;
				for (int x = offset; x < szX; x += schain.sz().x) {
					g.image(schain, new Coord(x, cy));
				}
				int fx = ((szX - sflarp.sz().x) * (val - min)) / (max - min);
				g.image(sflarp, new Coord(offset + fx, 0));
			} else {
				super.draw(g);
			}
		}
	}

	private class CFGLabel extends Label {

		public CFGLabel(String lbl) {
			this(lbl, null);
		}

		public CFGLabel(String lbl, String tip) {
			super(lbl);

			if (tip != null) {
				tooltip = Text.render(tip).tex();
			}
		}
	}

	private class CFGRadioGroup extends RadioGroup {

		public CFGRadioGroup(Widget parent) {
			super(parent);
		}

		public class CFGRadioButton extends RadioButton {

			protected final CFG cfg;
			protected final int cfgVal;

			CFGRadioButton(String lbl, CFG cfg, int val) {
				this(lbl, cfg, val, null);
			}

			CFGRadioButton(String lbl, CFG cfg, int val, String tip) {
				super(lbl);

				this.cfg = cfg;
				this.cfgVal = val;
				if (tip != null) {
					tooltip = Text.render(tip).tex();
				}
			}
		}

		public CFGRadioButton add(String lbl, CFG cfg, int val, Coord c) {
			return add(lbl, cfg, val, null, c);
		}

		public CFGRadioButton add(String lbl, CFG cfg, int val, String tip, Coord c) {
			CFGRadioButton rb = new CFGRadioButton(lbl, cfg, val, tip);
			parent.add(rb, c);
			btns.add(rb);
			map.put(lbl, rb);
			rmap.put(rb, lbl);
			if (checked == null) {
				check(rb);
			}
			return (rb);
		}

		@Override
		public void changed(int btn, String lbl) {
			super.changed(btn, lbl);
			CFGRadioButton radioButton = (CFGRadioButton) btns.get(btn);
			radioButton.cfg.set(radioButton.cfgVal);
		}
	}
}
