package haven;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class GobPathOptWnd extends Window {

	public static Window instance;

	public static void toggle(Widget parent) {
		if (instance == null) {
			instance = parent.add(new GobPathOptWnd());
		} else {
			remove();
		}
	}

	public static void remove() {
		if (instance != null) {
			instance.reqdestroy();
			instance = null;
		}
	}

	public GobPathOptWnd() {
		super(Coord.z, "Actor Path Options");

		justclose = true;

		List<String> animals = new LinkedList<>(GobPath.Cfg.gobPathCfg.keySet());
		Collections.sort(animals);

		WidgetList<Element> list = add(new WidgetList<Element>(new Coord(150, 16), 15) {
			@Override
			protected void itemclick(Element item, int button) {
				if (button == 1) {
					item.set(!item.a);
				}
			}
		});
		for (String animal : animals) {
			list.additem(new Element(animal));
		}

		pack();
	}

	@Override
	public void destroy() {
		instance = null;
		super.destroy();
		GobPath.Cfg.save();
	}

	private static class Element extends CheckBox {

		private final GobPath.Cfg cfg;

		public Element(String path) {
			super(name(path));
			cfg = GobPath.Cfg.get(path);
			a = cfg.show;
		}

		private static String name(String path) {
			GobPath.Cfg cfg = GobPath.Cfg.get(path);
			if (cfg != null && cfg.name != null) {
				return cfg.name;
			}
			return path;
		}

		@Override
		public void changed(boolean val) {
			cfg.show = val;
		}
	}
}
