# Night Ninja Duel

Source: https://github.com/tbizz46/JMonkeyEngine-Ninja-Game

Issues and work board: https://github.com/users/tbizz46/projects/6/views/1

A side-view sword fight in a moonlit field. You fight a computer ninja.
This is a first playable game, built with jMonkeyEngine 3.9.0.

## Play

For the local Mac app, open `dist/Night Ninja Duel.app` and press **Enter**.
That build is kept on the development Mac and is not stored in Git.
For a fresh clone, install the jMonkeyEngine SDK beside this folder and
double-click `run.command` to compile and play.
The blue ring marks your ninja. The red ring marks the computer.
Both use the ninja model shipped with the SDK.

| Key | Action |
| --- | --- |
| A / D or arrow keys | Move |
| J | Swing sword |
| K (hold) | Block |
| Space | Jump |
| Escape or P | Pause / resume |
| Enter | Start, resume, or rematch |
| Q | Quit from start, pause, or results |

Get close before you swing. A hit does 14 damage. A blocked hit does 3.
The first ninja to reach zero health loses. Enter starts a fresh round.
Leaving the window pauses the match.

## Edit in jMonkeyEngine SDK

Choose **File > Open Project**, then select this `NightNinjaDuel` folder.
The project uses the SDK's Ant project support. Run Project starts the game.
You can also open `src/mygame/Main.java` to change the scene and controls.
`DuelState.java` holds the combat rules.

Double-click `run.command` to build and run from source. Keep this source
folder beside `jMonkeyEngine SD.app`; the script uses its Java runtime.
The packaged app includes Java and does not need that folder layout.

To check the combat rules in Terminal:

```sh
./run.command --test
```

## Assets and scope

The ninja model, its texture, its sword and all character animations come
from the installed SDK's `jme3-testdata.jar`, under `Models/Ninja/`.
The field texture comes from `Textures/Terrain/splat/grass.jpg` in that jar.
Trees, hills, moon, stars, lanterns, and fighter markers use engine shapes.
No outside character downloads or paid assets were used.

Asset source: https://github.com/jMonkeyEngine/jmonkeyengine/tree/master/jme3-testdata
SDK source: https://github.com/jMonkeyEngine/sdk/releases/tag/v3.9.0-stable-sdk2
Retain upstream asset notices and review each asset's terms before publishing.
This project includes engine libraries copied from the installed SDK.

This first version has one arena, one computer opponent, and no sound.
It supports a keyboard on macOS Apple Silicon. There is no gamepad or
multiplayer mode.

## Checks performed

Combat checks passed for starting, missed swings, hit range, one hit per
swing, reduced block damage, pause, defeat, rematch, and jump landing.
A live graphics test on the Apple M3 loaded both animated ninjas and
passed combat, pause, victory, and rematch.
The packaged Mac app opened. Its start screen, health bars, and defeat
screen were inspected. A scene review led to fixes for pine orientation,
moon placement, and field brightness.

The interface was checked for visible results, clear task labels,
pause/resume and rematch, consistent keys, safe input states, control help,
repeat play, a clear fight view, build failure messages, and help at start.
Full manual combat balance, SDK Run Project, gamepad input, and other
platforms have not been verified. This version has no save or upload flow.

## Reporting issues

Report bugs and requests in this repository's Issues tab. Include what you
did, what you expected, and what happened. Work is tracked on project 6.
When you report issues in our coding sessions, the agent should create or
update the issue and add it to that board, as described in AGENTS.md.
This is a session workflow; no GitHub auto-add rule has been configured.
