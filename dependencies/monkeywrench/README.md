# MonkeyWrench model importing

Installed version: **1.0.1**, from the published Maven Central release.
Source: https://github.com/stephengold/MonkeyWrench
Release: https://github.com/stephengold/MonkeyWrench/releases/tag/1.0.1
Tracked work: https://github.com/tbizz46/JMonkeyEngine-Ninja-Game/issues/2

## What is enabled

The game calls `ModelImports.configure(assetManager)` when it starts.
The registered formats are 3ds, 3mf, blend, bvh, dae, fbx, glb, gltf, lwo,
obj, ply, and stl. The existing Ogre XML loader remains in use for the two
animated ninjas. A J3O file still uses jMonkeyEngine's binary loader.

To add a new model, copy its files into `assets/Models/`, keeping its
textures next to it, then load it from game code. For example:

```java
Spatial model = assetManager.loadModel("Models/Scenery/lantern.glb");
rootNode.attachChild(model);
```

Use an asset-relative path; leave off the leading `assets/`.
The example path is a guide, not an included lantern model. MonkeyWrench
imports models; it does not supply new models or change current graphics.
The author recommends glTF 2.0 over FBX or Blend for new assets. Blend
support is deprecated. Some formats or animations may need conversion.
For shipping content, import and save as J3O before gameplay where feasible.

## Dependencies

The live runtime files are in `lib/`, which the launcher and SDK use.
The Mac package script also copies them into the app.

| Library | Version |
| --- | --- |
| MonkeyWrench | 1.0.1 |
| Heart | 9.3.0 |
| Wes | 0.8.3 |
| lwjgl-assimp | 3.3.6 |
| Assimp native classifier | natives-macos-arm64 |
| TwelveMonkeys imageio-tga, imageio-webp, imageio-core, imageio-metadata | 3.15.3 |
| TwelveMonkeys common-lang, common-io, common-image | 3.15.3 |

Existing jMonkeyEngine 3.9.0 and LWJGL 3.3.6 libraries are reused.
No second engine version is added. The older Heart file under
`graphics-packages/` remains an inactive download and is not on the
runtime classpath. Only the Apple Silicon native file is installed;
other platforms require their own matching Assimp and LWJGL natives.

`manifest.json` records URLs, sizes, and SHA-256 hashes. Maven downloads
were checked against Central's SHA-1 sidecars and passed archive checks.
`poms/` records the published dependencies. Source and API docs JARs for
MonkeyWrench are here. `licenses/` retains upstream license notices.

## Verification

Run `./run.command --test` for combat and real model import checks.
The import check loads OBJ and glTF geometry using native Assimp, rejects
missing and malformed models, then imports a valid model after the error.
It also checks that the original ninja still loads with animation.
The malformed model case prints the expected Assimp error before PASS.

A live `./run.command --smoke` checks the fight's startup, both animated
ninjas, combat, pause, victory, and rematch. These checks passed on the
Apple M3. Large models, FBX animation rigs, textures in TGA/WebP, and the
other registered model formats have not been tested.

## Build the Mac app

Close the game, then double-click `package-mac.command` or run it in
Terminal. It builds `dist/Night Ninja Duel.app` with the current libraries.
A prior app is moved to the printed staging folder as a backup only after
the new package is built. The SDK app must remain beside this project.
