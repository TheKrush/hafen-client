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

import java.util.*;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.image.WritableRaster;
import static haven.Inventory.invsq;
import java.awt.image.BufferedImage;

public class GameUI extends ConsoleHost implements Console.Directory {

	public static final Text.Foundry msgfoundry = new Text.Foundry(Text.dfont, 14);
	private static int blpw = 142, brpw = 142;
	public final String chrid;
	public final long plid;
	private final Hidepanel ulpanel, urpanel, blpanel, brpanel, menupanel;
	public Avaview portrait;
	public MenuGrid menu;
	public MapView map;
	public LocalMiniMap mmap;
	public MiniMapPanel mmappanel;
	public Fightview fv;
	private List<Widget> meters = new LinkedList<>();
	private List<Widget> cmeters = new LinkedList<>();
	private Text lastmsg;
	private long msgtime;
	private Window invwnd, equwnd;
	private CraftWindow makewnd;
	public CraftDBWnd craftwnd = null;
	public ActWindow craftlist, buildlist;
	public TimerPanel timers;
	public StudyWnd studywnd;
	public Inventory maininv;
	public CharWnd chrwdg;
	public BuddyWnd buddies;
	public EquipProxy eqproxy;
	private final Zergwnd zerg;
	public Polity polity;
	public HelpWnd help;
	public OptWnd opts;
	public Collection<DraggedItem> hand = new LinkedList<>();
	private Collection<DraggedItem> handSave = new LinkedList<>();
	private WItem vhand;
	public ChatUI chat;
	public ChatUI.Channel syslog;
	public double prog = -1;
	private boolean afk = false;
	@SuppressWarnings("unchecked")
	public Indir<Resource>[] belt = new Indir[144];
	public Belt beltwdg;
	public String polowner;
	public Bufflist buffs;

	private final Label timelbl;
	public boolean dewyTime = false;

	public abstract class Belt extends Widget {

		public Belt(Coord sz) {
			super(sz);
		}

		public void keyact(final int slot) {
			if (map != null) {
				Coord mvc = map.rootxlate(ui.mc);
				if (mvc.isect(Coord.z, map.sz)) {
					map.delay(map.new Hittest(mvc) {
						@Override
						protected void hit(Coord pc, Coord mc, MapView.ClickInfo inf) {
							if (inf == null) {
								GameUI.this.wdgmsg("belt", slot, 1, ui.modflags(), mc);
							} else {
								GameUI.this.wdgmsg("belt", slot, 1, ui.modflags(), mc, (int) inf.gob.id, inf.gob.rc);
							}
						}

						@Override
						protected void nohit(Coord pc) {
							GameUI.this.wdgmsg("belt", slot, 1, ui.modflags());
						}
					});
				}
			}
		}
	}

	@RName("gameui")
	public static class $_ implements Factory {

		@Override
		public Widget create(Widget parent, Object[] args) {
			String chrid = (String) args[0];
			int plid = (Integer) args[1];
			return (new GameUI(chrid, plid));
		}
	}

	public GameUI(String chrid, long plid) {
		this.chrid = chrid;
		this.plid = plid;
		setcanfocus(true);
		setfocusctl(true);
		chat = add(new ChatUI(0, 0));
		timelbl = add(new Label("", 20, new Text.Foundry(Text.mono, 15)));
		if (Utils.getprefb("chatvis", true)) {
			chat.hresize(chat.savedh);
			chat.show();
		}
		beltwdg.raise();
		ulpanel = add(new Hidepanel("gui-ul", null, new Coord(-1, -1)));
		urpanel = add(new Hidepanel("gui-ur", null, new Coord(1, -1)));
		blpanel = add(new Hidepanel("gui-bl", null, new Coord(-1, 1)));
		brpanel = add(new Hidepanel("gui-br", null, new Coord(1, 1)) {
			@Override
			public void move(double a) {
				super.move(a);
				menupanel.move();
			}
		});
		menupanel = add(new Hidepanel("menu", new Indir<Coord>() {
			@Override
			public Coord get() {
				return (new Coord(GameUI.this.sz.x, Math.min(brpanel.c.y - 79, GameUI.this.sz.y - menupanel.sz.y)));
			}
		}, new Coord(1, 0)));
		blpanel.add(new Img(Resource.loadtex("gfx/hud/blframe")), 0, 9);
		blpanel.add(new Img(Resource.loadtex("gfx/hud/lbtn-bg")), 0, 0);
		menu = brpanel.add(new MenuGrid(), 20, 34);
		brpanel.add(new Img(Resource.loadtex("gfx/hud/brframe")), 0, 0);
		menupanel.add(new MainMenu(), 0, 0);
		mapbuttons();
		foldbuttons();
		portrait = ulpanel.add(new Avaview(Avaview.dasz, plid, "avacam") {
			@Override
			public boolean mousedown(Coord c, int button) {
				return (true);
			}
		}, new Coord(10, 10));
		buffs = ulpanel.add(new Bufflist(), new Coord(95, 65));
		eqproxy = ulpanel.add(new EquipProxy(new int[]{6, 7, 11}), new Coord(420, 5));
		syslog = chat.add(new ChatUI.Log("System"));
		opts = add(new OptWnd());
		opts.hide();
		zerg = add(new Zergwnd(), 187, 50);
		zerg.hide();
		showmmappanel(CFG.MINIMAP_FLOATING.valb());
	}

