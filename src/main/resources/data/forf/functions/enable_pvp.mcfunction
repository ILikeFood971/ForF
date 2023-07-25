title @a title [{"text":"⚠","color":"yellow"},{"text":" PvP is ON ","color":"red"},{"text":"⚠","color":"yellow"}]
tellraw @a [{"text":"PvP is now ","color":"yellow"},{"text":"ON","color":"yellow","bold":true}]

function forf:random
scoreboard players set TIMER pvp 1
scoreboard players set TIMER timer 1200

team modify Players friendlyFire true