package mygame;

public class DuelStateTest {
    static void check(boolean value,String reason) { if(!value) throw new AssertionError(reason); }
    static void near(float a,float b,String reason) { check(Math.abs(a-b)<.002f,reason+" "+a+" != "+b); }
    static void advance(DuelState s,float time) { for(int i=0;i<(int)(time*120);i++) s.step(1f/120); }
    static DuelState close() {
        DuelState s=new DuelState();s.started=true;s.player.x=0;s.enemy.x=1.6f;return s;
    }
    public static void main(String[] args) {
        DuelState s=new DuelState(); s.attack(s.player);s.kick(s.player);
        check(s.player.attack==0,"Title screen must reject strikes");
        s.started=true;s.attack(s.player);advance(s,.4f);check(s.enemy.hp==100,"Far sword misses");
        s=close();s.attack(s.player);advance(s,.4f);check(s.enemy.hp==86,"Sword damage");
        advance(s,.2f);check(s.enemy.hp==86,"One hit per swing");
        s=close();s.enemy.block=true;s.attack(s.player);advance(s,.4f);check(s.enemy.hp==97,"Sword block");
        s=close();s.kick(s.player);check(s.player.strike==DuelState.Strike.KICK,"Kick selected");
        advance(s,.3f);check(s.enemy.hp==90,"Kick damage");
        s.kick(s.player);check(s.player.attack<DuelState.Strike.KICK.duration,"Cooldown stops kick spam");
        s.attack(s.player);check(s.player.strike==DuelState.Strike.KICK,"Cannot replace active kick");
        s=close();s.enemy.block=true;s.kick(s.player);advance(s,.3f);check(s.enemy.hp==98,"Kick block");
        s=close();s.kick(s.enemy);advance(s,.3f);check(s.player.hp==90,"Enemy can kick");
        s=close();s.enemy.x=2.2f;s.kick(s.player);advance(s,.3f);check(s.enemy.hp==100,"Kick shorter than sword");
        s=close();s.enemy.z=3;s.attack(s.player);advance(s,.4f);check(s.enemy.hp==100,"Depth prevents remote hit");
        s=close();s.enemy.x=0;s.enemy.z=1.6f;s.kick(s.player);advance(s,.3f);
        check(s.enemy.hp==90,"Kick works along depth axis");check(s.enemy.z>1.6f,"Knockback follows depth");
        s=close();s.attack(s.player);s.enemy.x=-1.6f;advance(s,.4f);check(s.enemy.hp==100,"Moving behind a strike dodges it");
        s=close();s.player.x=-4;s.enemy.x=4;s.move(s.player,1,1,1);
        near((float)Math.hypot(s.player.x+4,s.player.z),4.2f,"Diagonal speed equals straight speed");
        s.move(s.player,1,1,100);check(s.player.x<=DuelState.ARENA_X&&s.player.z<=DuelState.ARENA_Z,"Arena bounds");
        s=close();s.player.x=s.enemy.x=9;s.player.z=s.enemy.z=5;s.step(.01f);
        check(s.distance()>=DuelState.BODY_GAP-.002f,"Bodies separate at arena corner");
        check(s.player.x<=9&&s.enemy.x<=9&&s.player.z<=5&&s.enemy.z<=5,"Separation respects bounds");
        s=close();s.kick(s.player);s.paused=true;float z=s.player.z;s.move(s.player,0,1,1);advance(s,1);
        near(s.player.attack,DuelState.Strike.KICK.duration,"Pause freezes kick");near(s.player.z,z,"Pause freezes depth movement");
        s.paused=false;s.enemy.hp=10;advance(s,.3f);check(s.over&&s.enemy.hp==0,"Kick ends round");
        float x=s.player.x;s.move(s.player,1,1,1);near(s.player.x,x,"No moves after round");
        s.reset();check(s.player.hp==100&&s.enemy.hp==100&&!s.started&&s.player.z==0,"Rematch resets round");
        s.started=true;s.jump(s.player);advance(s,1.2f);check(s.player.y==0&&s.player.vy==0,"Jump lands");
        s=close();s.player.y=2;s.kick(s.player);check(s.player.attack==0,"Kick requires ground");
        s=close();s.enemy.y=2;s.kick(s.player);advance(s,.24f);check(s.enemy.hp==100,"Height can dodge kick");
        System.out.println("PASS: 3D movement, diagonal speed, arena bounds, body spacing, facing, sword/kick range, both ninjas kicking, blocking, cooldown, jump, pause, victory, rematch.");
    }
}