	private void mapbuttons() {
		blpanel.add(new IButton("gfx/hud/lbtn-vil", "", "-d", "-h") {
			{
				tooltip = Text.render("Display personal claims");
			}

			@Override
			public void click() {
				if ((map != null) && !map.visol(0)) {
					map.enol(0, 1);
				} else {
					map.disol(0, 1);
				}
			}
		}, 0, 0);
		blpanel.add(new IButton("gfx/hud/lbtn-claim", "", "-d", "-h") {
			{
				tooltip = Text.render("Display village claims");
			}

			@Override
			public void click() {
				if ((map != null) && !map.visol(2)) {
					map.enol(2, 3);
				} else {
					map.disol(2, 3);
				}
			}
		}, 0, 0);
	}

	@Override
	protected void attach(UI ui) {
		super.attach(ui);
		ui.gui = this;
		super.attach(ui);
		timers = add(new TimerPanel(), 250, 100);
	}

	@Override
	public void destroy() {
		super.destroy();
		ui.gui = null;
	}

	public Equipory getEquipory() {
		if (equwnd != null) {
			Iterator<Equipory> iterator = equwnd.children(Equipory.class).iterator();
			if (iterator.hasNext()) {
				return iterator.next();
			}
		}
		return null;
	}

	/* Ice cream */
	private final IButton[] fold_br = new IButton[4];
	private final IButton[] fold_bl = new IButton[2];

	private void updfold(boolean reset) {
		int br;
		if (brpanel.tvis && menupanel.tvis) {
			br = 0;
		} else if (brpanel.tvis && !menupanel.tvis) {
			br = 1;
		} else if (!brpanel.tvis && !menupanel.tvis) {
			br = 2;
		} else {
			br = 3;
		}
		for (int i = 0; i < fold_br.length; i++) {
			fold_br[i].show(i == br);
		}

		fold_bl[1].show(!blpanel.tvis);

		if (reset) {
			resetui();
		}
	}

	private void foldbuttons() {
		final Tex rdnbg = Resource.loadtex("gfx/hud/rbtn-maindwn");
		final Tex rupbg = Resource.loadtex("gfx/hud/rbtn-upbg");
		fold_br[0] = new IButton("gfx/hud/rbtn-dwn", "", "-d", "-h") {
			@Override
			public void draw(GOut g) {
				g.image(rdnbg, Coord.z);
				super.draw(g);
			}

			@Override
			public void click() {
				menupanel.cshow(false);
				updfold(true);
			}
		};
		fold_br[1] = new IButton("gfx/hud/rbtn-dwn", "", "-d", "-h") {
			@Override
			public void draw(GOut g) {
				g.image(rdnbg, Coord.z);
				super.draw(g);
			}

			@Override
			public void click() {
				brpanel.cshow(false);
				updfold(true);
			}
		};
		fold_br[2] = new IButton("gfx/hud/rbtn-up", "", "-d", "-h") {
			@Override
			public void draw(GOut g) {
				g.image(rupbg, Coord.z);
				super.draw(g);
			}

			@Override
			public void click() {
				menupanel.cshow(true);
				updfold(true);
			}

			@Override
			public void presize() {
				this.c = parent.sz.sub(this.sz);
			}
		};
		fold_br[3] = new IButton("gfx/hud/rbtn-dwn", "", "-d", "-h") {
			@Override
			public void draw(GOut g) {
				g.image(rdnbg, Coord.z);
				super.draw(g);
			}

			@Override
			public void click() {
				brpanel.cshow(true);
				updfold(true);
			}
		};
		menupanel.add(fold_br[0], 0, 0);
		fold_br[0].lower();
		brpanel.adda(fold_br[1], brpanel.sz.x, 32, 1, 1);
		adda(fold_br[2], 1, 1);
		fold_br[2].lower();
		menupanel.add(fold_br[3], 0, 0);
		fold_br[3].lower();

		final Tex lupbg = Resource.loadtex("gfx/hud/lbtn-upbg");
		fold_bl[0] = new IButton("gfx/hud/lbtn-dwn", "", "-d", "-h") {
			@Override
			public void click() {
				blpanel.cshow(false);
				updfold(true);
			}
		};
		fold_bl[1] = new IButton("gfx/hud/lbtn-up", "", "-d", "-h") {
			@Override
			public void draw(GOut g) {
				g.image(lupbg, Coord.z);
				super.draw(g);
			}

			@Override
			public void click() {
				blpanel.cshow(true);
				updfold(true);
			}

			@Override
			public void presize() {
				this.c = new Coord(0, parent.sz.y - sz.y);
			}
		};
		blpanel.add(fold_bl[0], 0, 0);
		adda(fold_bl[1], 0, 1);
		fold_bl[1].lower();

		updfold(false);
	}

	@Override
	protected void added() {
		resize(parent.sz);
		ui.cons.out = new java.io.PrintWriter(new java.io.Writer() {
			StringBuilder buf = new StringBuilder();

			@Override
			public void write(char[] src, int off, int len) {
				buf.append(src, off, len);
				int p;
				while ((p = buf.indexOf("\n")) >= 0) {
					syslog.append(buf.substring(0, p), Color.WHITE);
					buf.delete(0, p + 1);
				}
			}

			@Override
			public void close() {
			}

			@Override
			public void flush() {
			}
		});
		Debug.log = ui.cons.out;
		opts.c = sz.sub(opts.sz).div(2);
	}

	public void toggleCraftList() {
		if (craftlist == null) {
			add(new ActWindow("Craft...", "paginae/craft/"));
		} else if (craftlist.visible) {
			craftlist.hide();
		} else {
			craftlist.show();
		}
	}

