package mygame;

import com.jme3.post.SceneProcessor;
import com.jme3.profile.AppProfiler;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Spatial;
import com.jme3.math.Vector3f;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.util.BufferUtils;
import java.nio.ByteBuffer;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/** Samples the actual rendered torsos during --smoke, before the HUD is drawn. */
final class UniformRenderCheck implements SceneProcessor {
    private final Camera camera;
    private final Spatial player, cpu;
    private RenderManager renderManager;
    private int frames;
    private boolean passed;
    private String pendingEvidence;
    void captureEvidence(String file) { pendingEvidence=file; }

    UniformRenderCheck(Camera camera, Spatial player, Spatial cpu) {
        this.camera=camera; this.player=player; this.cpu=cpu;
    }
    boolean passed() { return passed; }
    @Override public void initialize(RenderManager manager, ViewPort view) { renderManager=manager; }
    @Override public boolean isInitialized() { return renderManager!=null; }
    @Override public void reshape(ViewPort view,int width,int height) { }
    @Override public void preFrame(float tpf) { }
    @Override public void postQueue(RenderQueue queue) { }
    @Override public void setProfiler(AppProfiler profiler) { }
    @Override public void cleanup() { renderManager=null; }
    @Override public void postFrame(FrameBuffer output) {
        if(passed&&pendingEvidence==null)return;
        if(!passed&&++frames<8)return;
        ByteBuffer pixels=BufferUtils.createByteBuffer(camera.getWidth()*camera.getHeight()*4);
        renderManager.getRenderer().readFrameBufferWithFormat(output,pixels,Image.Format.RGBA8);
        if(pendingEvidence!=null){saveEvidence(pixels,pendingEvidence);pendingEvidence=null;}
        if(passed)return;
        int blue=countUniformPixels(pixels,player,true);
        int red=countUniformPixels(pixels,cpu,false);
        if(blue<12 || red<12) {
            throw new AssertionError("Uniform render check failed: blue torso="+blue+", red torso="+red);
        }
        passed=true;
        saveEvidence(pixels,"uniforms-render.png");
        System.out.println("UNIFORM_RENDER_OK: blue torso="+blue+", red torso="+red);
    }
    private void saveEvidence(ByteBuffer pixels,String file) {
        int width=camera.getWidth(), height=camera.getHeight();
        BufferedImage evidence=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<height;y++)for(int x=0;x<width;x++) {
            int i=(y*width+x)*4;
            int r=pixels.get(i)&255,g=pixels.get(i+1)&255,b=pixels.get(i+2)&255;
            evidence.setRGB(x,height-1-y,0xff000000|(r<<16)|(g<<8)|b);
        }
        try {
            File output=new File(".runtime/"+file);
            output.getParentFile().mkdirs();
            if(!ImageIO.write(evidence,"png",output))throw new IOException("PNG writer not found");
        } catch(IOException exception) {
            throw new IllegalStateException("Could not save uniform render evidence",exception);
        }
    }
    private int countUniformPixels(ByteBuffer pixels, Spatial fighter, boolean blue) {
        Vector3f center=camera.getScreenCoordinates(fighter.getWorldTranslation().add(0,1.6f,0));
        int count=0;
        // This is well above the colored floor ring and below the health bar.
        for(int y=(int)center.y-12;y<=(int)center.y+12;y++) {
            for(int x=(int)center.x-10;x<=(int)center.x+10;x++) {
                if(x<0||y<0||x>=camera.getWidth()||y>=camera.getHeight())continue;
                int offset=(y*camera.getWidth()+x)*4;
                int r=pixels.get(offset)&255, g=pixels.get(offset+1)&255, b=pixels.get(offset+2)&255;
                if(blue ? b>r*1.5f&&b>g*1.25f&&b>40 : r>b*1.5f&&r>g*1.25f&&r>40)count++;
            }
        }
        return count;
    }
}
