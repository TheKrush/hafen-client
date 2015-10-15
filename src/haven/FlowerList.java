package haven;

import haven.FlowerList.FlowerListItem;

import java.util.*;

public class FlowerList extends WidgetList<FlowerListItem> {

	public static final Comparator<FlowerListItem> ITEM_COMPARATOR = new Comparator<FlowerListItem>() {
		@Override
		public int compare(FlowerListItem o1, FlowerListItem o2) {
			return o1.name.compareTo(o2.name);
		}
	};

	private Map<String, Boolean> items = null;

	public FlowerList() {
		this(new Coord(200, 250));
	}

	public FlowerList(Coord sz) {
		this(new HashMap<String, Boolean>(), sz, 10);
	}

	public FlowerList(Map<String, Boolean> items) {
		this(items, new Coord(200, 25), 10);
	}

	public FlowerList(Map<String, Boolean> items, Coord sz) {
		this(items, sz, 10);
	}

	public FlowerList(Map<String, Boolean> items, Coord sz, int h) {
		super(sz, h);
		this.items = items;

		if (this.items != null) {
			for (Map.Entry<String, Boolean> entry : this.items.entrySet()) {
				additem(new FlowerListItem(entry.getKey(), entry.getValue()));
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
				removeitem((FlowerListItem) sender, true);
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
			additem(new FlowerListItem(name, checked));
			update();
		}
	}

	protected void change(String name, boolean value) {
	}

	protected void remove(String name) {
	}

	private void update() {
		Collections.sort(list, ITEM_COMPARATOR);
		int n = listitems();
		for (int i = 0; i < n; i++) {
			listitem(i).c = itempos(i);
		}
	}

	protected static class FlowerListItem extends Widget {

		public final String name;
		private final CheckBox cb;
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

			add(new Button(24, "X"), 175, 0);
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
					wdgmsg("changed", name, (int) args[0] > 0);
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