	public void toggleBuildList() {
		if (buildlist == null) {
			add(new ActWindow("Build...", "paginae/bld/"));
		} else if (buildlist.visible) {
			buildlist.hide();
		} else {
			buildlist.show();
		}
	}

	public class Hidepanel extends Widget {

		public final String id;
		public final Coord g;
		public final Indir<Coord> base;
		public boolean tvis;
		private double cur;

		public Hidepanel(String id, Indir<Coord> base, Coord g) {
			this.id = id;
			this.base = base;
			this.g = g;
			cur = show(tvis = Utils.getprefb(id + "-visible", true)) ? 0 : 1;
		}

		@Override
		public <T extends Widget> T add(T child) {
			super.add(child);
			pack();
			if (parent != null) {
				move();
			}
			return (child);
		}

		public Coord base() {
			if (base != null) {
				return (base.get());
			}
			return (new Coord((g.x > 0) ? parent.sz.x : 0,
							(g.y > 0) ? parent.sz.y : 0));
		}

		public void move(double a) {
			cur = a;
			Coord c = new Coord(base());
			if (g.x < 0) {
				c.x -= (int) (sz.x * a);
			} else if (g.x > 0) {
				c.x -= (int) (sz.x * (1 - a));
			}
			if (g.y < 0) {
				c.y -= (int) (sz.y * a);
			} else if (g.y > 0) {
				c.y -= (int) (sz.y * (1 - a));
			}
			this.c = c;
		}

		public void move() {
			move(cur);
		}

		@Override
		public void presize() {
			move();
		}

		public boolean mshow(final boolean vis) {
			clearanims(Anim.class);
			if (vis) {
				show();
			}
			new NormAnim(0.25) {
				final double st = cur, f = vis ? 0 : 1;

				@Override
				public void ntick(double a) {
					if ((a == 1.0) && !vis) {
						hide();
					}
					move(st + (Utils.smoothstep(a) * (f - st)));
				}
			};
			tvis = vis;
			updfold(false);
			return (vis);
		}

		public boolean mshow() {
			return (mshow(Utils.getprefb(id + "-visible", true)));
		}

		public boolean cshow(boolean vis) {
			Utils.setprefb(id + "-visible", vis);
			if (vis != tvis) {
				mshow(vis);
			}
			return (vis);
		}

		@Override
		public void cdestroy(Widget w) {
			parent.cdestroy(w);
		}
	}

	static class Hidewnd extends Window {

		Hidewnd(Coord sz, String cap, boolean lg) {
			super(sz, cap, lg);
		}

		Hidewnd(Coord sz, String cap) {
			super(sz, cap);
		}

		@Override
		public void wdgmsg(Widget sender, String msg, Object... args) {
			if ((sender == this) && msg.equals("close")) {
				this.hide();
				return;
			}
			super.wdgmsg(sender, msg, args);
		}
	}

	static class Zergwnd extends Hidewnd {

		Tabs tabs = new Tabs(Coord.z, Coord.z, this);
		final TButton kin, pol;

		class TButton extends IButton {

			Tabs.Tab tab = null;
			final Tex inv;

			TButton(String nm, boolean g) {
				super(Resource.loadimg("gfx/hud/buttons/" + nm + "u"), Resource.loadimg("gfx/hud/buttons/" + nm + "d"));
				if (g) {
					inv = Resource.loadtex("gfx/hud/buttons/" + nm + "g");
				} else {
					inv = null;
				}
			}

			@Override
			public void draw(GOut g) {
				if ((tab == null) && (inv != null)) {
					g.image(inv, Coord.z);
				} else {
					super.draw(g);
				}
			}

			@Override
			public void click() {
				if (tab != null) {
					tabs.showtab(tab);
					repack();
				}
			}
		}

		Zergwnd() {
			super(Coord.z, "Kith & Kin", true);
			kin = add(new TButton("kin", false));
			kin.tooltip = Text.render("Kin");
			pol = add(new TButton("pol", true));
		}

		private void repack() {
			tabs.indpack();
			kin.c = new Coord(0, tabs.curtab.contentsz().y + 20);
			pol.c = new Coord(kin.c.x + kin.sz.x + 10, kin.c.y);
			this.pack();
		}

		Tabs.Tab ntab(Widget ch, TButton btn) {
			Tabs.Tab tab = add(tabs.new Tab() {
				@Override
				public void cresize(Widget ch) {
					repack();
				}
			}, tabs.c);
			tab.add(ch, Coord.z);
			btn.tab = tab;
			repack();
			return (tab);
		}

		void dtab(TButton btn) {
			btn.tab.destroy();
			btn.tab = null;
			repack();
		}
	}

	static class DraggedItem {

		final GItem item;
		final Coord dc;

		DraggedItem(GItem item, Coord dc) {
			this.item = item;
			this.dc = dc;
		}
	}

	private void updhand() {
		if ((hand.isEmpty() && (vhand != null)) || ((vhand != null) && !hand.contains(vhand.item))) {
			ui.destroy(vhand);
			vhand = null;
		}
		if (!hand.isEmpty() && (vhand == null)) {
			DraggedItem fi = hand.iterator().next();
			vhand = add(new ItemDrag(fi.dc, fi.item));
		}
	}

	public void toggleHand() {
		if (hand.isEmpty()) {
			hand.addAll(handSave);
			handSave.clear();
			updhand();
		} else {
			handSave.addAll(hand);
			hand.clear();
			updhand();
		}
	}

	public void toggleStudy() {
		studywnd.show(!studywnd.visible);
	}

