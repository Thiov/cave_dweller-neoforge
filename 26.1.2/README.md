# Cave Dweller (NeoForge Port — 26.1.2)

Self-contained NeoForge build of Cave Dweller Evolved for **Minecraft 26.1.2**.
Same code era as the 26.2 port in the parent directory. (26.1.2 is the one 26.x
Minecraft version with non-beta NeoForge release builds.)

## Requirements

- Minecraft **26.1.2**
- NeoForge **26.1.2.75+**
- GeckoLib **5.5.1** (NeoForge)
- Java **25**

## Building from source

```bash
./gradlew build
# Output: build/libs/cave_dweller-1.6.4-neoforge-26.1.2.jar
```

The Gradle wrapper is included; JDK is provisioned automatically via the foojay
toolchain resolver (any JDK 17+ is enough to launch Gradle). GeckoLib resolves
from the Modrinth maven.

## License

MIT, preserved from upstream. See `../LICENSE`.
