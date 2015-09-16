//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package haven.res.lib.layspr;

import haven.Coord;
import haven.GOut;

abstract class Layer {
    final int z;
    final Coord sz;

    Layer(int var1, Coord var2) {
        this.z = var1;
        this.sz = var2;
    }

    abstract void draw(GOut var1);
}
