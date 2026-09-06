GUI representation of the [[Configuration]]

"mode" enums are handled as actual dropdown enum options rather than direct string inputs.

The loading order can be edited with the same arrows that the resource packs GUI uses.

Allows pasting links directly to the mod's website; the mod will dynamically trim the URL to just the project ID, then use that to web fetch the latest version.

Remember to hook this GUI up to configuration mods. On Forge/NeoForge you can do this directly with the engine. With Fabric, you'll need to add Mod Menu as an optional dependency.
