# Friend or Foe for Fabric

![Latest Release](https://img.shields.io/github/v/tag/ILIkeFood971/forf?label=Latest%20Release)
[![License](https://img.shields.io/github/license/ILIkeFood971/forf)](https://github.com/ILikeFood971/ForF/blob/1.20/LICENSE)
[![GitHub issues](https://img.shields.io/github/issues/ILikeFood971/forf)](https://github.com/ILikeFood971/ForF/issues)  
[![Discord](https://img.shields.io/discord/1142919875300430015?logo=discord&label=discord)](https://discord.gg/ypyRwVEaBT)  
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/forf?logo=modrinth&label=Modrinth%20Downloads)](https://modrinth.com/mod/forf)
[![CurseForge Downloads](https://cf.way2muchnoise.eu/full_919008_CurseForge%20Downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/forf)

This fabric mod allows you to recreate almost everything in the Friend or Foe YouTube series that has 4
members: [SB737,](https://youtube.com/playlist?list=PLWJikipm2MTbCmHm6e9KV0bPmW6chk1j9) [ClownPierce,](https://www.youtube.com/@ClownPierce) [Quiff,](https://www.youtube.com/@QuiffYT)
and [Mini](https://www.youtube.com/playlist?list=PLkUQm5HBzevSUv6iW8EMErAmIKPKD4uF7). They have some alterations that
differ from the regular vanilla experience. The Friend or Foe mod, forf for short, aims to add those new features and
gameplay changes for you to enjoy with your friends.

## What is included

- Only required on the server
    - Players do not need to install on their clients
- A config that allows you to customize much of what is added
    - Can be found in your servers config folder. (Default: `config/forf-config.json5`)
- A new player tracker item
    - Expires automatically after configured time
  - Points to the targeted player or the portal they went through in the current dimension
- Lives system that will decrease a life until player runs out
    - Customize amount of lives
        - Configure starting amounts as well as commands to change any player's lives
    - Allow players who run out to spectate in a configurable gamemode after they run out
    - Rewards for the first kill
        - The first player to get a kill earns a mending book
- Restrictions
    - No villager trading
    - No golden apple recipes
    - No elytra in the end ships
    - No totems drop from evokers
    - Explosions are nerfed
        - Every player except the player who detonated will take a configured percentage less damage from the explosion
    - Ender Pearl cooldowns are increased
        - The time taken to recharge an ender pearl is configurable (default is 7 seconds, vanilla is 1 second)
    - You can disable any of the restrictions from the config
- PvP Timer that runs for random amounts of time
    - Enable or disable it easily with commands
- Server Assassin
    - A player that will start with a different number of lives
    - Automatically changes the pvp mode to online when on the server
    - Cannot receive lives from other players

This mod tries to follow everything written in the official Friend or Foe
rules [written here](https://pastebin.com/X9j3ZsNb).

## How to use

<details><summary>Super simple installation step by step</summary>

1. Set up a Minecraft server
    1. You can use either a server host or host it yourself
    2. If you are hosting it yourself, you can
       use [this](https://help.minecraft.net/hc/en-us/articles/360058525452-How-to-Setup-a-Minecraft-Java-Edition-Server)
       tutorial
    3. Download and install the latest Fabric Loader for servers from [here](https://fabricmc.net/use/server/)
        1. Most server hosts can do this automatically for you, if you need help installing on a server host look
           through their documentation or look up how to install Fabric on your server host
        2. If you're self-hosting it then replace the server jar you downloaded in step 1 with this new jar file
2. Install Friend or Foe and it's dependencies
    1. Download the latest Friend or Foe version for your Minecraft version
       from [Modrinth](https://modrinth.com/mod/forf)
       or [CurseForge](https://www.curseforge.com/minecraft/mc-mods/forf/files/all)
    2. Download the latest Fabric API version for your Minecraft version
       from [Modrinth](https://modrinth.com/mod/fabric-api)
       or [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fabric-api/files/all)
    3. Put the 2 jar files you downloaded in step 3 and 4 into your server's `mods` folder
        1. If you are using a server host, you can usually do this through their file manager
        2. If you are self-hosting, you can find the `mods` folder in the same folder as the server jar
    4. Start up your server
        1. If you are using a server host, you can usually do this through their control panel
        2. If you are self-hosting, you can start it up by running the server jar file
3. Edit your Friend or Foe config file
    1. Stop your server
    2. Open the config file in your favorite text editor or in your server host's file manager
        1. You can find the config file in the same folder as the server jar
    3. Carefully look through all the options and edit them however you want
    4. Save the file
4. Start Friend or Foe
    1. Start up your server
    2. Run the `/forf join` command on every player you want to play with
        1. You can also append the target player(s) at the end if desired (`/forf join [players]`) (This works even with
           player who are not online)
    3. Run the `/forf start` command (op level 3 required) to set everything up
        1. Either have an op run it or run it on the server console
    4. Enjoy!

</details>

The mod is fairly simple to use. After installing, you must start it up at least once to generate the config file. You
can then change that according to how you like it.

Once everything in the config has been set to what you like, save the file. After/while doing this, have every player
you would like to play with log on and run the `/forf join` command. An op can also append the target player(s) at the
end if desired (`/forf join [players]`). If you would like to join a player after Friend or Foe has already started then
run `/forf join [players] late <lives>` as an op and the players do not need to be online. Once everyone has joined, you
can then run the `/forf start` command (op level 3 required) to set everything up. If at anytime you need to stop Friend
or Foe, run the `/forf stop` command.

### Timer

You can set the randomness range in the config. The timer can also be disabled if so desired. It automatically starts
after `/forf start` is run. To manually change it, you can use `/forf pvp (on|off) [minutes]`. Without the minutes
argument the timer will pick a random amount.

### Lives

Are stored per-player. You can set how many one will start with in the config. Every death will remove 1 life until you
reach 0. A player on 0 lives will not be able to join unless spectators is enabled in the config.

#### Lives Commands

Format is as follows:  
`/forf lives set <players> <amount>` - You must be an op level 3 or higher to run this command. This will set the
players' lives to exactly the amount you set. (Useful for manual extra lives quest.) It will also work on offline
players.  
`/forf lives give <player> <amount>` - Any player can run this command to give their lives to another player. This is
useful for deals and such (Like how SB737 gives a life to ClownPierce). This command requires both players to be online.

### Player Tracker

The player tracker can be crafted using 4 diamonds, 2 emeralds, 2 eyes of ender, and 1 compass. It
uses [this recipe.](https://gyazo.com/444fa8fc199afff4d1af3c8dd641fab4) By default, it will expire after 1 hour. It will
track any player of your choice by right-clicking to open a selection GUI. After selecting a player it will update to
the tracked players location or the position of the portal they last used in your current dimension (Technically it
points to their last known position in that dimension so if they get teleported or die it will point to the position
where they were when that happened). If the tracked player goes offline, the compass will spin randomly. The player
tracker works fully server-side by using the Polymer library.

### Server Assassin

You can set a player to be the server assassin by running `/forf assassin set <player>`. The assassin will change the
pvp mode to online whenever they join. They will also start out with a different number of lives (default is 1, you can
change this in the config). Other players cannot give lives to the assassin. If you would like to clear the server
assassin, run `/forf assassin clear`.

## Missing features

- Extra lives quests - Not currently planned. You can do this manually with the `/forf lives set` command
- Spawn Structure - I don't have the actual structure as well as it would take a lot of work to place it in the world.
  Just use [litematica](https://www.curseforge.com/minecraft/mc-mods/litematica) or creative mode if you need this
- No Reloading Chunks - Not possible without a client mod
- Forge and Plugin ports - If enough people request a port, then I **may** port this mod for other platforms. Not
  currently planned
- A system to prevent players from logging on without all the forf players online (Stop people from grinding like it is
  in the real series) - May be added in the future
- Server Assassin - In development

## Getting support and contributing

Feel free to [file an issue](https://github.com/ILikeFood971/ForF/issues)
or [pr](https://github.com/ILikeFood971/ForF/pulls). I would be happy to fix any bugs you may encounter.

If you want you could make
a [translation pr.](https://github.com/ILikeFood971/ForF/tree/main/src/main/resources/assets/forf/lang)

If you need any more help please join the [Discord Server](https://discord.gg/ypyRwVEaBT).

## Credits

**Requires [Fabric API](https://modrinth.com/mod/fabric-api)**  
Forf utilizes the [sgui library](https://github.com/Patbox/sgui) for the player tracker gui  
[Server Translations API](https://github.com/NucleoidMC/Server-Translations) is included  
[Polymer](https://github.com/Patbox/polymer) is included for the tracker

[Jankson JSON parser](https://github.com/falkreon/Jankson) is used for config