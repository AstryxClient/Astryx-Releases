# Astryx Client

Astryx is an independent Minecraft: Java Edition client and Windows launcher. It combines a customisable HUD and client modules with version installation and mod management.

The project is still in development. This repository holds the published AstryxCore JAR, the manifest used to deliver client updates and the current version of the Astryx Launcher within the releases.

## Versions

| Profile | Availability |
| --- | --- |
| Astryx Client | The current available Astryx release is for Minecraft **1.8.9**, using Forge **11.15.1.2318** |
| Forge | Other stable Minecraft releases where Forge publishes support (You can use the mod manager to customise)|
| Fabric | Other stable Minecraft releases where Fabric publishes support (You can also use the mod manager here)|

The launcher groups versions into **Astryx**, **Forge** and **Fabric**, with a searchable release picker.

A regular Forge or Fabric profile does not automatically include Astryx's modules. We hope to release Astryx for more Minecraft versions in the future, but no dates or additional versions are confirmed as of yet.

## Client features

Astryx includes custom menus, a HUD editor and module profiles. HUD elements can be moved and resized, with appearance options such as text colour, background transparency and font selection. Module settings vary by feature.

Examples of the modules included:

| Module | What it does |
| --- | --- |
| FPS, CPS and Keystrokes | Show frame rate, click counts and keyboard/mouse input on the HUD. |
| Coordinates, Clock and Compass | Display position, time and heading. |
| Nick Detector | Attempts to identify disguised players on Hypixel and other servers and mark their names. Detection depends on the data available and should not be treated as definitive but more often than not the users flagged as nicked will be nicked. |
| Urchin Tools | Looks up player tags using the user's Urchin API key (You will have to input this inside the Urchin Tools module settings, you can get your urchin api key by running /dashboard if you have the Urchin Bot linked to your discord). /stats and /duels commands use the separate stats service so these can be used without an Urchin API Key. /stats shows bedwars stats and /duels shows duel stats (These commands only work on Hypixel.)|
| TAB Editor | Adds display options to the Minecraft player list, including stats, tags and NPC filtering where supported. |
| Notifier | Provides configurable alerts for player stats and displays likely parties joining, with local or party-chat notification options. If party chat setting is on it should fall back to showing only the player if they aren't in a party but this is not guaranteed. |
| Scoreboard | Customises the scoreboard's appearance and display options. |
| Armour Status and Effects | Display equipped armour durability and active potion effects. |
| TNT Timer | Displays a countdown above primed TNT. |
| Waypoints | Adds named locations with configurable markers and labels. |
| Zoom and Crosshair | Provide zoom controls and custom crosshair presets. |
| Toggle Sprint / Sneak | Toggle movement states through configurable binds, with optional HUD status. |
| Chat | Adds chat appearance controls and per-message copy/delete actions. |
| Item Physics and Block Outline | Customise dropped-item rendering and the selected block's outline. |

Features are still being refined, and their behaviour can depend on the Minecraft version, server and external services. Server-specific modules are not a claim of approval by that server; check its rules before using them although you should be alright to use them but we cannot guarantee that especially if the rules change. (We so far only know the modules are (currently) allowed on Hypixel, use of modules on other servers is at your own risk.)

## Launcher and mod manager

The Astryx launcher provides:

- Installation and repair of Minecraft Releases for Forge, Fabric and Astryx's own releases.
- A Modrinth browser filtered by the selected profile's Minecraft version and loader.
- Downloads of compatible mods and required project dependencies.
- Local JAR imports, mod enable/disable controls and recoverable removal of mods.
- Memory allocation and Minecraft log settings.

The installed-mod list belongs to the selected profile. Removed mods are moved into that profile's `removed-mods` folder so they can be recovered.

AstryxCore downloads are verified against the SHA-256 checksum in `manifest.json`. Modrinth downloads are checked against the file hashes supplied by Modrinth. These checks detect changed or corrupted downloads; they do not guarantee that third-party mods are compatible with one another or allowed on specific servers, use at your own risk.

Astryx 1.8.9 installation fetches OptiFine from its official site. OptiFine is not hosted in this repository.

## Installation

Windows builds are packaged as `AstryxSetup.exe`. The installer places the launcher in the current user's Programs folder, creates a Start menu shortcut and offers a desktop shortcut. It includes .NET and checks for WebView2, installing that runtime when needed. Downloading WebView2 and Minecraft files requires an internet connection.

Published downloads belong in this repository's [Releases](https://github.com/AstryxClient/Astryx-Releases/releases) section. The source-code ZIP generated by GitHub is not the Windows installer.

## Current status

Minecraft Services currently rejects Astryx's Microsoft application registration. Microsoft sign-in requires Minecraft API approval before it can be used successfully. Development builds also contain local testing profiles; these are not authenticated Microsoft accounts.

Forge/Fabric installation, module behaviour and the launcher interface are under active development. Expect fixes and changes between builds.

## Files and support

Launcher data, game installations and logs are stored under:

```text
%AppData%\Roaming\AstryxClient
```

Uninstalling the launcher preserves that data, including instances, mods and saves.

When reporting a problem, include the selected profile, Minecraft version, loader, steps to reproduce it and relevant logs. Remove authentication tokens, API keys and personal information before sharing files.

Astryx is not affiliated with Mojang, Microsoft, Hypixel, Modrinth or Urchin.
