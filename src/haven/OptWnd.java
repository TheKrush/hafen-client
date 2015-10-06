
/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     BjÃ¶rn Johannessen <johannessen.bjorn@gmail.com>
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

import haven.CFG.CFGObserver;
import haven.CheckListbox.CheckListboxItem;
import haven.Globals.Data.DataObserver;
import static haven.UI.mapSaver;
import java.awt.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OptWnd extends Window {

	private static final int BUTTON_WIDTH = 200;
	public static final Coord PANEL_POS = new Coord(220, 30);
	public final Panel panelMain;
	public final Panel panelAudio;
	public final Panel panelCamera;
	public final Panel panelDisplay;
	public final Panel panelGeneral;
	public final Panel panelHotkey;
	public final Panel panelMinimap;
	public final Panel panelUI;
	public final Panel panelVideo;
	public Panel current;

	private void chpanel(Panel p, boolean center) {
		int cx = 0, cy = 0;
		if (current != null) {
			current.hide();
		} else {
			center = false;
		}
		if (center) {
			cx = c.x + (sz.x / 2); // center x
			cy = c.y + (sz.y / 2); // center y
		}
		(current = p).show();
		pack();
		if (center) {
			c.x = cx - (sz.x / 2);
			c.y = cy - (sz.y / 2);
		}
	}

	private void chpanel(Panel p) {
		chpanel(p, true);
	}

	public class PButton extends Button {

		public final Panel tgt;
		public final int key;

		public PButton(int w, String title, int key, Panel tgt) {
			super(w, title);
			this.tgt = tgt;
			this.key = key;
		}

		@Override
		public void click() {
			chpanel(tgt);
		}

		@Override
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

					@Override
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

					@Override
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

					@Override
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
						@Override
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

						@Override
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
					@Override
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

		@Override
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
		panelMinimap = add(new Panel());
		panelUI = add(new Panel());

		double y = 0;
		CFGLabel vlbl = new CFGLabel("Vanilla Options");
		panelMain.add(vlbl, getPanelButtonCoord(1, y).sub(new Coord(vlbl.sz.x / 2, 0)).sub(new Coord(10, 0)));
		y += 0.5;
		initAudioPanel(0, y);
		initVideoPanel(1, y);
		y += 2;
		CFGLabel mlbl = new CFGLabel("Minion Options");
		panelMain.add(mlbl, getPanelButtonCoord(1, y).sub(new Coord(mlbl.sz.x / 2, 0)).sub(new Coord(10, 0)));
		y += 0.5;
		initDisplayPanel(0, y);
		initCameraPanel(1, y);
		y += 1;
		initGeneralPanel(0, y);
		initHotkeyPanel(1, y);
		y += 1;
		initMinimapPanel(0, y);
		initUIPanel(1, y);
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
		chpanel(panelMain, false);
	}

	private Coord getPanelButtonCoord(double x, double y) {
		return new Coord((int) ((double) PANEL_POS.x * x), (int) ((double) PANEL_POS.y * y));
	}

	private void addPanelButton(String name, char key, Panel panel, double x, double y) {
		panelMain.add(new PButton(BUTTON_WIDTH, name, key, panel), getPanelButtonCoord(x, y));
	}

	private void initAudioPanel(double buttonX, double buttonY) {
		Panel panel = panelAudio;
		int x = 0, y = 0, my = 0;
		addPanelButton("Audio Settings", 'a', panel, buttonX, buttonY);

		panel.add(new Label("Master audio volume"), new Coord(x, y));
		y += 15;
		panel.add(new HSlider(200, 0, 1000, (int) (Audio.volume * 1000)) {
			@Override
			public void changed() {
				Audio.setvolume(val / 1000.0);
			}
		}, new Coord(0, y));
		y += 30;
		panel.add(new Label("In-game event volume"), new Coord(x, y));
		y += 15;
		panel.add(new HSlider(200, 0, 1000, 0) {
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
		panel.add(new Label("Ambient volume"), new Coord(x, y));
		y += 15;
		panel.add(new HSlider(200, 0, 1000, 0) {
			@Override
			protected void attach(UI ui) {
				super.attach(ui);
				val = (int) (ui.audio.amb.volume * 1000);
			}

			@Override
			public void changed() {
				ui.audio.amb.setvolume(val / 1000.0);
			}
		}, new Coord(x, y));

		panel.pack();
		x = panel.sz.x > BUTTON_WIDTH ? (panel.sz.x / 2) - (BUTTON_WIDTH / 2) : 0;
		y = panel.sz.y + 35;
		panel.add(new PButton(BUTTON_WIDTH, "Back", 27, panelMain), new Coord(x, y));
		panel.pack();
	}

	private void initCameraPanel(double buttonX, double buttonY) {
		Panel panel = panelCamera;
		int x = 0, y = 0, my = 0;
		addPanelButton("Camera Settings", 'c', panel, buttonX, buttonY);

		panel.add(new CFGLabel("Camera type"), new Coord(x, y));
		y += 15;
		CFGRadioGroup qualityRadioGroup = new CFGRadioGroup(panel) {
			@Override
			public void changed(int btn, String lbl) {
				super.changed(btn, lbl);
				CFGRadioGroup.CFGRadioButton radioButton = (CFGRadioGroup.CFGRadioButton) btns.get(btn);
				String oldCam = Utils.getpref("defcam", null);
				String newCam = "";
				switch (radioButton.cfgVal) {
					case 0:
					case 1:
					case 2:
					case 3:
						newCam = "ortho";
						break;
					case 4:
						newCam = "follow";
						break;
					case 5:
						newCam = "bad";
						break;
				}
				try {
					if (!newCam.equals(oldCam)) {
						Utils.setpref("defcam", newCam);
						ui.gui.map.changecamera(newCam);
					}
					ui.gui.map.camera.release();
				} catch (Exception ex) {
					// usually throws when initially setting up menu
				}
			}
		};
		int qualityRadioGroupCheckedIndex = CFG.CAMERA_TYPE.vali();
		qualityRadioGroup.add("Default (snap to 45, 135, 225, 315)", CFG.CAMERA_TYPE, 0, new Coord(x, y));
		y += 15;
		qualityRadioGroup.add("Default (snap to 0, 90, 180, 270)", CFG.CAMERA_TYPE, 1, new Coord(x, y));
		y += 15;
		qualityRadioGroup.add("Default (snap to all 8 directions)", CFG.CAMERA_TYPE, 2, new Coord(x, y));
		y += 15;
		qualityRadioGroup.add("Default (no snapping)", CFG.CAMERA_TYPE, 3, new Coord(x, y));
		y += 15;
		qualityRadioGroup.add("Follow", CFG.CAMERA_TYPE, 4, new Coord(x, y));
		y += 15;
		qualityRadioGroup.add("Free", CFG.CAMERA_TYPE, 5, new Coord(x, y));
		qualityRadioGroup.check(qualityRadioGroupCheckedIndex);

		panel.pack();
		x = panel.sz.x > BUTTON_WIDTH ? (panel.sz.x / 2) - (BUTTON_WIDTH / 2) : 0;
		y = panel.sz.y + 35;
		panel.add(new PButton(BUTTON_WIDTH, "Back", 27, panelMain), new Coord(x, y));
		panel.pack();
	}

	private void initDisplayPanel(double buttonX, double buttonY) {
		Panel panel = panelDisplay;
		int x = 0, y = 0, my = 0;
		addPanelButton("Display Settings", 'd', panel, buttonX, buttonY);

		panel.add(new CFGLabel("Foreground FPS limit"), new Coord(x, y));
		y += 15;
		panel.add(new CFGHSlider(null, CFG.DISPLAY_FPS_FOREGROUND, null, 200, 1, 144, 1), new Coord(x, y));
		y += 25;
		panel.add(new CFGLabel("Background FPS limit"), new Coord(x, y));
		y += 15;
		panel.add(new CFGHSlider(null, CFG.DISPLAY_FPS_BACKGROUND, null, 200, 1, 144, 1), new Coord(x, y));
		y += 25;
		panel.add(new CFGCheckBox("Show dynamic lighting", CFG.DISPLAY_LIGHTING_DYNAMIC), new Coord(x, y));
		y += 25;
		panel.add(new CFGCheckBox("Show weather", CFG.DISPLAY_WEATHER), new Coord(x, y));
		y += 25;
		panel.add(new CFGCheckBox("Show flavor objects", CFG.DISPLAY_FLAVOR), new Coord(x, y));
		y += 25;
		panel.add(new CFGCheckBox("Show simple crops (requires restart)", CFG.DISPLAY_CROPS_SIMPLE), new Coord(x, y));
		y += 25;
		panel.add(new CFGCheckBox("Show simple foragables (requires restart)", CFG.DISPLAY_FORAGABLES_SIMPLE), new Coord(x, y));
		y += 25;
		panel.add(new CFGCheckBox("Show plant growth", CFG.DISPLAY_PLANT_GROWTH) {
			{
				CFG.DISPLAY_PLANT_GROWTH.addObserver(this);
			}

			@Override
			public void destroy() {
				CFG.DISPLAY_PLANT_GROWTH.remObserver(this);
				super.destroy();
			}
		}, new Coord(x, y));

		my = Math.max(my, y);
		x += 225;
		y = 0;

		panel.add(new CFGLabel("Brighten view"), new Coord(x, y));
		y += 15;
		panel.add(new CFGHSlider(null, CFG.DISPLAY_BRIGHTNESS) {
			@Override
			public void changed() {
				super.changed();
				if (ui.sess != null && ui.sess.glob != null) {
					ui.sess.glob.brighten();
				}
			}
		}, new Coord(x, y));
		y += 25;
		panel.add(new CFGCheckBox("Display grid", CFG.DISPLAY_GRID_SHOW) {
			{
				CFG.DISPLAY_GRID_SHOW.addObserver(this);
			}

			@Override
			public void set(boolean a) {
				if (ui != null && ui.gui != null && ui.gui.map != null) {
					ui.gui.map.initgrid(a);
				}
				super.set(a);
			}

			@Override
			public void destroy() {
				CFG.DISPLAY_GRID_SHOW.remObserver(this);
				super.destroy();
			}
		}, new Coord(x, y));
		y += 15;
		panel.add(new CFGLabel("Grid thickness"), new Coord(x, y));
		y += 15;
		panel.add(new CFGHSlider(null, CFG.DISPLAY_GRID_THICKNESS, null, 200, 1, 3, 1), new Coord(x, y));
		y += 25;
		panel.add(new CFGCheckBox("Always show kin names", CFG.DISPLAY_KIN_NAMES), new Coord(x, y));
		y += 25;
		panel.add(new CFGCheckBox("Show object damage", CFG.DISPLAY_OBJECT_DAMAGE) {
			{
				CFG.DISPLAY_OBJECT_DAMAGE.addObserver(this);
			}

			@Override
			public void destroy() {
				CFG.DISPLAY_OBJECT_DAMAGE.remObserver(this);
				super.destroy();
			}
		}, new Coord(x, y));
		y += 25;
		panel.add(new CFGCheckBox("Show object radius", CFG.DISPLAY_OBJECT_RADIUS) {
			{
				CFG.DISPLAY_OBJECT_RADIUS.addObserver(this);
			}

			@Override
			public void destroy() {
				CFG.DISPLAY_OBJECT_RADIUS.remObserver(this);
				super.destroy();
			}
		}, new Coord(x, y));
		y += 25;
		panel.add(new CFGCheckBox("Show critter paths", CFG.DISPLAY_PATH_CRITTER), new Coord(x, y));
		y += 15;
		panel.add(new CFGCheckBox("Show player paths", CFG.DISPLAY_PATH_PLAYER), new Coord(x, y));
		y += 15;
		panel.add(new CFGLabel("Path thickness"), new Coord(x, y));
		y += 15;
		panel.add(new CFGHSlider(null, CFG.DISPLAY_PATH_THICKNESS, null, 200, 1, 3, 1), new Coord(x, y));

		panel.pack();
		x = sz.x > BUTTON_WIDTH ? (panel.sz.x / 2) - (BUTTON_WIDTH / 2) : 0;
		y = panel.sz.y + 35;
		panel.add(new PButton(BUTTON_WIDTH, "Back", 27, panelMain), new Coord(x, y));
		panel.pack();
	}

	private void initGeneralPanel(double buttonX, double buttonY) {
		Panel panel = panelGeneral;
		int x = 0, y = 0, my = 0;
		addPanelButton("General Settings", 'g', panel, buttonX, buttonY);

		panel.add(new CFGCheckBox("Store general game data", CFG.GENERAL_DATA_SAVE, "Data is stored in 'data' folder and is used to automaticaly update options menus, but can make the game laggy"), new Coord(x, y));
		y += 25;
		panel.add(new CFGCheckBox("Store chat logs", CFG.GENERAL_CHAT_SAVE, "Logs are stored in 'chat' folder"), new Coord(x, y));
		y += 25;
		panel.add(new CFGCheckBox("Store minimap tiles", CFG.GENERAL_MAP_SAVE, "Tiles are stored in 'map' folder") {
			@Override
			public void changed(boolean val) {
				super.changed(val);
				if (val && UI.mapSaver == null) {
					mapSaver = new MapSaver(ui);
				}
			}
		}, new Coord(x, y));

		panel.pack();
		x = panel.sz.x > BUTTON_WIDTH ? (panel.sz.x / 2) - (BUTTON_WIDTH / 2) : 0;
		y = panel.sz.y + 35;
		panel.add(new PButton(BUTTON_WIDTH, "Back", 27, panelMain), new Coord(x, y));
		panel.pack();
	}

	private void initHotkeyPanel(double buttonX, double buttonY) {
		Panel panel = panelHotkey;
		int x = 0, y = 0, my = 0;
		addPanelButton("Hotkey Settings", 'h', panel, buttonX, buttonY);

		panel.add(new CFGCheckBox("Mouse follow", CFG.HOTKEY_MOUSE_FOLLOW, "If enabled holding LMB or using ALT+F will make player follow mouse"), new Coord(x, y));
		y += 25;
		panel.add(new CFGLabel("Show all qualities",
						"Multiple selections means ANY key must be pressed to activate"), new Coord(x, y));
		y += 15;
		panel.add(new CFGCheckBox("SHIFT", CFG.HOTKEY_ITEM_QUALITY) {
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
		panel.add(new CFGCheckBox("CTRL", CFG.HOTKEY_ITEM_QUALITY) {
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
		panel.add(new CFGCheckBox("ALT", CFG.HOTKEY_ITEM_QUALITY) {
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
		panel.add(new CFGLabel("Transfer items / Stockpile transfer items in",
						"Multiple selections means ALL keys must be pressed to activate"), new Coord(x, y));
		y += 15;
		panel.add(new CFGCheckBox("SHIFT", CFG.HOTKEY_ITEM_TRANSFER_IN) {
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
		panel.add(new CFGCheckBox("CTRL", CFG.HOTKEY_ITEM_TRANSFER_IN) {
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
		panel.add(new CFGCheckBox("ALT", CFG.HOTKEY_ITEM_TRANSFER_IN) {
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
		panel.add(new CFGLabel("Drop items / Stockpile transfer items out",
						"Multiple selections means ALL keys must be pressed to activate"), new Coord(x, y));
		y += 15;
		panel.add(new CFGCheckBox("SHIFT", CFG.HOTKEY_ITEM_TRANSFER_OUT) {
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
		panel.add(new CFGCheckBox("CTRL", CFG.HOTKEY_ITEM_TRANSFER_OUT) {
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
		panel.add(new CFGCheckBox("ALT", CFG.HOTKEY_ITEM_TRANSFER_OUT) {
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

		panel.pack();
		x = panel.sz.x > BUTTON_WIDTH ? (panel.sz.x / 2) - (BUTTON_WIDTH / 2) : 0;
		y = panel.sz.y + 35;
		panel.add(new PButton(BUTTON_WIDTH, "Back", 27, panelMain), new Coord(x, y));
		panel.pack();
	}

	private void initMinimapPanel(double buttonX, double buttonY) {
		Panel panel = panelMinimap;
		int x = 0, y = 0, my = 0;
		addPanelButton("Minimap Settings", 'm', panel, buttonX, buttonY);

		panel.add(new CFGCheckBox("Undock minimap", CFG.MINIMAP_FLOATING) {
			{
				CFG.MINIMAP_FLOATING.addObserver(this);
			}

			@Override
			public void destroy() {
				CFG.MINIMAP_FLOATING.remObserver(this);
				super.destroy();
			}

			@Override
			public void set(boolean a) {
				super.set(a);
				if (ui != null && ui.gui != null) {
					ui.gui.showmmappanel(a);
				}
			}
		}, new Coord(x, y));
		y += 25;
		panel.add(new CFGCheckBox("Use Ender's Radar", CFG.MINIMAP_RADAR, "If this is enabled all the other minimap icon options are ignored"), new Coord(x, y));
		y += 25;

		my = Math.max(my, y);
		x += 200;
		y = 0;

		panel.add(new CFGCheckBox("Show players on minimap", CFG.MINIMAP_PLAYERS), new Coord(x, y));
		y += 25;

		my = Math.max(my, y);
		x = 0;
		y = my;

		int curY = my;
		panel.add(new CFGLabel("Show bumlings"), new Coord(x, y));
		y += 15;
		CFGCheckListbox bumlingsCheckListbox = new CFGCheckListbox(CFG.MINIMAP_BUMLINGS, 130, 10) {
			private DataObserver observer;

			{
				CFG.MINIMAP_BUMLINGS.addObserver(this);
				Globals.Data.addObserver("resource", observer = new DataObserver() {
					@Override
					public void dataUpdated(String name) {
						if (!"resource".equals(name)) {
							return;
						}
						additems(Globals.Data.get(name, "gfx/terobjs/bumlings/", true, true));
					}
				});
				observer.dataUpdated("resource");
			}

			@Override
			public void destroy() {
				CFG.MINIMAP_BUMLINGS.remObserver(this);
				Globals.Data.remObserver("resource", observer);
				super.destroy();
			}
		};
		panel.add(bumlingsCheckListbox, new Coord(x, y));
		x += bumlingsCheckListbox.sz.x + 10;
		y = curY;
		panel.add(new CFGLabel("Show bushes"), new Coord(x, y));
		y += 15;
		CFGCheckListbox bushesCheckListbox = new CFGCheckListbox(CFG.MINIMAP_BUSHES, 130, 10) {
			private DataObserver observer;

			{
				CFG.MINIMAP_BUSHES.addObserver(this);
				Globals.Data.addObserver("resource", observer = new DataObserver() {
					@Override
					public void dataUpdated(String name) {
						if (!"resource".equals(name)) {
							return;
						}
						additems(Globals.Data.get(name, "gfx/terobjs/bushes/", true, true));
					}
				});
				observer.dataUpdated("resource");
			}

			@Override
			public void destroy() {
				CFG.MINIMAP_BUSHES.remObserver(this);
				Globals.Data.remObserver("resource", observer);
				super.destroy();
			}
		};
		panel.add(bushesCheckListbox, new Coord(x, y));
		x += bushesCheckListbox.sz.x + 10;
		y = curY;
		panel.add(new CFGLabel("Show trees"), new Coord(x, y));
		y += 15;
		CFGCheckListbox treesCheckListbox = new CFGCheckListbox(CFG.MINIMAP_TREES, 130, 10) {
			private DataObserver observer;

			{
				CFG.MINIMAP_TREES.addObserver(this);
				Globals.Data.addObserver("resource", observer = new DataObserver() {
					@Override
					public void dataUpdated(String name) {
						if (!"resource".equals(name)) {
							return;
						}
						additems(Globals.Data.get(name, "gfx/terobjs/trees/", true, true));
					}
				});
				observer.dataUpdated("resource");
			}

			@Override
			public void destroy() {
				CFG.MINIMAP_TREES.remObserver(this);
				Globals.Data.remObserver("resource", observer);
				super.destroy();
			}
		};
		panel.add(treesCheckListbox, new Coord(x, y));
		x += treesCheckListbox.sz.x + 10;

		panel.pack();
		x = panel.sz.x > BUTTON_WIDTH ? (panel.sz.x / 2) - (BUTTON_WIDTH / 2) : 0;
		y = panel.sz.y + 35;
		panel.add(new PButton(BUTTON_WIDTH, "Back", 27, panelMain), new Coord(x, y));
		panel.pack();
	}

	private void initUIPanel(double buttonX, double buttonY) {
		Panel panel = panelUI;
		int x = 0, y = 0, my = 0;
		addPanelButton("UI Settings", 'u', panel, buttonX, buttonY);

		panel.add(new CFGCheckBox("Show hslider values", CFG.UI_HSLIDER_VALUE_SHOW), new Coord(x, y));
		y += 25;
		panel.add(new CFGCheckBox("Show kin online/offline", CFG.UI_KIN_STATUS), new Coord(x, y));
		y += 25;
		panel.add(new CFGCheckBox("Show timestamps in chat", CFG.UI_CHAT_TIMESTAMP), new Coord(x, y));
		//y += 25;
		//panel.add(new CFGCheckBox("Study lock", CFG.UI_STUDYLOCK), new Coord(x, y));
		y += 25;
		panel.add(new CFGCheckBox("Show percentage above hourglass", CFG.UI_ACTION_PROGRESS_PERCENTAGE), new Coord(x, y));
		y += 25;
		panel.add(new CFGCheckBox("Force long tooltips", CFG.UI_TOOLTIP_LONG), new Coord(x, y));
		y += 25;
		panel.add(new CFGCheckBox("Single item auto choose", CFG.UI_MENU_FLOWER_CLICK_SINGLE, "If checked, will automatically select single item menus if SHIFT is not down when menu is opened"), x, y);
		y += 25;
		panel.add(new CFGLabel("Choose menu items to select automatically"), new Coord(x, y));
		y += 15;
		CFGCheckListbox actionsCheckListbox = new CFGCheckListbox(CFG.UI_MENU_FLOWER_CLICK_AUTO, 200, 15) {
			private DataObserver observer;

			{
				CFG.UI_MENU_FLOWER_CLICK_SINGLE.addObserver(this);
				Globals.Data.addObserver("action", observer = new DataObserver() {
					@Override
					public void dataUpdated(String name) {
						if (!"action".equals(name)) {
							return;
						}
						additems(Globals.Data.get(name));
					}
				});
			}

			@Override
			public void destroy() {
				CFG.UI_MENU_FLOWER_CLICK_SINGLE.remObserver(this);
				Globals.Data.remObserver("action", observer);
				super.destroy();
			}
		};
		actionsCheckListbox.additems(Globals.Data.get("action"));
		panel.add(actionsCheckListbox, new Coord(x, y));

		my = Math.max(my, y);
		x += 225;
		y = 0;

		panel.add(new CFGLabel("Item qualities"), new Coord(x, y));
		y += 15;
		CFGRadioGroup qualityRadioGroup = new CFGRadioGroup(panel);
		int qualityRadioGroupCheckedIndex = CFG.UI_ITEM_QUALITY_SHOW.vali();
		qualityRadioGroup.add("Do not show quality", CFG.UI_ITEM_QUALITY_SHOW, 0, new Coord(x, y));
		y += 15;
		qualityRadioGroup.add("Show single quality as average", CFG.UI_ITEM_QUALITY_SHOW, 1, new Coord(x, y));
		y += 15;
		qualityRadioGroup.add("Show single quality as max", CFG.UI_ITEM_QUALITY_SHOW, 2, new Coord(x, y));
		y += 15;
		qualityRadioGroup.add("Show single quality as Essence", CFG.UI_ITEM_QUALITY_SHOW, 3, new Coord(x, y));
		y += 15;
		qualityRadioGroup.add("Show single quality as Substance", CFG.UI_ITEM_QUALITY_SHOW, 4, new Coord(x, y));
		y += 15;
		qualityRadioGroup.add("Show single quality as Vitality", CFG.UI_ITEM_QUALITY_SHOW, 5, new Coord(x, y));
		y += 15;
		qualityRadioGroup.add("Show all qualities", CFG.UI_ITEM_QUALITY_SHOW, 6, new Coord(x, y));
		qualityRadioGroup.check(qualityRadioGroupCheckedIndex);
		y += 25;
		panel.add(new CFGCheckBox("Show content quality if available (requires restart)", CFG.UI_ITEM_QUALITY_CONTENTS, "If contents quality is available this option uses it instead of the container quality on the item"), new Coord(x, y));
		y += 25;
		panel.add(new CFGCheckBox("Show item wear bar", CFG.UI_ITEM_BAR_WEAR), new Coord(x, y));
		y += 25;
		panel.add(new CFGCheckBox("Show item armor", CFG.UI_ITEM_ARMOR), new Coord(x, y));
		y += 25;
		panel.add(new CFGCheckBox("Show item durability", CFG.UI_ITEM_DURABILITY), new Coord(x, y));
		y += 25;
		panel.add(new CFGCheckBox("Item meter as progress bar", CFG.UI_ITEM_METER_PROGRESSBAR, "If checked all item progress meters be shown as bars at the top of the item icon"), new Coord(x, y));
		y += 25;
		panel.add(new CFGCheckBox("Item meter countdown", CFG.UI_ITEM_METER_COUNTDOWN, "If checked all item progress meters will start full and empty over time"), new Coord(x, y));
		y += 25;
		panel.add(new CFGLabel("Item meter"), new Coord(x, y));
		y += 15;
		panel.add(new CFGHSlider("R", CFG.UI_ITEM_METER_RED, null, 200, 0, 255) {
			{
				displayRawValue = true;
			}
		}, new Coord(x, y));
		y += 15;
		panel.add(new CFGHSlider("G", CFG.UI_ITEM_METER_GREEN, null, 200, 0, 255) {
			{
				displayRawValue = true;
			}
		}, new Coord(x, y));
		y += 15;
		panel.add(new CFGHSlider("B", CFG.UI_ITEM_METER_BLUE, null, 200, 0, 255) {
			{
				displayRawValue = true;
			}
		}, new Coord(x, y));
		y += 15;
		panel.add(new CFGHSlider("A", CFG.UI_ITEM_METER_ALPHA, null, 200, 0, 255) {
			{
				displayRawValue = true;
			}
		}, new Coord(x, y));

		panel.pack();
		x = panel.sz.x > BUTTON_WIDTH ? (panel.sz.x / 2) - (BUTTON_WIDTH / 2) : 0;
		y = panel.sz.y + 35;
		panel.add(new PButton(BUTTON_WIDTH, "Back", 27, panelMain), new Coord(x, y));
		panel.pack();
	}

	private void initVideoPanel(double buttonX, double buttonY) {
		addPanelButton("Video Settings", 'v', panelVideo, buttonX, buttonY);
	}

	public OptWnd() {
		this(true);
	}

	@Override
	public void wdgmsg(Widget sender, String msg, Object... args) {
		if ((sender == this) && (msg == "close")) {
			hide();
		} else {
			super.wdgmsg(sender, msg, args);
		}
	}

	@Override
	public void show() {
		chpanel(panelMain);
		super.show();
	}

	private class CFGButton extends Button implements CFGObserver {

		protected final CFG cfg;

		public CFGButton(String lbl, CFG cfg, int w) {
			this(lbl, cfg, w, null);
		}

		public CFGButton(String lbl, CFG cfg, int w, String tip) {
			super(w, lbl);

			this.cfg = cfg;
			defval();
			if (tip != null) {
				tooltip = Text.render(tip).tex();
			}
		}

		@Override
		public void click() {
		}

		protected void defval() {
		}

		@Override
		public void cfgUpdated(CFG cfg) {
			defval();
		}
	}

	private class CFGCheckBox extends CheckBox implements CFGObserver {

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

		@Override
		public void cfgUpdated(CFG cfg) {
			super.set(cfg.valb());
		}
	}

	private class CFGFlowerList extends FlowerList implements CFGObserver {

		protected final CFG cfg;

		public CFGFlowerList(CFG cfg) {
			this(cfg, new Coord(200, 250));
		}

		public CFGFlowerList(CFG cfg, Coord sz) {
			super(sz);
			this.cfg = cfg;

			Map<String, Boolean> mapVal = cfg.valo();
			Set<String> mapValKeys = mapVal.keySet();
			additems(mapValKeys.toArray(new String[mapValKeys.size()]));
		}

		private boolean cfgVal(String name) {
			Map<String, Boolean> mapVal = cfg.valo();
			return mapVal.containsKey(name) ? mapVal.get(name) : false;
		}

		private void cfgVal(String name, boolean val) {
			Map<String, Boolean> mapVal = cfg.valo();
			mapVal.put(name, val);
			cfg.set(mapVal);
		}

		public class CFGFlowerListItem extends FlowerListItem {

			public CFGFlowerListItem(String name) {
				super(name);
			}

			public CFGFlowerListItem(String name, boolean checked) {
				super(name, checked);
			}
		}

		public final void additems(List<String> names) {
			additems(names.toArray(new String[names.size()]));
		}

		public final void additems(String[] keys) {
			Arrays.sort(keys);
			for (String key : keys) {
				add(key, cfgVal(key));
			}
		}

		@Override
		protected void change(String name, boolean value) {
			super.change(name, value);
			cfgVal(name, value);
		}

		@Override
		protected void remove(String name) {
			super.remove(name);
			Map<String, Boolean> mapVal = cfg.valo();
			mapVal.remove(name);
			cfg.set(mapVal);
		}

		@Override
		public void cfgUpdated(CFG cfg) {
			Map<String, Boolean> mapVal = cfg.valo();
			additems(mapVal.keySet().toArray(new String[mapVal.keySet().size()]));

			for (Widget wdg = cont.lchild; wdg != null; wdg = wdg.prev) {
				FlowerListItem item = ((FlowerListItem) wdg);
				item.set(cfgVal(item.name));
			}
		}
	}

	private class CFGHSlider extends HSlider implements CFGObserver {

		protected final CFG cfg;
		Text lbl;
		protected final float valuePer;
		public boolean displayRawValue = false;

		public CFGHSlider(String lbl, CFG cfg) {
			this(lbl, cfg, null);
		}

		public CFGHSlider(String lbl, CFG cfg, String tip) {
			this(lbl, cfg, tip, 200);
		}

		public CFGHSlider(String lbl, CFG cfg, String tip, int w) {
			this(lbl, cfg, tip, w, 0, 1000);
		}

		public CFGHSlider(String lbl, CFG cfg, String tip, int w, int min, int max) {
			this(lbl, cfg, tip, w, min, max, max);
		}

		public CFGHSlider(String lbl, CFG cfg, String tip, int w, int min, int max, int valuePer) {
			this(lbl, cfg, tip, w, min, max, valuePer, min);
		}

		public CFGHSlider(String lbl, CFG cfg, String tip, int w, int min, int max, int valuepPer, int val) {
			super(w, min, max, val);

			this.cfg = cfg;
			this.valuePer = valuepPer;
			defval();
			if (lbl != null) {
				this.lbl = Text.std.render(lbl, java.awt.Color.WHITE);
			}
			if (tip != null) {
				tooltip = Text.render(tip).tex();
			}
		}

		protected void defval() {
			val = (int) (this.valuePer * cfg.valf());
		}

		@Override
		public void changed() {
			super.changed();
			cfg.set(val / this.valuePer);
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
				drawValue(g);
			} else {
				super.draw(g);
			}
		}

		@Override
		public void drawValue(GOut g) {
			if (displayRawValue) {
				super.drawValue(g);
			} else {
				if (CFG.UI_HSLIDER_VALUE_SHOW.valb()) {
					float v = val / this.valuePer;
					String str = v == (long) v ? String.format("%d", (long) v) : String.format("%.2f", v);
					Tex valueTex = Text.std.renderstroked(str, Color.WHITE, Color.BLACK, new Color(0, 0, 0, 64), new Coord(2, 0)).tex();
					g.image(valueTex, new Coord((sz.x / 2) - (valueTex.sz().x / 2), 0));
				}
			}
		}

		@Override
		public void cfgUpdated(CFG cfg) {
			val = (int) (this.valuePer * cfg.valf());
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

	private class CFGCheckListbox extends CheckListbox implements CFGObserver {

		protected final CFG cfg;
		protected final List<String> keys;

		public CFGCheckListbox(CFG cfg, int w, int h) {
			super(w, h);
			this.cfg = cfg;
			this.keys = new ArrayList<>();

			Map<String, Boolean> mapVal = cfg.valo();
			Set<String> mapValKeys = mapVal.keySet();
			additems(mapValKeys.toArray(new String[mapValKeys.size()]));
		}

		private boolean cfgVal(String name) {
			Map<String, Boolean> mapVal = cfg.valo();
			return mapVal.containsKey(name) ? mapVal.get(name) : false;
		}

		private void cfgVal(String name, boolean val) {
			Map<String, Boolean> mapVal = cfg.valo();
			mapVal.put(name, val);
			cfg.set(mapVal);
		}

		public class CFGCheckListboxItem extends CheckListboxItem {

			public CFGCheckListboxItem(String name, boolean selected) {
				super(name, selected);
			}
		}

		@Override
		protected void itemclick(CheckListboxItem itm, int button) {
			super.itemclick(itm, button);
			cfgVal(itm.name, itm.selected);
		}

		public final void additems(List<String> names) {
			additems(names.toArray(new String[names.size()]));
		}

		public final void additems(String[] names) {
			for (String name : names) {
				additem(name);
			}
			Collections.sort(items);
		}

		public final void additem(String name) {
			if (this.keys.contains(name)) {
				return;
			}
			this.keys.add(name);
			items.add(new CFGCheckListboxItem(name, cfgVal(name)));
			Collections.sort(items);
		}

		@Override
		public void cfgUpdated(CFG cfg) {
			Map<String, Boolean> mapVal = cfg.valo();
			additems(mapVal.keySet().toArray(new String[mapVal.keySet().size()]));
			for (CheckListboxItem item : items) {
				item.selected = cfgVal(item.name);
			}
		}
	}

	private class CFGRadioGroup extends RadioGroup {

		public CFGRadioGroup(Widget parent) {
			super(parent);
		}

		public class CFGRadioButton extends RadioButton implements CFGObserver {

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

			@Override
			public void cfgUpdated(CFG cfg) {
				if (cfg.vali() == cfgVal) {
					check(this);
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
			if (radioButton.cfg.vali() != radioButton.cfgVal) {
				radioButton.cfg.set(radioButton.cfgVal);
			}
		}
	}
}
