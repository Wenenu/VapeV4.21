

## Minecraft Compatibility

| Minecraft | Vanilla | Forge | Fabric |
| --- | :---: | :---: | :---: |
| 1.7.10 | ✓ | ✓ | - |
| 1.8.9 | ✓ | ✓ | - |
| 1.12.2 | ✓ | ✓ | - |
| 1.16.5 | | | |
| 1.21.11 | ✓ | ✓ | ✓ |
| 26.2 | ✓ | ✓ | ✓ |

Injection into Lunar Client and Badlion Client 1.8.9 instances is also supported.

Support for Minecraft 1.16.5 is poor; certain mappings, rendering, and module features may not function properly.

**For version 26.2, please inject after joining a server or singleplayer world.**

All target instances must use a 64-bit JVM.

| Command | Purpose |
| --- | --- |
| `.\gradlew.bat check` | Compile, source coverage, and recovery quality verification |
| `.\gradlew.bat injectionJar` | Build self-contained Java injection payload |
| `.\gradlew.bat verifyInjectionPayload` | Verify dependency integrity and Java 8 bytecode version |
| `.\gradlew.bat buildNative` | Build x64 DLL and injector |
| `.\gradlew.bat prepareInjectionBundle` | Assemble native bundle ready for isolated testing |
