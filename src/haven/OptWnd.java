
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

import haven.Globals.Data.DataObserver;
import static haven.UI.mapSaver;
import haven.cfg.CFGCheckBox;
import haven.cfg.CFGCheckListbox;
import haven.cfg.CFGHSlider;
import haven.cfg.CFGLabel;
import haven.cfg.CFGRadioGroup;

public class OptWnd extends Window {

	private static final int BUTTON_WIDTH = 200;
	public static final Coord PANEL_POS = new Coord(220, 30);
	public final Panel panelMain;
	public final Panel panelAudio;
	public final Panel panelCamera;
	public final Panel panelDisplay;
	public final Panel panelDisplayGobPath;
	public final Panel panelGeneral;
	public final Panel panelHotkey;
	public final Panel panelMinimap;
	public final Panel panelMinimapRadarEnder;
	public final Panel panelMinimapRadarRomov;
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

	public class PIButton extends IButton {

		public final Panel tgt;
		public final int key;

		public PIButton(String base, String up, String down, String hover, int key, Panel tgt) {
			super(base, up, down, hover);
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
			super();
			visible = false;
		}

		public Panel(Coord sz) {
			super(sz);
			visible = false;
		}
	}

	public class Pane extends Widget {
	}

	public class StackPane extends Pane {

		private final int padding;
		private final boolean vertical;

		public StackPane() {
			this(0, true);
		}

		public StackPane(boolean vertical) {
			this(0, vertical);
		}

		public StackPane(int padding) {
			this(padding, true);
		}

		public StackPane(int padding, boolean vertical) {
			this.padding = padding;
			this.vertical = vertical;
		}

		public StackPane(Widget[] widgets) {
			this(widgets, 0);
		}

		public StackPane(Widget[] widgets, int padding) {
			this(widgets, padding, true);
		}

		public StackPane(Widget[] widgets, int padding, boolean vertical) {
			this(padding, vertical);

			for (Widget widget : widgets) {
				add(widget);
			}
		}

		@Override
		public <T extends Widget> T add(T child) {
			if (this.child == null) {
				child.c = new Coord(0, 0);
			} else if (vertical) {
				child.c = new Coord(0, sz.y + padding);
			} else {
				child.c = new Coord(sz.x + padding, 0);
			}
			T w = super.add(child);
			pack();
			return w;
		}

		@Override
		public <T extends Widget> T add(T child, Coord c) {
			child.c = c;
			T w = super.add(child);
			pack();
			return w;
		}
	}

	public class VideoPanel extends Panel {

		public VideoPanel() {
			super();

			sz = new Coord(175, 250);
		}

		public class CPanel extends Widget {

			public final GLSettings cf;

			public CPanel(GLSettings gcf) {
				this.cf = gcf;

				StackPane sp = new StackPane(10);
				sp.add(new CheckBox("Per-fragment lighting") {
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
				});
				sp.add(new CheckBox("Render shadows") {
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
				});
				sp.add(new CheckBox("Antialiasing") {
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
				});
				if (cf.anisotex.max() > 1) {
					final Label dpy = new Label("");
					sp.add(new StackPane(new Widget[]{
						new Label("Anisotropic filtering"),
						new StackPane(new Widget[]{
							new HSlider(160, (int) (cf.anisotex.min() * 2), (int) (cf.anisotex.max() * 2), (int) (cf.anisotex.val * 2)) {
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
							},
							dpy,}, 5, false),}));
				}
				add(new StackPane(new Widget[]{
					new CFGLabel("Video Settings", Text.std_big),
					sp,
				}, 15));
				pack();

				adda(new Button(Math.min(sz.x, BUTTON_WIDTH), "Reset to defaults") {
					@Override
					public void click() {
						cf.cfg.resetprefs();
						curcf.destroy();
						curcf = null;
					}
				}, new Coord(sz.x / 2, sz.y + 15), 0.5, 0);
				pack();

				adda(new PButton(Math.min(sz.x, BUTTON_WIDTH), "Back", 27, panelMain), new Coord(sz.x / 2, sz.y + 15), 0.5, 0);
				pack();
			}
		}

