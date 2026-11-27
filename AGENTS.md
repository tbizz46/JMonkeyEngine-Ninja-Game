# Working on Night Ninja Duel

Use plain language. Ask about unclear requests before dependent work.

## Repository and issue tracking

- Repository: https://github.com/tbizz46/JMonkeyEngine-Ninja-Game
- Project board: https://github.com/users/tbizz46/projects/6/views/1
- When the user reports a bug or asks for a tracked improvement, check for
  an existing issue first. Create or update a repo issue, then add it to
  project 6. Verify the issue appears on that board.
- Put new work in Backlog. Move it to In progress when work starts, In
  review when a pull request is ready, and Done after completion is verified.
- Use the user's description. Include clear steps, expected and actual
  results for bugs, and completion checks for improvements. Label anything
  not yet reproduced. Do not invent issues or claim a fix passed without proof.
- Open regular pull requests ready for review unless the user asks for drafts.
- Keep project credentials and tokens out of this repo.

## Game and build

Source is in src/mygame. Assets are in assets. Run ./run.command --test
for combat checks and ./run.command --build to compile. The launcher uses
Java from the jMonkeyEngine app beside this folder.

Keep build/, dist/, .runtime/, the SDK app, and installers out of Git.
Downloaded graphics add-ons in graphics-packages are staged for future
use. They are not enabled in the game. Retain their licenses and hashes.

## Usability

Before changing a workflow, identify the player, the trigger, and how
success is shown. Reuse the game's terms and keys. Check these principles:

1. Show what is happening.
2. Use the user's vocabulary and task order.
3. Give users a way back.
4. Keep familiar actions consistent.
5. Prevent costly mistakes.
6. Put choices and context where they are needed.
7. Make repeat work faster.
8. Keep attention on the current task.
9. Make failures actionable.
10. Put help at the point of difficulty.

Walk through the main path, a likely mistake, and recovery. Check pause,
cancel, and retry where relevant. Report only what was actually checked.
For each review finding, give its location, principle, evidence and impact,
and fix with verification steps. End with the inspected scope and gaps.
