# Cave Dweller (NeoForge Port — 1.21.11)

Self-contained NeoForge build of Cave Dweller Evolved for **Minecraft 1.21.11**.
Downported from the 26.1.2 port in [`../26.1.2/`](../26.1.2/).

## Requirements

- Minecraft **1.21.11**
- NeoForge **21.11.42+**
- GeckoLib **5.4+** (NeoForge, built against 5.4.5)
- Java **21**

## Building from source

```bash
./gradlew build
# Output: build/libs/cave_dweller-1.6.4-neoforge-1.21.11.jar
```

The Gradle wrapper is included; JDK 21 is provisioned automatically via the
foojay toolchain resolver (any JDK 17+ is enough to launch Gradle). GeckoLib
resolves from its maven repository.

## License

MIT, preserved from upstream. See `../LICENSE`.
