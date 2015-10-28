package haven.cfg;

import haven.Label;
import haven.Text;

public class CFGLabel extends Label {

	public CFGLabel(String lbl) {
		this(lbl, null);
	}

	public CFGLabel(String lbl, String tip) {
		super(lbl);

		if (tip != null) {
			tooltip = Text.render(tip).tex();
		}
	}
}
