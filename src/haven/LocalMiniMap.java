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

import static haven.MCache.cmaps;
import static haven.MCache.tilesz;
import haven.MCache.Grid;
import haven.resutil.Ridges;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;

public class LocalMiniMap extends Widget {

	private static final Tex mapgrid = Resource.loadtex("gfx/hud/mmap/mapgrid");
	public static final Coord VIEW_SZ = MCache.sgridsz.mul(9).div(tilesz);// view radius is 9x9 "server" grids
	public static final Color VIEW_BG_COLOR = new Color(255, 255, 255, 60);
	public static final Color VIEW_BORDER_COLOR = new Color(0, 0, 0, 128);

	public final MapView mv;
	private Coord cc = null;
	private final Map<Coord, Defer.Future<MapTile>> cache = new LinkedHashMap<Coord, Defer.Future<MapTile>>(5, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<Coord, Defer.Future<MapTile>> eldest) {
			if (size() > 100) {
				try {
					MapTile t = eldest.getValue().get();
					t.img.dispose();
				} catch (RuntimeException ignored) {
				}
				return (true);
			}
			return (false);
		}
	};
	private Coord off = new Coord(0, 0);
	private Coord doff = null;
	private String biome;
	private Tex biometex = null;

	private static final Map<String, Tex> iconTexMap = new HashMap<String, Tex>(3) {
		{
			put("bumling", Text.renderstroked("\u25C6", Color.CYAN, Color.BLACK).tex());
			put("bush", Text.renderstroked("\u2605", Color.CYAN, Color.BLACK).tex());
			put("tree", Text.renderstroked("\u25B2", Color.CYAN, Color.BLACK).tex());
		}
	};

	private final Map<Color, Tex> playerTex = new HashMap<>();
	private final Map<Color, Tex> partyTex = new HashMap<>();

	public static class MapTile {

		public final Tex img;
		public final Coord ul, c;
		public final Grid grid;
		public final int seq;

		public MapTile(Tex img, Coord ul, Coord c, Grid grid, int seq) {
			this.img = img;
			this.ul = ul;
			this.c = c;
			this.grid = grid;
			this.seq = seq;
		}
	}

	private BufferedImage tileimg(int t, BufferedImage[] texes) {
		BufferedImage img = texes[t];
		if (img == null) {
			Resource r = ui.sess.glob.map.tilesetr(t);
			if (r == null) {
				return (null);
			}
			Resource.Image ir = r.layer(Resource.imgc);
			if (ir == null) {
				return (null);
			}
			img = ir.img;
			texes[t] = img;
		}
		return (img);
	}

	public BufferedImage drawmap(Coord ul, Coord sz) {
		BufferedImage[] texes = new BufferedImage[256];
		MCache m = ui.sess.glob.map;
		BufferedImage buf = TexI.mkbuf(sz);
		Coord c = new Coord();
		for (c.y = 0; c.y < sz.y; c.y++) {
			for (c.x = 0; c.x < sz.x; c.x++) {
				int t = m.gettile(ul.add(c));
				BufferedImage tex = tileimg(t, texes);
				int rgb = 0;
				if (tex != null) {
					rgb = tex.getRGB(Utils.floormod(c.x + ul.x, tex.getWidth()),
									Utils.floormod(c.y + ul.y, tex.getHeight()));
				}
				buf.setRGB(c.x, c.y, rgb);

				try {
					if ((m.gettile(ul.add(c).add(-1, 0)) > t)
									|| (m.gettile(ul.add(c).add(1, 0)) > t)
									|| (m.gettile(ul.add(c).add(0, -1)) > t)
									|| (m.gettile(ul.add(c).add(0, 1)) > t)) {
						buf.setRGB(c.x, c.y, Color.BLACK.getRGB());
					}
				} catch (Exception e) {
				}
			}
		}

		for (c.y = 1; c.y < sz.y - 1; c.y++) {
			for (c.x = 1; c.x < sz.x - 1; c.x++) {
				try {
					int t = m.gettile(ul.add(c));
					Tiler tl = m.tiler(t);
					if (tl instanceof Ridges.RidgeTile) {
						if (Ridges.brokenp(m, ul.add(c))) {
							for (int y = c.y - 1; y <= c.y + 1; y++) {
								for (int x = c.x - 1; x <= c.x + 1; x++) {
									Color cc = new Color(buf.getRGB(x, y));
									buf.setRGB(x, y, Utils.blendcol(cc, Color.BLACK, ((x == c.x) && (y == c.y)) ? 1 : 0.1).getRGB());
								}
							}
						}
					}
				} catch (Exception e) {
				}
			}
		}

		return (buf);
	}

	public LocalMiniMap(Coord sz, MapView mv) {
		super(sz);
		this.mv = mv;
	}

	public Coord p2c(Coord pc) {
		return (pc.div(tilesz).sub(cc.add(off)).add(sz.div(2)));
	}

	public Coord c2p(Coord c) {
		return (c.sub(sz.div(2)).add(cc.add(off)).mul(tilesz).add(tilesz.div(2)));
	}

	public void drawicons(GOut g) {
		switch (CFG.MINIMAP_RADAR.vali()) {
			case 0: // default
			{
				OCache oc = ui.sess.glob.oc;
				synchronized (oc) {
					for (Gob gob : oc) {
						try {
							GobIcon icon = gob.getattr(GobIcon.class);
							Coord gc = p2c(gob.rc);
							if (icon != null) {
								Tex tex = icon.tex();
								g.image(tex, gc.sub(tex.sz().div(2)));
							}
						} catch (Loading l) {
						}
					}
				}
				break;
			}
			case 1: // romov
			{
				OCache oc = ui.sess.glob.oc;
				synchronized (oc) {
					for (Gob gob : oc) {
						try {
							Resource res = gob.getres();

							GobIcon icon = gob.getattr(GobIcon.class);
							Coord gc = p2c(gob.rc);
							if (icon != null) {
								Tex tex = icon.tex();
								g.image(tex, gc.sub(tex.sz().div(2)));
							} else if (res != null) {
								String basename = res.basename().replaceAll("\\d*$", "");
								if ("body".equals(basename)) {
									if (CFG.MINIMAP_PLAYERS.valb()) {
										KinInfo kininfo = gob.getattr(KinInfo.class);
										if (gob.id == mv.player().id) {
										} else if (!ui.sess.glob.party.memberGobIds().contains(gob.id)) {
											Color cl = kininfo != null ? BuddyWnd.gc[kininfo.group] : Color.DARK_GRAY;
											if (!playerTex.containsKey(cl)) {
												playerTex.put(cl, Tex.fellipse(new Coord(10, 10), cl, Color.BLACK, 1));
											}
											Tex tex = playerTex.get(cl);
											g.image(tex, gc.sub(tex.sz().div(2)));
										}
									}
								} else if (res.name.startsWith("gfx/terobjs/bumlings")) {
									Map<String, Boolean> mapVal = CFG.MINIMAP_BUMLINGS.<Map<String, Boolean>>valo();
									Tex tex = iconTexMap.get("bumling");
									if (mapVal.containsValue(true) && mapVal.containsKey(basename) && mapVal.get(basename)) {
										g.image(tex, gc.sub(tex.sz().div(2)));
									}
								} else if (res.name.startsWith("gfx/terobjs/bushes")) {
									Map<String, Boolean> mapVal = CFG.MINIMAP_BUSHES.<Map<String, Boolean>>valo();
									Tex tex = iconTexMap.get("bush");
									if (mapVal.containsValue(true) && mapVal.containsKey(basename) && mapVal.get(basename)) {
										g.image(tex, gc.sub(tex.sz().div(2)));
									}
								} else if (res.name.startsWith("gfx/terobjs/trees")) {
									Map<String, Boolean> mapVal = CFG.MINIMAP_TREES.<Map<String, Boolean>>valo();
									Tex tex = iconTexMap.get("tree");
									if (mapVal.containsValue(true) && mapVal.containsKey(basename) && mapVal.get(basename)) {
										g.image(tex, gc.sub(tex.sz().div(2)));
									}
								}
							}
						} catch (Loading l) {
						}
					}
				}
				break;
			}
			case 2: // ender
				synchronized (Radar.markers) {
					for (Radar.Marker marker : Radar.markers) {
						if (marker.gob.id == mv.plgob) {
							continue;
						}
						try {
							Coord gc = p2c(marker.gob.rc);
							Tex tex = marker.tex();
							if (tex != null) {
								g.chcolor(marker.color());
								g.aimage(tex, gc, 0.5, 0.5);
							}
						} catch (Loading ignored) {
						}
					}
				}
				g.chcolor();
				break;
		}
	}

	@Override
	public Object tooltip(Coord c, Widget prev) {
		Gob gob = findicongob(c);
		if (gob != null) {
			Radar.Marker icon = gob.getattr(Radar.Marker.class);
			if (icon != null) {
				return icon.tooltip(ui.modshift);
			}
		}
		return super.tooltip(c, prev);
	}

	public Gob findicongob(Coord c) {
		switch (CFG.MINIMAP_RADAR.vali()) {
			case 0: // default
			case 1: // romov
				OCache oc = ui.sess.glob.oc;
				synchronized (oc) {
					for (Gob gob : oc) {
						try {
							GobIcon icon = gob.getattr(GobIcon.class);
							if (icon != null) {
								Coord gc = p2c(gob.rc);
								Coord sz = icon.tex().sz();
								if (c.isect(gc.sub(sz.div(2)), sz)) {
									return (gob);
								}
							}
						} catch (Loading l) {
						}
					}
				}
				break;
			case 2: // ender
				synchronized (Radar.markers) {
					ListIterator<Radar.Marker> li = Radar.markers.listIterator(Radar.markers.size());
					while (li.hasPrevious()) {
						Radar.Marker icon = li.previous();
						try {
							Gob gob = icon.gob;
							if (gob.id == mv.plgob) {
								continue;
							}
							Tex tex = icon.tex();
							if (tex != null) {
								Coord gc = p2c(gob.rc);
								Coord sz = tex.sz();
								if (c.isect(gc.sub(sz.div(2)), sz)) {
									return (gob);
								}
							}
						} catch (Loading ignored) {
						}
					}
				}
				break;
		}

		return (null);
	}

	@Override
	public void tick(double dt) {
		Gob pl = ui.sess.glob.oc.getgob(mv.plgob);
		if (pl == null) {
			this.cc = null;
			return;
		}
		this.cc = pl.rc.div(tilesz);

		Coord mc = rootxlate(ui.mc);
		if (mc.isect(Coord.z, sz)) {
			setBiome(c2p(mc).div(tilesz));
		} else {
			setBiome(cc);
		}
	}

	private void setBiome(Coord c) {
		try {
			if (c.div(cmaps).manhattan2(cc.div(cmaps)) > 1) {
				return;
			}
			int t = mv.ui.sess.glob.map.gettile(c);
			Resource r = ui.sess.glob.map.tilesetr(t);
			String newbiome;
			if (r != null) {
				newbiome = (r.name);
			} else {
				newbiome = "Void";
			}
			if (!newbiome.equals(biome)) {
				biome = newbiome;
				biometex = Text.renderstroked(prettybiome(biome)).tex();
			}
		} catch (Loading ignored) {
		}
	}

	@Override
	public void draw(GOut g) {
		if (cc == null) {
			return;
		}
		Coord plg = cc.div(cmaps);

		Coord center = cc.add(off);
		Coord hsz = sz.div(2);

		Coord ulg = center.sub(hsz).div(cmaps);
		Coord brg = center.add(hsz).div(cmaps);

		Coord cur = new Coord();
		for (cur.x = ulg.x; cur.x <= brg.x; cur.x++) {
			for (cur.y = ulg.y; cur.y <= brg.y; cur.y++) {
				Defer.Future<MapTile> f;
				synchronized (cache) {
					f = cache.get(cur);
					if (cur.manhattan2(plg) <= 1) {
						final Grid grid;
						try {
							grid = ui.sess.glob.map.getgrid(new Coord(cur));
						} catch (Loading e) {
							continue;
						}
						final int seq = grid.seq;
						if (f == null || (f.done() && (f.get().grid.id != grid.id || f.get().seq != seq))) {
							final Coord tmp = new Coord(cur);
							f = Defer.later(new Defer.Callable<MapTile>() {
								@Override
								public MapTile call() {
									Coord ul = tmp.mul(cmaps);
									BufferedImage drawmap = drawmap(ul, cmaps);
									return (new MapTile(new TexI(drawmap), ul, tmp, grid, seq));
								}
							});
							cache.put(tmp, f);
						}
					}
				}
				if (f != null && f.done()) {
					MapTile map = f.get();
					Coord tc = map.ul.sub(center).add(hsz);
					//g.image(MiniMap.bg, tc);
					g.image(map.img, tc);
					if (CFG.MINIMAP_GRID.valb()) {
						g.image(mapgrid, tc);
					}
				}
			}
		}

		if (CFG.MINIMAP_VIEW.valb()) {
			Gob pl = ui.sess.glob.oc.getgob(mv.plgob);
			if (pl != null) {
				Coord rc = p2c(pl.rc.div(MCache.sgridsz).sub(4, 4).mul(MCache.sgridsz));
				g.chcolor(VIEW_BG_COLOR);
				g.frect(rc, VIEW_SZ);
				g.chcolor(VIEW_BORDER_COLOR);
				g.rect(rc, VIEW_SZ);
				g.chcolor();
			}
		}

		drawicons(g);

		try {
			synchronized (ui.sess.glob.party.memb) {
				for (Party.Member m : ui.sess.glob.party.memb.values()) {
					Coord ptc;
					try {
						ptc = m.getc();
					} catch (MCache.LoadingMap e) {
						ptc = null;
					}
					if (ptc == null) {
						continue;
					}
					ptc = p2c(ptc);
					switch (CFG.MINIMAP_RADAR.vali()) {
						case 0: // default
						case 1: // romov
							if (!partyTex.containsKey(m.col)) {
								partyTex.put(m.col, Tex.frect(new Coord(8, 8), m.col, Color.BLACK, 1));
							}
							Tex tex = partyTex.get(m.col);
							g.image(tex, ptc.sub(tex.sz().div(2)));
							break;
						case 2: // ender
							g.chcolor(m.col);
							g.aimage(MiniMap.plx.layer(Resource.imgc).tex(), ptc, 0.5, 0.5);
							g.chcolor();
							break;
					}
				}
			}
		} catch (Loading ignored) {
		}

		if (CFG.MINIMAP_BIOME_SHOW.valb()) {
			Coord mc = rootxlate(ui.mc);
			if (mc.isect(Coord.z, sz)) {
				setBiome(c2p(mc).div(tilesz));
			} else {
				setBiome(cc);
			}
			if (biometex != null) {
				g.image(biometex, Coord.z);
			}
		}
	}

	@Override
	public boolean mousedown(Coord c, int button) {
		if (cc == null) {
			return (false);
		}
		Gob gob = findicongob(c);
		if (gob == null) {
			mv.wdgmsg("click", rootpos().add(c), c2p(c), button, ui.modflags());
		} else {
			mv.wdgmsg("click", rootpos().add(c), c2p(c), button, ui.modflags(), 0, (int) gob.id, gob.rc, 0, -1);
		}
		if (button == 3) {
			doff = c;
		} else if (button == 2) {
			off = new Coord();
		}
		return (true);
	}

	@Override
	public void mousemove(Coord c
	) {
		if (doff != null) {
			off = off.add(doff.sub(c));
			doff = c;
		}
		super.mousemove(c);
	}

	@Override
	public boolean mouseup(Coord c, int button
	) {
		if (button == 3) {
			doff = null;
		}
		return super.mouseup(c, button);
	}

	public void clearcache() {
		synchronized (cache) {
			cache.clear();
		}
	}

	private static String prettybiome(String biome) {
		int k = biome.lastIndexOf("/");
		biome = biome.substring(k + 1);
		biome = biome.substring(0, 1).toUpperCase() + biome.substring(1);
		return biome;
	}
}
