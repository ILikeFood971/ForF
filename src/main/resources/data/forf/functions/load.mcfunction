function forf:timer_countdown

scoreboard objectives add timer dummy
scoreboard objectives add pvp dummy


scoreboard objectives add minutes dummy
scoreboard objectives add seconds dummy

scoreboard objectives add 60 dummy
scoreboard players set TIMER 60 60

scoreboard objectives add 0 dummy
scoreboard players set TIMER 0 0

team add Players

scoreboard objectives add setupForf trigger
scoreboard players enable @a setupForf

scoreboard objectives add resetForf trigger
scoreboard players enable @a resetForf

scoreboard objectives add lives dummy
