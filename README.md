# Astryx Client

Astryx is a Minecraft: Java Edition client and Windows launcher, currently in development. This repository contains the published AstryxCore JAR and the release manifest used by the launcher.

## The client

Astryx adds custom menus, a movable HUD and configurable modules for things such as FPS, CPS, coordinates, keystrokes and the scoreboard. HUD elements can be positioned and resized, with appearance settings and profiles for different setups.

The current Astryx release targets **Minecraft 1.8.9 with Forge 11.15.1.2318**. We hope to bring Astryx to more Minecraft versions in the future; no dates are set yet.

## The launcher

The launcher installs Astryx and provides separate Forge and Fabric profiles for other supported Minecraft releases. The version browser groups Astryx, Forge and Fabric, with snapshots hidden. Forge and Fabric profiles have their own mods and configuration folders; they do not include Astryx's client features unless an Astryx build exists for that version.

The mod manager includes a Modrinth browser filtered by the selected Minecraft version and loader. It supports downloading mods and required project dependencies, importing JARs, enabling or disabling mods, and removing them to a recovery folder.

Astryx downloads are checked against the SHA-256 checksum in `manifest.json`. OptiFine for Astryx 1.8.9 is fetched from its official site during installation and is not hosted in this repository.

## Development status

Installation, mod management and the launcher interface are still being refined. Microsoft sign-in is implemented, but Minecraft Services is currently rejecting Astryx's app registration; API approval is required before that sign-in flow can be used. Development builds also include local testing profiles.

Launcher data and logs are stored in `%AppData%\AstryxClient`. When reporting a problem, include the selected Minecraft version, loader and relevant logs. Remove tokens and personal information before sharing them.

Astryx is an independent project and is not affiliated with Mojang or Microsoft.
