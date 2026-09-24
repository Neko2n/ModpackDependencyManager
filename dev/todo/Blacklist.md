Allow modpack developers to blacklist certain mods, resource packs, and data packs.

The blacklist has a "client handling" setting: BLOCK, WARN, or NONE.
- BLOCK prevents the game from loading (or even detecting) said packs if they're installed. If it's a mod, the game will outright not start up, throwing a warning and telling the user to restart their game.
- WARN simply throws a "modified instance" warning when the game starts if illegal items are detected. This warning is thrown every time the game starts.
- NONE does nothing.
The blacklist also has a "server handling" setting: BLOCK or WARN. Same thing, but for joining servers rather than your game starting up.

There's a setting which allows you to change the blacklist into a whitelist. Anything loaded by the mod or listed in the whitelist will be allowed, while anything else will be treated as illegal.