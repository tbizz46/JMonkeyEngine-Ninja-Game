# 3D arena and ninja kicks: code change list

Work: [issue #4](https://github.com/tbizz46/JMonkeyEngine-Ninja-Game/issues/4).
This list covers the changes after the MonkeyWrench work in PR #3.

The game now lets the player move across the ground in X and Z, jump in
Y, and use a sword or kick against a computer ninja. An angled camera
shows the arena. Both fighters keep the existing bundled ninja model.

## src/mygame/DuelState.java

1. Added `ARENA_X`, `ARENA_Z`, and `BODY_GAP`: a ground area bounded by
   X = -9 to 9 and Z = -5 to 5, with 1.25 units of space between bodies.
2. Added the `Strike` enum. Each strike defines duration, hit time, range,
   cooldown, full damage, block damage, and animation name.
3. Added the kick strike: duration 0.58 seconds, hit at 0.22 seconds,
   reach 1.9 units, cooldown 0.72 seconds, damage 10, blocked damage 2,
   and the existing `Kick` animation.
4. Moved the sword values into `Strike.SWORD`: duration 0.72 seconds,
   hit at 0.28 seconds, reach 2.5 units, cooldown 0.93 seconds,
   damage 14, blocked damage 3, and `Attack1` animation.
5. Added `Fighter.z`, `facingX`, `facingZ`, `moving`, and `strike`. Facing
   is a ground direction rather than a left/right sign.
6. Updated `reset()` to restore all position, strike, and message state.
7. Added `distance()` and `groundDistance()` for X/Z distances.
8. Added `kick()` and shared `startStrike()`. Kicks require the ground.
   Both strikes reject input before start, during pause, after defeat,
   while blocking or stunned, and during a current strike or cooldown.
9. Changed `move()` to take X and Z input. Diagonal input is normalized
   so it has the same speed as straight input. Movement sets `moving`.
10. Added `clamp()` to keep movement and knockback inside the arena.
11. Added `face()` so each ninja turns toward the other in two ground axes.
    Strike direction is fixed at launch so dodging behind it can work.
12. Added `separateBodies()` to resolve overlaps in X/Z, including at walls.
13. Updated `step()` to use arena spacing and full ground facing, while
    retaining jump physics, timers, pause rules, and round state.
14. Updated `hit()` to use strike-specific timing and damage, full ground
    distance, height difference, and a forward-facing check. Knockback
    follows the strike direction in both X and Z. Messages name kick,
    sword, block, or miss. A kick can end the round.

## src/mygame/Main.java

15. Added forward/back input flags and mappings for W/S and Up/Down.
    A/D and Left/Right still move sideways. Added L for kicks. Registered
    every new action with the keyboard listener and its switch handler.
16. Expanded `clearInput()` to clear depth movement on pause/focus loss.
17. Updated `Avatar.sync()` to place each model at X/Y/Z and turn it with
    the full facing direction. It selects `Kick` or `Attack1` from the
    strike and scales animation speed to its duration. Walking follows
    actual movement. Pause keeps the current animation pose frozen.
18. Added `cameraTarget` and `updateArenaCamera()`: a raised angled view
    follows the fight's center smoothly and backs off with fighter spacing.
19. Expanded the narrow clearing into a full arena. Moved tall grass and
    lantern posts out of its playable area so they do not hide the fighters.
20. Updated the title strip, control strip, start help, and fight hint for
    3D movement and kicks. Expanded the start panel for the extra help line.
21. Updated `simpleUpdate()` to move the player in X/Z and have the computer
    pursue along both ground axes. The computer uses kicks at close range
    or its sword, with the existing delay between attacks.
22. Added `aiSawAttack` and `aiGuard`. The computer makes one block choice
    per incoming strike (35 percent) and reacts after 0.12 seconds, rather
    than choosing a new block response every frame.
23. Updated `resetRound()` to reset camera tracking and AI response flags.
24. Expanded `smokeTest()` to check player and computer kick damage and
    animation selection, depth movement, pause/resume, kick victory,
    restored health on rematch, and normal app shutdown.

## tests/mygame/DuelStateTest.java

25. Updated movement calls for the X/Z API and added reusable `near()` and
    `close()` helpers for checking movement and close-range combat.
26. Added checks for depth movement and distance, normalized diagonal speed,
    arena edges, body separation at an arena corner, facing and dodging
    behind a strike, and depth-direction knockback.
27. Added checks for both ninjas kicking, kick range versus sword range,
    full and blocked kick damage, cooldown, attack replacement prevention,
    ground-only kicks, and height-based dodging.
28. Retained checks for title input, sword hits and misses, one hit per
    strike, block damage, pause, victory, rematch, and jump landing. Updated
    the pause/victory checks to exercise kicks and depth movement too.

## Documentation and local build

29. Updated `README.md` with the 3D description, movement and kick keys,
    damage values, camera behavior, and link to this full change list.
30. Added this change list with the scope, checks, and remaining gaps.
31. Rebuilt the local Mac app with `package-mac.command`. The app remains
    build output under `dist/`; the code and tests are tracked in Git.

## Verification

Combat and model-import tests passed on the Apple M3. The live fight test
passed 3D movement, both kick animations and damage, pause/resume,
kick victory, and rematch. The rebuilt Mac app passed the same live checks.
`git diff --check` passed.

The updated Mac app opened, but the window/screenshot tool timed out on
two attempts. Manual keyboard play and visual framing remain unverified.
The input mappings and help text were reviewed in source. No visual
layout finding is claimed without a working screen capture.

## Player workflow review

The task begins at Enter on the start screen. Success is moving around
the opponent, landing a kick with visible health loss, and reaching a
win/lose result with Enter for rematch. Existing sword, block, jump,
pause, and quit keys are retained. Controls are shown at start and in the
bottom strip. Strike range, cooldowns, arena limits, and body spacing
prevent invalid actions. Miss and hit text give visible feedback.

The review covers all ten project usability principles: action feedback,
player terms, pause/rematch recovery, consistent keys, safe action states,
visible context, repeat play, fight focus, actionable misses, and control
help. Full balance tuning, long play sessions, other platforms, gamepads,
and alternate cameras are outside this check.
