package mygame;

import com.jme3.anim.AnimComposer;
import com.jme3.asset.AssetLoadException;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import org.lwjgl.assimp.Assimp;

/** Loads actual models through Assimp without opening a game window. */
public final class ModelImportsTest {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static boolean hasAnimation(Spatial spatial) {
        if (spatial.getControl(AnimComposer.class) != null) return true;
        if (spatial instanceof Node) {
            for (Spatial child : ((Node) spatial).getChildren()) {
                if (hasAnimation(child)) return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        DesktopAssetManager assets = new DesktopAssetManager(true);
        assets.registerLocator("tests/resources", FileLocator.class);
        ModelImports.configure(assets);
        System.out.println("Native Assimp " + Assimp.aiGetVersionMajor()
                + "." + Assimp.aiGetVersionMinor() + " on " + System.getProperty("os.arch"));
        for (String path : new String[]{"triangle.obj", "triangle.gltf"}) {
            Spatial model = assets.loadModel(path);
            check(model.getTriangleCount() == 1, "Expected one triangle from " + path);
        }
        boolean rejected = false;
        try { assets.loadModel("missing.obj"); }
        catch (AssetNotFoundException expected) { rejected = true; }
        check(rejected, "Missing model must fail");
        rejected = false;
        try { assets.loadModel("broken.obj"); }
        catch (AssetLoadException expected) { rejected = true; }
        check(rejected, "Broken model must fail");
        assets.clearCache();
        check(assets.loadModel("triangle.obj").getTriangleCount() == 1,
                "A failed import must allow a fresh import");
        Spatial ninja = assets.loadModel("Models/Ninja/Ninja.mesh.xml");
        check(ninja.getTriangleCount() > 0 && hasAnimation(ninja),
                "Existing ninja must retain mesh and animation");
        System.out.println("PASS: native Assimp, OBJ, glTF, missing and broken files, recovery, animated ninja.");
    }
}
