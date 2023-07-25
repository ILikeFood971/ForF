execute as @a run execute store result score @s lives run data get entity @s "forf.data".lives

execute as @a[nbt={forf.data: {lives: 0}}] run gamemode spectator