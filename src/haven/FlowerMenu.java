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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.awt.Color;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.PI;

public class FlowerMenu extends Widget {

	public static final Color pink = new Color(255, 0, 128);
	public static final Color ptc = Color.YELLOW;
	public static final Text.Foundry ptf = new Text.Foundry(Text.dfont, 12);
	public static final IBox pbox = Window.wbox;
	public static final Tex pbg = Window.bg;
	public static final int ph = 30, ppl = 8;
	public static Map<String, Boolean> AUTOCHOOSE = null;
	private final String[] options;
	private Petal autochoose;
	public Petal[] opts;
	private UI.Grab mg, kg;

	static {
		loadAutochoose();
	}

	private static void loadAutochoose() {
		String json = Config.loadFile("autochoose.json");
		if (json != null) {
			try {
				Gson gson = (new GsonBuilder()).create();
				Type collectionType = new TypeToken<HashMap<String, Boolean>>() {
				}.getType();
				AUTOCHOOSE = gson.fromJson(json, collectionType);
			} catch (Exception ignored) {
			}
		}
		if (AUTOCHOOSE == null) {
			AUTOCHOOSE = new HashMap<>();
			AUTOCHOOSE.put("Pick", false);
		}
	}

	@SuppressWarnings("SynchronizeOnNonFinalField")
	public static void saveAutochoose() {
		synchronized (AUTOCHOOSE) {
			Gson gson = (new GsonBuilder()).create();
			Config.saveFile("autochoose.json", gson.toJson(AUTOCHOOSE));
		}
	}

	@RName("sm")
	public static class $_ implements Factory {

		public Widget create(Widget parent, Object[] args) {
			String[] opts = new String[args.length];
			for (int i = 0; i < args.length; i++) {
				opts[i] = (String) args[i];
			}
			return (new FlowerMenu(opts));
		}
	}

	public class Petal extends Widget {

		public String name;
		public double ta, tr;
		public int num;
		private Text text;
		private double a = 1;

		public Petal(String name) {
			super(Coord.z);
			this.name = name;
			text = ptf.render(name, ptc);
			resize(text.sz().x + 25, ph);
		}

		public void move(Coord c) {
			this.c = c.sub(sz.div(2));
		}

		public void move(double a, double r) {
			move(Coord.sc(a, r));
		}

		public void draw(GOut g) {
			g.chcolor(new Color(255, 255, 255, (int) (255 * a)));
			g.image(pbg, new Coord(3, 3), new Coord(3, 3), sz.add(new Coord(-6, -6)));
			pbox.draw(g, Coord.z, sz);
			g.image(text.tex(), sz.div(2).sub(text.sz().div(2)));
		}

		public boolean mousedown(Coord c, int button) {
			choose(this);
			return (true);
		}

		public Area ta(Coord tc) {
			return (Area.sized(tc.sub(sz.div(2)), sz));
		}

		public Area ta(double a, double r) {
			return (ta(Coord.sc(a, r)));
		}
	}

	public class Opening extends NormAnim {

		Opening() {
			super(0.25);
		}

		public void ntick(double s) {
			for (Petal p : opts) {
				p.move(p.ta + ((1 - s) * PI), p.tr * s);
				p.a = s;
			}
		}
	}

	public class Chosen extends NormAnim {

		Petal chosen;

		Chosen(Petal c) {
			super(0.75);
			chosen = c;
		}

		public void ntick(double s) {
			for (Petal p : opts) {
				if (p == chosen) {
					if (s > 0.6) {
						p.a = 1 - ((s - 0.6) / 0.4);
					} else if (s < 0.3) {
						p.move(p.ta, p.tr * (1 - (s / 0.3)));
					}
				} else {
					if (s > 0.3) {
						p.a = 0;
					} else {
						p.a = 1 - (s / 0.3);
					}
				}
			}
			if (s == 1.0) {
				ui.destroy(FlowerMenu.this);
			}
		}
	}

	public class Cancel extends NormAnim {

		Cancel() {
			super(0.25);
		}

		public void ntick(double s) {
			for (Petal p : opts) {
				p.move(p.ta + ((s) * PI), p.tr * (1 - s));
				p.a = 1 - s;
			}
			if (s == 1.0) {
				ui.destroy(FlowerMenu.this);
			}
		}
	}

	private void organize(Petal[] opts) {
		Area bounds = parent.area().xl(c.inv());
		int l = 1, p = 0, i = 0, mp = 0, ml = 1, t = 0, tt = -1;
		boolean muri = false;
		while (i < opts.length) {
			place:
			{
				double ta = (PI / 2) - (p * (2 * PI / (l * ppl)));
				double tr = 75 + (50 * (l - 1));
				if (!muri && !bounds.contains(opts[i].ta(ta, tr))) {
					if (tt < 0) {
						tt = ppl * l;
						t = 1;
						mp = p;
						ml = l;
					} else if (++t >= tt) {
						muri = true;
						p = mp;
						l = ml;
						continue;
					}
					break place;
				}
				tt = -1;
				opts[i].ta = ta;
				opts[i].tr = tr;
				i++;
			}
			if (++p >= (ppl * l)) {
				l++;
				p = 0;
			}
		}
	}

	public FlowerMenu(String... options) {
		super(Coord.z);
		this.options = options;
	}

	@Override
	protected void attach(UI ui) {
		super.attach(ui);
		opts = new Petal[options.length];
		for (int i = 0; i < options.length; i++) {
			String name = options[i];
			Petal p = add(new Petal(name));
			p.num = i;
			boolean auto = AUTOCHOOSE.containsKey(name) && AUTOCHOOSE.get(name);
			boolean single = ui.modctrl && options.length == 1 && CFG.MENU_SINGLE_CTRL_CLICK.valb();
			if (!ui.modshift && (auto || single)) {
				autochoose = p;
			}
			opts[i] = p;
		}
	}

	@Override
	public void tick(double dt) {
		if (autochoose != null) {
			choose(autochoose);
			autochoose = null;
		}
		super.tick(dt);
	}

	protected void added() {
		if (c.equals(-1, -1)) {
			c = parent.ui.lcc;
		}
		mg = ui.grabmouse(this);
		kg = ui.grabkeys(this);
		organize(opts);
		new Opening();
	}

	public boolean mousedown(Coord c, int button) {
		if (!anims.isEmpty()) {
			return (true);
		}
		if (!super.mousedown(c, button)) {
			choose(null);
		}
		return (true);
	}

	public void uimsg(String msg, Object... args) {
		if (msg == "cancel") {
			new Cancel();
			mg.remove();
			kg.remove();
		} else if (msg == "act") {
			new Chosen(opts[(Integer) args[0]]);
			mg.remove();
			kg.remove();
		}
	}

	public void draw(GOut g) {
		super.draw(g, false);
	}

	public boolean keydown(java.awt.event.KeyEvent ev) {
		return (true);
	}

	public boolean type(char key, java.awt.event.KeyEvent ev) {
		if ((key >= '0') && (key <= '9')) {
			int opt = (key == '0') ? 10 : (key - '1');
			if (opt < opts.length) {
				choose(opts[opt]);
				kg.remove();
			}
			return (true);
		} else if (key == 27) {
			choose(null);
			kg.remove();
			return (true);
		}
		return (false);
	}

	public void choose(Petal option) {
		if (option == null) {
			wdgmsg("cl", -1);
		} else {
			wdgmsg("cl", option.num, ui.modflags());
		}
	}
}
