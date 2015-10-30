package haven.cfg;

import haven.Label;
import haven.Text;

public class CFGLabel extends Label {

	public CFGLabel(String lbl) {
		this(lbl, Text.std);
	}

	public CFGLabel(String lbl, String tip) {
		this(lbl, Text.std, tip);
	}

	public CFGLabel(String lbl, Text.Foundry f) {
		this(lbl, f, null);
	}

	public CFGLabel(String lbl, Text.Foundry f, String tip) {
		super(lbl, f);

		if (tip != null) {
			tooltip = Text.render(tip).tex();
		}
	}
}
