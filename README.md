# Miniverse

Miniverse can create a miniature version of the overworld and allow
players to travel between the two.

## Switching

There's a command, `/switch`. Players entering the command will
instantly switch between the original and its corresponding miniverse
world, assuming they are in such a world.

While in a miniature world, it is also possible to switch back quickly
by sneaking.

## Generating

The miniverse should be created after the overworld has been
generated, or at least exists with a reasonable world border.

The miniverse world should have empty world generation
(e.g. VoidGenerator) and all gamerules set up to be as non-invasive as
possible. Then it must be entered in the `config.yml`.

The admin commands must be executed by a player in the miniverse world:
- `/miniverse init` to initialize the world if required, like the scaled WorldBorder.
- `/miniverse fill` to fill the entire world.

Other admin commands are available.