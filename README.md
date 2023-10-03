# Friend or Foe for Fabric

![Latest Release](https://img.shields.io/github/v/tag/ILIkeFood971/forf?label=Latest%20Release)
[![License](https://img.shields.io/github/license/ILIkeFood971/forf)](https://github.com/ILikeFood971/ForF/blob/1.20/LICENSE) 
[![GitHub issues](https://img.shields.io/github/issues/ILikeFood971/forf)](https://github.com/ILikeFood971/ForF/issues)  
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/forf?logo=modrinth&label=Modrinth%20Downloads)](https://modrinth.com/mod/forf) 
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/919008?logo=curseforge&label=CurseForge%20Downloads&link=https%3A%2F%2Fcurseforge.com%2Fminecraft%2Fmc-mods%2Fforf)](https://www.curseforge.com/minecraft/mc-mods/forf)
[![GitHub releases](https://img.shields.io/github/downloads/ILikeFood971/forf/total?logo=github&label=GitHub%20Downloads)](https://github.com/ILikeFood971/Deodorant-Mod/releases)

![1.20.2](https://img.shields.io/badge/MC-1.20.2-green) ![1.20.1](https://img.shields.io/badge/MC-1.20.1-green) ![1.19.4](https://img.shields.io/badge/MC-1.19.4-green)

This fabric mod allows you to recreate almost everything in the Friend or Foe YouTube series that has 4 members: [SB737,](https://youtube.com/playlist?list=PLWJikipm2MTbCmHm6e9KV0bPmW6chk1j9) [ClownPierce,](https://www.youtube.com/@ClownPierce) [Quiff,](https://www.youtube.com/@QuiffYT) and [Mini](https://www.youtube.com/playlist?list=PLkUQm5HBzevSUv6iW8EMErAmIKPKD4uF7). They have some alterations that differ from the regular vanilla experience. The Friend or Foe mod, forf for short, aims to add those new features and gameplay changes for you to enjoy with your friends.

## What is included

- A config that allows you to customize much of what is added
- A new player tracker item \(unfinished\)
- Lives system that will decrease a life until player runs out
  - Customize amount of lives
  - Allow players who run out to spectate in a configurable gamemode after they run out
- Restrictions
    - No villager trading
    - No golden apple recipes
    - No elytra in the end ships
    - No totems drop from evokers
- PvP Timer that runs for random amounts of time
  - Enable or disable it easily with commands

This mod tries to follow everything written in the official Friend or Foe rules [written here](https://pastebin.com/X9j3ZsNb).

## How to use

The mod is fairly simple to use. First you must start it up at least once to generate the config file. You can then change that according to how you like it.

Once everything has been set to what you like, save the file. After/while doing this, have every player you would like to play with log on and run the `/forf join` command. An op can also set the target player at the end if desired. Once everyone has joined, you can then run the `/forf start` command (op level 4 required) to set everything up. If at anytime you need to stop forf, run the `/forf stop` command. 

### Timer

You can set the randomness range in the config. The timer can also be disabled if so desired. It automatically starts after `/forf start` is run. To manually change it, you can use `forf pvp on minutes` or `forf pvp off minutes`. Without the minutes argument the timer will pick a random amount.

### Lives

Are stored per-player. You can set how many one will start with in the config. Every death will remove 1 life until you reach 0. A player on 0 lives will not be able to join unless spectators is enabled in the config. In the future, lives will also be able to be manipulated with commands (give, set, remove).

## Missing features

- Tracking Compass - Expected in v1.0.0 update
- Extra life quests - Not currently planned
- Explosion debuffs - I have no idea how much this is and how it's implemented in the official forf server as it isn't on the rules site. If anyone has any numbers/information, file an issue and I could probably implement this fairly easily

## Getting support and contributing

Feel free to [file an issue](https://github.com/ILikeFood971/ForF/issues) or [pr](https://github.com/ILikeFood971/ForF/pulls). I would be happy to fix any bugs you may encounter. 

If you want you could make a [translation pr.](https://github.com/ILikeFood971/ForF/tree/main/src/main/resources/assets/forf/lang)

If you need any more help please join the [Discord Server](https://discord.gg/ypyRwVEaBT).

## Credits

This GitHub repo is based on [Fallen Breath's mod template](https://github.com/Fallen-Breath/fabric-mod-template)

**Requires Fabric API**  
Forf utilizes the [sgui library](https://github.com/Patbox/sgui) for the player tracker gui  
Server Translations API is included  
Polymer is included for tracker