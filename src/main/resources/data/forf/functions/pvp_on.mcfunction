scoreboard players operation TIMER minutes = TIMER timer
scoreboard players operation TIMER seconds = TIMER timer

scoreboard players operation TIMER minutes /= TIMER 60
scoreboard players operation TIMER seconds %= TIMER 60

execute if score TIMER minutes > TIMER 0 run title @a actionbar [{"text":"PvP Enabled: ","color":"#5E514B"},{"score":{"name":"TIMER","objective":"minutes"},"color":"dark_red","bold":true},{"text":"m ","color":"dark_red","bold":true},{"score":{"name":"TIMER","objective":"seconds"},"color":"dark_red","bold":true},{"text":"s","color":"dark_red","bold":true},{"text":" left","color":"#5E514B"}]
execute if score TIMER minutes = TIMER 0 run title @a actionbar [{"text":"PvP Enabled: ","color":"#5E514B"},{"score":{"name":"TIMER","objective":"seconds"},"color":"dark_red","bold":true},{"text":"s","color":"dark_red","bold":true},{"text":" left","color":"#5E514B"}]

