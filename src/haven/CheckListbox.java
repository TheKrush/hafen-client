package haven;

import haven.CheckListbox.CheckListboxItem;
import java.util.ArrayList;
import java.util.List;

public class CheckListbox extends Listbox<CheckListboxItem> {

	private static final Tex chk = Resource.loadtex("gfx/hud/chkmarks");
	public List<CheckListboxItem> items = new ArrayList<>();

	public CheckListbox(int w, int h) {
		super(w, h, 18);
	}

	public class CheckListboxItem implements Comparable<CheckListboxItem> {

		public String name;
		public boolean selected;

		public CheckListboxItem(String name, boolean selected) {
			this.name = name;
			this.selected = selected;
		}

		public void setselected(boolean selected) {
			this.selected = selected;
		}

		@Override
		public int compareTo(CheckListboxItem o) {
			return this.name.compareTo(o.name);
		}
	}

	@Override
	protected void itemclick(CheckListboxItem itm, int button) {
		if (button == 1) {
			itm.setselected(!itm.selected);
			super.itemclick(itm, button);
		}
	}

	@Override
	protected CheckListboxItem listitem(int idx) {
		return (items.get(idx));
	}

	@Override
	protected int listitems() {
		return items.size();
	}

	@Override
	public void drawbg(GOut g) {
		g.chcolor(0, 0, 0, 128);
		g.frect(Coord.z, sz);
		g.chcolor();
	}

	@Override
	protected void drawitem(GOut g, CheckListboxItem itm, int idx) {
		if (itm.selected) {
			g.image(chk, new Coord(sz.x - sb.sz.x - chk.sz().x - 3, -1), new Coord(itemh, itemh));
		}
		Text t = Text.render(itm.name);
		Tex T = t.tex();
		g.image(T, new Coord(2, 2), t.sz());
		T.dispose();
	}

	public String[] getselected() {
		List<String> sitems = new ArrayList<>();
		for (CheckListboxItem itm : items) {
			if (itm.selected) {
				sitems.add(itm.name);
			}
		}
		return sitems.toArray(new String[sitems.size()]);
	}
}
