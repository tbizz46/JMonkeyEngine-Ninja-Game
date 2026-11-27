package mygame;

/** Combat rules, kept independent of rendering and keyboard input. */
public final class DuelState {
    public static final float RANGE = 2.5f, ATTACK_TIME = .72f, HIT_TIME = .28f;
    public static final class Fighter {
        public float x, y, vy, attack, cooldown, stun;
        public int hp = 100, facing;
        public boolean block, hit;
        Fighter(float x, int facing) { this.x = x; this.facing = facing; }
    }
    public Fighter player, enemy;
    public boolean started, paused, over;
    public String message = "";
    public float messageTime;
    public DuelState() { reset(); }
    public void reset() {
        player = new Fighter(-4, 1); enemy = new Fighter(4, -1);
        started = false; paused = false; over = false; message = "";
    }
    public boolean active() { return started && !paused && !over; }
    public void attack(Fighter f) {
        if (active() && f.cooldown <= 0 && f.stun <= 0 && !f.block) {
            f.attack = ATTACK_TIME; f.cooldown = .93f; f.hit = false;
        }
    }
    public void jump(Fighter f) {
        if (active() && f.y == 0 && f.attack <= 0 && !f.block && f.stun <= 0) f.vy = 6.6f;
    }
    public void move(Fighter f, float direction, float dt) {
        if (active() && f.attack <= 0 && f.stun <= 0) {
            f.x = Math.max(-9, Math.min(9, f.x + direction * (f.block ? 1.15f : 4.2f) * dt));
        }
    }
    public void step(float dt) {
        if (!active()) return;
        messageTime = Math.max(0, messageTime - dt);
        for (Fighter f : new Fighter[]{player, enemy}) {
            f.cooldown = Math.max(0, f.cooldown-dt); f.stun = Math.max(0, f.stun-dt);
            if (f.y > 0 || f.vy > 0) { f.vy -= 17*dt; f.y = Math.max(0, f.y+f.vy*dt); if (f.y==0) f.vy=0; }
            f.attack = Math.max(0, f.attack-dt);
        }
        // Keep bodies apart and preserve the side-view fighting lane.
        float gap = enemy.x-player.x;
        if (Math.abs(gap)<1.25f) {
            float mid=(player.x+enemy.x)/2; float sign=gap>=0?1:-1;
            player.x=mid-sign*.625f; enemy.x=mid+sign*.625f;
        }
        player.facing=enemy.x>=player.x?1:-1; enemy.facing=-player.facing;
        hit(player,enemy); if (!over) hit(enemy,player);
    }
    private void hit(Fighter a, Fighter b) {
        if (a.hit || a.attack <= 0 || ATTACK_TIME-a.attack < HIT_TIME) return;
        a.hit=true;
        if (Math.abs(a.x-b.x)>RANGE || Math.abs(a.y-b.y)>1.1f) {
            message="Out of reach"; messageTime=.55f; return;
        }
        boolean blocked=b.block && b.y==0 && b.attack<=0;
        int damage=blocked?3:14;
        b.hp=Math.max(0,b.hp-damage); b.stun=blocked?.07f:.17f;
        b.x=Math.max(-9,Math.min(9,b.x+a.facing*(blocked?.12f:.36f)));
        message=blocked?"BLOCK  -3":"SWORD HIT  -14"; messageTime=.55f;
        if(b.hp==0) { over=true; player.block=false; enemy.block=false; }
    }
}
