//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
package haven.res.lib.layspr;

import haven.Coord;
import haven.GOut;
import haven.GSprite;
import haven.GSprite.Owner;
import haven.Glob;
import haven.Indir;
import haven.Message;
import haven.Resource;
import haven.Resource.Anim;
import haven.Resource.Image;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class Layered extends GSprite {

	public static class LayeredComparator implements Comparator<Layer> {

		private Layered base;

		LayeredComparator(Layered var1) {
			this.base = var1;
		}

		@Override
		public int compare(Layer var1, Layer var2) {
			return var1.z - var2.z;
		}
	}

	final Layer[] lay;
	final Coord sz;

	@SuppressWarnings("unchecked")
	public static List<Indir<Resource>> decode(Glob var0, Message var1) {
		ArrayList var2 = new ArrayList();

		while (!var1.eom()) {
			var2.add(var0.sess.getres(var1.uint16()));
		}

		return var2;
	}

	@SuppressWarnings("unchecked")
	public Layered(Owner var1, Collection<Indir<Resource>> var2) {
		super(var1);
		ArrayList var3 = new ArrayList(var2.size());
		Iterator var4 = var2.iterator();

		while (true) {
			Indir var5;
			boolean var6;
			Iterator var7;
			do {
				if (!var4.hasNext()) {
					Collections.sort(var3, new LayeredComparator(this));
					this.lay = (Layer[]) var3.toArray(new Layer[var3.size()]);
					this.sz = new Coord();
					Layer[] var9 = this.lay;
					int var10 = var9.length;

					for (int var11 = 0; var11 < var10; ++var11) {
						Layer var12 = var9[var11];
						this.sz.x = Math.max(this.sz.x, var12.sz.x);
						this.sz.y = Math.max(this.sz.y, var12.sz.y);
					}

					this.tick(Math.random() * 10.0D);
					return;
				}

				var5 = (Indir) var4.next();
				var6 = false;
				var7 = ((Resource) var5.get()).layers(Resource.animc).iterator();

				while (var7.hasNext()) {
					Anim var8 = (Anim) var7.next();
					var6 = true;
					var3.add(new Animation(var8));
				}
			} while (var6);

			var7 = ((Resource) var5.get()).layers(Resource.imgc).iterator();

			while (var7.hasNext()) {
				Image var13 = (Image) var7.next();
				var3.add(new haven.res.lib.layspr.Image(var13));
			}
		}
	}

	@Override
	public void draw(GOut var1) {
		Layer[] var2 = this.lay;
		int var3 = var2.length;

		for (int var4 = 0; var4 < var3; ++var4) {
			Layer var5 = var2[var4];
			var5.draw(var1);
		}

	}

	@Override
	public Coord sz() {
		return this.sz;
	}

	@Override
	public void tick(double var1) {
		Layer[] var3 = this.lay;
		int var4 = var3.length;

		for (int var5 = 0; var5 < var4; ++var5) {
			Layer var6 = var3[var5];
			if (var6 instanceof Animation) {
				((Animation) var6).tick(var1);
			}
		}

	}
}
