# Astryx Releases

Official release repository for the Astryx Minecraft client and launcher.

## Release manifest

`manifest.json` is the launcher's version catalogue. It will list published Astryx client releases, their Minecraft/Forge requirements, download URLs, and SHA-256 checksums.

## Publishing a release

1. Build AstryxCore from the matching source revision.
2. Create a GitHub Release with a version tag (for example, `v0.1.0`).
3. Attach the built AstryxCore JAR and any release assets.
4. Calculate SHA-256 checksums for the uploaded assets.
5. Add the release metadata and exact asset URLs to `manifest.json`.

A release must not be added to the manifest until its assets have been uploaded and their checksums verified.
