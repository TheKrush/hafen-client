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

import me.ender.Reflect;

import java.util.*;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ItemInfo {

	static final Pattern count_patt = Pattern.compile("(?:^|[\\s])([0-9]*\\.?[0-9]+\\s*%?)");
	public final Owner owner;

	public interface Owner {

		public Glob glob();

		public List<ItemInfo> info();
	}

	public interface ResOwner extends Owner {

		public Resource resource();
	}

	public interface SpriteOwner extends ResOwner {

		public GSprite sprite();
	}

	@Resource.PublishedCode(name = "tt")
	public static interface InfoFactory {

		public ItemInfo build(Owner owner, Object... args);
	}

	public ItemInfo(Owner owner) {
		this.owner = owner;
	}

	public static class Layout {

		private final List<Tip> tips = new ArrayList<>();
		private final Map<ID, Tip> itab = new HashMap<>();
		public final CompImage cmp = new CompImage();

		public interface ID<T extends Tip> {

			public T make();
		}

		@SuppressWarnings("unchecked")
		public <T extends Tip> T intern(ID<T> id) {
			T ret = (T) itab.get(id);
			if (ret == null) {
				itab.put(id, ret = id.make());
				add(ret);
			}
			return (ret);
		}

		public void add(Tip tip) {
			tips.add(tip);
			tip.prepare(this);
		}

		public BufferedImage render() {
			Collections.sort(tips, new Comparator<Tip>() {
				@Override
				public int compare(Tip a, Tip b) {
					return (a.order() - b.order());
				}
			});
			for (Tip tip : tips) {
				tip.layout(this);
			}
			return (cmp.compose());
		}
	}

	public static abstract class Tip extends ItemInfo {

		public Tip(Owner owner) {
			super(owner);
		}

		@Deprecated
		public BufferedImage longtip() {
			return (null);
		}

		public BufferedImage tipimg() {
			return (longtip());
		}

		public Tip shortvar() {
			return (null);
		}

		public void prepare(Layout l) {
		}

		public void layout(Layout l) {
			BufferedImage t = tipimg();
			if (t != null) {
				l.cmp.add(tipimg(), new Coord(0, l.cmp.sz.y));
			}
		}

		public int order() {
			return (100);
		}
	}

	public static class AdHoc extends Tip {

		public final Text str;

		public AdHoc(Owner owner, String str) {
			super(owner);
			this.str = Text.render(str);
		}

		@Override
		public BufferedImage tipimg() {
			return (str.img);
		}
	}

	public static class Name extends Tip {

		public final Text str;

		public Name(Owner owner, Text str) {
			super(owner);
			this.str = str;
		}

		public Name(Owner owner, String str) {
			this(owner, Text.render(str));
		}

		@Override
		public BufferedImage tipimg() {
			return (str.img);
		}

		@Override
		public int order() {
			return (0);
		}

		@Override
		public Tip shortvar() {
			return (new Tip(owner) {
				@Override
				public BufferedImage tipimg() {
					return (str.img);
				}

				@Override
				public int order() {
					return (0);
				}
			});
		}
	}

	public static class Contents extends Tip {

		public final List<ItemInfo> sub;
		private static final Text.Line ch = Text.render("Contents:");

		public Contents(Owner owner, List<ItemInfo> sub) {
			super(owner);
			this.sub = sub;
		}

		@Override
		public BufferedImage tipimg() {
			BufferedImage stip = longtip(sub);
			BufferedImage img = TexI.mkbuf(new Coord(stip.getWidth() + 10, stip.getHeight() + 15));
			Graphics g = img.getGraphics();
			g.drawImage(ch.img, 0, 0, null);
			g.drawImage(stip, 10, 15, null);
			g.dispose();
			return (img);
		}

		@Override
		public Tip shortvar() {
			return (new Tip(owner) {
				@Override
				public BufferedImage tipimg() {
					return (shorttip(sub));
				}

				@Override
				public int order() {
					return (100);
				}
			});
		}
	}

	public static BufferedImage catimgs(int margin, BufferedImage... imgs) {
		return catimgs(margin, false, imgs);
	}

	public static BufferedImage catimgs(int margin, boolean right, BufferedImage... imgs) {
		int w = 0, h = -margin;
		for (BufferedImage img : imgs) {
			if (img == null) {
				continue;
			}
			if (img.getWidth() > w) {
				w = img.getWidth();
			}
			h += img.getHeight() + margin;
		}
		BufferedImage ret = TexI.mkbuf(new Coord(w, h));
		Graphics g = ret.getGraphics();
		int y = 0;
		for (BufferedImage img : imgs) {
			if (img == null) {
				continue;
			}
			g.drawImage(img, right ? w - img.getWidth() : 0, y, null);
			y += img.getHeight() + margin;
		}
		g.dispose();
		return (ret);
	}

	public static BufferedImage catimgsh(int margin, BufferedImage... imgs) {
		int w = -margin, h = 0;
		for (BufferedImage img : imgs) {
			if (img == null) {
				continue;
			}
			if (img.getHeight() > h) {
				h = img.getHeight();
			}
			w += img.getWidth() + margin;
		}
		BufferedImage ret = TexI.mkbuf(new Coord(w, h));
		Graphics g = ret.getGraphics();
		int x = 0;
		for (BufferedImage img : imgs) {
			if (img == null) {
				continue;
			}
			g.drawImage(img, x, (h - img.getHeight()) / 2, null);
			x += img.getWidth() + margin;
		}
		g.dispose();
		return (ret);
	}

	public static BufferedImage longtip(List<ItemInfo> info) {
		Layout l = new Layout();
		for (ItemInfo ii : info) {
			if (ii instanceof Tip) {
				Tip tip = (Tip) ii;
				l.add(tip);
			}
		}
		if (l.tips.size() < 1) {
			return (null);
		}
		return (l.render());
	}

	public static BufferedImage shorttip(List<ItemInfo> info) {
		Layout l = new Layout();
		for (ItemInfo ii : info) {
			if (ii instanceof Tip) {
				Tip tip = ((Tip) ii).shortvar();
				if (tip != null) {
					l.add(tip);
				}
			}
		}
		if (l.tips.size() < 1) {
			return (null);
		}
		return (l.render());
	}

	public static <T> T find(Class<T> cl, List<ItemInfo> il) {
		for (ItemInfo inf : il) {
			if (cl.isInstance(inf)) {
				return (cl.cast(inf));
			}
		}
		return (null);
	}

	public static <T> List<T> findall(Class<T> cl, List<ItemInfo> il) {
		List<T> ret = new LinkedList<>();
		for (ItemInfo inf : il) {
			if (cl.isInstance(inf)) {
				ret.add(cl.cast(inf));
			}
		}
		return ret;
	}

	public static List<ItemInfo> findall(String cl, List<ItemInfo> il) {
		List<ItemInfo> ret = new LinkedList<>();
		for (ItemInfo inf : il) {
			String name = inf.getClass().getName();
			if (cl.equals(name)) {
				ret.add(inf);
			}
		}
		return ret;
	}

	public static List<ItemInfo> buildinfo(Owner owner, Object[] rawinfo) {
		List<ItemInfo> ret = new ArrayList<>();
		for (Object o : rawinfo) {
			if (o instanceof Object[]) {
				Object[] a = (Object[]) o;
				Resource ttres;
				if (a[0] instanceof Integer) {
					ttres = owner.glob().sess.getres((Integer) a[0]).get();
				} else if (a[0] instanceof Resource) {
					ttres = (Resource) a[0];
				} else if (a[0] instanceof Indir) {
					ttres = (Resource) ((Indir) a[0]).get();
				} else {
					throw (new ClassCastException("Unexpected info specification " + a[0].getClass()));
				}
				InfoFactory f = ttres.getcode(InfoFactory.class, true);
				ItemInfo inf = f.build(owner, a);
				if (inf != null) {
					ret.add(inf);
				}
			} else if (o instanceof String) {
				ret.add(new AdHoc(owner, (String) o));
			} else {
				throw (new ClassCastException("Unexpected object type " + o.getClass() + " in item info array."));
			}
		}
		return (ret);
	}

	public static String getCount(List<ItemInfo> infos) {
		String res = null;
		for (ItemInfo info : infos) {
			if (info instanceof Contents) {
				Contents cnt = (Contents) info;
				res = getCount(cnt.sub);
			} else if (info instanceof AdHoc) {
				AdHoc ah = (AdHoc) info;
				try {
					Matcher m = count_patt.matcher(ah.str.text);
					if (m.find()) {
						res = m.group(1);
					}
				} catch (Exception ignored) {
				}
			} else if (info instanceof Name) {
				Name name = (Name) info;
				try {
					Matcher m = count_patt.matcher(name.str.text);
					if (m.find()) {
						res = m.group(1);
					}
				} catch (Exception ignored) {
				}
			}
			if (res != null) {
				return res.trim();
			}
		}
		return null;
	}

	public static Wear getWear(List<ItemInfo> infos) {
		infos = findall("Wear", infos);
		for (ItemInfo info : infos) {
			if (Reflect.hasField(info, "m") && Reflect.hasField(info, "d")) {
				return new Wear(Reflect.getFieldValueInt(info, "d"), Reflect.getFieldValueInt(info, "m"));
			}
		}
		return null;
	}

	public static Wear getArmor(List<ItemInfo> infos) {
	//loftar is wonderful sunshine and has same class name for wear and armor tooltips even though
		//they are different classes with different fields :)
		infos = findall("Wear", infos);
		for (ItemInfo info : infos) {
			if (Reflect.hasField(info, "hard") && Reflect.hasField(info, "soft")) {
				return new Wear(Reflect.getFieldValueInt(info, "hard"), Reflect.getFieldValueInt(info, "soft"));
			}
		}
		return null;
	}

	public static class Wear {

		public final int a, b;

		public Wear(int a, int b) {
			this.a = a;
			this.b = b;
		}
	}

	private static String dump(Object arg) {
		if (arg instanceof Object[]) {
			StringBuilder buf = new StringBuilder();
			buf.append("[");
			boolean f = true;
			for (Object a : (Object[]) arg) {
				if (!f) {
					buf.append(", ");
				}
				buf.append(dump(a));
				f = false;
			}
			buf.append("]");
			return (buf.toString());
		} else {
			return (arg.toString());
		}
	}
}
