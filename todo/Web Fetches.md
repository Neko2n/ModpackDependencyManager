
Create web fetch APIs for both Modrinth and CurseForge.

These APIs should only be able to download resource packs and data packs.

They will use provided slugs to search up the file, and then download the newest version of the pack for the game's version.

If no files are found matching your game's version, the newest file is downloaded and a warning is logged.

If no files are found, a warning displays on the title screen and in the logs, prompting users to report the bug to the modpack dev. This does not interrupt loading, but will display after every game load.

This warning can be disabled in the [[Configuration]]
