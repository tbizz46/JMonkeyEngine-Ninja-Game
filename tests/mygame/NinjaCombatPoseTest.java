package mygame;

import com.jme3.anim.*;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;

public final class NinjaCombatPoseTest {
    static <T extends Control>T find(Spatial s,Class<T> type) {
        T c=s.getControl(type);if(c!=null)return c;
        if(s instanceof Node)for(Spatial child:((Node)s).getChildren()){c=find(child,type);if(c!=null)return c;}
        return null;
    }
    static void check(boolean condition,String reason) { if(!condition)throw new AssertionError(reason); }
    static void sample(AnimClip clip,Armature armature,double fraction) {
        armature.applyBindPose();
        for(AnimTrack<?> track:clip.getTracks())if(track instanceof TransformTrack) {
            TransformTrack transform=(TransformTrack)track;Transform pose=new Transform();
            transform.getDataAtTime(clip.getLength()*fraction,pose);
            transform.getTarget().getLocalTransform().set(pose);
        }
        armature.update();
    }
    public static void main(String[] args) {
        DesktopAssetManager assets=new DesktopAssetManager(true);
        Spatial model=assets.loadModel("Models/Ninja/Ninja.mesh.xml");
        Armature armature=find(model,SkinningControl.class).getArmature();
        AnimComposer composer=find(model,AnimComposer.class);
        NinjaCombatPose pose=NinjaCombatPose.attach(model);
        for(float fraction:new float[]{.3f,.4f,.5f,.6f,.7f}) {
            sample(composer.getAnimClip(DuelState.Strike.SWORD.animation),armature,fraction);
            Vector3f foot=armature.getJoint("Joint22").getModelTransform().getTranslation().clone();
            Vector3f grip=armature.getJoint("Joint13").getModelTransform().getTranslation();
            Vector3f tip=armature.getJoint("Joint29").getModelTransform().getTranslation();
            Vector3f direction=tip.subtract(grip).normalizeLocal();
            pose.setStrike(true,fraction);pose.update(0);
            Vector3f right=armature.getJoint("Joint13").getModelTransform().getTranslation();
            Vector3f left=armature.getJoint("Joint17").getModelTransform().getTranslation();
            float span=right.distance(left);
            System.out.println("GRIP phase="+fraction+" hand spacing="+span);
            check(span<10,"Both hands must be on the grip during the strike: "+span);
            Vector3f currentDirection=armature.getJoint("Joint29").getModelTransform().getTranslation().subtract(right).normalizeLocal();
            check(direction.dot(currentDirection)>.999f,"Grip correction must preserve blade direction");
            check(foot.distance(armature.getJoint("Joint22").getModelTransform().getTranslation())<.001f,"Arm correction must not move feet");
        }
        pose.setStrike(false,0);pose.setFlipping(true);
        sample(composer.getAnimClip("Backflip"),armature,.5);pose.update(0);
        check(Math.abs(armature.getJoint("Joint1").getLocalTranslation().z)<.001f,"Flip horizontal root motion must be removed");
        System.out.println("PASS: two-handed strike grip, preserved blade direction and foot pose, flip root motion.");
    }
}
