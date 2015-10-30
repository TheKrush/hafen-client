package haven.cfg;

import haven.CFG;
import haven.Coord;
import haven.RadioGroup;
import haven.Text;

public class CFGRadioGroup extends RadioGroup {

	public CFGRadioGroup() {
		super();
	}

	public class CFGRadioButton extends RadioGroup.RadioButton implements CFGObserver {

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

	public CFGRadioButton add(String lbl, CFG cfg, int val) {
		return add(new CFGRadioButton(lbl, cfg, val));
	}

	public CFGRadioButton add(String lbl, CFG cfg, int val, String tip) {
		return add(new CFGRadioButton(lbl, cfg, val, tip));
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
