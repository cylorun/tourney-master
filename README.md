# Tourney Master
Helps manage OBS and other stuff for live streaming MCSR tourneys.

## Setup
- Download the [latest release](https://github.com/cylorun/tourney-master/releases/latest)
- Run the downloaded .jar file
- Load the TMC.lua file which is found in the [config folder](#config) as an OBS script(OBS -> tools > scripts -> "+" icon)
- Press "Player Sources" inside of Tourney master, this will generate *n* ammounts of player groups, 6 is fine for most tourneys.
- You can then align these browser and text sources as you want, but make sure to read [this](#obs-layout) first, 

## OBS Layout
each player browser source must be paired with its corresponding text source, you can check this by making sure their number is the same (p1-ttv and p1-label would be for the same player for example)
try making sources go from top to bottom, so that they correspond correctly to the grid inside of tourney master which is used to move players around

## Config
You can go to the config folder by pressing "Open Config Folder" in Tourney master

## Features
- Writes [Paceman](https://paceman.gg) event leaderboards to a file
- Displays pace of players in a [Paceman](https://paceman.gg) event
- Swap players around on OBS in any layout
