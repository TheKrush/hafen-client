package haven;

import java.awt.*;
import java.util.*;

public class FlowerList extends Scrollport {

	public static final Color BGCOLOR = new Color(0, 0, 0, 64);
	private final IBox box;
	private Map<String, Boolean> items = null;

	public FlowerList() {
		this(new Coord(200, 250));
	}

	public FlowerList(Coord sz) {
		this(new HashMap<String, Boolean>(), sz);
	}

	public FlowerList(Map<String, Boolean> items) {
		this(items, new Coord(200, 250));
	}

	public FlowerList(Map<String, Boolean> items, Coord sz) {
		super(sz);
		this.items = items;
		box = new IBox("gfx/hud/box", "tl", "tr", "bl", "br", "extvl", "extvr", "extht", "exthb");

		int i = 0;
		if (this.items != null) {
			for (Map.Entry<String, Boolean> entry : this.items.entrySet()) {
				cont.add(new FlowerListItem(entry.getKey(), entry.getValue()), 0, 25 * i++);
			}
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
				synchronized (items) {
					items.put(name, val);
				}
				change(name, val);
				break;
			}
			case "delete": {
				String name = (String) args[0];
				synchronized (items) {
					items.remove(name);
				}
				remove(name);
				ui.destroy(sender);
				update();
				break;
			}
			default:
				super.wdgmsg(sender, msg, args);
				break;
		}
	}

	public void add(String name) {
		add(name, false);
	}

	@SuppressWarnings("SynchronizeOnNonFinalField")
	public void add(String name, boolean checked) {
		if (name != null && !name.isEmpty() && !items.containsKey(name)) {
			synchronized (items) {
				items.put(name, true);
			}
			change(name, checked);
			cont.add(new FlowerListItem(name, checked), new Coord());
			update();
		}
	}

	protected void change(String name, boolean value) {
	}

	protected void remove(String name) {
	}

	private void update() {
		LinkedList<String> order = new LinkedList<>(items.keySet());
		Collections.sort(order);
		for (Widget wdg = cont.lchild; wdg != null; wdg = wdg.prev) {
			int i = order.indexOf(((FlowerListItem) wdg).name);
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

	protected static class FlowerListItem extends Widget {

		public final String name;
		private final CheckBox cb;
		private boolean highlight = false;
		private boolean a = false;
		private UI.Grab grab;

		public FlowerListItem(String name) {
			this(name, false);
		}

		public FlowerListItem(String name, boolean checked) {
			super(new Coord(200, 25));
			this.name = name;

			cb = add(new CheckBox(name), 3, 3);
			cb.a = checked;
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

		public void set(boolean a) {
			cb.a = a;
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
