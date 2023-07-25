function forf:load
execute as @a run team join Players

scoreboard players set TIMER pvp 0
team modify Players friendlyFire false
scoreboard players operation TIMER timer = RNG RNG_Variable

difficulty hard


# Random generation stuff
scoreboard objectives add RNG_Constant dummy
scoreboard objectives add RNG_Variable dummy

scoreboard players set C_600 RNG_Constant 600
scoreboard players set C_1000 RNG_Constant 1000
scoreboard players set C_1200 RNG_Constant 1200
scoreboard players set C_314159 RNG_Constant 314159
scoreboard players set C_2718281 RNG_Constant 2718281

scoreboard players set RNGseed RNG_Variable 0

scoreboard objectives setdisplay list lives
