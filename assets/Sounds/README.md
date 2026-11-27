# Original game audio

These ten mono 24 kHz / 16-bit PCM WAV files were synthesized for Night
Ninja Duel using `tools/generate_audio.py`. They contain no third-party
recordings or commercial music. The script uses only Python's standard
library and a fixed random seed, so the files can be reproduced.

- `night_field_music.wav`: 12-second drum, plucked-tone, and ambient loop.
- `sword.wav`: a short noise-based swing.
- `kick.wav`: a short lower whoosh.
- `hit.wav`: an impact thump.
- `block.wav`: a metallic ring.
- `jump.wav`: a short rising tone.
- `land.wav`: a soft landing thump.
- `round_start.wav`: a two-note cue.
- `victory.wav`: a rising four-note cue.
- `defeat.wav`: a falling four-note cue.

Run `python3 tools/generate_audio.py` from the project folder to rebuild
them. Peak amplitude is capped at 0.78 before the game's volume controls.
The game plays music at 0.22 volume and effects at 0.5 volume.
