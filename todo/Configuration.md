Handles which resource packs and which data packs to download, and in what order they should be loaded.

Handled by `config/dependencies.mdm.json`

### Example Config

```json
{
	"production": false,
	"assets": [
		{
		    "slug": "fresh-animations",
		    "mirror": [
			    "fresh-animations-mirror"
		    ],
		    "host": "MODRINTH",
			"mode": "FORCED_HIDDEN"
		},
		{
		    "slug": "fresher-animations",
		    "mirror": [
			    "fresher-animations-mirror"
		    ],
		    "host": "CURSEFORGE",
			"mode": "OPTIONAL_DISABLED"
		},
		{
		    "slug": "freshest-animations",
		    "host": "ANY",
			"mode": "SUPPORT"
		}
	],
	"data": [
		{
		    "slug": "duck-origin",
		    "host": "ANY",
			"mode": "FORCED"
		}
	],
	"downloaded": [],
	"warn_enabled": true
}
```

Users likely won't touch the configuration file itself. Instead, it will be handled through the in-game GUI.

The downloaded array populates at runtime with the project slug strings once you've turned "production" on. Any projects in that array will not be downloaded in future launches.

When "production" is true, the [[Title GUI]] is hidden.

When "warn_enabled" is false, the in-game warning screen is not shown for download failures.

### Modes

Data packs: FORCED, OPTIONAL_ENABLED, OPTIONAL_DISABLED, SUPPORT

Resource packs: FORCED, FORCED_HIDDEN, OPTIONAL_ENABLED, OPTIONAL_DISABLED, SUPPORT

Support mode is basically force disabled and hidden. This is for modpacks who want to use edited versions of the dependency, and just need to download it to support the original author.

### Hosts

MODRINTH, CURSEFORGE, ANY

"MODRINTH" downloads exclusively from modrinth.
"CURSEFORGE" downloads exclusively from curseforge.

"ANY" will attempt to download from modrinth. If it fails, it will then attempt to download from curseforge.

If the available hosts fail to download a file, a warning is thrown on the title screen and in the logs. This warning advises users to report the issue to the modpack author.

If the available hosts fail to find a file for your version of Minecraft, they will throw a warning in the logs but not on the title screen. They'll then default to downloading the newest possible file.

All downloaded files are logged in debug.log with the download URL, the project name, and the project version.
