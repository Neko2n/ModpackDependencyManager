
Button below "Exit Game" that says "Dependencies" with a little wrench icon in front of the text. The button is orange, unlike the other grey buttons.

The GUI just displays the possible edits a user can make to the configuration.

"mode" enums are handled as actual dropdown enum options rather than direct string inputs.

The loading order can be edited with the same arrows that the resource packs GUI uses.

Allows pasting links directly to the mod's website; the mod will dynamically trim the URL to just the project ID, then use that to web fetch the latest version.

Remember to hook this GUI up to configuration mods. On Forge/NeoForge you can do this directly with the engine. With Fabric, you'll need to add Mod Menu as an optional dependency.
