/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */
package haven;

import static haven.Inventory.sqsz;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

public class WItem extends Widget implements DTarget {

	public static final Resource missing = Resource.local().loadwait("gfx/invobjs/missing");
	public static final Coord TEXT_PADD_TOP = new Coord(0, -3), TEXT_PADD_BOT = new Coord(0, 2);
	public static final Color DURABILITY_COLOR = new Color(214, 253, 255);
	public static final Color ARMOR_COLOR = new Color(255, 227, 191);
	public final GItem item;
	private Resource cspr = null;
	private Message csdt = Message.nil;

	public WItem(GItem item) {
		super(sqsz);
		this.item = item;
	}

	public void drawmain(GOut g, GSprite spr) {
		spr.draw(g);
	}

	public static BufferedImage shorttip(List<ItemInfo> info) {
		return (ItemInfo.shorttip(info));
	}

	public static BufferedImage longtip(GItem item, List<ItemInfo> info) {
		BufferedImage img = ItemInfo.longtip(info);
		Resource.Pagina pg = item.res.get().layer(Resource.pagina);
		if (pg != null) {
			img = ItemInfo.catimgs(0, img, RichText.render("\n" + pg.text, 200).img);
		}
		return (img);
	}

	public BufferedImage longtip(List<ItemInfo> info) {
		return (longtip(item, info));
	}

	public class ItemTip implements Indir<Tex> {

		private final TexI tex;

		public ItemTip(BufferedImage img) {
			if (img == null) {
				throw (new Loading());
			}
			tex = new TexI(img);
		}

		public GItem item() {
			return (item);
		}

		@Override
		public Tex get() {
			return (tex);
		}
	}

	public class ShortTip extends ItemTip {

		public ShortTip(List<ItemInfo> info) {
			super(shorttip(info));
		}
	}

	public class LongTip extends ItemTip {

		public LongTip(List<ItemInfo> info) {
			super(longtip(info));
		}
	}

	private long hoverstart;
	private ItemTip shorttip = null, longtip = null;
	private List<ItemInfo> ttinfo = null;

	@Override
	public Object tooltip(Coord c, Widget prev) {
		long now = System.currentTimeMillis();
		if (prev == this) {
		} else if (prev instanceof WItem) {
			long ps = ((WItem) prev).hoverstart;
			if (now - ps < 1000) {
				hoverstart = now;
			} else {
				hoverstart = ps;
			}
		} else {
			hoverstart = now;
		}
		try {
			List<ItemInfo> info = item.info();
			if (info.size() < 1) {
				return (null);
			}
			if (info != ttinfo) {
				shorttip = longtip = null;
				ttinfo = info;
			}
			if (!CFG.UI_TOOLTIP_LONG.valb() && now - hoverstart < 1000) {
				if (shorttip == null) {
					shorttip = new ShortTip(info);
				}
				return (shorttip);
			} else {
				if (longtip == null) {
					longtip = new LongTip(info);
				}
				return (longtip);
			}
		} catch (Loading e) {
			return ("...");
		}
	}

	public abstract class AttrCache<T> {

		private List<ItemInfo> forinfo = null;
		private T save = null;

		public T get() {
			try {
				List<ItemInfo> info = item.info();
				if (info != forinfo) {
					save = find(info);
					forinfo = info;
				}
			} catch (Loading e) {
				return (null);
			}
			return (save);
		}

		protected abstract T find(List<ItemInfo> info);
	}

	public final AttrCache<Color> olcol = new AttrCache<Color>() {
		@Override
		protected Color find(List<ItemInfo> info) {
			GItem.ColorInfo cinf = ItemInfo.find(GItem.ColorInfo.class, info);
			return ((cinf == null) ? null : cinf.olcol());
		}
	};

	public final AttrCache<Tex> itemnum = new AttrCache<Tex>() {
		@Override
		protected Tex find(List<ItemInfo> info) {
			GItem.NumberInfo ninf = ItemInfo.find(GItem.NumberInfo.class, info);
			if (ninf == null) {
				return (null);
			}
			return (new TexI(Utils.outline2(Text.render(Integer.toString(ninf.itemnum()), Color.WHITE).img, Color.BLACK)));
		}
	};

