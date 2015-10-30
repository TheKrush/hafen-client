package haven.cfg;

import haven.CFG;
import haven.Coord;
import haven.FlowerList;
import haven.Widget;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CFGFlowerList extends FlowerList implements CFGObserver {

	protected final CFG cfg;

	public CFGFlowerList(CFG cfg) {
		this(cfg, new Coord(200, 250));
	}

	public CFGFlowerList(CFG cfg, Coord sz) {
		super(sz);
		this.cfg = cfg;

		Map<String, Boolean> mapVal = cfg.valo();
		Set<String> mapValKeys = mapVal.keySet();
		additems(mapValKeys.toArray(new String[mapValKeys.size()]));
	}

	private boolean cfgVal(String name) {
		Map<String, Boolean> mapVal = cfg.valo();
		return mapVal.containsKey(name) ? mapVal.get(name) : false;
	}

	private void cfgVal(String name, boolean val) {
		Map<String, Boolean> mapVal = cfg.valo();
		mapVal.put(name, val);
		cfg.set(mapVal);
	}

	public class CFGFlowerListItem extends FlowerList.FlowerListItem {

		public CFGFlowerListItem(String name) {
			super(name);
		}

		public CFGFlowerListItem(String name, boolean checked) {
			super(name, checked);
		}
	}

	public final void additems(List<String> names) {
		additems(names.toArray(new String[names.size()]));
	}

	public final void additems(String[] keys) {
		Arrays.sort(keys);
		for (String key : keys) {
			add(key, cfgVal(key));
		}
	}

	@Override
	protected void change(String name, boolean value) {
		super.change(name, value);
		cfgVal(name, value);
	}

	@Override
	protected void remove(String name) {
		super.remove(name);
		Map<String, Boolean> mapVal = cfg.valo();
		mapVal.remove(name);
		cfg.set(mapVal);
	}

	@Override
	public void cfgUpdated(CFG cfg) {
		Map<String, Boolean> mapVal = cfg.valo();
		for (Map.Entry<String, Boolean> entry : mapVal.entrySet()) {
			this.cfg.set(entry.getKey(), entry.getValue());
		}
		additems(mapVal.keySet().toArray(new String[mapVal.keySet().size()]));

		for (Widget wdg : list) {
			FlowerList.FlowerListItem item = ((FlowerList.FlowerListItem) wdg);
			item.set(cfgVal(item.name));
		}
	}
}