		private CPanel curcf = null;

		private void checkcf(GOut g) {
			if ((curcf == null) || (g.gc.pref != curcf.cf)) {
				if (curcf != null) {
					curcf.destroy();
				}
				curcf = add(new CPanel(g.gc.pref), Coord.z);
				pack();
				System.out.println(sz);
			}
		}

		@Override
		public void draw(GOut g) {
			checkcf(g);
			super.draw(g);
		}
	}

	public OptWnd(boolean gopts) {
		super(Coord.z, "Options", true);

		panelMain = add(new Panel());

		panelAudio = add(new Panel());
		panelVideo = add(new VideoPanel());

		panelCamera = add(new Panel());
		panelDisplay = add(new Panel());
		panelDisplayGobPath = add(new Panel());
		panelGeneral = add(new Panel());
		panelHotkey = add(new Panel());
		panelMinimap = add(new Panel());
		panelMinimapRadarEnder = add(new Panel());
		panelMinimapRadarRomov = add(new Panel());
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
				public
								void click() {
					getparent(GameUI.class
					).act("lo", "cs");
				}
			}, getPanelButtonCoord(0, y));
			panelMain.add(new Button(200, "Log out") {
				@Override
				public
								void click() {
					getparent(GameUI.class
					).act("lo");
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

		// sub panels
		initDisplayGobPath();
		initMinimapRadarEnderPanel();
		initMinimapRadarRomovPanel();

		panelMain.pack();
		chpanel(panelMain, false);
	}

	private Coord getPanelButtonCoord(double x, double y) {
		return new Coord((int) ((double) PANEL_POS.x * x), (int) ((double) PANEL_POS.y * y));
	}

	private PButton addPanelButton(String name, char key, Panel panel, double x, double y) {
		return panelMain.add(new PButton(BUTTON_WIDTH, name, key, panel), getPanelButtonCoord(x, y));
	}

	private void initAudioPanel(double buttonX, double buttonY) {
		Panel panel = panelAudio;
		String title = "Audio Settings";
		addPanelButton(title, 'a', panel, buttonX, buttonY);

		panel.add(new StackPane(new Widget[]{
			new CFGLabel(title, Text.std_big),
			new StackPane(new Widget[]{
				new StackPane(new Widget[]{
					new Label("Master audio volume"),
					new HSlider(200, 0, 1000, (int) (Audio.volume * 1000)) {
						@Override
						public void changed() {
							Audio.setvolume(val / 1000.0);
						}
					},}),
				new StackPane(new Widget[]{
					new Label("In-game event volume"),
					new HSlider(200, 0, 1000, 0) {
						@Override
						protected void attach(UI ui) {
							super.attach(ui);
							val = (int) (ui.audio.pos.volume * 1000);
						}

						@Override
						public void changed() {
							ui.audio.pos.setvolume(val / 1000.0);
						}
					},}),
				new StackPane(new Widget[]{
					new Label("Ambient volume"),
					new HSlider(200, 0, 1000, 0) {
						@Override
						protected void attach(UI ui) {
							super.attach(ui);
							val = (int) (ui.audio.amb.volume * 1000);
						}

						@Override
						public void changed() {
							ui.audio.amb.setvolume(val / 1000.0);
						}
					},}),}, 10),}, 15));

		panel.pack();
		panel.adda(new PButton(Math.min(panel.sz.x, BUTTON_WIDTH), "Back", 27, panelMain), new Coord(panel.sz.x / 2, panel.sz.y + 15), 0.5, 0);
		panel.pack();
	}

	private void initCameraPanel(double buttonX, double buttonY) {
		Panel panel = panelCamera;
		String title = "Camera Settings";
		addPanelButton(title, 'c', panel, buttonX, buttonY);

		CFGRadioGroup cameraRadioGroup = new CFGRadioGroup() {
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
						if (ui != null && ui.gui != null && ui.gui.map != null) {
							ui.gui.map.changecamera(newCam);
						}
					}
					if (ui != null && ui.gui != null && ui.gui.map != null) {
						ui.gui.map.camera.release();
					}
				} catch (Exception ex) {
					// usually throws when initially setting up menu
				}
			}
		};
		int cameraRadioGroupCheckedIndex = CFG.CAMERA_TYPE.vali();

		panel.add(new StackPane(new Widget[]{
			new CFGLabel(title, Text.std_big),
			new StackPane(new Widget[]{
				new CFGLabel("Camera type"),
				cameraRadioGroup.add("Default (snap to 45, 135, 225, 315)", CFG.CAMERA_TYPE, 0),
				cameraRadioGroup.add("Default (snap to 0, 90, 180, 270)", CFG.CAMERA_TYPE, 1),
				cameraRadioGroup.add("Default (snap to all 8 directions)", CFG.CAMERA_TYPE, 2),
				cameraRadioGroup.add("Default (no snapping)", CFG.CAMERA_TYPE, 3),
				cameraRadioGroup.add("Follow", CFG.CAMERA_TYPE, 4),
				cameraRadioGroup.add("Free", CFG.CAMERA_TYPE, 5),}),}, 15));
		panel.pack();

		cameraRadioGroup.check(cameraRadioGroupCheckedIndex);

		panel.adda(new PButton(Math.min(panel.sz.x, BUTTON_WIDTH), "Back", 27, panelMain), new Coord(panel.sz.x / 2, panel.sz.y + 15), 0.5, 0);
		panel.pack();
	}

	private void initDisplayPanel(double buttonX, double buttonY) {
		Panel panel = panelDisplay;
		String title = "Display Settings";
		addPanelButton(title, 'd', panel, buttonX, buttonY);

		panel.add(new StackPane(new Widget[]{
			new CFGLabel(title, Text.std_big),
			new StackPane(new Widget[]{
				new StackPane(new Widget[]{
					new StackPane(new Widget[]{
						new CFGLabel("Foreground FPS limit"),
						new CFGHSlider(null, CFG.DISPLAY_FPS_FOREGROUND, null, 200, 1, 144, 1),}),
					new StackPane(new Widget[]{
						new CFGLabel("Background FPS limit"),
						new CFGHSlider(null, CFG.DISPLAY_FPS_BACKGROUND, null, 200, 1, 144, 1),}),
					new CFGCheckBox("Show dynamic lighting", CFG.DISPLAY_LIGHTING_DYNAMIC),
					new CFGCheckBox("Show weather", CFG.DISPLAY_WEATHER),
					new CFGCheckBox("Show flavor objects", CFG.DISPLAY_FLAVOR),
					new CFGCheckBox("Show simple crops (requires restart)", CFG.DISPLAY_CROPS_SIMPLE),
					new CFGCheckBox("Show simple foragables (requires restart)", CFG.DISPLAY_FORAGABLES_SIMPLE),
					new CFGCheckBox("Show plant growth", CFG.DISPLAY_PLANT_GROWTH) {
						{
							CFG.DISPLAY_PLANT_GROWTH.addObserver(this);
						}

						@Override
						public void destroy() {
							CFG.DISPLAY_PLANT_GROWTH.remObserver(this);
							super.destroy();
						}
					},}, 10),
				new StackPane(new Widget[]{
					new StackPane(new Widget[]{
						new CFGLabel("Brighten view"),
						new CFGHSlider(null, CFG.DISPLAY_BRIGHTNESS) {
							@Override
							public void changed() {
								super.changed();
								if (ui.sess != null && ui.sess.glob != null) {
									ui.sess.glob.brighten();
								}
							}
						},}
					), new StackPane(new Widget[]{
						new CFGCheckBox("Display grid", CFG.DISPLAY_GRID_SHOW) {
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
						},
						new CFGLabel("Grid thickness"),
						new CFGHSlider(null, CFG.DISPLAY_GRID_THICKNESS, null, 200, 1, 3, 1),}),
					new CFGCheckBox("Always show kin names", CFG.DISPLAY_KIN_NAMES),
					new CFGCheckBox("Show object damage", CFG.DISPLAY_OBJECT_DAMAGE) {
						{
							CFG.DISPLAY_OBJECT_DAMAGE.addObserver(this);
						}

						@Override
						public void destroy() {
							CFG.DISPLAY_OBJECT_DAMAGE.remObserver(this);
							super.destroy();
						}
					},
					new CFGCheckBox("Show object radius", CFG.DISPLAY_OBJECT_RADIUS) {
						{
							CFG.DISPLAY_OBJECT_RADIUS.addObserver(this);
						}

						@Override
						public void destroy() {
							CFG.DISPLAY_OBJECT_RADIUS.remObserver(this);
							super.destroy();
						}
					},
					new StackPane(new Widget[]{
						new StackPane(new Widget[]{
							new CFGCheckBox("Show gob paths", CFG.DISPLAY_PATH_GOB),
							new PIButton("gfx/hud/opt", "", "-d", "-h", -1, panelDisplayGobPath),}, 5, false),
						new CFGLabel("Path thickness"),
						new CFGHSlider(null, CFG.DISPLAY_PATH_THICKNESS, null, 200, 1, 3, 1),}),}, 10),}, 25, false),}, 15));
		panel.pack();

		panel.adda(new PButton(Math.min(panel.sz.x, BUTTON_WIDTH), "Back", 27, panelMain), new Coord(panel.sz.x / 2, panel.sz.y + 15), 0.5, 0);
		panel.pack();
	}

	private void initDisplayGobPath() {
		Panel panel = panelDisplayGobPath;
		String title = "Display Settings - Gob Path";

		final WidgetList<GobPathCFG.PathCheck> paths = new WidgetList<GobPathCFG.PathCheck>(new Coord(200, 16), 20) {
			@Override
			protected void itemclick(GobPathCFG.PathCheck item, int button) {
				if (button == 1) {
					item.set(!item.a);
				}
			}
		};
		paths.canselect = false;

		final WidgetList<GobPathCFG.GroupCheck> groups = new WidgetList<GobPathCFG.GroupCheck>(new Coord(200, 16), 20) {
			@Override
			public void selected(GobPathCFG.GroupCheck item) {
				paths.clear();
				for (GobPathCFG.PathCFG path : item.group.pathCFGs) {
					paths.additem(new GobPathCFG.PathCheck(path));
				}
			}
		};
		for (GobPathCFG.Group group : GobPathCFG.groups) {
			groups.additem(new GobPathCFG.GroupCheck(group)).hitbox = true;
		}

		GobPathCFG.addObserver(new GobPathCFG.GobPathCFGObserver() {
			@Override
			public void cfgUpdated() {
				groups.clear();
				paths.clear();
				for (GobPathCFG.Group group : GobPathCFG.groups) {
					groups.additem(new GobPathCFG.GroupCheck(group)).hitbox = true;
				}
			}
		});

		panel.add(new StackPane(new Widget[]{
			new CFGLabel(title, Text.std_big),
			new StackPane(new Widget[]{
				groups,
				paths,}, 10, false),}, 15));
		panel.pack();

		panel.adda(new Button(60, "Save") {
			@Override
			public void click() {
				GobPathCFG.saveConfig();
			}
		}, new Coord(panel.sz.x / 2, panel.sz.y + 15), 0.5, 0);
		panel.pack();

		panel.adda(new PButton(Math.min(panel.sz.x, BUTTON_WIDTH), "Back", 27, panelDisplay), new Coord(panel.sz.x / 2, panel.sz.y + 15), 0.5, 0);
		panel.pack();
	}

	private void initGeneralPanel(double buttonX, double buttonY) {
		Panel panel = panelGeneral;
		String title = "General Settings";
		addPanelButton(title, 'g', panel, buttonX, buttonY);

		panel.add(new StackPane(new Widget[]{
			new CFGLabel(title, Text.std_big),
			new StackPane(new Widget[]{
				new CFGCheckBox("Store general game data", CFG.GENERAL_DATA_SAVE, "Data is stored in 'data' folder and is used to automaticaly update options menus, but can make the game laggy"),
				new CFGCheckBox("Store chat logs", CFG.GENERAL_CHAT_SAVE, "Logs are stored in 'chat' folder"),
				new CFGCheckBox("Store minimap tiles", CFG.GENERAL_MAP_SAVE, "Tiles are stored in 'map' folder") {
					@Override
					public void changed(boolean val) {
						super.changed(val);
						if (val && UI.mapSaver == null) {
							mapSaver = new MapSaver(ui);
						}
					}
				},}, 10),}, 15));
		panel.pack();

		panel.adda(new PButton(Math.min(panel.sz.x, BUTTON_WIDTH), "Back", 27, panelMain), new Coord(panel.sz.x / 2, panel.sz.y + 15), 0.5, 0);
		panel.pack();
	}

	private void initHotkeyPanel(double buttonX, double buttonY) {
		Panel panel = panelHotkey;
		String title = "Hotkey Settings";
		addPanelButton(title, 'h', panel, buttonX, buttonY);

		panel.add(new StackPane(new Widget[]{
			new CFGLabel(title, Text.std_big),
			new StackPane(new Widget[]{
				new CFGCheckBox("Mouse follow", CFG.HOTKEY_MOUSE_FOLLOW, "If enabled holding LMB or using ALT+F will make player follow mouse"),
				new StackPane(new Widget[]{
					new CFGLabel("Show all qualities", "Multiple selections means ANY key must be pressed to activate"),
					new CFGCheckBox("SHIFT", CFG.HOTKEY_ITEM_QUALITY) {
						@Override
						protected void defval() {
							a = Utils.checkbit(cfg.vali(), 0);
						}

						@Override
						public void set(boolean a) {
							this.a = a;
							cfg.set(Utils.setbit(cfg.vali(), 0, a));

						}
					},
					new CFGCheckBox("CTRL", CFG.HOTKEY_ITEM_QUALITY) {
						@Override
						protected void defval() {
							a = Utils.checkbit(cfg.vali(), 1);
						}

						@Override
						public void set(boolean a) {
							this.a = a;
							cfg.set(Utils.setbit(cfg.vali(), 1, a));
						}
					},
					new CFGCheckBox("ALT", CFG.HOTKEY_ITEM_QUALITY) {
						@Override
						protected void defval() {
							a = Utils.checkbit(cfg.vali(), 2);
						}

						@Override
						public void set(boolean a) {
							this.a = a;
							cfg.set(Utils.setbit(cfg.vali(), 2, a));

						}
					},}),
				new StackPane(new Widget[]{
					new CFGLabel("Transfer items / Stockpile transfer items in", "Multiple selections means ALL keys must be pressed to activate"),
					new CFGCheckBox("SHIFT", CFG.HOTKEY_ITEM_TRANSFER_IN) {
						@Override
						protected void defval() {
							a = Utils.checkbit(cfg.vali(), 0);
						}

						@Override
						public void set(boolean a) {
							this.a = a;
							cfg.set(Utils.setbit(cfg.vali(), 0, a));
						}
					},
					new CFGCheckBox("CTRL", CFG.HOTKEY_ITEM_TRANSFER_IN) {
						@Override
						protected void defval() {
							a = Utils.checkbit(cfg.vali(), 1);
						}

						@Override
						public void set(boolean a) {
							this.a = a;
							cfg.set(Utils.setbit(cfg.vali(), 1, a));
						}
					},
					new CFGCheckBox("ALT", CFG.HOTKEY_ITEM_TRANSFER_IN) {
						@Override
						protected void defval() {
							a = Utils.checkbit(cfg.vali(), 2);
						}

						@Override
						public void set(boolean a) {
							this.a = a;
							cfg.set(Utils.setbit(cfg.vali(), 2, a));
						}
					},}),
				new StackPane(new Widget[]{
					new CFGLabel("Drop items / Stockpile transfer items out", "Multiple selections means ALL keys must be pressed to activate"),
					new CFGCheckBox("SHIFT", CFG.HOTKEY_ITEM_TRANSFER_OUT) {
						@Override
						protected void defval() {
							a = Utils.checkbit(cfg.vali(), 0);
						}

						@Override
						public void set(boolean a) {
							this.a = a;
							cfg.set(Utils.setbit(cfg.vali(), 0, a));

						}
					},
					new CFGCheckBox("CTRL", CFG.HOTKEY_ITEM_TRANSFER_OUT) {
						@Override
						protected void defval() {
							a = Utils.checkbit(cfg.vali(), 1);
						}

						@Override
						public void set(boolean a) {
							this.a = a;
							cfg.set(Utils.setbit(cfg.vali(), 1, a));
						}
					},
					new CFGCheckBox("ALT", CFG.HOTKEY_ITEM_TRANSFER_OUT) {
						@Override
						protected void defval() {
							a = Utils.checkbit(cfg.vali(), 2);
						}

						@Override
						public void set(boolean a) {
							this.a = a;
							cfg.set(Utils.setbit(cfg.vali(), 2, a));
						}
					},}),}, 10),}, 15));
		panel.pack();

		panel.adda(new PButton(Math.min(panel.sz.x, BUTTON_WIDTH), "Back", 27, panelMain), new Coord(panel.sz.x / 2, panel.sz.y + 15), 0.5, 0);
		panel.pack();
	}

	private void initMinimapPanel(double buttonX, double buttonY) {
		Panel panel = panelMinimap;
		String title = "Minimap Settings";
		addPanelButton(title, 'm', panel, buttonX, buttonY);

		CFGRadioGroup radarRadioGroup = new CFGRadioGroup();
		int radarRadioGroupCheckedIndex = CFG.MINIMAP_RADAR.vali();

		panel.add(new StackPane(new Widget[]{
			new CFGLabel(title, Text.std_big),
			new StackPane(new Widget[]{
				new CFGCheckBox("Undock minimap", CFG.MINIMAP_FLOATING) {
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
				},
				new CFGCheckBox("Show biomes on minimap", CFG.MINIMAP_BIOME_SHOW, "If mouse is over minimap it shows name of biome it is over, otherwise it shows biome player is in"),
				new StackPane(new Widget[]{
					new CFGLabel("Radar type"),
					radarRadioGroup.add("Default", CFG.MINIMAP_RADAR, 0),
					new StackPane(new Widget[]{
						radarRadioGroup.add("romov", CFG.MINIMAP_RADAR, 1),
						new PIButton("gfx/hud/opt", "", "-d", "-h", -1, panelMinimapRadarRomov),}, 5, false),
					new StackPane(new Widget[]{
						radarRadioGroup.add("ender", CFG.MINIMAP_RADAR, 2),
						new PIButton("gfx/hud/opt", "", "-d", "-h", -1, panelMinimapRadarEnder),}, 5, false),}),}, 10),}, 15));
		panel.pack();

		radarRadioGroup.check(radarRadioGroupCheckedIndex);

		panel.adda(new PButton(Math.min(panel.sz.x, BUTTON_WIDTH), "Back", 27, panelMain), new Coord(panel.sz.x / 2, panel.sz.y + 15), 0.5, 0);
		panel.pack();
	}

	private void initMinimapRadarEnderPanel() {
		Panel panel = panelMinimapRadarEnder;
		String title = "Minimap Settings - Radar - ender";

		final WidgetList<RadarCFG.MarkerCheck> markers = new WidgetList<RadarCFG.MarkerCheck>(new Coord(200, 16), 20) {
			@Override
			protected void itemclick(RadarCFG.MarkerCheck item, int button) {
				if (button == 1) {
					item.set(!item.a);
				}
			}
		};
		markers.canselect = false;

		final WidgetList<RadarCFG.GroupCheck> groups = new WidgetList<RadarCFG.GroupCheck>(new Coord(200, 16), 20) {
			@Override
			public void selected(RadarCFG.GroupCheck item) {
				markers.clear();
				for (RadarCFG.MarkerCFG marker : item.group.markerCFGs) {
					markers.additem(new RadarCFG.MarkerCheck(marker));
				}
			}
		};
		for (RadarCFG.Group group : RadarCFG.groups) {
			groups.additem(new RadarCFG.GroupCheck(group)).hitbox = true;
		}

		panel.add(new StackPane(new Widget[]{
			new CFGLabel(title, Text.std_big),
			new StackPane(new Widget[]{
				groups,
				markers,}, 10, false),}, 15));
		panel.pack();

		panel.adda(new Button(60, "Save") {
			@Override
			public void click() {
				RadarCFG.saveConfig();
			}
		}, new Coord(panel.sz.x / 2, panel.sz.y + 15), 0.5, 0);

		panel.pack();
		panel.adda(new PButton(Math.min(panel.sz.x, BUTTON_WIDTH), "Back", 27, panelMinimap), new Coord(panel.sz.x / 2, panel.sz.y + 35), 0.5, 0);
		panel.pack();
	}

	private void initMinimapRadarRomovPanel() {
		Panel panel = panelMinimapRadarRomov;
		String title = "Minimap Settings - Radar - romov";

		panel.add(new StackPane(new Widget[]{
			new CFGLabel(title, Text.std_big),
			new StackPane(new Widget[]{
				new CFGCheckBox("Show players on minimap", CFG.MINIMAP_PLAYERS),
				new StackPane(new Widget[]{
					new StackPane(new Widget[]{
						new CFGLabel("Show bumlings"),
						new CFGCheckListbox(CFG.MINIMAP_BUMLINGS, 130, 10) {
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
						},}),
					new StackPane(new Widget[]{
						new CFGLabel("Show bushes"),
						new CFGCheckListbox(CFG.MINIMAP_BUSHES, 130, 10) {
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
						},}),
					new StackPane(new Widget[]{
						new CFGLabel("Show trees"),
						new CFGCheckListbox(CFG.MINIMAP_TREES, 130, 10) {
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
						},}),}, 5, false),}, 10),}, 15));
		panel.pack();

		panel.adda(new PButton(Math.min(panel.sz.x, BUTTON_WIDTH), "Back", 27, panelMinimap), new Coord(panel.sz.x / 2, panel.sz.y + 15), 0.5, 0);
		panel.pack();
	}

	private void initUIPanel(double buttonX, double buttonY) {
		Panel panel = panelUI;
		String title = "UI Settings";
		addPanelButton(title, 'u', panel, buttonX, buttonY);

		CFGRadioGroup qualityRadioGroup = new CFGRadioGroup();
		int qualityRadioGroupCheckedIndex = CFG.UI_ITEM_QUALITY_SHOW.vali();
		CFGRadioGroup meterRadioGroup = new CFGRadioGroup();
		int meterRadioGroupCheckedIndex = CFG.UI_ITEM_METER_SHOW.vali();

		panel.add(new StackPane(new Widget[]{
			new CFGLabel(title, Text.std_big),
			new StackPane(new Widget[]{
				new StackPane(new Widget[]{
					new CFGCheckBox("Show hslider values", CFG.UI_HSLIDER_VALUE_SHOW),
					new CFGCheckBox("Show kin online/offline", CFG.UI_KIN_STATUS),
					new CFGCheckBox("Show timestamps in chat", CFG.UI_CHAT_TIMESTAMP),
					//new CFGCheckBox("Study lock", CFG.UI_STUDYLOCK),
					new CFGCheckBox("Show percentage above hourglass", CFG.UI_ACTION_PROGRESS_PERCENTAGE),
					new CFGCheckBox("Force long tooltips", CFG.UI_TOOLTIP_LONG),
					new CFGCheckBox("Single item auto choose", CFG.UI_MENU_FLOWER_CLICK_SINGLE, "If checked, will automatically select single item menus if SHIFT is not down when menu is opened"),
					new StackPane(new Widget[]{
						new CFGLabel("Choose menu items to select automatically"),
						new CFGCheckListbox(CFG.UI_MENU_FLOWER_CLICK_AUTO, 200, 15) {
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

								observer.dataUpdated("action");
							}

							@Override
							public void destroy() {
								CFG.UI_MENU_FLOWER_CLICK_SINGLE.remObserver(this);
								Globals.Data.remObserver("action", observer);
								super.destroy();
							}
						},}),}, 10),
				new StackPane(new Widget[]{
					new StackPane(new Widget[]{
						new CFGLabel("Item qualities"),
						qualityRadioGroup.add("Do not show quality", CFG.UI_ITEM_QUALITY_SHOW, 0),
						qualityRadioGroup.add("Show single quality as average", CFG.UI_ITEM_QUALITY_SHOW, 1),
						qualityRadioGroup.add("Show single quality as max", CFG.UI_ITEM_QUALITY_SHOW, 2),
						qualityRadioGroup.add("Show single quality as Essence", CFG.UI_ITEM_QUALITY_SHOW, 3),
						qualityRadioGroup.add("Show single quality as Substance", CFG.UI_ITEM_QUALITY_SHOW, 4),
						qualityRadioGroup.add("Show single quality as Vitality", CFG.UI_ITEM_QUALITY_SHOW, 5),
						qualityRadioGroup.add("Show all qualities", CFG.UI_ITEM_QUALITY_SHOW, 6),}),
					new CFGCheckBox("Swap item quality and number locations", CFG.UI_ITEM_QUALITY_SWAP),
					new CFGCheckBox("Show content quality if available (requires restart)", CFG.UI_ITEM_QUALITY_CONTENTS, "If contents quality is available this option uses it instead of the container quality on the item"),
					new CFGCheckBox("Show item wear bar", CFG.UI_ITEM_BAR_WEAR),
					new CFGCheckBox("Show item armor", CFG.UI_ITEM_ARMOR),
					new CFGCheckBox("Show item durability", CFG.UI_ITEM_DURABILITY),
					new StackPane(new Widget[]{
						new CFGLabel("Item meter"),
						meterRadioGroup.add("Do not show meter", CFG.UI_ITEM_METER_SHOW, 0),
						meterRadioGroup.add("Show default meter", CFG.UI_ITEM_METER_SHOW, 1),
						meterRadioGroup.add("Show meter as progress bar", CFG.UI_ITEM_METER_SHOW, 2),
						meterRadioGroup.add("Show meter as number", CFG.UI_ITEM_METER_SHOW, 3),}),
					new CFGCheckBox("Item meter countdown", CFG.UI_ITEM_METER_COUNTDOWN, "If checked all item progress meters will start full and empty over time"),
					new StackPane(new Widget[]{
						new CFGLabel("Item meter"),
						new CFGHSlider("R", CFG.UI_ITEM_METER_RED, null, 200, 0, 255) {
							{
								displayRawValue = true;
							}
						},
						new CFGHSlider("G", CFG.UI_ITEM_METER_GREEN, null, 200, 0, 255) {
							{
								displayRawValue = true;
							}
						},
						new CFGHSlider("B", CFG.UI_ITEM_METER_BLUE, null, 200, 0, 255) {
							{
								displayRawValue = true;
							}
						},
						new CFGHSlider("A", CFG.UI_ITEM_METER_ALPHA, null, 200, 0, 255) {
							{
								displayRawValue = true;
							}
						},}),}, 10),}, 25, false),}, 15));
		panel.pack();

		qualityRadioGroup.check(qualityRadioGroupCheckedIndex);
		meterRadioGroup.check(meterRadioGroupCheckedIndex);

		panel.adda(new PButton(Math.min(panel.sz.x, BUTTON_WIDTH), "Back", 27, panelMain), new Coord(panel.sz.x / 2, panel.sz.y + 15), 0.5, 0);
		panel.pack();
	}

	private void initVideoPanel(double buttonX, double buttonY) {
		Panel panel = panelVideo;
		addPanelButton("Video Settings", 'v', panel, buttonX, buttonY);
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
}