	public void addcmeter(Widget meter) {
		ulpanel.add(meter);
		cmeters.add(meter);
		updcmeters();
	}

	public <T extends Widget> void delcmeter(Class<T> cl) {
		Widget widget = null;
		for (Widget meter : cmeters) {
			if (cl.isAssignableFrom(meter.getClass())) {
				widget = meter;
				break;
			}
		}
		if (widget != null) {
			cmeters.remove(widget);
			widget.destroy();
			updcmeters();
		}
	}

	private void updcmeters() {
		int i = meters.size();
		for (Widget meter : cmeters) {
			int x = (i % 3) * (IMeter.fsz.x + 5);
			int y = (i / 3) * (IMeter.fsz.y + 2);
			meter.c = new Coord(portrait.c.x + portrait.sz.x + 10 + x, portrait.c.y + y);
			i++;
		}
	}

	@Override
	public void addchild(Widget child, Object... args) {
		String place = ((String) args[0]).intern();
		Coord center = new Coord(c.x + (sz.x / 2), c.y + (sz.y / 2));
		if (place == "mapview") {
			child.resize(sz);
			map = add((MapView) child, Coord.z);
			map.lower();
			if (mmap != null) {
				ui.destroy(mmap);
			}
			mmap = new LocalMiniMap(new Coord(133, 133), map);
			placemmap();
		} else if (place == "fight") {
			fv = urpanel.add((Fightview) child, 0, 0);
		} else if (place == "fsess") {
			add(child, Coord.z);
		} else if (place == "inv") {
			invwnd = new Hidewnd(Coord.z, "Inventory") {
				@Override
				public void cresize(Widget ch) {
					pack();
				}
			};
			invwnd.add(maininv = (Inventory) child, Coord.z);
			invwnd.pack();
			invwnd.hide();
			add(invwnd, new Coord(100, 100));
		} else if (place == "equ") {
			equwnd = new Hidewnd(Coord.z, "Equipment");
			equwnd.add(child, Coord.z);
			equwnd.pack();
			equwnd.hide();
			add(equwnd, new Coord(sz.x - child.sz.x, 0)); // upper right
		} else if (place == "hand") {
			GItem g = add((GItem) child);
			Coord lc = (Coord) args[1];
			hand.add(new DraggedItem(g, lc));
			updhand();
		} else if (place == "chr") {
			Coord childcenter = new Coord(child.sz.x / 2, child.sz.y / 2);
			studywnd = add(new StudyWnd());
			studywnd.hide();
			chrwdg = add((CharWnd) child, new Coord(center.x - childcenter.x, 0)); // upper center
			chrwdg.hide();
			if (CFG.UI_METER_HUNGER.valb()) {
				addcmeter(new HungerMeter(chrwdg.glut));
			}
			if (CFG.UI_METER_FEP.valb()) {
				addcmeter(new FEPMeter(chrwdg.feps));
			}
		} else if (place == "craft") {
			final Widget mkwdg = child;
			if (craftwnd != null) {
				craftwnd.setMakewindow(mkwdg);
			} else {
				if (makewnd == null) {
					makewnd = new CraftWindow();

					makewnd = add(new CraftWindow(), new Coord(400, 200));
				}
				makewnd.add(child);
				makewnd.pack();
				Coord childcenter = new Coord(makewnd.sz.x / 2, makewnd.sz.y / 2);
				add(makewnd, new Coord(center.x - childcenter.x, 0)); // upper center
				makewnd.raise();
				makewnd.show();
			}
		} else if (place == "buddy") {
			zerg.ntab(buddies = (BuddyWnd) child, zerg.kin);
		} else if (place == "pol") {
			zerg.ntab(polity = (Polity) child, zerg.pol);
			zerg.pol.tooltip = Text.render(polity.cap);
		} else if (place == "chat") {
			chat.addchild(child);
		} else if (place == "party") {
			add(child, 10, 95);
		} else if (place == "meter") {
			int x = (meters.size() % 3) * (IMeter.fsz.x + 5);
			int y = (meters.size() / 3) * (IMeter.fsz.y + 2);
			ulpanel.add(child, portrait.c.x + portrait.sz.x + 10 + x, portrait.c.y + y);
			meters.add(child);
			updcmeters();
		} else if (place == "buff") {
			buffs.addchild(child);
		} else if (place == "misc") {
			add(child, (Coord) args[1]);
		} else {
			throw (new UI.UIException("Illegal gameui child", place, args));
		}
	}

	@Override
	public void cdestroy(Widget w) {
		if (w instanceof GItem) {
			for (Iterator<DraggedItem> i = hand.iterator(); i.hasNext();) {
				DraggedItem di = i.next();
				if (di.item == w) {
					i.remove();
					updhand();
				}
			}
		} else if (w == polity) {
			polity = null;
			zerg.dtab(zerg.pol);
		} else if (w == chrwdg) {
			chrwdg = null;
		} else if (w == mmappanel) {
			mmappanel = null;
			CFG.MINIMAP_FLOATING.set(false, true);
			showmmappanel(false);
		}
		meters.remove(w);
		cmeters.remove(w);
		updcmeters();
	}

	public void placemmap() {
		if (mmap == null) {
			return;
		}
		if (mmap.parent != null) {
			mmap.unlink();
		}
		if (mmappanel != null) {
			mmappanel.setmap(mmap);
			blpanel.hide();
		} else {
			mmap.sz = new Coord(133, 133);
			blpanel.add(mmap, 4, 34 + 9);
			blpanel.show();
			mmap.lower();
		}

		toggleui(uimode);
	}

