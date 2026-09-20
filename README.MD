## ASJCore is an API/library mod for Minecraft 1.7.10
Except helper and utility methods, it also includes Kotlin JDK 8 stdlib, GloomyFolken's HookLib, VamigA's WorldEngine, as well as a bunch of fixes and patches improving vanilla Minecraft.  
You can get builds on [CurseForge](https://www.curseforge.com/minecraft/mc-mods/asjcore) or [Modrinth](https://modrinth.com/mod/asjcore)

### How to compile for yourself:
1. Clone or download repository
2. Run those commands in terminal:
   1. `gradlew setupDecompWorkspace`
   2. `git restore build/dirtyArtifacts/forgeSrc-1.7.10-10.13.4.1614-1.7.10.jar`
      - if you don't have git on your system and just downloaded the repo, replace the file manually to one you downloaded 
   3. `gradlew build`
3. Mod .jar files would be in `build/libs`

For the second and other times you want to compile the mod, only third command is required.