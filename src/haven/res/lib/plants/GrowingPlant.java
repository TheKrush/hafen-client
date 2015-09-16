package haven.res.lib.plants;

import haven.Message;
import haven.Resource;
import haven.Sprite;
import haven.FastMesh.MeshRes;
import haven.Sprite.Factory;
import haven.Sprite.Owner;
import haven.Sprite.ResourceException;
import haven.resutil.CSprite;
import org.apxeolog.shovel.Shovel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class GrowingPlant implements Factory {
    public int num;

    public GrowingPlant(int var1) {
        if (Shovel.getSettings().enableSimpleCrops) {
            this.num = 1;
        } else {
            this.num = var1;
        }
    }

    public Sprite create(Owner var1, Resource var2, Message var3) {
        int var4 = var3.uint8();
        ArrayList var5 = new ArrayList();
        Iterator var6 = var2.layers(MeshRes.class).iterator();

        while(var6.hasNext()) {
            MeshRes var7 = (MeshRes)var6.next();
            if(var7.id / 10 == var4) {
                var5.add(var7);
            }
        }

        if(var5.size() < 1) {
            throw new ResourceException("No variants for grow stage " + var4, var2);
        } else {
            Random var10 = var1.mkrandoom();
            CSprite var11 = new CSprite(var1, var2);

            for(int var8 = 0; var8 < this.num; ++var8) {
                MeshRes var9 = (MeshRes)var5.get(var10.nextInt(var5.size()));
                if(this.num > 1) {
                    var11.addpart(var10.nextFloat() * 11.0F - 5.5F, var10.nextFloat() * 11.0F - 5.5F, var9.mat.get(), var9.m);
                } else {
                    var11.addpart(0, 0, var9.mat.get(), var9.m);
                }
            }
            return var11;
        }
    }
}
