package haven.cfg;

import haven.CFG;
import haven.CheckBox;
import haven.Text;

public class CFGCheckBox extends CheckBox implements CFG.CFGObserver {

	protected final CFG cfg;

	public CFGCheckBox(String lbl, CFG cfg) {
		this(lbl, cfg, null);
	}

	public CFGCheckBox(String lbl, CFG cfg, String tip) {
		super(lbl);

		this.cfg = cfg;
		defval();
		if (tip != null) {
			tooltip = Text.render(tip).tex();
		}
	}

	protected void defval() {
		super.set(cfg.valb());
	}

	@Override
	public void set(boolean a) {
		super.set(a);
		cfg.set(a);
	}

	@Override
	public void cfgUpdated(CFG cfg) {
		super.set(cfg.valb());
	}

	//@Override
	//public void destroy() {
	//	GobPathOptWnd.remove();
	//	super.destroy();
	//}
}
