# Gates Awakened

![Header, taken from Starsector](screenshot2.jpg)

Gates Awakened is a mod for the excellent game [Starsector](https://fractalsoftworks.com/).

It adds two short missions that open up instantaneous travel between Gates from the ancient Domain of Man - but you are limited to only a small number of Gates, so adjust accordingly.

Gates Awakened tries to encourage early exploration and ease later-game traveling, without feeling like a cheat. While it is, of course, not canon, it fits smoothly into lore of the game.

Once a Gate is activated, the player may use fuel, based on distance, to travel to any other active Gate.

Massive thanks to _toast_ for his inspirational mod, [Active Gates](https://fractalsoftworks.com/forum/index.php?topic=12791.0), without which this mod would not exist.

![Intro story screenshot](screenshot.jpg)

## Dear other modders: avoiding conflicts

There are two ways to tell Gates Awakened never to touch your systems.

1. Add your system(s) here: `data/gates-awakened/gates_awakened_system_blacklist.csv`.
There is [a template here](https://github.com/davidwhitman/Gates-Awakened/blob/7f31059f438653c753bf2984d2bc38488b336ff2/data/gates-awakened/gates_awakened_system_blacklist.csv).

1. Or, to change blacklisted systems dynamically, add or remove this tag from the system: `GatesAwakened_blacklisted_system`.
The code that drives this logic is around [here](https://github.com/davidwhitman/Gates-Awakened/blob/master/src/main/kotlin/org/wisp/gatesawakened/constants/Tags.kt#L22).

## Compiling this Mod

1. Clone this source code.
1. Ensure you have java installed and accessible via system variable.
1. Open up `build.gradle.kts` in a text editor and change `starsectorCoreDirectory` to point to your `starsector-core` directory.
1. Open a terminal to the root directory of the mod and type
   1. Unix: `./gradlew build jar`
   1. Windows: `gradlew.bat build jar`
1. You should now (eventually) have a jar built at `jars/Gates_Awakened.jar`.
