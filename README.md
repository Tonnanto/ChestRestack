# ChestRestack
A plugin for Spigot servers that allows players to move items into their chests by shift-clicking them.

## General
Author: Tonnanto  
Current Version: 1.0

Download the plugin and find the Project Page on [SpigotMC](https://www.spigotmc.org/resources/chestrestack/) and [Bukkit](https://dev.bukkit.org/projects/chestrestack).

## How it works

### Shift-click a chest.
Its dead simple. 
Shift-left click on a chest, and all items from your inventory that are already present in the chest will be moved into the chest. 
You do not have to open the chest, nor your inventory.
This makes it straightforward and efficient to unload your inventory after a mining session or any other kind of expedition. 
Just click on every chest in your storage room, and your inventory will be clutter-free in no time.

### Individual Player Preferences
ChestRestack also gives every player the option to adjust their individual preferences to tailor the functionality to suit their needs.
To view and configure preferences, use the `/cr prefs` command.
These preferences include the following:
- Should the chest be sorted after restacking?
- Should items from the hotbar be moved?
- Should tools and weapons be moved?
- Should armor be moved?
- Should arrows and rockets be moved?
- Should food items be moved?
- Should potions be moved?
- Should shift-right or shift-left click trigger the functionality?
- Or should the feature be disabled entirely?

## Commands
All commands for this plugin start with `/chestrestack` or its shorter aliases `/restack` or `/cr`
- `/cr help` - Shows a Help Menu that explains the plugin and all commands
- `/cr <disable | enable>` - Disables or enables the plugin for an individual player
- `/cr prefs` - Show an interactive chat-menu of the player's preferences
- `/cr prefs sorting` - Toggle whether chests should be sorted after restacking
- `/cr prefs clickmode <shift-left | shift-right>` - Choose a click-mode for restacking
- `/cr prefs reset` - Restore the default preferences
- `/cr prefs <tools | armor | arrows | food | potions>` - Enable or disable restacking of certain items

Admin Command:
- `/cr reload` - reloads the plugin and its configuration.

## License
Copyright (C) 2024, Anton Stamme

ChestRestack is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

ChestRestack is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with ChestRestack. If not, see <https://www.gnu.org/licenses/>.