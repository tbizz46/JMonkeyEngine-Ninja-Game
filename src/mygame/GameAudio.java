package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import java.util.EnumMap;
import java.util.List;

/** Routes confirmed game events to audio and keeps mute/pause state in one place. */
public final class GameAudio {
    public enum Cue {
        SWORD("sword"), KICK("kick"), HIT("hit"), BLOCK("block"), JUMP("jump"), LAND("land"),
        ROUND_START("round_start"), VICTORY("victory"), DEFEAT("defeat");
        final String file;
        Cue(String file) { this.file=file; }
    }
    interface Backend {
        void effect(Cue cue);
        void music(boolean playing);
        void stopEffects();
        boolean musicPlaying();
        void close();
    }
    private final Backend backend;
    private boolean musicEnabled=true, effectsEnabled=true;
    private boolean hadStarted, wasOver, wasPaused;
    private int effectsPlayed;

    GameAudio(Backend backend) { this.backend=backend; }
    public static GameAudio available(AssetManager assets) { return new GameAudio(new NativeBackend(assets)); }
    public static GameAudio unavailable() { return new GameAudio(null); }
    public boolean available() { return backend!=null; }
    public boolean musicEnabled() { return musicEnabled; }
    public boolean effectsEnabled() { return effectsEnabled; }
    public int effectsPlayed() { return effectsPlayed; }
    public boolean musicPlaying() { return available()&&backend.musicPlaying(); }
    public void toggleMusic() { musicEnabled=!musicEnabled; }
    public void toggleEffects() {
        effectsEnabled=!effectsEnabled;
        if(!effectsEnabled&&available())backend.stopEffects();
    }
    private void effect(Cue cue) {
        if(available()&&effectsEnabled) { backend.effect(cue); effectsPlayed++; }
    }
    public void sync(DuelState duel) {
        List<DuelState.Event> events=duel.drainEvents();
        if(!available())return;
        boolean freshRound=duel.started&&(!hadStarted||(wasOver&&!duel.over));
        if(freshRound || (duel.paused&&!wasPaused))backend.stopEffects();
        boolean wantMusic=musicEnabled&&!duel.paused&&!duel.over;
        // Check the real source state so a resumed/reset device cannot defeat mute or pause.
        if(wantMusic!=backend.musicPlaying())backend.music(wantMusic);
        if(duel.started&&!duel.paused) {
            if(freshRound)effect(Cue.ROUND_START);
            for(DuelState.Event event:events)effect(Cue.valueOf(event.name()));
            if(duel.over&&!wasOver)effect(duel.player.hp>0?Cue.VICTORY:Cue.DEFEAT);
        }
        hadStarted=duel.started; wasOver=duel.over; wasPaused=duel.paused;
    }
    public String help() {
        return available()?"M  Music: "+(musicEnabled?"On":"Off")+"    N  Effects: "+(effectsEnabled?"On":"Off")
                :"Audio unavailable - check your output device and restart";
    }
    public void close() { if(available())backend.close(); }

    private static final class NativeBackend implements Backend {
        private final AudioNode music;
        private final EnumMap<Cue,AudioNode[]> effects=new EnumMap<>(Cue.class);
        private final EnumMap<Cue,Integer> next=new EnumMap<>(Cue.class);
        NativeBackend(AssetManager assets) {
            music=node(assets,"night_field_music");music.setLooping(true);music.setVolume(.22f);
            for(Cue cue:Cue.values()) {
                AudioNode[] voices=new AudioNode[3];
                for(int i=0;i<voices.length;i++) { voices[i]=node(assets,cue.file); voices[i].setVolume(.5f); }
                effects.put(cue,voices);next.put(cue,0);
            }
        }
        private AudioNode node(AssetManager assets,String file) {
            AudioNode node=new AudioNode(assets,"Sounds/"+file+".wav",AudioData.DataType.Buffer);
            node.setPositional(false);node.setReverbEnabled(false);return node;
        }
        @Override public void effect(Cue cue) {
            AudioNode[] voices=effects.get(cue);int index=next.get(cue);
            voices[index].stop();voices[index].play();next.put(cue,(index+1)%voices.length);
        }
        @Override public void music(boolean playing) {
            if(playing)music.play();else music.pause();
        }
        @Override public boolean musicPlaying() { return music.getStatus()==AudioSource.Status.Playing; }
        @Override public void stopEffects() {
            for(AudioNode[] voices:effects.values())for(AudioNode node:voices)node.stop();
        }
        @Override public void close() { music.stop();stopEffects(); }
    }
}