	public final AttrCache<QualityList> itemq = new AttrCache<QualityList>() {
		@Override
		protected QualityList find(List<ItemInfo> info) {
			List<ItemInfo.Contents> contents = ItemInfo.findall(ItemInfo.Contents.class, info);
			List<ItemInfo> qualities = null;
			if (CFG.UI_ITEM_QUALITY_CONTENTS.valb() && !contents.isEmpty()) {
				for (ItemInfo.Contents content : contents) {
					List<ItemInfo> tmp = ItemInfo.findall(QualityList.classname, content.sub);
					if (!tmp.isEmpty()) {
						qualities = tmp;
					}
				}
			}
			if (qualities == null || qualities.isEmpty()) {
				qualities = ItemInfo.findall(QualityList.classname, info);
			}

			QualityList qualityList = new QualityList(qualities);
			return !qualityList.isEmpty() ? qualityList : null;
		}
	};

	public final AttrCache<Tex> heurnum = new AttrCache<Tex>() {
		@Override
		protected Tex find(List<ItemInfo> info) {
			String num = ItemInfo.getCount(info);
			if (num == null) {
				return (null);
			}
			return Text.renderstroked(num, Color.WHITE, Color.BLACK).tex();
		}
	};

	public final AttrCache<Tex> durability = new AttrCache<Tex>() {
		@Override
		public Tex get() {
			return CFG.UI_ITEM_DURABILITY.valb() ? super.get() : null;
		}

		@Override
		protected Tex find(List<ItemInfo> info) {
			ItemInfo.Wear wear = ItemInfo.getWear(info);
			if (wear == null) {
				return (null);
			}
			return Text.renderstroked(String.valueOf(wear.b - wear.a), DURABILITY_COLOR).tex();
		}
	};

	public final AttrCache<ItemInfo.Wear> wear = new AttrCache<ItemInfo.Wear>() {
		@Override
		protected ItemInfo.Wear find(List<ItemInfo> info) {
			return ItemInfo.getWear(info);
		}
	};

	public final AttrCache<Tex> armor = new AttrCache<Tex>() {
		@Override
		public Tex get() {
			return CFG.UI_ITEM_ARMOR.valb() ? super.get() : null;
		}

		@Override
		protected Tex find(List<ItemInfo> info) {
			ItemInfo.Wear wear = ItemInfo.getArmor(info);
			if (wear == null) {
				return (null);
			}
			return Text.renderstroked(String.format("%d/%d", wear.a, wear.b), ARMOR_COLOR).tex();
		}
	};

	private GSprite lspr = null;

	@Override
	public void tick(double dt) {
		/* XXX: This is ugly and there should be a better way to
		 * ensure the resizing happens as it should, but I can't think
		 * of one yet. */
		GSprite spr = item.spr();
		if ((spr != null) && (spr != lspr)) {
			Coord sz = new Coord(spr.sz());
			if ((sz.x % sqsz.x) != 0) {
				sz.x = sqsz.x * ((sz.x / sqsz.x) + 1);
			}
			if ((sz.y % sqsz.y) != 0) {
				sz.y = sqsz.y * ((sz.y / sqsz.y) + 1);
			}
			resize(sz);
			lspr = spr;
		}
	}

	@Override
	public void draw(GOut g) {
		GSprite spr = item.spr();
		if (spr != null) {
			Coord sz = spr.sz();
			g.defstate();
			if (olcol.get() != null) {
				g.usestate(new ColorMask(olcol.get()));
			}
			drawbars(g, sz);
			drawmain(g, spr);
			g.defstate();
			drawnum(g, sz);
			drawmeter(g, sz);
			drawq(g);
		} else {
			g.image(missing.layer(Resource.imgc).tex(), Coord.z, sz);
		}
	}

	private void drawmeter(GOut g, Coord sz) {
		if (item.meter > 0) {
			int meter = item.meter;
			if (CFG.UI_ITEM_METER_COUNTDOWN.valb()) {
				meter = 100 - meter;
			}
			double a = ((double) meter) / 100.0;

			Color color = new Color(
							Math.round(255 * CFG.UI_ITEM_METER_RED.valf()),
							Math.round(255 * CFG.UI_ITEM_METER_GREEN.valf()),
							Math.round(255 * CFG.UI_ITEM_METER_BLUE.valf()),
							Math.round(255 * CFG.UI_ITEM_METER_ALPHA.valf()));

			g.chcolor(color);
			switch (CFG.UI_ITEM_METER_SHOW.vali()) {
				case 0:
					break;
				case 1:
					Coord half = sz.div(2);
					g.prect(half, half.inv(), half, a * Math.PI * 2);
					break;
				case 2:
					g.frect(Coord.z, new Coord((int) (sz.x / (100 / (double) (a * 100))), 4));
					break;
				case 3:
					Tex tex = Text.renderstroked(String.format("%d%%", meter), color).tex();
					g.aimage(tex, sz.div(2), 0.5, 0.5);
					tex.dispose();
					break;
			}
			g.chcolor();
		}
	}

