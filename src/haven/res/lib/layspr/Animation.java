//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package haven.res.lib.layspr;

import haven.Coord;
import haven.GOut;
import haven.Resource.Anim;
import haven.res.lib.layspr.Layer;

class Animation extends Layer {
    Anim a;
    double te;
    double dur;
    int cf;

    static Coord sz(Anim var0) {
        Coord var1 = new Coord();

        for(int var2 = 0; var2 < var0.f.length; ++var2) {
            for(int var3 = 0; var3 < var0.f[var2].length; ++var3) {
                var1.x = Math.max(var1.x, var0.f[var2][var3].sz.x);
                var1.y = Math.max(var1.y, var0.f[var2][var3].sz.y);
            }
        }

        return var1;
    }

    Animation(Anim var1) {
        super(var1.f[0][0].z, sz(var1));
        this.a = var1;
        this.dur = (double)var1.d / 1000.0D;
        this.te = 0.0D;
    }

    void tick(double var1) {
        for(this.te += var1; this.te > this.dur; this.cf = (this.cf + 1) % this.a.f.length) {
            this.te -= this.dur;
        }

    }

    void draw(GOut var1) {
        for(int var2 = 0; var2 < this.a.f[this.cf].length; ++var2) {
            var1.image(this.a.f[this.cf][var2], Coord.z);
        }

    }
}
