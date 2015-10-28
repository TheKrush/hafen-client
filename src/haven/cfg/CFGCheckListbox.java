package haven.cfg;

import haven.CFG;
import haven.CheckListbox;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CFGCheckListbox extends CheckListbox implements CFG.CFGObserver {

	protected final CFG cfg;
	protected final List<String> keys;

	public CFGCheckListbox(CFG cfg, int w, int h) {
		super(w, h);
		this.cfg = cfg;
		this.keys = new ArrayList<>();

		Map<String, Boolean> mapVal = cfg.<Map<String, Boolean>>valo();
		Set<String> mapValKeys = mapVal.keySet();
		additems(mapValKeys.toArray(new String[mapValKeys.size()]));
	}

	private boolean cfgVal(String name) {
		Map<String, Boolean> mapVal = cfg.<Map<String, Boolean>>valo();
		return mapVal.containsKey(name) ? mapVal.get(name) : false;
	}

	private void cfgVal(String name, boolean val) {
		Map<String, Boolean> mapVal = cfg.<Map<String, Boolean>>valo();
		mapVal.put(name, val);
		cfg.set(mapVal);
	}

	public class CFGCheckListboxItem extends CheckListbox.CheckListboxItem {

		public CFGCheckListboxItem(String name, boolean selected) {
			super(name, selected);
		}
	}

	@Override
	protected void itemclick(CheckListbox.CheckListboxItem itm, int button) {
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
		for (CheckListbox.CheckListboxItem item : items) {
			item.selected = cfgVal(item.name);
		}
	}
}
