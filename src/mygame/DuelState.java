package mygame;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/** Combat rules for a bounded 3D arena, independent of rendering and input. */
public final class DuelState {
    public enum Event { SWORD, KICK, HIT, BLOCK, JUMP, LAND }
    private final ArrayDeque<Event> events = new ArrayDeque<>();
    public List<Event> drainEvents() {
        List<Event> result=new ArrayList<>(events); events.clear(); return result;
    }
    public static final float RANGE = 2.5f, ATTACK_TIME = .72f, HIT_TIME = .28f;
    public static final float ARENA_X = 9, ARENA_Z = 5, BODY_GAP = 1.25f;
    public enum Strike {
        SWORD(ATTACK_TIME, HIT_TIME, RANGE, .93f, 14, 3, "Attack1"),
        KICK(.58f, .22f, 1.9f, .72f, 10, 2, "Kick");
        public final float duration, hitTime, range, cooldown;
        public final int damage, blockDamage;
        public final String animation;
        Strike(float duration, float hitTime, float range, float cooldown,
                int damage, int blockDamage, String animation) {
            this.duration=duration; this.hitTime=hitTime; this.range=range;
            this.cooldown=cooldown; this.damage=damage;
            this.blockDamage=blockDamage; this.animation=animation;
        }
    }
    public static final class Fighter {
        public float x, z, y, vy, attack, cooldown, stun;
        public float facingX, facingZ;
        public int hp=100;
        public boolean block, hit, moving;
        public Strike strike=Strike.SWORD;
        Fighter(float x, float facingX) { this.x=x; this.facingX=facingX; }
    }
    public Fighter player, enemy;
    public boolean started, paused, over;
    public String message="";
    public float messageTime;
    public DuelState() { reset(); }
    public void reset() {
        player=new Fighter(-4,1); enemy=new Fighter(4,-1);
        started=false; paused=false; over=false; message=""; messageTime=0;
        events.clear();
    }
    public boolean active() { return started && !paused && !over; }
    public float distance() { return groundDistance(player,enemy); }
    private static float groundDistance(Fighter a, Fighter b) {
        return (float)Math.hypot(a.x-b.x,a.z-b.z);
    }
    public void attack(Fighter f) { startStrike(f,Strike.SWORD); }
    public void kick(Fighter f) { if(f.y==0) startStrike(f,Strike.KICK); }
    private void startStrike(Fighter f, Strike strike) {
        if(active() && f.cooldown<=0 && f.stun<=0 && f.attack<=0 && !f.block) {
            face(f,f==player?enemy:player);
            f.strike=strike; f.attack=strike.duration;
            f.cooldown=strike.cooldown; f.hit=false;
            events.add(strike==Strike.SWORD?Event.SWORD:Event.KICK);
        }
    }
    public void jump(Fighter f) {
        if(active() && f.y==0 && f.attack<=0 && !f.block && f.stun<=0) {
            f.vy=6.6f; events.add(Event.JUMP);
        }
    }
    public void move(Fighter f, float dx, float dz, float dt) {
        if(active() && f.attack<=0 && f.stun<=0) {
            float length=(float)Math.hypot(dx,dz);
            if(length>1) { dx/=length; dz/=length; }
            float oldX=f.x, oldZ=f.z;
            float speed=f.block?1.15f:4.2f;
            f.x+=dx*speed*dt; f.z+=dz*speed*dt; clamp(f);
            f.moving=Math.abs(f.x-oldX)+Math.abs(f.z-oldZ)>.0001f;
        }
    }
    private static void clamp(Fighter f) {
        f.x=Math.max(-ARENA_X,Math.min(ARENA_X,f.x));
        f.z=Math.max(-ARENA_Z,Math.min(ARENA_Z,f.z));
    }
    private static void face(Fighter a,Fighter b) {
        float length=groundDistance(a,b);
        if(length>.001f) { a.facingX=(b.x-a.x)/length; a.facingZ=(b.z-a.z)/length; }
    }
    private void separateBodies() {
        float dx=enemy.x-player.x, dz=enemy.z-player.z, length=distance();
        if(length>=BODY_GAP) return;
        float nx=length>.001f?dx/length:1, nz=length>.001f?dz/length:0;
        float overlap=BODY_GAP-length;
        float oldX=player.x, oldZ=player.z;
        player.x-=nx*overlap/2; player.z-=nz*overlap/2; clamp(player);
        float moved=(oldX-player.x)*nx+(oldZ-player.z)*nz;
        oldX=enemy.x; oldZ=enemy.z;
        enemy.x+=nx*(overlap-moved); enemy.z+=nz*(overlap-moved); clamp(enemy);
        float remaining=overlap-moved-(enemy.x-oldX)*nx-(enemy.z-oldZ)*nz;
        player.x-=nx*remaining; player.z-=nz*remaining; clamp(player);
    }
    public void step(float dt) {
        if(!active()) return;
        messageTime=Math.max(0,messageTime-dt);
        for(Fighter f:new Fighter[]{player,enemy}) {
            f.cooldown=Math.max(0,f.cooldown-dt); f.stun=Math.max(0,f.stun-dt);
            if(f.y>0 || f.vy>0) {
                f.vy-=17*dt; f.y=Math.max(0,f.y+f.vy*dt);
                if(f.y==0) { f.vy=0; events.add(Event.LAND); }
            }
            f.attack=Math.max(0,f.attack-dt);
        }
        separateBodies();
        // Idle fighters track their target. A strike keeps its launch direction.
        if(player.attack<=0) face(player,enemy);
        if(enemy.attack<=0) face(enemy,player);
        hit(player,enemy); if(!over) hit(enemy,player);
    }
    private void hit(Fighter a,Fighter b) {
        Strike strike=a.strike;
        if(a.hit || a.attack<=0 || strike.duration-a.attack<strike.hitTime) return;
        a.hit=true;
        float length=groundDistance(a,b);
        float facing=length>.001f?((b.x-a.x)*a.facingX+(b.z-a.z)*a.facingZ)/length:1;
        if(length>strike.range || Math.abs(a.y-b.y)>1.1f || facing<.45f) {
            message="Miss - move closer and face your opponent"; messageTime=.7f; return;
        }
        boolean blocked=b.block && b.y==0 && b.attack<=0;
        int damage=blocked?strike.blockDamage:strike.damage;
        events.add(blocked?Event.BLOCK:Event.HIT);
        b.hp=Math.max(0,b.hp-damage); b.stun=blocked?.07f:.17f;
        float push=blocked?.12f:.36f;
        b.x+=a.facingX*push; b.z+=a.facingZ*push; clamp(b);
        message=(blocked?"BLOCK":strike==Strike.KICK?"KICK HIT":"SWORD HIT")+"  -"+damage;
        messageTime=.7f;
        if(b.hp==0) { over=true; player.block=false; enemy.block=false; }
    }
}
