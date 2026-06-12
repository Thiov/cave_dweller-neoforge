# Cave Dweller (Forge/NeoForge Port — 1.20.1)

Self-contained build of Cave Dweller Evolved for **Minecraft 1.20.1**.
Targets Forge and also runs on NeoForge 1.20.1 (which is compatible with
Forge mods on that version).

Based on the upstream Forge 1.20.1 source by SiverDX (Cadentem), with the
JSON config system from the newer ports for consistency
(`config/cave_dweller-server.json` instead of the old TOML server config).

## Requirements

- Minecraft **1.20.1**
- Forge **47.1+** or NeoForge **1.20.1**
- GeckoLib **4.2+** (Forge, built against 4.4.9)
- Java **17+**

## Building from source

```bash
./gradlew build
# Output: build/libs/cave_dweller-1.6.4-forge-1.20.1.jar
```

The Gradle wrapper is included; JDK 17 is provisioned automatically via the
foojay toolchain resolver. GeckoLib resolves from its maven repository.
Built with ModDevGradle (legacy) and parchment mappings.

## License

MIT, preserved from upstream. See `../LICENSE`.
