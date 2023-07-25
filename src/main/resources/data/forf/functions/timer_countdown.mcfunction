schedule function forf:timer_countdown 1s replace

execute if score TIMER pvp matches 1 run function forf:pvp_on

execute if score TIMER timer matches 0 run execute if score TIMER pvp matches 0 run function forf:enable_pvp
execute if score TIMER timer matches 0 run execute if score TIMER pvp matches 1 run function forf:disable_pvp

scoreboard players remove TIMER timer 1

