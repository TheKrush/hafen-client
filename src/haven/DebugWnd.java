package haven;

import haven.Session.LoadingIndir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DebugWnd extends Window {

	DebugListbox gobListbox;

	public DebugWnd() {
		super(new Coord(300, 360), "Debug");
		gobListbox = add(new DebugListbox(300, 20));
	}

	@Override
	public void draw(GOut g) {
		super.draw(g);

		OCache oc = ui.sess.glob.oc;
		synchronized (oc) {
			List<Gob> removed = new ArrayList<>();
			for (Gob gob : gobListbox.items) {
				if (oc.getgob(gob.id) == null) {
					removed.add(gob);
				}
			}
			gobListbox.remitems(removed);
			for (Gob gob : oc) {
				if (gob.sc == null) {
					continue;
				}
				if (gob.sc.x < 0 || gob.sc.x > ui.gui.map.sz.x) {
					continue;
				}
				if (gob.sc.y < 0 || gob.sc.y > ui.gui.map.sz.y) {
					continue;
				}
				gobListbox.additem(gob);
			}
		}
	}

	private Gob player() {
		return ui.gui.map.player();
	}

	public class DebugListbox extends Listbox<Gob> {

		public List<Gob> items = new ArrayList<>();
		private Coord c = Coord.z;

		public DebugListbox(int w, int h) {
			super(w, h, 18);
		}

		@Override
		public boolean mousedown(Coord c, int button) {
			this.c = c;
			return super.mousedown(c, button);
		}

		@Override
		protected void itemclick(Gob itm, int button) {
			super.itemclick(itm, button);
			if (button == 1 || button == 3) {
				change(itm);
			}
			ui.gui.map.wdgmsg("click", rootpos().add(c), itm.sc, button, ui.modflags(), 0, (int) itm.id, itm.rc, 0, -1);
		}

		public void additems(List<Gob> tms) {
			for (Gob itm : tms) {
				additem(itm);
			}
		}

		public void additem(Gob itm) {
			try {
				Resource res = itm.getres();
				if (res == null || items.contains(itm)) {
					return;
				}
				items.add(itm);
				Collections.sort(items, new Comparator<Gob>() {
					@Override
					public int compare(Gob g1, Gob g2) {
						return g1.getres().name.compareTo(g2.getres().name);
					}
				});
			} catch (LoadingIndir ex) {
			}
		}

		public void remitems(List<Gob> itms) {
			for (Gob itm : itms) {
				remitem(itm);
			}
		}

		public void remitem(Gob itm) {
			items.remove(itm);
		}

		@Override
		protected Gob listitem(int idx) {
			return (items.get(idx));
		}

		@Override
		protected int listitems() {
			return items.size();
		}

		@Override
		protected void drawitem(GOut g, Gob itm, int idx) {
			String str = itm.getres().name + " " + itm.rc;
			Text text = Text.render(str);
			Tex tex = text.tex();
			g.image(tex, new Coord(2, 2), text.sz());
			tex.dispose();
		}
	}
}
