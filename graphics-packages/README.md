# Graphics packages for Night Ninja Duel

Reviewed and downloaded: November 27, 2026.
Status: downloaded for use in a future graphics pass. The game has not been
changed or rebuilt in this task. Downloads total about 6.6 MB.

## Selected packages

| Package | Download | What it can add | Fit for this game |
| --- | --- | --- | --- |
| SkyControl 1.1.0 | Runtime, source, API docs, POM, license | Moon phases, stars, cloud layers, horizon haze, linked lighting | First choice: replace the plain moon and star shapes with a full night sky |
| Heart 9.1.0 | Runtime, source, API docs, POM, license | Shared tools used by SkyControl | Required by the published SkyControl 1.1.0 POM; not a visual effect by itself |
| Particle Monkey 1.1.0 | Runtime, source, API docs, POM, license | Particle emitters with animated particle properties | Use for short sword sparks, footstep dust, smoke, and fireflies; these effects must be authored |
| ShaderBlowEx | Source ZIP pinned to commit 7c2e19320e71a36c5f8858b8d701b1c3c3b05daa, MIT license | Ground fog, color correction, tone mapping, bloom, and other screen effects | Use a small amount of ground fog and color grading; this source needs a build update before use |

## Files

- `runtime/`: three runtime JAR files, ready for a compatibility trial.
- `source/`: source JARs and the pinned ShaderBlowEx source ZIP.
- `docs/`: API docs JARs, dependency POMs, and ShaderBlowEx build details.
- `licenses/`: upstream license text for all four projects.
- `manifest.json`: exact download URLs, sizes, hashes, and checksum results.
- `SHA256SUMS.txt`: local SHA-256 hashes for all 17 downloaded files.

## Fit and limits

The game uses jMonkeyEngine 3.9.0-stable and runs on Apple Silicon.
SkyControl's published POM targets jME 3.6.1 and requires Heart 9.1.0.
Heart targets jME 3.6.1. Particle Monkey targets jME 3.3.2. Their runtime
JARs contain no bundled platform-native libraries. That makes them useful
candidates, but does not prove that their shaders will work on this Mac.

ShaderBlowEx targets jME 3.2.4 and its build uses JCenter. Keep it as source
until its build points to Maven Central and the game's current engine.
Do not add older jME core or renderer JARs alongside the game's 3.9 JARs.

The README for SkyControl suggests an older Heart version in its manual
install steps. These downloads follow the exact dependency in the 1.1.0
published POM instead: Heart 9.1.0.

The engine's existing effects library already includes bloom, fog, SSAO,
FXAA, and shadow tools. These should be tried before adding more filters.
PBR terrain shaders also ship with jME terrain; the SDK has that module.
Extra world streaming tools offer little benefit for this small arena.

I also reviewed Jme-VFX and SevenSky as alternatives. Jme-VFX describes
itself as work in progress and adds several dependencies. SkyControl and
Particle Monkey offer a smaller starting point for this game's needs.
No alternate rendering engine or engine beta was downloaded.

## Suggested order

1. Add SkyControl and Heart in a separate graphics preview. Keep the fight
   lane, camera, and controls. Check that the moon stays below the HUD.
2. Add gentle lantern bloom, shadows, and distance haze using engine tools.
3. Add short Particle Monkey sparks when a sword hit or block is confirmed.
4. Try ShaderBlowEx ground fog and color grading after updating its build.
5. Check that the two fighters, their swords, and health bars stay clear.
   Compare frame time and pause/resume behavior before keeping the effects.

These are code libraries, not high-detail ninja or field art packs. New
models and textures would be a separate step if those are needed.

## Review coverage

The supplied Library page loaded its static shell in web search, but the
live catalog could not be retrieved. The browser tool was unavailable;
the public catalog API returned HTTP 403. I checked the website's public
source to find the normal API, then reviewed the authors' repositories
and published Maven metadata. I cannot confirm the current listed state,
star order, or a complete review of all catalog pages.

All 12 Maven downloads matched Maven Central SHA-1 sidecars. Every JAR
and ZIP passed archive integrity checks. All 17 files have recorded
SHA-256 hashes. The archive hashes for GitHub downloads are local records,
not an independent publisher signature. No downloaded source was executed.
Visual compatibility and frame rate have not been tested for these add-ons.

## Sources

- Requested catalog: https://jmonkeyengine.org/library/?page=1&sort=stars&direction=desc&states=LISTED
- Catalog frontend source: https://github.com/jMonkeyEngine/jmonkeyengine-website/blob/master/static/js/library.js
- SkyControl: https://github.com/stephengold/SkyControl
- SkyControl release: https://github.com/stephengold/SkyControl/releases/tag/1.1.0
- SkyControl POM: https://repo.maven.apache.org/maven2/com/github/stephengold/SkyControl/1.1.0/SkyControl-1.1.0.pom
- Heart: https://github.com/stephengold/Heart
- Particle Monkey: https://github.com/Jeddic/particlemonkey
- ShaderBlowEx: https://github.com/polincdev/ShaderBlowEx
- Jme-VFX: https://github.com/codex128/Jme-VFX
- SevenSky: https://github.com/TehLeo/SevenSky
- Engine effects: https://wiki.jmonkeyengine.org/docs/3.8/core/effect/effects_overview.html
