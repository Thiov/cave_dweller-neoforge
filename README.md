# Cave Dweller (NeoForge Port)

An unofficial NeoForge port of [Cave Dweller Evolved](https://www.curseforge.com/minecraft/mc-mods/cave-dweller-evolved)
by **SiverDX (Cadentem)**, ported to NeoForge and Minecraft 26.1.2.
Sibling project of the [Fabric port](https://github.com/Thiov/cave_dweller-fabric).

> **Looking for an older Minecraft version?** The [`1.21.11/`](1.21.11/), [`1.21.10/`](1.21.10/) and [`1.20.1/`](1.20.1/)
> subdirectories are self-contained builds for those Minecraft versions.
> The 1.20.1 jar targets Forge and also runs on NeoForge 1.20.1.

> All gameplay design, model, animations, and sounds are by the original authors.
> This project only adapts the existing code to NeoForge.
>
> - Upstream (archived): https://github.com/SiverDX/cave_dweller
> - Original CurseForge: https://www.curseforge.com/minecraft/mc-mods/cave-dweller-evolved
> - License: MIT (preserved from upstream — see [LICENSE](LICENSE))

## Requirements

- Minecraft **26.1.2**
- NeoForge **26.1.2.75+**
- GeckoLib **5.5.1** (NeoForge)
- Java **25**

## Features

- Cave Dweller mob with original GeckoLib model and animations
- 21 unique sounds (cave noises, chase, flee, hurt, death, footsteps)
- Configurable spawn timing, light level limits, dimension whitelist, biome rules
- Climbs walls, crawls through 1-block gaps, breaks doors
- Custom spawn egg
- Configuration via `config/cave_dweller-server.json`

## Building from source

```bash
./gradlew build
# Output: build/libs/cave_dweller-1.6.4-neoforge-26.1.2.jar
```

The Gradle wrapper is included and the correct JDK is downloaded automatically
(via the foojay toolchain resolver); you only need some JDK 17+ installed to
launch Gradle. GeckoLib resolves from the Modrinth maven.

## License

MIT, preserved from upstream. See [LICENSE](LICENSE).

## Credits

- **SiverDX (Cadentem)** — Cave Dweller Evolved (Forge upstream)
- The original Cave Dweller mod authors — base concept and assets
- [GeckoLib](https://github.com/bernie-g/geckolib) team — animation engine
- [Thiov](https://github.com/Thiov) — NeoForge port
