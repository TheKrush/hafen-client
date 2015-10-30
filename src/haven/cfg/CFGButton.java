package haven.cfg;

import haven.Button;
import haven.CFG;
import haven.Text;

public class CFGButton extends Button implements CFGObserver {

	protected final CFG cfg;

	public CFGButton(String lbl, CFG cfg, int w) {
		this(lbl, cfg, w, null);
	}

	public CFGButton(String lbl, CFG cfg, int w, String tip) {
		super(w, lbl);

		this.cfg = cfg;
		defval();
		if (tip != null) {
			tooltip = Text.render(tip).tex();
		}
	}

	@Override
	public void click() {
	}

	protected void defval() {
	}

	@Override
	public void cfgUpdated(CFG cfg) {
		defval();
	}
}
