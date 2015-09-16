//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
package haven.res.lib;

import haven.Indir;
import haven.Message;
import haven.Resource;
import haven.GSprite.Owner;
import haven.Resource.Tooltip;
import haven.res.lib.layspr.Layered;
import haven.res.ui.tt.defn.DynName;

import java.util.Iterator;
import java.util.List;

public class Meat extends Layered implements DynName {
    public final String name;

    private static String ncomb(String var0, String var1) {
        int var2 = var0.indexOf(37);
        return var2 < 0?(var1.indexOf(37) >= 0?ncomb(var1, var0):var0):var0.substring(0, var2) + var1 + var0.substring(var2 + 1);
    }

    private Meat(Owner var1, List<Indir<Resource>> var2) {
        super(var1, var2);
        String var3 = null;
        Iterator var4 = var2.iterator();

        while(var4.hasNext()) {
            Indir var5 = (Indir)var4.next();
            Tooltip var6 = (Tooltip)((Resource)var5.get()).layer(Resource.tooltip);
            if(var6 != null) {
                if(var3 == null) {
                    var3 = var6.t;
                } else {
                    var3 = ncomb(var3, var6.t);
                }
            }
        }

        int var7 = var3.indexOf(37);
        if(var7 >= 0) {
            var3 = var3.substring(0, var7).trim() + " " + var3.substring(var7 + 1).trim();
        }

        this.name = var3;
    }

    public Meat(Owner var1, Resource var2, Message var3) {
        this(var1, decode(var1.glob(), var3));
    }

    public String name() {
        return this.name;
    }
}
