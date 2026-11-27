package mygame;

import com.jme3.anim.Armature;
import com.jme3.anim.Joint;
import com.jme3.anim.SkinningControl;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

/** Holds the sword with two hands and keeps backflip ground travel in game state. */
public final class NinjaCombatPose extends AbstractControl {
    private final Armature armature;
    private boolean striking, flipping;
    private final Vector3f rootBind;
    private float progress;

    private NinjaCombatPose(Armature armature) { this.armature=armature; rootBind=armature.getJoint("Joint1").getLocalTranslation().clone(); }
    public static NinjaCombatPose attach(Spatial model) {
        SkinningControl[] found=new SkinningControl[1];
        model.depthFirstTraversal(s -> { if(s.getControl(SkinningControl.class)!=null)found[0]=s.getControl(SkinningControl.class); });
        if(found[0]==null)throw new IllegalArgumentException("Ninja armature is missing");
        NinjaCombatPose control=new NinjaCombatPose(found[0].getArmature());
        found[0].getSpatial().addControl(control);
        return control;
    }
    public void setStrike(boolean active,float fraction) { striking=active;progress=fraction; }
    public float handSpacing() { return position(armature.getJoint("Joint13")).distance(position(armature.getJoint("Joint17"))); }
    public void setFlipping(boolean active) { flipping=active; }
    @Override protected void controlUpdate(float tpf) {
        if(flipping){Joint root=armature.getJoint("Joint1");Vector3f t=root.getLocalTranslation().clone();t.x=rootBind.x;t.z=rootBind.z;root.setLocalTranslation(t);armature.update();}
        if(!striking)return;
        float weight=Math.min(smooth(.05f,.25f,progress),1-smooth(.75f,.98f,progress));
        if(weight<=0)return;
        armature.update();
        Joint right=armature.getJoint("Joint12"),grip=armature.getJoint("Joint13");
        Joint left=armature.getJoint("Joint17"),tip=armature.getJoint("Joint29");
        Vector3f rightPosition=position(right),oldGrip=position(grip),leftPosition=position(left);
        Vector3f swordDirection=position(tip).subtract(oldGrip).normalizeLocal();
        Vector3f sharedGrip=oldGrip.add(leftPosition).multLocal(.5f);
        Vector3f rightTarget=rightPosition.add(sharedGrip.subtract(oldGrip).multLocal(weight));
        solve("Joint10","Joint11","Joint12",rightTarget);
        Vector3f supportTarget=position(grip).subtract(swordDirection.mult(8));
        supportTarget.interpolateLocal(leftPosition,1-weight);
        solve("Joint15","Joint16","Joint17",supportTarget);
    }
    private float smooth(float low,float high,float value) {
        float t=FastMath.clamp((value-low)/(high-low),0,1);return t*t*(3-2*t);
    }
    private Vector3f position(Joint joint) { return joint.getModelTransform().getTranslation().clone(); }
    private void solve(String shoulderName,String elbowName,String wristName,Vector3f target) {
        Joint shoulder=armature.getJoint(shoulderName),elbow=armature.getJoint(elbowName),wrist=armature.getJoint(wristName);
        Vector3f a=position(shoulder),b=position(elbow),c=position(wrist);
        Quaternion wristRotation=wrist.getModelTransform().getRotation().clone();
        float upper=a.distance(b),lower=b.distance(c);
        Vector3f direction=target.subtract(a);
        float distance=FastMath.clamp(direction.length(),Math.abs(upper-lower)+.001f,upper+lower-.001f);
        direction.normalizeLocal();
        Vector3f bend=b.subtract(a);bend.subtractLocal(direction.mult(bend.dot(direction)));
        if(bend.lengthSquared()<.0001f)bend=direction.cross(Vector3f.UNIT_Z);
        if(bend.lengthSquared()<.0001f)bend=direction.cross(Vector3f.UNIT_X);
        bend.normalizeLocal();
        float along=(upper*upper-lower*lower+distance*distance)/(2*distance);
        float height=FastMath.sqrt(Math.max(0,upper*upper-along*along));
        Vector3f desiredElbow=a.add(direction.mult(along)).addLocal(bend.mult(height));
        rotateToward(shoulder,b.subtract(a),desiredElbow.subtract(a));armature.update();
        b=position(elbow);c=position(wrist);
        rotateToward(elbow,c.subtract(b),a.add(direction.mult(distance)).subtractLocal(b));armature.update();
        wrist.setLocalRotation(wrist.getParent().getModelTransform().getRotation().inverse().mult(wristRotation));
        armature.update();
    }
    private void rotateToward(Joint joint,Vector3f from,Vector3f to) {
        from.normalizeLocal();to.normalizeLocal();float dot=FastMath.clamp(from.dot(to),-1,1);
        if(dot>.99999f)return;
        Vector3f axis=from.cross(to);
        if(axis.lengthSquared()<.00001f)axis=from.cross(Vector3f.UNIT_Y);
        if(axis.lengthSquared()<.00001f)axis=from.cross(Vector3f.UNIT_X);
        Quaternion world=new Quaternion().fromAngleAxis(FastMath.acos(dot),axis.normalizeLocal())
                .mult(joint.getModelTransform().getRotation());
        Quaternion parent=joint.getParent()==null?Quaternion.IDENTITY:joint.getParent().getModelTransform().getRotation();
        joint.setLocalRotation(parent.inverse().mult(world));
    }
    @Override protected void controlRender(RenderManager manager,ViewPort view) { }
}
