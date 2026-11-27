package mygame;

import java.util.List;

public final class FlipTest {
    static void check(boolean value,String reason) { if(!value)throw new AssertionError(reason); }
    static void near(float a,float b,String reason) { check(Math.abs(a-b)<.005f,reason+": "+a+" != "+b); }
    static void advance(DuelState duel,float time) { for(int i=0;i<time*120;i++)duel.step(1f/120); }
    static DuelState round() { DuelState d=new DuelState();d.started=true;d.player.x=0;d.enemy.x=8;return d; }
    public static void main(String[] args) {
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++) {
            if(x==0&&z==0)continue;
            DuelState d=round();d.flip(d.player,x,z);advance(d,.95f);
            float norm=(float)Math.hypot(x,z);
            near(d.player.x,DuelState.FLIP_DISTANCE*x/norm,"Directional X travel");
            near(d.player.z,DuelState.FLIP_DISTANCE*z/norm,"Directional Z travel");
            check(d.player.flip==0&&d.player.y==0,"Flip lands");
            check(d.drainEvents().equals(List.of(DuelState.Event.FLIP,DuelState.Event.LAND)),"One launch and landing cue");
        }
        DuelState d=new DuelState();d.flip(d.player,1,0);check(d.player.flip==0,"Title rejects flip");
        d=round();d.player.facingX=.6f;d.player.facingZ=.8f;d.flip(d.player,0,0);
        near(d.player.flipX,.6f,"No-input facing fallback X");near(d.player.flipZ,.8f,"No-input facing fallback Z");
        advance(d,.2f);float x=d.player.x,z=d.player.z,y=d.player.y,remaining=d.player.flip;
        d.move(d.player,-1,-1,1);d.attack(d.player);d.kick(d.player);d.jump(d.player);
        near(d.player.x,x,"Movement cannot steer a flip");check(d.player.attack==0&&d.player.vy==0,"No strike/jump during flip");
        d.paused=true;advance(d,.5f);near(d.player.x,x,"Pause freezes travel");near(d.player.y,y,"Pause freezes flip height");near(d.player.flip,remaining,"Pause freezes flip timer");
        d.paused=false;advance(d,1);check(d.player.flip==0,"Resume lands");
        d=round();d.flip(d.player,1,0);advance(d,.95f);d.flip(d.player,0,1);check(d.player.flip==0,"Cooldown stops repeated flips");
        check(d.message.startsWith("Flip ready in"),"Cooldown explains when to retry");
        advance(d,.45f);d.flip(d.player,0,1);check(d.player.flip>0,"Cooldown allows a later flip");
        d=round();d.player.x=9;d.player.z=5;d.flip(d.player,1,1);advance(d,1);
        check(d.player.x<=9&&d.player.z<=5&&d.player.y==0,"Arena corner clamps travel and still lands");
        d=round();d.player.x=-2;d.enemy.x=0;d.flip(d.player,1,0);advance(d,.55f);
        check(d.player.x>d.enemy.x&&d.player.y>1.1f,"Flip can pass over an opponent");
        d=round();d.flip(d.enemy,-1,0);advance(d,.95f);near(d.enemy.x,8-DuelState.FLIP_DISTANCE,"CPU can flip");
        d=round();d.player.block=true;d.flip(d.player,1,0);check(d.player.flip==0,"Blocking rejects flip");
        check(d.message.equals("Release K to flip"),"Blocked flip gives a recovery hint");
        d.player.block=false;d.player.stun=.2f;d.flip(d.player,1,0);check(d.player.flip==0,"Stun rejects flip");
        d=round();d.attack(d.player);d.flip(d.player,1,0);check(d.player.flip==0,"Attack rejects flip");
        d=round();d.jump(d.player);d.flip(d.player,1,0);check(d.player.flip==0,"Jump launch rejects flip");
        d=round();d.paused=true;d.flip(d.player,1,0);check(d.player.flip==0,"Pause rejects flip");
        d=round();d.over=true;d.flip(d.player,1,0);check(d.player.flip==0,"Results reject flip");
        d=round();d.flip(d.player,1,0);d.reset();check(d.player.flip==0&&d.player.flipCooldown==0&&d.drainEvents().isEmpty(),"Rematch clears flip state and cues");
        System.out.println("PASS: eight flip directions, facing fallback, locked travel, bounds, overhead dodge, CPU flip, cooldown, invalid states, pause, landing, rematch.");
    }
}
