package mygame;

import com.jme3.asset.AssetNotFoundException;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.audio.AudioBuffer;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioKey;
import java.util.ArrayList;
import java.util.List;

public final class GameAudioTest {
    static final class FakeBackend implements GameAudio.Backend {
        final List<GameAudio.Cue> cues=new ArrayList<>();
        boolean music,closed;
        int stops;
        public void effect(GameAudio.Cue cue) { cues.add(cue); }
        public void music(boolean playing) { music=playing; }
        public void stopEffects() { stops++; }
        public boolean musicPlaying() { return music; }
        public void close() { closed=true;music=false; }
    }
    static void check(boolean value,String reason) { if(!value)throw new AssertionError(reason); }
    static DuelState round() {
        DuelState duel=new DuelState();duel.started=true;duel.player.x=0;duel.enemy.x=1.6f;return duel;
    }
    static void advance(DuelState duel,float time) { for(int i=0;i<time*120;i++)duel.step(1f/120); }
    public static void main(String[] args) {
        FakeBackend output=new FakeBackend();GameAudio sound=new GameAudio(output);DuelState duel=new DuelState();
        sound.sync(duel);check(output.music&&output.cues.isEmpty(),"Title has music without fight cues");
        duel=round();duel.kick(duel.player);sound.sync(duel);
        check(output.cues.equals(List.of(GameAudio.Cue.ROUND_START,GameAudio.Cue.KICK)),"Accepted kick cue");
        advance(duel,.3f);sound.sync(duel);check(output.cues.get(2)==GameAudio.Cue.HIT,"Confirmed hit cue");
        sound.sync(duel);check(output.cues.size()==3,"No duplicate frame sounds");
        int stops=output.stops;duel.paused=true;sound.sync(duel);
        check(!output.music&&output.stops>stops,"Pause stops music and active effects");
        output.music=true;sound.sync(duel);
        check(!output.music,"Unexpected playback must be silenced while paused");
        duel.kick(duel.player);sound.sync(duel);check(output.cues.size()==3,"Paused input stays silent");
        duel.paused=false;sound.sync(duel);check(output.music,"Resume music");
        sound.toggleEffects();duel.player.cooldown=0;duel.player.attack=0;
        duel.kick(duel.player);advance(duel,.3f);sound.sync(duel);
        check(output.cues.size()==3,"Muted effects stay silent");
        sound.toggleEffects();sound.sync(duel);check(output.cues.size()==3,"No delayed muted cues");
        sound.toggleMusic();sound.sync(duel);check(!output.music&&sound.effectsEnabled(),"Music mute independent of effects");
        duel.over=true;duel.enemy.hp=0;sound.sync(duel);
        check(output.cues.get(output.cues.size()-1)==GameAudio.Cue.VICTORY,"Victory cue");
        int size=output.cues.size();sound.sync(duel);check(output.cues.size()==size,"Ending cue only once");
        duel.reset();duel.started=true;sound.sync(duel);
        check(!output.music&&!sound.musicEnabled(),"Rematch keeps music preference");
        check(output.cues.get(output.cues.size()-1)==GameAudio.Cue.ROUND_START,"Rematch cue");
        sound.toggleMusic();sound.sync(duel);check(output.music,"Music can be restored");
        duel.over=true;duel.player.hp=0;sound.sync(duel);
        check(output.cues.get(output.cues.size()-1)==GameAudio.Cue.DEFEAT,"Defeat cue");
        sound.close();check(output.closed,"Close releases playback");

        output=new FakeBackend();sound=new GameAudio(output);duel=round();duel.enemy.block=true;
        duel.attack(duel.player);advance(duel,.4f);sound.sync(duel);
        check(output.cues.contains(GameAudio.Cue.BLOCK)&&!output.cues.contains(GameAudio.Cue.HIT),"Block has its own cue");
        duel=round();duel.enemy.x=8;duel.attack(duel.player);advance(duel,.4f);
        check(duel.drainEvents().equals(List.of(DuelState.Event.SWORD)),"Miss has swing but no impact");
        duel=round();duel.jump(duel.player);advance(duel,1.2f);
        check(duel.drainEvents().equals(List.of(DuelState.Event.JUMP,DuelState.Event.LAND)),"One jump and landing cue");
        GameAudio silent=GameAudio.unavailable();duel=round();duel.kick(duel.player);silent.sync(duel);
        check(!silent.available()&&duel.drainEvents().isEmpty(),"No-device path consumes events safely");
        silent.toggleMusic();silent.toggleEffects();silent.close();

        DesktopAssetManager assets=new DesktopAssetManager(true);
        List<String> files=new ArrayList<>();for(GameAudio.Cue cue:GameAudio.Cue.values())files.add(cue.file);
        files.add("night_field_music");
        for(String file:files) {
            AudioData audio=assets.loadAsset(new AudioKey("Sounds/"+file+".wav",false));
            check(audio instanceof AudioBuffer,"Buffered WAV: "+file);
            check(audio.getChannels()==1&&audio.getBitsPerSample()==16&&audio.getSampleRate()==24000,"PCM format: "+file);
            check(audio.getDuration()>.1f&&audio.getDuration()<30,"Decoded duration: "+file);
        }
        boolean rejected=false;
        try { assets.loadAsset(new AudioKey("Sounds/missing.wav",false)); }
        catch(AssetNotFoundException expected) { rejected=true; }
        check(rejected,"Missing audio reports a load failure");
        System.out.println("PASS: confirmed sound cues, music/effects toggles, pause/resume, rematch preferences, no queued muted sounds, no-device recovery, all 10 WAV assets decoded.");
    }
}
