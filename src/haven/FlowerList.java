package haven;

import java.awt.*;
import java.util.*;

import static haven.FlowerMenu.AUTOCHOOSE;

public class FlowerList extends Scrollport {

	public static final Color BGCOLOR = new Color(0, 0, 0, 64);
	private final IBox box;


	public FlowerList() {
		this(new Coord(200, 250));
	}

	public FlowerList(Coord sz) {
		super(sz);
		box = new IBox("gfx/hud/box", "tl", "tr", "bl", "br", "extvl", "extvr", "extht", "exthb");

		int i = 0;
		for (Map.Entry<String, Boolean> entry : AUTOCHOOSE.entrySet()) {
			cont.add(new Item(entry.getKey()), 0, 25 * i++);
		}

		update();
	}

	@SuppressWarnings("SynchronizeOnNonFinalField")
	@Override
	public void wdgmsg(Widget sender, String msg, Object... args) {
		switch (msg) {
			case "changed": {
				String name = (String) args[0];
				boolean val = (Boolean) args[1];
				synchronized (AUTOCHOOSE) {
					AUTOCHOOSE.put(name, val);
				}
				FlowerMenu.saveAutochoose();
				break;
			}
			case "delete": {
				String name = (String) args[0];
				synchronized (AUTOCHOOSE) {
					AUTOCHOOSE.remove(name);
				}
				FlowerMenu.saveAutochoose();
				ui.destroy(sender);
				update();
				break;
			}
			default:
				super.wdgmsg(sender, msg, args);
				break;
		}
	}

	@SuppressWarnings("SynchronizeOnNonFinalField")
	public void add(String name) {
		if (name != null && !name.isEmpty() && !AUTOCHOOSE.containsKey(name)) {
			synchronized (AUTOCHOOSE) {
				AUTOCHOOSE.put(name, true);
			}
			FlowerMenu.saveAutochoose();
			cont.add(new Item(name), new Coord());
			update();
		}
	}

	private void update() {
		LinkedList<String> order = new LinkedList<>(AUTOCHOOSE.keySet());
		Collections.sort(order);
		for (Widget wdg = cont.lchild; wdg != null; wdg = wdg.prev) {
			int i = order.indexOf(((Item) wdg).name);
			wdg.c.y = 25 * i;
		}
		cont.update();
	}

	@Override
	public void draw(GOut g) {
		g.chcolor(BGCOLOR);
		g.frect(Coord.z, sz);
		g.chcolor();
		super.draw(g);
		box.draw(g, Coord.z, sz);
	}

	private static class Item extends Widget {

		public final String name;
		private final CheckBox cb;
		private boolean highlight = false;
		private boolean a = false;
		private UI.Grab grab;

		public Item(String name) {
			super(new Coord(200, 25));
			this.name = name;

			cb = add(new CheckBox(name), 3, 3);
			cb.a = AUTOCHOOSE.get(name);
			cb.canactivate = true;

			add(new Button(24, "X"), 165, 0);
		}

		@Override
		public void draw(GOut g) {
			if (highlight) {
				g.chcolor(Listbox.overc);
				g.frect(Coord.z, sz);
				g.chcolor();
			}
			super.draw(g);
		}

		@Override
		public void mousemove(Coord c) {
			highlight = c.isect(Coord.z, sz);
			super.mousemove(c);
		}

		@Override
		public boolean mousedown(Coord c, int button) {
			if (super.mousedown(c, button)) {
				return true;
			}
			if (button != 1) {
				return (false);
			}
			a = true;
			grab = ui.grabmouse(this);
			return (true);
		}

		@Override
		public boolean mouseup(Coord c, int button) {
			if (a && button == 1) {
				a = false;
				if (grab != null) {
					grab.remove();
					grab = null;
				}
				if (c.isect(new Coord(0, 0), sz)) {
					click();
				}
				return (true);
			}
			return (false);
		}

		private void click() {
			cb.a = !cb.a;
			wdgmsg("changed", name, cb.a);
		}

		@Override
		public void wdgmsg(Widget sender, String msg, Object... args) {
			switch (msg) {
				case "ch":
					wdgmsg("changed", name, args[0]);
					break;
				case "activate":
					wdgmsg("delete", name);
					break;
				default:
					super.wdgmsg(sender, msg, args);
					break;
			}
		}
	}
}