	public void showmmappanel(boolean show) {
		if (show) {
			blpw = 0;
			if (mmappanel == null) {
				mmappanel = add(new MiniMapPanel());
			}
		} else {
			blpw = 142;
			if (mmappanel != null) {
				ui.destroy(mmappanel);
			}
		}
		resize(sz);
		placemmap();
	}

	private static final Resource.Anim progt = Resource.local().loadwait("gfx/hud/prog").layer(Resource.animc);
	private Tex curprog = null;
	private int curprogf, curprogb;

	private void drawprog(GOut g, double prog) {
		int fr = Utils.clip((int) Math.floor(prog * progt.f.length), 0, progt.f.length - 2);
		int bf = Utils.clip((int) (((prog * progt.f.length) - fr) * 255), 0, 255);
		if ((curprog == null) || (curprogf != fr) || (curprogb != bf)) {
			if (curprog != null) {
				curprog.dispose();
			}
			WritableRaster buf = PUtils.imgraster(progt.f[fr][0].sz);
			PUtils.blit(buf, progt.f[fr][0].img.getRaster(), Coord.z);
			PUtils.blendblit(buf, progt.f[fr + 1][0].img.getRaster(), Coord.z, bf);
			if (CFG.UI_ACTION_PROGRESS_PERCENTAGE.valb()) {
				BufferedImage img = PUtils.rasterimg(buf);

				BufferedImage txt = Text.renderstroked(String.format("%d%%", (int) (100 * prog))).img;
				img.getGraphics().drawImage(txt, 24 - txt.getWidth() / 2, 9 - txt.getHeight() / 2, null);

				curprog = new TexI(img);
			}
			curprogf = fr;
			curprogb = bf;
		}
		g.aimage(curprog, new Coord(sz.x / 2, (sz.y * 4) / 10), 0.5, 0.5);
	}

	@Override
	public void draw(GOut g) {
		beltwdg.c = new Coord(chat.c.x, Math.min(chat.c.y - beltwdg.sz.y + 4, sz.y - beltwdg.sz.y));
		super.draw(g);
		if (prog >= 0) {
			drawprog(g, prog);
		}
		int by = sz.y;
		if (chat.visible) {
			by = Math.min(by, chat.c.y);
		}
		if (beltwdg.visible) {
			by = Math.min(by, beltwdg.c.y);
		}
		if (cmdline != null) {
			drawcmd(g, new Coord(blpw + 10, by -= 20));
		} else if (lastmsg != null) {
			if ((System.currentTimeMillis() - msgtime) > 3000) {
				lastmsg = null;
			} else {
				g.chcolor(0, 0, 0, 192);
				g.frect(new Coord(blpw + 8, by - 22), lastmsg.sz().add(4, 4));
				g.chcolor();
				g.image(lastmsg.tex(), new Coord(blpw + 10, by -= 20));
			}
		}
		if (!chat.visible) {
			chat.drawsmall(g, new Coord(blpw + 10, by), 50);
		}
	}

	@Override
	public void tick(double dt) {
		super.tick(dt);
		if (!afk && (System.currentTimeMillis() - ui.lastevent > 300000)) {
			afk = true;
			wdgmsg("afk");
		} else if (afk && (System.currentTimeMillis() - ui.lastevent < 300000)) {
			afk = false;
		}

		timelbl.settext(Utils.formatGameTime(ui.sess.glob.globtime()));
		if (((ui.sess.glob.globtime() / 1000) / 3600 % 24) < 8 && ((ui.sess.glob.globtime() / 1000) / 3600 % 24) > 4) {
			timelbl.setcolor(new Color(50, 255, 50));
			if (!dewyTime) {
				ui.message(String.format("It's Dewy Mantle Time!"), GameUI.MsgType.GOOD);
				dewyTime = true;
			}
		} else {
			timelbl.setcolor(new Color(200, 200, 200));
			if (dewyTime) {
				dewyTime = false;
			}
		}
		timelbl.c = new Coord(c.x + (sz.x / 2) - (timelbl.sz.x / 2), 0);
	}

	@Override
	public void uimsg(String msg, Object... args) {
		if (msg == "err") {
			String err = (String) args[0];
			error(err);
		} else if (msg == "msg") {
			String text = (String) args[0];
			msg(text);
		} else if (msg == "prog") {
			if (args.length > 0) {
				prog = ((Number) args[0]).doubleValue() / 100.0;
			} else {
				prog = -1;
			}
		} else if (msg == "setbelt") {
			int slot = (Integer) args[0];
			if (args.length < 2) {
				belt[slot] = null;
			} else {
				belt[slot] = ui.sess.getres((Integer) args[1]);
			}
		} else if (msg == "polowner") {
			String o = (String) args[0];
			boolean n = ((Integer) args[1]) != 0;
			if (o.length() == 0) {
				o = null;
			} else {
				o = o.intern();
			}
			if (o != polowner) {
				if (map != null) {
					if (o == null) {
						if (polowner != null) {
							map.setpoltext("Leaving " + polowner);
						}
					} else {
						map.setpoltext("Entering " + o);
					}
				}
				polowner = o;
			}
		} else if (msg == "showhelp") {
			Indir<Resource> res = ui.sess.getres((Integer) args[0]);
			if (help == null) {
				help = adda(new HelpWnd(res), 0.5, 0.5);
			} else {
				help.res = res;
			}
		} else {
			super.uimsg(msg, args);
		}
	}

