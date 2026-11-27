package mygame;

import com.github.stephengold.wrench.LwjglAssetLoader;
import com.jme3.asset.AssetManager;

/** Shared model import setup for the game and import checks. */
public final class ModelImports {
    private ModelImports() { }

    public static void configure(AssetManager assets) {
        assets.registerLoader(LwjglAssetLoader.class,
                "3ds", "3mf", "blend", "bvh", "dae", "fbx", "glb", "gltf",
                "lwo", "obj", "ply", "stl");
        // Keep the SDK's Ogre XML loader for the existing animated ninjas.
    }
}
