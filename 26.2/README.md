# Cave Dweller (NeoForge Port — 26.2)

Self-contained NeoForge build of Cave Dweller Evolved for **Minecraft 26.2**.
Same code era as the 26.3 port in the parent directory; includes the reworked
26.2 `LivingEntity.knockback` mixin target. (NeoForge's 26.x line is still in
beta for 26.2; this build targets the 26.2 beta.)

## Requirements

- Minecraft **26.2**
- NeoForge **26.2.0.7-beta+**
- GeckoLib **5.5.3** (NeoForge)
- Java **25**

## Building from source

```bash
./gradlew build
# Output: build/libs/cave_dweller-1.6.4-neoforge-26.2.jar
```

The Gradle wrapper is included; JDK is provisioned automatically via the foojay
toolchain resolver (any JDK 17+ is enough to launch Gradle). GeckoLib resolves
from the Modrinth maven.

## License

MIT, preserved from upstream. See `../LICENSE`.