	@Override
	public void wdgmsg(Widget sender, String msg, Object... args) {
		if (sender == menu) {
			wdgmsg(msg, args);
			return;
		} else if ((sender == chrwdg) && (msg == "close")) {
			chrwdg.hide();
		} else if ((sender == help) && (msg == "close")) {
			ui.destroy(help);
			help = null;
			return;
		}
		super.wdgmsg(sender, msg, args);
	}

	private void fitwdg(Widget wdg) {
		if (wdg.c.x < 0) {
			wdg.c.x = 0;
		}
		if (wdg.c.y < 0) {
			wdg.c.y = 0;
		}
		if (wdg.c.x + wdg.sz.x > sz.x) {
			wdg.c.x = sz.x - wdg.sz.x;
		}
		if (wdg.c.y + wdg.sz.y > sz.y) {
			wdg.c.y = sz.y - wdg.sz.y;
		}
	}

	public static class MenuButton extends IButton {

		private final int gkey;

		MenuButton(String base, int gkey, String tooltip) {
			super("gfx/hud/" + base, "", "-d", "-h");
			this.gkey = (char) gkey;
			this.tooltip = RichText.render(tooltip, 0);
		}

		@Override
		public void click() {
		}

		@Override
		public boolean globtype(char key, KeyEvent ev) {
			if ((gkey != -1) && (key == gkey)) {
				click();
				return (true);
			}
			return (super.globtype(key, ev));
		}
	}

	private static final Tex menubg = Resource.loadtex("gfx/hud/rbtn-bg");

	public class MainMenu extends Widget {

		public MainMenu() {
			super(menubg.sz());
			add(new MenuButton("rbtn-inv", 9, "Inventory ($col[255,255,0]{Tab})") {
				@Override
				public void click() {
					if ((invwnd != null) && invwnd.show(!invwnd.visible)) {
						invwnd.raise();
						fitwdg(invwnd);
					}
				}
			}, 0, 0);
			add(new MenuButton("rbtn-equ", 5, "Equipment ($col[255,255,0]{Ctrl+E})") {
				@Override
				public void click() {
					if ((equwnd != null) && equwnd.show(!equwnd.visible)) {
						equwnd.raise();
						fitwdg(equwnd);
					}
				}
			}, 0, 0);
			add(new MenuButton("rbtn-chr", 20, "Character Sheet ($col[255,255,0]{Ctrl+T})") {
				@Override
				public void click() {
					if ((chrwdg != null) && chrwdg.show(!chrwdg.visible)) {
						chrwdg.raise();
						fitwdg(chrwdg);
					}
				}
			}, 0, 0);
			add(new MenuButton("rbtn-bud", 2, "Kith & Kin ($col[255,255,0]{Ctrl+B})") {
				@Override
				public void click() {
					if (zerg.show(!zerg.visible)) {
						zerg.raise();
						fitwdg(zerg);
						setfocus(zerg);
					}
				}
			}, 0, 0);
			add(new MenuButton("rbtn-opt", 15, "Options ($col[255,255,0]{Ctrl+O})") {
				@Override
				public void click() {
					if (opts.show(!opts.visible)) {
						opts.raise();
						fitwdg(opts);
						setfocus(opts);
					}
				}
			}, 0, 0);
		}

		@Override
		public void draw(GOut g) {
			g.image(menubg, Coord.z);
			super.draw(g);
		}
	}

	@Override
	public boolean globtype(char key, KeyEvent ev) {
		if (key == ':') {
			entercmd();
			return (true);
		} else if (key == ' ') {
			toggleui();
			return (true);
		} else if (key == 3) {
			if (chat.visible && !chat.hasfocus) {
				setfocus(chat);
			} else {
				if (chat.targeth == 0) {
					chat.sresize(chat.savedh);
					setfocus(chat);
				} else {
					chat.sresize(0);
				}
			}
			Utils.setprefb("chatvis", chat.targeth != 0);
		} else if (key == 16) {
			/*
			 if((polity != null) && polity.show(!polity.visible)) {
			 polity.raise();
			 fitwdg(polity);
			 setfocus(polity);
			 }
			 */
			return (true);
		} else if ((key == 27) && (map != null) && !map.hasfocus) {
			setfocus(map);
			return (true);
		}
		return (super.globtype(key, ev));
	}

	@Override
	public boolean mousedown(Coord c, int button) {
		return (super.mousedown(c, button));
	}

	private int uimode = 1;

	public void toggleui(int mode) {
		Hidepanel[] panels = {blpanel, brpanel, ulpanel, urpanel, menupanel};
		if (mmappanel != null) {
			panels = new Hidepanel[]{brpanel, ulpanel, urpanel, menupanel};
		}
		switch (uimode = mode) {
			case 0:
				for (Hidepanel p : panels) {
					p.mshow(true);
				}
				break;
			case 1:
				for (Hidepanel p : panels) {
					p.mshow();
				}
				break;
			case 2:
				for (Hidepanel p : panels) {
					p.mshow(false);
				}
				break;
		}
	}

	public void resetui() {
		Hidepanel[] panels = {blpanel, brpanel, ulpanel, urpanel, menupanel};
		if (mmappanel != null) {
			panels = new Hidepanel[]{brpanel, ulpanel, urpanel, menupanel};
		}
		for (Hidepanel p : panels) {
			p.cshow(p.tvis);
		}
		uimode = 1;
	}

	public void toggleui() {
		toggleui((uimode + 1) % 3);
	}

	@Override
	public void resize(Coord sz) {
		this.sz = sz;
		chat.resize(sz.x - blpw - brpw);
		chat.move(new Coord(blpw, sz.y));
		if (map != null) {
			map.resize(sz);
		}
		beltwdg.c = new Coord(blpw + 10, sz.y - beltwdg.sz.y - 5);
		super.resize(sz);
	}

