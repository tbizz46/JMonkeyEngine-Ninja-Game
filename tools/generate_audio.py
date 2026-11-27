"""Generate original PCM audio for Night Ninja Duel; uses only Python's stdlib."""
from pathlib import Path
import math
import random
import struct
import wave

RATE = 24000
OUTPUT = Path(__file__).resolve().parents[1] / "assets" / "Sounds"
rng = random.Random(8)


def tone(frequency, t):
    return math.sin(2 * math.pi * frequency * t)


def write(name, samples):
    peak = max(abs(value) for value in samples) or 1
    scale = min(1, 0.78 / peak)
    with wave.open(str(OUTPUT / (name + ".wav")), "wb") as output:
        output.setparams((1, 2, RATE, 0, "NONE", "not compressed"))
        output.writeframes(b"".join(struct.pack("<h", round(value * scale * 32767)) for value in samples))


def effect(name, duration, sample):
    values = []
    for i in range(round(duration * RATE)):
        t = i / RATE
        fade = min(1, t / 0.008, (duration - t) / 0.012)
        values.append(sample(t) * max(0, fade))
    write(name, values)


def melody(name, notes):
    step = 0.18
    effect(name, len(notes) * step + 0.25,
           lambda t: sum(0.3 * math.exp(-7 * (t - i * step)) * tone(note, t - i * step)
                         for i, note in enumerate(notes) if t >= i * step))


def main():
    OUTPUT.mkdir(parents=True, exist_ok=True)
    effect("sword", 0.23, lambda t: rng.uniform(-1, 1) * 0.5 * math.sin(math.pi * t / 0.23) ** 2)
    effect("kick", 0.18, lambda t: (0.28 * rng.uniform(-1, 1) + 0.25 * tone(180, t)) * math.exp(-14 * t))
    effect("hit", 0.19, lambda t: (0.6 * tone(85, t) + 0.3 * rng.uniform(-1, 1)) * math.exp(-23 * t))
    effect("block", 0.28, lambda t: sum(0.2 * tone(f, t) for f in (1440, 2270, 3590)) * math.exp(-15 * t))
    effect("jump", 0.21, lambda t: 0.25 * math.sin(2 * math.pi * (190 * t + 500 * t * t)) * math.exp(-8 * t))
    effect("land", 0.16, lambda t: (0.35 * tone(65, t) + 0.17 * rng.uniform(-1, 1)) * math.exp(-25 * t))
    melody("round_start", (293.66, 440))
    melody("victory", (293.66, 349.23, 440, 587.33))
    melody("defeat", (220, 196, 174.61, 146.83))

    # Four bars at 80 BPM. Wrap note tails to keep the loop boundary continuous.
    duration, beat = 12, 0.75
    music = [0.0] * (RATE * duration)
    for i in range(len(music)):
        t = i / RATE
        music[i] = 0.035 * (tone(73.416, t) + tone(110, t)) * math.sin(math.pi * t / duration) ** 2
    for b in range(16):
        start = round(b * beat * RATE)
        for j in range(round(0.65 * RATE)):
            t = j / RATE
            drum = math.sin(2 * math.pi * (65 * t + 2 * (1 - math.exp(-30 * t))))
            music[(start + j) % len(music)] += 0.13 * drum * math.exp(-9 * t) * min(1, t / 0.008)
    notes = (146.83, 220, 196, 174.61, 146.83, 261.63, 220, 196)
    for n, note in enumerate(notes):
        start = round(n * 2 * beat * RATE)
        for j in range(round(1.8 * RATE)):
            t = j / RATE
            pluck = tone(note, t) + 0.25 * tone(2 * note, t)
            music[(start + j) % len(music)] += 0.10 * pluck * math.exp(-3.5 * t) * min(1, t / 0.012)
    write("night_field_music", music)
    print("Generated 10 original mono 24 kHz / 16-bit WAV files.")


if __name__ == "__main__":
    main()
