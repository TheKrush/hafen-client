//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package haven.res.lib.plants;

import haven.Gob;
import haven.Message;
import haven.Resource;
import haven.Sprite;
import haven.FastMesh.MeshRes;
import haven.Sprite.Factory;
import haven.Sprite.Owner;
import haven.Sprite.ResourceException;
import haven.resutil.CSprite;
import org.apxeolog.shovel.ALS;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class TrellisPlant implements Factory {
    public final int num;

    public TrellisPlant(int var1) {
        this.num = var1;
    }

    public TrellisPlant() {
        this(2);
    }

    public Sprite create(Owner var1, Resource var2, Message var3) {
        double var4 = -((Gob)var1).a;
        float var6 = (float)Math.cos(var4);
        float var7 = -((float)Math.sin(var4));
        int var8 = var3.uint8();
        ArrayList var9 = new ArrayList();
        Iterator var10 = var2.layers(MeshRes.class).iterator();

        while(var10.hasNext()) {
            MeshRes var11 = (MeshRes)var10.next();
            if(var11.id / 10 == var8) {
                var9.add(var11);
            }
        }

        if(var9.size() < 1) {
            throw new ResourceException("No variants for grow stage " + var8, var2);
        } else {
            Random var16 = var1.mkrandoom();
            CSprite var17 = new CSprite(var1, var2);
            float var12 = 11.0F / (float)this.num;
            float var13 = -5.5F + var12 / 2.0F;

            for(int var14 = 0; var14 < this.num; ++var14) {
                MeshRes var15 = (MeshRes)var9.get(var16.nextInt(var9.size()));
                var17.addpart(var13 * var7, var13 * var6, var15.mat.get(), var15.m);
                var13 += var12;
            }

            return var17;
        }
    }
}
