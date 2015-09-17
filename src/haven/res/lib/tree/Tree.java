//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package haven.res.lib.tree;

import haven.Location;
import haven.Matrix4f;
import haven.Message;
import haven.RenderList;
import haven.Resource;
import haven.States;
import haven.StaticSprite;
import haven.Sprite.Owner;

public class Tree extends StaticSprite {
    private final Location scale;
    public final float fscale;
    Message sdt;

    public Tree(Owner var1, Resource var2, float var3) {
        super(var1, var2, Message.nil);
        this.fscale = var3;
        if(var3 == 1.0F) {
            this.scale = null;
        } else {
            this.scale = mkscale(var3);
        }

    }

    public Tree(Owner var1, Resource var2, Message var3) {
        this(var1, var2, var3.eom()?1.0F:(float)var3.uint8() / 100.0F);
        this.sdt = var3;
    }

    public static Location mkscale(float var0, float var1, float var2) {
        return new Location(new Matrix4f(var0, 0.0F, 0.0F, 0.0F, 0.0F, var1, 0.0F, 0.0F, 0.0F, 0.0F, var2, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F));
    }

    public static Location mkscale(float var0) {
        return mkscale(var0, var0, var0);
    }

    public boolean setup(RenderList var1) {
        if(this.scale != null) {
            var1.prepc(this.scale);
            var1.prepc(States.normalize);
        }

        return super.setup(var1);
    }
}
