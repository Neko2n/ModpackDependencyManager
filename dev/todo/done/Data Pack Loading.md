
You'll need to implement your own version of global data packs.

Data packs are downloaded to .minecraft/datapacks

Depending on how they're configured, datapacks will load themselves into worlds from this folder.

Only forced datapacks will load themselves into existing worlds.
Optionals will only be loaded into newly created worlds, if they're enabled by default.

This probably requires the mod to be server-sided, not just client-sided.

https://github.com/YUNG-GANG/Paxi/blob/1.21.1/Common/src/main/java/com/yungnickyoung/minecraft/paxi/PaxiRepositorySource.java

Replace mixins with child classes of RepositorySource, Pack, and PackSource.

Use one mixin into PackRepository which pulls packs from DependencyRepositorySource.

All packs, resources and data, are now downloaded to `.minecraft/dependencies/resourcepacks` and `.minecraft/dependencies/datapacks` respectively.
DependencyRepositorySource fetches from these folders.