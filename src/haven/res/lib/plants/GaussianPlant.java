//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package haven.res.lib.plants;

import haven.Message;
import haven.Resource;
import haven.Sprite;
import haven.FastMesh.MeshRes;
import haven.Sprite.Factory;
import haven.Sprite.Owner;
import haven.resutil.CSprite;
import org.apxeolog.shovel.ALS;

import java.util.ArrayList;
import java.util.Random;

public class GaussianPlant implements Factory {
    public final int numl;
    public final int numh;
    public final float r;

    public GaussianPlant(int var1, int var2, float var3) {
        this.numl = var1;
        this.numh = var2;
        this.r = var3;
    }

    public Sprite create(Owner var1, Resource var2, Message var3) {
        ArrayList var4 = new ArrayList(var2.layers(MeshRes.class));
        Random var5 = var1.mkrandoom();
        CSprite var6 = new CSprite(var1, var2);
        int var7 = var5.nextInt(this.numh - this.numl + 1) + this.numl;

        for(int var8 = 0; var8 < var7; ++var8) {
            MeshRes var9 = (MeshRes)var4.get(var5.nextInt(var4.size()));
            var6.addpart((float)var5.nextGaussian() * this.r, (float)var5.nextGaussian() * this.r, var9.mat.get(), var9.m);
        }

        return var6;
    }
}
