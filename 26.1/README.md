# Cave Dweller (NeoForge Port — 26.1)

Self-contained NeoForge build of Cave Dweller Evolved for **Minecraft 26.1**.
Same code era as the 26.2 / 26.1.2 ports; identical source to the 26.1.2 build
(the 26.1.x line shares the same mod-facing API). Targets the latest 26.1 NeoForge
beta.

## Requirements

- Minecraft **26.1**
- NeoForge **26.1.0.19-beta+**
- GeckoLib **5.5** (NeoForge)
- Java **25**

## Building from source

```bash
./gradlew build
# Output: build/libs/cave_dweller-1.6.4-neoforge-26.1.jar
```

The Gradle wrapper is included; JDK is provisioned automatically via the foojay
toolchain resolver (any JDK 17+ is enough to launch Gradle). GeckoLib resolves
from the Modrinth maven.

## License

MIT, preserved from upstream. See `../LICENSE`.
