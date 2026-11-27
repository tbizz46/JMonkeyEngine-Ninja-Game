package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.anim.AnimComposer;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.*;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.scene.*;
import com.jme3.scene.shape.*;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeSystem;
import com.jme3.audio.AudioContext;
import com.jme3.audio.Listener;
import com.jme3.texture.Texture;
import java.util.Random;

/** A complete local, keyboard-controlled duel using the SDK's bundled ninja. */
public class Main extends SimpleApplication {
    private final DuelState duel=new DuelState();
    private final Random random=new Random(42);
    private final Node world=new Node("Moonlit field");
    private Avatar blue,red;
    private boolean left,right,forward,back,guard;
    private final Vector3f cameraTarget = new Vector3f();
    private float aiTimer=.8f, elapsed, deathTime;
    private BitmapText title,subtitle,status,help,pName,eName,center,centerSmall;
    private Geometry pBar,eBar;
    private Node overlay=new Node("Start and pause panel");
    private final boolean smoke=Boolean.getBoolean("duel.smoke");
    private int smokeStage;
    private float smokeFlipRemaining;
    private UniformRenderCheck uniformRenderCheck;
    private GameAudio gameAudio;
    private BitmapText audioHelp;
    private boolean aiSawAttack, aiGuard;
    private class Avatar {
        Node root=new Node(); Spatial model; AnimComposer composer;
        NinjaCombatPose combatPose;
        String animation="";
        Avatar(String name, ColorRGBA color, boolean player) {
            root.setName(name); model=assetManager.loadModel("Models/Ninja/Ninja.mesh.xml");
            NinjaUniform.apply(assetManager, model, player);
            model.setLocalScale(.0162f); root.attachChild(model);
            composer=findComposer(model);
            combatPose=NinjaCombatPose.attach(model);
            if(composer==null) throw new IllegalStateException("Ninja animation is missing");
            Geometry ring=shape("Fighter marker",new Torus(12,64,.025f,.62f),unlit(color));
            ring.rotate(FastMath.HALF_PI,0,0); ring.setLocalTranslation(0,.045f,0);root.attachChild(ring);
            rootNode.attachChild(root); play("Idle1",1);
            System.out.println("ASSET_OK: "+name+" animations="+composer.getAnimClipsNames());
        }
        void play(String name,float speed) {
            if(!animation.equals(name)) { composer.setCurrentAction(name); animation=name; }
            composer.setGlobalSpeed(speed);
        }
        void sync(DuelState.Fighter f) {
            combatPose.setStrike(f.hp>0&&f.flip<=0&&f.attack>0&&f.strike==DuelState.Strike.SWORD,
                    1-f.attack/f.strike.duration);
            combatPose.setFlipping(f.flip>0);
            if(duel.paused){composer.setGlobalSpeed(0);return;}
            // Backflip supplies its own vertical pose; game state supplies ground travel and hit height.
            root.setLocalTranslation(f.x,f.flip>0?0:f.y,f.z);
            root.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.atan2(-f.facingX,-f.facingZ),Vector3f.UNIT_Y));
            if(f.hp==0) play("Death1",deathTime>.56f?0:1);
            else if(f.flip>0) play("Backflip",1/DuelState.FLIP_TIME);
            else if(f.attack>0) {
                float clipLength=(float)composer.getAnimClip(f.strike.animation).getLength();
                play(f.strike.animation,clipLength/f.strike.duration);
            }
            else if(f.y>0) play("JumpNoHeight",.8f);
            else if(f.block) play("Block",1);
            else if(f.moving && duel.active()) play("Walk",1.4f);
            else play("Idle1",1);
        }
    }
    public static void main(String[] args) {
        for(String arg:args)if(arg.equals("--smoke"))System.setProperty("duel.smoke","true");
        Main game=new Main(); AppSettings settings=new AppSettings(true);
        settings.setTitle("Night Ninja Duel");settings.setResolution(1280,800);
        settings.setResizable(false);settings.setSamples(4);settings.setVSync(true);
        // Initialize audio below so an unavailable output device cannot block play.
        settings.setAudioRenderer(null);settings.setFrameRate(60);
        game.setSettings(settings);game.setShowSettings(false);game.setPauseOnLostFocus(false);game.start();
    }
    @Override public void simpleInitApp() {
        ModelImports.configure(assetManager);
        initializeGameAudio();
        setDisplayFps(false);setDisplayStatView(false);flyCam.setEnabled(false);
        inputManager.setCursorVisible(true);inputManager.deleteMapping(INPUT_MAPPING_EXIT);
        rootNode.attachChild(world);viewPort.setBackgroundColor(new ColorRGBA(.018f,.028f,.075f,1));
        cam.setLocation(new Vector3f(0,17,25));cam.lookAt(new Vector3f(0,.8f,0),Vector3f.UNIT_Y);
        cam.setFrustumPerspective(43,cam.getWidth()/(float)cam.getHeight(),.1f,180);
        rootNode.addLight(new AmbientLight(new ColorRGBA(.42f,.47f,.68f,1)));
        DirectionalLight moon=new DirectionalLight(new Vector3f(-.4f,-1,-.6f).normalizeLocal(),new ColorRGBA(.75f,.82f,1,1));rootNode.addLight(moon);
        createField();blue=new Avatar("Blue ninja / You",new ColorRGBA(.2f,.75f,1,1),true);red=new Avatar("Red ninja / CPU",new ColorRGBA(1,.3f,.35f,1),false);
        if(smoke){uniformRenderCheck=new UniformRenderCheck(cam,blue.root,red.root);viewPort.addProcessor(uniformRenderCheck);}
        createHud(); bind("Left",KeyInput.KEY_A,KeyInput.KEY_LEFT);bind("Right",KeyInput.KEY_D,KeyInput.KEY_RIGHT);
        bind("Forward",KeyInput.KEY_W,KeyInput.KEY_UP);bind("Back",KeyInput.KEY_S,KeyInput.KEY_DOWN);
        bind("Kick",KeyInput.KEY_L);bind("Attack",KeyInput.KEY_J);bind("Guard",KeyInput.KEY_K);bind("Jump",KeyInput.KEY_SPACE);
        bind("Confirm",KeyInput.KEY_RETURN);bind("Pause",KeyInput.KEY_ESCAPE,KeyInput.KEY_P);bind("Quit",KeyInput.KEY_Q);
        bind("Music",KeyInput.KEY_M);bind("Effects",KeyInput.KEY_N);
        bind("Flip",KeyInput.KEY_F);
        inputManager.addListener((com.jme3.input.controls.ActionListener)(name,down,tpf)->{
            if(smoke)return; // Keep the automated sequence independent of live keyboard input.
            switch(name) {
                case "Left":left=down;break;case "Right":right=down;break;case "Guard":guard=down;break;
                case "Forward":forward=down;break;case "Back":back=down;break;
                case "Kick":if(down)duel.kick(duel.player);break;
                case "Flip":if(down)duel.flip(duel.player,(right?1:0)-(left?1:0),(back?1:0)-(forward?1:0));break;
                case "Attack":if(down)duel.attack(duel.player);break;
                case "Jump":if(down)duel.jump(duel.player);break;
                case "Music":if(down)gameAudio.toggleMusic();break;
                case "Effects":if(down)gameAudio.toggleEffects();break;
                case "Confirm":if(down){ if(duel.over)resetRound();else if(!duel.started)duel.started=true;else if(duel.paused)duel.paused=false;}break;
                case "Pause":if(down&&duel.started&&!duel.over){duel.paused=!duel.paused;clearInput();}break;
                case "Quit":if(down&&(!duel.started||duel.paused||duel.over))stop();break;
            }
        },"Left","Right","Forward","Back","Kick","Flip","Guard","Attack","Jump","Music","Effects","Confirm","Pause","Quit");
        // Losing focus pauses the match, so a hidden window cannot cost a round.
        System.out.println("READY: Night Ninja Duel loaded");
    }
    private void initializeGameAudio() {
        try {
            AppSettings audioSettings=new AppSettings(false);audioSettings.copyFrom(settings);
            audioSettings.setAudioRenderer(AppSettings.LWJGL_OPENAL);
            audioRenderer=JmeSystem.newAudioRenderer(audioSettings);
            if(audioRenderer==null)throw new IllegalStateException("Audio output is not available");
            audioRenderer.initialize();AudioContext.setAudioRenderer(audioRenderer);
            listener=new Listener();audioRenderer.setListener(listener);
            gameAudio=GameAudio.available(assetManager);
        } catch(RuntimeException|LinkageError failure) {
            System.err.println("Audio unavailable: "+failure.getMessage());
            if(audioRenderer!=null)try{audioRenderer.cleanup();}catch(RuntimeException ignored){ }
            audioRenderer=null;listener=null;AudioContext.setAudioRenderer(null);
            gameAudio=GameAudio.unavailable();
        }
    }
    @Override public void destroy() {
        if(gameAudio!=null)gameAudio.close();
        super.destroy();
    }
    @Override public void loseFocus() { super.loseFocus();if(duel.started&&!duel.over){duel.paused=true;clearInput();} }
    private void clearInput(){left=false;right=false;forward=false;back=false;guard=false;}
    private void resetRound(){duel.reset();duel.started=true;deathTime=0;aiTimer=.8f;aiSawAttack=false;aiGuard=false;cameraTarget.set(0,0,0);clearInput();blue.animation="";red.animation="";}
    private void bind(String name,int...keys){for(int key:keys)inputManager.addMapping(name,new KeyTrigger(key));}
    private AnimComposer findComposer(Spatial s){AnimComposer c=s.getControl(AnimComposer.class);if(c!=null)return c;if(s instanceof Node)for(Spatial child:((Node)s).getChildren()){c=findComposer(child);if(c!=null)return c;}return null;}
    private Material unlit(ColorRGBA c){Material m=new Material(assetManager,"Common/MatDefs/Misc/Unshaded.j3md");m.setColor("Color",c);return m;}
    private Material lit(ColorRGBA c){Material m=new Material(assetManager,"Common/MatDefs/Light/Lighting.j3md");m.setBoolean("UseMaterialColors",true);m.setColor("Diffuse",c);m.setColor("Ambient",c);return m;}
    private Geometry shape(String name,Mesh mesh,Material mat){Geometry g=new Geometry(name,mesh);g.setMaterial(mat);return g;}
    private void fieldShape(String name,Mesh mesh,Material mat,float x,float y,float z){Geometry g=shape(name,mesh,mat);g.setLocalTranslation(x,y,z);world.attachChild(g);}
    private void createField(){
        Material grass=lit(new ColorRGBA(.07f,.14f,.1f,1));Texture tex=assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");tex.setWrap(Texture.WrapMode.Repeat);grass.setTexture("DiffuseMap",tex);
        Box ground=new Box(65,.12f,65);ground.scaleTextureCoordinates(new Vector2f(30,30));fieldShape("Grass field",ground,grass,0,-.15f,-20);
        Material path=lit(new ColorRGBA(.08f,.10f,.12f,1));fieldShape("Duel clearing",new Box(DuelState.ARENA_X+.65f,.018f,DuelState.ARENA_Z+.65f),path,0,-.015f,0);
        Material mountain=unlit(new ColorRGBA(.038f,.065f,.105f,1));
        for(int i=0;i<20;i++){float x=-70+i*7;Geometry g=shape("Distant hill",new Sphere(12,20,1),mountain);g.setLocalScale(8+random.nextFloat()*8,1+random.nextFloat()*3,5);g.setLocalTranslation(x,0,-42-random.nextFloat()*12);world.attachChild(g);}
        Material reed=lit(new ColorRGBA(.24f,.35f,.3f,1));
        for(int i=0;i<720;i++){float x=random.nextFloat()*46-23,z=random.nextFloat()*25-17;if(Math.abs(z)<6.2f&&Math.abs(x)<10.2f)continue;float h=.15f+random.nextFloat()*.45f;Geometry g=shape("Night grass",new Box(.022f,h,.018f),reed);g.setLocalTranslation(x,h,z);g.rotate(0,random.nextFloat()*6,.12f);world.attachChild(g);}
        Material trunk=lit(new ColorRGBA(.1f,.14f,.16f,1)),leaves=lit(new ColorRGBA(.055f,.16f,.15f,1));
        for(int i=0;i<13;i++){float x=-30+i*5,z=-13-random.nextFloat()*8,h=3+random.nextFloat()*3;fieldShape("Tree trunk",new Box(.2f,h/2,.2f),trunk,x,h/2,z);for(int j=0;j<3;j++){Geometry g=shape("Pine canopy",new Cylinder(12,20,.01f,1.7f-j*.3f,2.5f,true,false),leaves);g.rotate(-FastMath.HALF_PI,0,0);g.setLocalTranslation(x,h-1+j*.9f,z);world.attachChild(g);}}
        fieldShape("Moon",new Sphere(32,32,2.0f),unlit(new ColorRGBA(.83f,.9f,1,1)),9,9,-25);
        Material star=unlit(new ColorRGBA(.6f,.72f,.88f,1));for(int i=0;i<100;i++)fieldShape("Star",new Sphere(4,4,.035f+random.nextFloat()*.04f),star,random.nextFloat()*100-50,6+random.nextFloat()*12,-65);
        for(int x:new int[]{-10,10}){fieldShape("Lantern post",new Box(.08f,.85f,.08f),trunk,x,.85f,-6.5f);fieldShape("Lantern light",new Box(.19f,.24f,.19f),unlit(new ColorRGBA(1,.67f,.22f,1)),x,1.6f,-6.5f);PointLight l=new PointLight(new Vector3f(x,1.8f,-6.5f),new ColorRGBA(1,.48f,.13f,1),5);rootNode.addLight(l);}
    }
    private BitmapText text(String value,int size,ColorRGBA color,float x,float y,Node parent){BitmapText t=new BitmapText(guiFont);t.setSize(size);t.setText(value);t.setColor(color);t.setLocalTranslation(x,y,3);parent.attachChild(t);return t;}
    private Geometry panel(String name,float x,float y,float w,float h,ColorRGBA color,Node parent){Geometry g=shape(name,new Quad(w,h),unlit(color));g.setLocalTranslation(x,y,1);parent.attachChild(g);return g;}
    private void createHud(){
        int w=cam.getWidth(),h=cam.getHeight();ColorRGBA white=new ColorRGBA(.88f,.93f,1,1),muted=new ColorRGBA(.57f,.67f,.79f,1);
        panel("Header",0,h-130,w,130,new ColorRGBA(.025f,.045f,.08f,1),guiNode);
        title=text("NIGHT NINJA DUEL",23,white,40,h-22,guiNode);subtitle=text("3D ARENA  /  SWORDS + KICKS",13,muted,w-335,h-27,guiNode);
        pName=text("YOU  /  BLUE",17,new ColorRGBA(.27f,.78f,1,1),40,h-62,guiNode);eName=text("CPU  /  RED",17,new ColorRGBA(1,.43f,.45f,1),w-440,h-62,guiNode);
        panel("Blue health track",40,h-104,400,16,new ColorRGBA(.12f,.17f,.23f,1),guiNode);panel("Red health track",w-440,h-104,400,16,new ColorRGBA(.12f,.17f,.23f,1),guiNode);
        pBar=panel("Blue health",40,h-104,400,16,new ColorRGBA(.12f,.65f,1,1),guiNode);eBar=panel("Red health",w-440,h-104,400,16,new ColorRGBA(.95f,.2f,.3f,1),guiNode);
        status=text("",17,white,0,h-150,guiNode);
        panel("Control strip",0,0,w,76,new ColorRGBA(.025f,.045f,.08f,1),guiNode);
        help=text("WASD / Arrows  Move   J  Two-hand sword   L  Kick   F  Flip   K  Block   SPACE  Jump   ESC  Pause",17,white,0,53,guiNode);centerText(help,w/2);
        audioHelp=text(gameAudio.help(),14,muted,0,24,guiNode);centerText(audioHelp,w/2);
        panel("Menu panel",w/2-330,h/2-112,660,280,new ColorRGBA(.035f,.065f,.11f,1),overlay);
        center=text("",34,white,0,h/2+94,overlay);centerSmall=text("",18,white,0,h/2+36,overlay);guiNode.attachChild(overlay);
    }
    private void centerText(BitmapText t,float x){t.setLocalTranslation(x-t.getLineWidth()/2,t.getLocalTranslation().y,t.getLocalTranslation().z);}
    private void updateHud(){
        audioHelp.setText(gameAudio.help());centerText(audioHelp,cam.getWidth()/2);
        pBar.setLocalScale(duel.player.hp/100f,1,1);eBar.setLocalScale(duel.enemy.hp/100f,1,1);
        pName.setText("YOU  /  BLUE     "+duel.player.hp+" HP");eName.setText("CPU  /  RED     "+duel.enemy.hp+" HP");
        status.setText(duel.messageTime>0?duel.message:duel.active()?"Circle your opponent. Get close to kick.":"");centerText(status,cam.getWidth()/2);
        boolean show=!duel.started||duel.paused||duel.over;overlay.setCullHint(show?Spatial.CullHint.Never:Spatial.CullHint.Always);
        if(show){center.setText(!duel.started?"THE FIELD AWAITS":duel.over?(duel.player.hp>0?"YOU WIN":"CPU WINS"):"PAUSED");
            centerSmall.setText(!duel.started?"You are BLUE. Defeat the RED ninja.\n\nENTER  Start fight\nWASD / Arrows  Move  |  J  Two-hand sword  |  L  Kick\nK  Hold to block  |  SPACE  Jump\nHold a direction + F  Flip  |  F alone  Facing\n\nQ  Quit":duel.over?"ENTER  Rematch\n\nTip: hold K to block, then strike when the sword drops.\n\nQ  Quit":"ENTER or ESC  Resume\n\nYour round is safe while paused.\n\nQ  Quit");centerSmall.setSize(duel.started?18:17);centerText(center,cam.getWidth()/2);centerText(centerSmall,cam.getWidth()/2);}
    }
    @Override public void simpleUpdate(float tpf){
        if(blue==null)return;float dt=Math.min(tpf,.04f);elapsed+=dt;
        duel.player.moving=false;duel.enemy.moving=false;
        if(duel.active()){
            duel.player.block=guard&&duel.player.attack<=0&&duel.player.flip<=0&&duel.player.y==0;
            duel.move(duel.player,(right?1:0)-(left?1:0),(back?1:0)-(forward?1:0),dt);
            float distance=duel.distance();aiTimer-=dt;
            if(duel.player.attack>0&&!aiSawAttack) {
                aiSawAttack=true;aiGuard=random.nextFloat()<.35f;
                if(!smoke&&distance<3.1f&&random.nextFloat()<.3f){
                    duel.flip(duel.enemy,duel.enemy.x-duel.player.x,duel.enemy.z-duel.player.z);aiGuard=false;
                }
            } else if(duel.player.attack<=0) { aiSawAttack=false;aiGuard=false; }
            float attackAge=duel.player.strike.duration-duel.player.attack;
            duel.enemy.block=!smoke&&aiGuard&&distance<2.6f&&duel.player.attack>0
                    &&attackAge>.12f&&duel.enemy.attack<=0&&duel.enemy.flip<=0;
            if(distance>1.65f) {
                float dx=duel.player.x-duel.enemy.x,dz=duel.player.z-duel.enemy.z;
                duel.move(duel.enemy,dx/distance,dz/distance,dt);
            }
            if(aiTimer<=0&&distance<DuelState.RANGE){
                duel.enemy.block=false;
                if(distance<=DuelState.Strike.KICK.range&&random.nextBoolean())duel.kick(duel.enemy);
                else duel.attack(duel.enemy);
                aiTimer=1.05f+random.nextFloat()*.75f;
            }
            duel.step(dt);
        }
        updateArenaCamera(dt);
        if(duel.over)deathTime+=dt;
        blue.sync(duel.player);red.sync(duel.enemy);
        // Freeze skeletal animation with the same pause state as combat.
        if(duel.paused){blue.composer.setGlobalSpeed(0);red.composer.setGlobalSpeed(0);}
        updateHud();
        gameAudio.sync(duel);
        if(smoke)smokeTest();
    }
    private void updateArenaCamera(float dt) {
        Vector3f midpoint=new Vector3f((duel.player.x+duel.enemy.x)*.125f,
                .8f,(duel.player.z+duel.enemy.z)*.125f);
        cameraTarget.interpolateLocal(midpoint,Math.min(1,dt*3));
        float spread=duel.distance();
        cam.setLocation(cameraTarget.add(0,16+spread*.12f,23+spread*.24f));
        cam.lookAt(cameraTarget,Vector3f.UNIT_Y);
    }
    private void smokeTest(){
        if(!gameAudio.available())throw new AssertionError("Audio output did not initialize");
        if(elapsed>1&&smokeStage==0){
            if(!gameAudio.musicPlaying())throw new AssertionError("Music did not start");
            duel.started=true;duel.player.x=-1;duel.enemy.x=.6f;
            duel.player.z=duel.enemy.z=2;duel.kick(duel.player);smokeStage++;
        }
        if(elapsed>1.5f&&smokeStage==1){
            if(gameAudio.effectsPlayed()<3)throw new AssertionError("Fight effects did not play");
            if(duel.enemy.hp!=90||!blue.animation.equals("Kick"))throw new AssertionError("Player kick failed");
            duel.enemy.cooldown=0;duel.enemy.stun=0;duel.player.attack=0;
            duel.enemy.x=duel.player.x+1.6f;duel.kick(duel.enemy);
            aiTimer=5;smokeStage++;
        }
        if(elapsed>2&&smokeStage==2){
            if(duel.player.hp!=90||!red.animation.equals("Kick"))throw new AssertionError("CPU kick failed");
            duel.player.cooldown=0;duel.player.stun=0;
            duel.player.x=-3;duel.enemy.x=3;duel.enemy.z=-2;
            duel.move(duel.player,0,-1,.2f);
            if(duel.player.z>=2)throw new AssertionError("3D movement failed");
            duel.paused=true;System.out.println("SMOKE_KICKS_AND_3D_OK");smokeStage++;
        }
        if(elapsed>3&&smokeStage==3){
            if(gameAudio.musicPlaying())throw new AssertionError("Music did not pause; game paused="+duel.paused);
            duel.paused=false;duel.enemy.hp=10;
            duel.enemy.x=duel.player.x+1.6f;duel.enemy.z=duel.player.z;
            duel.enemy.attack=0;duel.enemy.cooldown=5;
            duel.player.cooldown=0;duel.player.stun=0;duel.kick(duel.player);smokeStage++;
        }
        if(elapsed>4&&smokeStage==4){
            if(!duel.over)throw new AssertionError("Kick victory failed");
            System.out.println("SMOKE_KICK_VICTORY");resetRound();smokeStage++;
        }
        if(elapsed>4.5f&&smokeStage==5){
            if(duel.player.hp!=100||duel.enemy.hp!=100)throw new AssertionError("Rematch failed");
            duel.player.x=-.8f;duel.enemy.x=.8f;duel.player.z=duel.enemy.z=0;
            duel.attack(duel.player);duel.attack(duel.enemy);aiTimer=10;smokeStage++;
        }
        if(elapsed>5&&smokeStage==6){
            if(!blue.animation.equals("Attack3")||!red.animation.equals("Attack3")
                    ||blue.combatPose.handSpacing()>10||red.combatPose.handSpacing()>10)
                throw new AssertionError("Two-hand grip failed");
            Vector3f center=new Vector3f((duel.player.x+duel.enemy.x)/2,0,0);
            cam.setLocation(center.add(0,5,7));cam.lookAt(center.add(0,1.5f,0),Vector3f.UNIT_Y);
            uniformRenderCheck.captureEvidence("issue-10-two-hand-strike.png");
            System.out.println("TWO_HAND_GRIP_OK: player="+blue.combatPose.handSpacing()+", cpu="+red.combatPose.handSpacing());smokeStage++;
        }
        if(elapsed>5.5f&&smokeStage==7){
            duel.player.x=-4;duel.enemy.x=4;duel.player.z=duel.enemy.z=0;
            duel.player.attack=duel.enemy.attack=0;duel.player.stun=duel.enemy.stun=0;
            duel.flip(duel.player,0,-1);duel.flip(duel.enemy,1,1);aiTimer=10;smokeStage++;
        }
        if(elapsed>5.9f&&smokeStage==8){
            if(duel.player.flip<=0||duel.enemy.flip<=0||!blue.animation.equals("Backflip")||!red.animation.equals("Backflip")
                    ||duel.player.z>=-.5f||duel.enemy.x<=4.5f||duel.enemy.z<=.5f)
                throw new AssertionError("Directional flip failed");
            uniformRenderCheck.captureEvidence("issue-10-directional-flips.png");smokeStage++;
        }
        if(elapsed>6.05f&&smokeStage==9){smokeFlipRemaining=duel.player.flip;duel.paused=true;smokeStage++;}
        if(elapsed>6.5f&&smokeStage==10){
            if(Math.abs(duel.player.flip-smokeFlipRemaining)>.0001f||gameAudio.musicPlaying())throw new AssertionError("Flip pause failed");
            duel.paused=false;smokeStage++;
        }
        if(elapsed>7.4f&&smokeStage==11){
            if(duel.player.flip>0||duel.enemy.flip>0||duel.player.y!=0||duel.enemy.y!=0)throw new AssertionError("Flip landing failed");
            System.out.println("DIRECTIONAL_FLIPS_OK: both actors, travel, animation, pause/resume, landing");resetRound();smokeStage++;
        }
        if(elapsed>8&&smokeStage==12){
            if(!gameAudio.musicPlaying())throw new AssertionError("Music did not resume for rematch");
            System.out.println("AUDIO_OK: native output, music play/pause/resume, fight effects="+gameAudio.effectsPlayed());
            if(!uniformRenderCheck.passed())throw new AssertionError("Uniform render check did not run");
            if(duel.player.hp!=100||duel.enemy.hp!=100)throw new AssertionError("Rematch failed");
            System.out.println("SMOKE_OK: two-hand swords, directional flips, 3D arena, kicks, audio, pause, victory, rematch");stop();
        }
    }
}
