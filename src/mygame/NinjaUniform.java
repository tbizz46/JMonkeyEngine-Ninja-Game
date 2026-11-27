package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;

/** Fighter-specific cloth colors with the original skin and metal texture. */
public final class NinjaUniform {
    private NinjaUniform() { }

    public static void apply(AssetManager assets, Spatial ninja, boolean player) {
        Material material = new Material(assets, "MatDefs/NinjaUniform.j3md");
        material.setTexture("DiffuseMap", assets.loadTexture("Models/Ninja/Ninja.jpg"));
        material.setColor("UniformColor", player
                ? new ColorRGBA(.055f, .28f, 1f, 1f)
                : new ColorRGBA(1f, .055f, .075f, 1f));
        material.setBoolean("UseMaterialColors", true);
        material.setColor("Diffuse", ColorRGBA.White);
        material.setColor("Ambient", ColorRGBA.White);
        material.setColor("Specular", ColorRGBA.White.mult(.35f));
        ninja.depthFirstTraversal(spatial -> {
            if (spatial instanceof Geometry) ((Geometry) spatial).setMaterial(material);
        });
    }
}
