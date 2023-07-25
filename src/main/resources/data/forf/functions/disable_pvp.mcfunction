scoreboard players set TIMER pvp 0

scoreboard players operation TIMER timer = RNG RNG_Variable

title @a actionbar {"text":"PvP disabled","color":"dark_green"}
tellraw @a [{"text":"PvP is now ","color":"yellow"},{"text":"OFF","color":"red","bold":true}]

team modify Players friendlyFire false