	private void drawbars(GOut g, Coord sz) {
		float bar = 0f;

		if (CFG.UI_ITEM_BAR_WEAR.valb() && this.wear.get() != null) {
			ItemInfo.Wear wear = this.wear.get();
			if (wear.a > 0) {
				bar = (float) (wear.b - wear.a) / wear.b;
			}
		}

		if (bar > 0) {
			g.chcolor(Utils.blendcol(Color.RED, Color.GREEN, bar));
			int h = (int) (sz.y * bar);
			g.frect(new Coord(0, sz.y - h), new Coord(4, h));
			g.chcolor();
		}

	}

	private void drawnum(GOut g, Coord sz) {
		Tex tex;
		if (item.num >= 0) {
			tex = Text.render(Integer.toString(item.num)).tex();
		} else {
			tex = chainattr(itemnum, heurnum, armor, durability);
		}
		if (tex != null) {
			if (CFG.UI_ITEM_QUALITY_SWAP.valb()) {
				g.aimage(tex, TEXT_PADD_TOP.add(sz.x, 0), 1, 0);
			} else {
				g.aimage(tex, TEXT_PADD_BOT.add(sz), 1, 1);
			}
		}
	}

	@SafeVarargs //actually, method just assumes you'll feed it correctly typed var args
	private static Tex chainattr(AttrCache<Tex>... attrs) {
		for (AttrCache<Tex> attr : attrs) {
			Tex tex = attr.get();
			if (tex != null) {
				return tex;
			}
		}
		return null;
	}

	private void drawq(GOut g) {
		QualityList quality = itemq.get();
		if (quality != null) {
			Tex tex = null;
			if ((ui.modflags() & CFG.HOTKEY_ITEM_QUALITY.vali()) != 0) {
				tex = quality.tex();
			} else if (!quality.isEmpty()) {
				switch (CFG.UI_ITEM_QUALITY_SHOW.vali()) {
					case 0:
						break;
					case 6:
						tex = quality.tex();
						break;
					default:
						tex = quality.tex(quality.single());
						break;
				}
			}

			if (tex != null) {
				if (CFG.UI_ITEM_QUALITY_SWAP.valb()) {
					g.aimage(tex, TEXT_PADD_BOT.add(sz), 1, 1);
				} else {
					g.aimage(tex, TEXT_PADD_TOP.add(sz.x, 0), 1, 0);
				}
			}
		}
	}

	@Override
	public boolean mousedown(Coord c, int btn) {
		if (checkXfer(btn)) {
			return (true);
		} else if (btn == 1) {
			item.wdgmsg("take", c);
			return (true);
		} else if (btn == 3) {
			item.wdgmsg("iact", c, ui.modflags());
			return (true);
		}
		return (false);
	}

	private boolean checkXfer(int button) {
		boolean inv = parent instanceof Inventory;
		switch (button) {
			case 1: // left
				if (ui.modflags() == CFG.HOTKEY_ITEM_TRANSFER_IN.vali()) {
					item.wdgmsg("transfer", c);
					return (true);
				} else if (ui.modflags() == CFG.HOTKEY_ITEM_TRANSFER_OUT.vali()) {
					item.wdgmsg("drop", c);
					return (true);
				}
				break;
			case 2: // middle
			case 3: // right
				if (ui.modflags() == CFG.HOTKEY_ITEM_TRANSFER_IN.vali()) {
					if (inv) {
						wdgmsg("transfer-same", item, button == 2);
						return (true);
					}
				} else if (ui.modflags() == CFG.HOTKEY_ITEM_TRANSFER_OUT.vali()) {
					if (inv) {
						wdgmsg("drop-same", item, button == 2);
						return (true);
					}
				}
				break;
		}
		return (false);
	}

	@Override
	public boolean drop(Coord cc, Coord ul) {
		return (false);
	}

	@Override
	public boolean iteminteract(Coord cc, Coord ul) {
		item.wdgmsg("itemact", ui.modflags());
		return (true);
	}
}