	@Override
	public void presize() {
		resize(parent.sz);
	}

	public void msg(String msg, Color color, Color logcol) {
		msgtime = System.currentTimeMillis();
		lastmsg = msgfoundry.render(msg, color);
		syslog.append(msg, logcol);
	}

	public void msg(String msg, Color color) {
		msg(msg, color, color);
	}

	@Override
	public void error(String msg) {
		msg(msg, MsgType.ERROR);
	}

	public void msg(String msg) {
		msg(msg, MsgType.INFO);
	}

	public void msg(String msg, MsgType type) {
		msg(msg, type.color, type.logcol);
		if (type.sfx != null) {
			Audio.play(type.sfx);
		}
	}

	public enum MsgType {

		INFO(Color.WHITE), GOOD(Color.GREEN), BAD(Color.RED),
		ERROR(new Color(192, 0, 0), new Color(255, 0, 0), "sfx/error");

		public final Color color, logcol;
		public final Resource sfx;

		MsgType(Color color) {
			this(color, color, null);
		}

		MsgType(Color color, Color logcol, String sfx) {
			this.logcol = logcol;
			this.color = color;
			this.sfx = (sfx != null) ? Resource.local().loadwait(sfx) : null;
		}
	}

	public void act(String... args) {
		wdgmsg("act", (Object[]) args);
	}

	public void act(int mods, Coord mc, Gob gob, String... args) {
		int n = args.length;
		Object[] al = new Object[n];
		System.arraycopy(args, 0, al, 0, n);
		if (mc != null) {
			al = Utils.extend(al, al.length + 2);
			al[n++] = mods;
			al[n++] = mc;
			if (gob != null) {
				al = Utils.extend(al, al.length + 2);
				al[n++] = (int) gob.id;
				al[n++] = gob.rc;
			}
		}
		wdgmsg("act", al);
	}

	public class FKeyBelt extends Belt implements DTarget, DropTarget {

		public final int beltkeys[] = {KeyEvent.VK_F1, KeyEvent.VK_F2, KeyEvent.VK_F3, KeyEvent.VK_F4,
			KeyEvent.VK_F5, KeyEvent.VK_F6, KeyEvent.VK_F7, KeyEvent.VK_F8,
			KeyEvent.VK_F9, KeyEvent.VK_F10, KeyEvent.VK_F11, KeyEvent.VK_F12};
		public int curbelt = 0;

		public FKeyBelt() {
			super(new Coord(450, 34));
		}

		private Coord beltc(int i) {
			return (new Coord(((invsq.sz().x + 2) * i) + (10 * (i / 4)), 0));
		}

		private int beltslot(Coord c) {
			for (int i = 0; i < 12; i++) {
				if (c.isect(beltc(i), invsq.sz())) {
					return (i + (curbelt * 12));
				}
			}
			return (-1);
		}

		@Override
		public void draw(GOut g) {
			for (int i = 0; i < 12; i++) {
				int slot = i + (curbelt * 12);
				Coord c = beltc(i);
				g.image(invsq, beltc(i));
				try {
					if (belt[slot] != null) {
						g.image(belt[slot].get().layer(Resource.imgc).tex(), c.add(1, 1));
					}
				} catch (Loading e) {
				}
				g.chcolor(156, 180, 158, 255);
				FastText.aprintf(g, c.add(invsq.sz().sub(2, 0)), 1, 1, "F%d", i + 1);
				g.chcolor();
			}
		}

		@Override
		public boolean mousedown(Coord c, int button) {
			int slot = beltslot(c);
			if (slot != -1) {
				if (button == 1) {
					GameUI.this.wdgmsg("belt", slot, 1, ui.modflags());
				}
				if (button == 3) {
					GameUI.this.wdgmsg("setbelt", slot, 1);
				}
				return (true);
			}
			return (false);
		}

		@Override
		public boolean globtype(char key, KeyEvent ev) {
			if (key != 0 || ui.modctrl) {
				return (false);
			}
			boolean M = (ev.getModifiersEx() & (KeyEvent.META_DOWN_MASK | KeyEvent.ALT_DOWN_MASK)) != 0;
			for (int i = 0; i < beltkeys.length; i++) {
				if (ev.getKeyCode() == beltkeys[i]) {
					if (M) {
						curbelt = i;
						return (true);
					} else {
						keyact(i + (curbelt * 12));
						return (true);
					}
				}
			}
			return (false);
		}

		@Override
		public boolean drop(Coord c, Coord ul) {
			int slot = beltslot(c);
			if (slot != -1) {
				GameUI.this.wdgmsg("setbelt", slot, 0);
				return (true);
			}
			return (false);
		}

		@Override
		public boolean iteminteract(Coord c, Coord ul) {
			return (false);
		}

		@Override
		public boolean dropthing(Coord c, Object thing) {
			int slot = beltslot(c);
			if (slot != -1) {
				if (thing instanceof Resource) {
					Resource res = (Resource) thing;
					if (res.layer(Resource.action) != null) {
						GameUI.this.wdgmsg("setbelt", slot, res.name);
						return (true);
					}
				}
			}
			return (false);
		}
	}

	private static final Tex nkeybg = Resource.loadtex("gfx/hud/hb-main");

	public class NKeyBelt extends Belt implements DTarget, DropTarget {

		public int curbelt = 0;
		final Coord pagoff = new Coord(5, 25);

