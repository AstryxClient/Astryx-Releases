# Astryx Client

Astryx is a custom Minecraft Java client built around **Minecraft 1.8.9 and Forge**. The project has its own launcher and an in-game interface, rather than being a pre-packed collection of unrelated mods.

The aim is to give 1.8.9 a consistent look and feel, with Astryx handling the menus, presentation and client-side features in one place. The interface uses a dark purple and blue colour scheme, custom typography, subtle effects and compact controls.

## What’s in the client

The current work is focused on the client itself and its core interface:

- Custom main menu and multiplayer screens
- Custom world creation screen with separate settings for gameplay, generation and advanced options
- Astryx-styled buttons, panels, typography and visual effects
- Minecraft 1.8.9 compatibility through the Forge 1.8.9 environment

Astryx is still being developed, so the feature set and interface may change between releases. This README only describes features that are part of the current project; planned features aren't presented as finished functionality.

## Minecraft version

| Component | Version |
| --- | --- |
| Minecraft | 1.8.9 |
| Mod loader | Forge |
| Forge profile | `1.8.9-forge1.8.9-11.15.1.2318-1.8.9` |

## Releases

This repository is the public release and distribution repository for Astryx. It is separate from the client's development source.

The launcher reads `manifest.json` to find the available client build. Each entry contains the release ID, display name, Minecraft version, Forge profile, JAR download URL and SHA-256 checksum.

Published builds are attached to GitHub Releases and then referenced by the manifest. The checksum lets the launcher verify that the downloaded client file matches the published build.

### Current release

The manifest currently lists **Astryx 1.8.9** (`astryx-1.8.9`). Check the [releases page](https://github.com/AstryxClient/Astryx-Releases/releases) for the available download and release notes.

## Installing

Astryx is distributed through its launcher. Use the launcher to install or update the client so it can select the correct build and verify the downloaded file against the manifest.

The client targets Minecraft 1.8.9 with the Forge profile listed above.

## Repository contents

- `manifest.json` — release information read by the launcher
- `AstryxCore-*.jar` — published client build assets, where included
- GitHub Releases — versioned downloads and release notes

This repository is for distributing builds. It isn't the place for the launcher's or client's full development source.

## Reporting issues

If something isn't working, include the Astryx version, Minecraft/Forge version and a description of what happened. For crashes, attach the relevant crash report or log where possible.
