package mygame;

public class DuelStateTest {
    static void check(boolean value,String reason) { if(!value) throw new AssertionError(reason); }
    static void advance(DuelState s,float time) { for(int i=0;i<(int)(time*120);i++) s.step(1f/120); }
    public static void main(String[] args) {
        DuelState s=new DuelState(); s.attack(s.player);
        check(s.player.attack==0,"Title screen must not accept attacks");
        s.started=true; s.attack(s.player); advance(s,.4f);
        check(s.enemy.hp==100,"Far attacks must miss");
        s.reset(); s.started=true; s.player.x=0; s.enemy.x=2;
        s.attack(s.player); advance(s,.4f); check(s.enemy.hp==86,"Sword hit damage");
        advance(s,.2f); check(s.enemy.hp==86,"Only one hit per swing");
        s.reset(); s.started=true; s.player.x=0; s.enemy.x=2; s.enemy.block=true;
        s.attack(s.player); advance(s,.4f); check(s.enemy.hp==97,"Block reduces damage");
        s.reset(); s.started=true; s.player.x=0; s.enemy.x=2; s.enemy.y=2;
        s.attack(s.player); s.paused=true; advance(s,1);
        check(s.player.attack==DuelState.ATTACK_TIME,"Pause freezes combat");
        s.paused=false; s.enemy.hp=14; s.enemy.y=0;
        advance(s,.4f); check(s.over && s.enemy.hp==0,"Defeat ends round");
        float x=s.player.x; s.move(s.player,1,1); check(s.player.x==x,"No moves after round");
        s.reset(); check(s.player.hp==100 && s.enemy.hp==100 && !s.started,"Rematch resets round");
        s.started=true; s.jump(s.player); advance(s,1.2f);
        check(s.player.y==0 && s.player.vy==0,"Jump lands on ground");
        System.out.println("PASS: start, miss, hit, block, pause, defeat, rematch, jump.");
    }
}