		public NKeyBelt() {
			super(nkeybg.sz());
			adda(new IButton("gfx/hud/hb-btn-chat", "", "-d", "-h") {
				Tex glow;

				{
					this.tooltip = RichText.render("Chat ($col[255,255,0]{Ctrl+C})", 0);
					glow = new TexI(PUtils.rasterimg(PUtils.blurmask(up.getRaster(), 2, 2, Color.WHITE)));
				}

				@Override
				public void click() {
					if (chat.targeth == 0) {
						chat.sresize(chat.savedh);
						setfocus(chat);
					} else {
						chat.sresize(0);
					}
					Utils.setprefb("chatvis", chat.targeth != 0);
				}

				@Override
				public void draw(GOut g) {
					super.draw(g);
					Color urg = chat.urgcols[chat.urgency];
					if (urg != null) {
						GOut g2 = g.reclipl(new Coord(-2, -2), g.sz.add(4, 4));
						g2.chcolor(urg.getRed(), urg.getGreen(), urg.getBlue(), 128);
						g2.image(glow, Coord.z);
					}
				}
			}, sz, 1, 1);
		}

		private Coord beltc(int i) {
			return (pagoff.add(((invsq.sz().x + 2) * i) + (10 * (i / 5)), 0));
		}

		private int beltslot(Coord c) {
			for (int i = 0; i < 10; i++) {
				if (c.isect(beltc(i), invsq.sz())) {
					return (i + (curbelt * 12));
				}
			}
			return (-1);
		}

		@Override
		public void draw(GOut g) {
			g.image(nkeybg, Coord.z);
			for (int i = 0; i < 10; i++) {
				int slot = i + (curbelt * 12);
				Coord c = beltc(i);
				g.image(invsq, beltc(i));
				try {
					if (belt[slot] != null) {
						g.image(belt[slot].get().layer(Resource.imgc).tex(), c.add(1, 1));
					}
				} catch (Loading e) {
				}
				g.chcolor(156, 180, 158, 255);
				FastText.aprintf(g, c.add(invsq.sz().sub(2, 0)), 1, 1, "%d", (i + 1) % 10);
				g.chcolor();
			}
			super.draw(g);
		}

		@Override
		public boolean mousedown(Coord c, int button) {
			int slot = beltslot(c);
			if (slot != -1) {
				if (button == 1) {
					GameUI.this.wdgmsg("belt", slot, 1, ui.modflags());
				}
				if (button == 3) {
					GameUI.this.wdgmsg("setbelt", slot, 1);
				}
				return (true);
			}
			return (super.mousedown(c, button));
		}

		@Override
		public boolean globtype(char key, KeyEvent ev) {
			if (key != 0 || ui.modctrl) {
				return (false);
			}
			int c = ev.getKeyChar();
			if ((c < KeyEvent.VK_0) || (c > KeyEvent.VK_9)) {
				return (false);
			}
			int i = Utils.floormod(c - KeyEvent.VK_0 - 1, 10);
			boolean M = (ev.getModifiersEx() & (KeyEvent.META_DOWN_MASK | KeyEvent.ALT_DOWN_MASK)) != 0;
			if (M) {
				curbelt = i;
			} else {
				keyact(i + (curbelt * 12));
			}
			return (true);
		}

		@Override
		public boolean drop(Coord c, Coord ul) {
			int slot = beltslot(c);
			if (slot != -1) {
				GameUI.this.wdgmsg("setbelt", slot, 0);
				return (true);
			}
			return (false);
		}

		@Override
		public boolean iteminteract(Coord c, Coord ul) {
			return (false);
		}

		@Override
		public boolean dropthing(Coord c, Object thing) {
			int slot = beltslot(c);
			if (slot != -1) {
				if (thing instanceof Resource) {
					Resource res = (Resource) thing;
					if (res.layer(Resource.action) != null) {
						GameUI.this.wdgmsg("setbelt", slot, res.name);
						return (true);
					}
				}
			}
			return (false);
		}
	}

	{
		String val = Utils.getpref("belttype", "n");
		if (val.equals("n")) {
			beltwdg = add(new NKeyBelt());
		} else if (val.equals("f")) {
			beltwdg = add(new FKeyBelt());
		} else {
			beltwdg = add(new NKeyBelt());
		}
	}

	private Map<String, Console.Command> cmdmap = new TreeMap<>();

	{
		cmdmap.put("afk", new Console.Command() {
			@Override
			public void run(Console cons, String[] args) {
				afk = true;
				wdgmsg("afk");
			}
		});
		cmdmap.put("act", new Console.Command() {
			@Override
			public void run(Console cons, String[] args) {
				Object[] ad = new Object[args.length - 1];
				System.arraycopy(args, 1, ad, 0, ad.length);
				wdgmsg("act", ad);
			}
		});
		cmdmap.put("belt", new Console.Command() {
			@Override
			public void run(Console cons, String[] args) {
				if (args[1].equals("f")) {
					beltwdg.destroy();
					beltwdg = add(new FKeyBelt());
					Utils.setpref("belttype", "f");
					resize(sz);
				} else if (args[1].equals("n")) {
					beltwdg.destroy();
					beltwdg = add(new NKeyBelt());
					Utils.setpref("belttype", "n");
					resize(sz);
				}
			}
		});
		cmdmap.put("tool", new Console.Command() {
			@Override
			public void run(Console cons, String[] args) {
				add(gettype(args[1]).create(GameUI.this, new Object[0]), 200, 200);
			}
		});
	}

	@Override
	public Map<String, Console.Command> findcmds() {
		return (cmdmap);
	}
}
