//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
package haven.res.lib.layspr;

import haven.Coord;
import haven.GOut;

class Image extends Layer {

	final haven.Resource.Image img;

	Image(haven.Resource.Image var1) {
		super(var1.z, var1.sz);
		this.img = var1;
	}

	@Override
	void draw(GOut var1) {
		var1.image(this.img, Coord.z);
	}
}
