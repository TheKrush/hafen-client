package haven.cfg;

import haven.CFG;
import haven.Coord;
import haven.GOut;
import haven.HSlider;
import haven.Tex;
import haven.Text;
import java.awt.Color;

public class CFGHSlider extends HSlider implements CFG.CFGObserver {

	protected final CFG cfg;
	Text lbl;
	protected final float valuePer;
	public boolean displayRawValue = false;

	public CFGHSlider(String lbl, CFG cfg) {
		this(lbl, cfg, null);
	}

	public CFGHSlider(String lbl, CFG cfg, String tip) {
		this(lbl, cfg, tip, 200);
	}

	public CFGHSlider(String lbl, CFG cfg, String tip, int w) {
		this(lbl, cfg, tip, w, 0, 1000);
	}

	public CFGHSlider(String lbl, CFG cfg, String tip, int w, int min, int max) {
		this(lbl, cfg, tip, w, min, max, max);
	}

	public CFGHSlider(String lbl, CFG cfg, String tip, int w, int min, int max, int valuePer) {
		this(lbl, cfg, tip, w, min, max, valuePer, min);
	}

	public CFGHSlider(String lbl, CFG cfg, String tip, int w, int min, int max, int valuepPer, int val) {
		super(w, min, max, val);

		this.cfg = cfg;
		this.valuePer = valuepPer;
		defval();
		if (lbl != null) {
			this.lbl = Text.std.render(lbl, java.awt.Color.WHITE);
		}
		if (tip != null) {
			tooltip = Text.render(tip).tex();
		}
	}

	protected void defval() {
		val = (int) (this.valuePer * cfg.valf());
	}

	@Override
	public void changed() {
		super.changed();
		cfg.set(val / this.valuePer);
	}

	@Override
	public void draw(GOut g) {
		if (lbl != null) {
			g.image(lbl.tex(), new Coord());

			int offset = Math.max(10, lbl.tex().sz().x);
			int szX = sz.x - offset;
			int cy = (sflarp.sz().y - schain.sz().y) / 2;
			for (int x = offset; x < szX; x += schain.sz().x) {
				g.image(schain, new Coord(x, cy));
			}
			int fx = ((szX - sflarp.sz().x) * (val - min)) / (max - min);
			g.image(sflarp, new Coord(offset + fx, 0));
			drawValue(g);
		} else {
			super.draw(g);
		}
	}

	@Override
	public void drawValue(GOut g) {
		if (displayRawValue) {
			super.drawValue(g);
		} else {
			if (CFG.UI_HSLIDER_VALUE_SHOW.valb()) {
				float v = val / this.valuePer;
				String str = v == (long) v ? String.format("%d", (long) v) : String.format("%.2f", v);
				Tex valueTex = Text.std.renderstroked(str, Color.WHITE, Color.BLACK, new Color(0, 0, 0, 64), new Coord(2, 0)).tex();
				g.image(valueTex, new Coord((sz.x / 2) - (valueTex.sz().x / 2), 0));
			}
		}
	}

	@Override
	public void cfgUpdated(CFG cfg) {
		val = (int) (this.valuePer * cfg.valf());
	}
}
