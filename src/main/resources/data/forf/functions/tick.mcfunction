execute as @a[scores={setupForf=1..}] run function forf:setup
execute as @a[scores={resetForf=1..}] run function forf:reset

scoreboard players set @a[scores={setupForf=1..}] setupForf 0
scoreboard players set @a[scores={resetForf=1..}] resetForf 0

function forf:lives