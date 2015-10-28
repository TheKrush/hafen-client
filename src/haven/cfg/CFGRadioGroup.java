package haven.cfg;

import haven.CFG;
import haven.Coord;
import haven.RadioGroup;
import haven.Text;
import haven.Widget;

public class CFGRadioGroup extends RadioGroup {

	public CFGRadioGroup(Widget parent) {
		super(parent);
	}

	public class CFGRadioButton extends RadioGroup.RadioButton implements CFG.CFGObserver {

		protected final CFG cfg;
		public final int cfgVal;

		CFGRadioButton(String lbl, CFG cfg, int val) {
			this(lbl, cfg, val, null);
		}

		CFGRadioButton(String lbl, CFG cfg, int val, String tip) {
			super(lbl);

			this.cfg = cfg;
			this.cfgVal = val;
			if (tip != null) {
				tooltip = Text.render(tip).tex();
			}
		}

		@Override
		public void cfgUpdated(CFG cfg) {
			if (cfg.vali() == cfgVal) {
				check(this);
			}
		}
	}

	public CFGRadioButton add(String lbl, CFG cfg, int val, Coord c) {
		return add(lbl, cfg, val, null, c);
	}

	public CFGRadioButton add(String lbl, CFG cfg, int val, String tip, Coord c) {
		CFGRadioButton rb = new CFGRadioButton(lbl, cfg, val, tip);
		parent.add(rb, c);
		btns.add(rb);
		map.put(lbl, rb);
		rmap.put(rb, lbl);
		if (checked == null) {
			check(rb);
		}
		return (rb);
	}

	@Override
	public void changed(int btn, String lbl) {
		super.changed(btn, lbl);
		CFGRadioButton radioButton = (CFGRadioButton) btns.get(btn);
		if (radioButton.cfg.vali() != radioButton.cfgVal) {
			radioButton.cfg.set(radioButton.cfgVal);
		}
	}
}
