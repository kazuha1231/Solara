# Build Instructions for Defenders of Solara

## Building a Runnable JAR

### Prerequisites
- Java 8 or higher
- Gradle (included via wrapper)

### Build Commands

**Windows:**
```bash
gradlew.bat core:fatJar
```

**Linux/Mac:**
```bash
./gradlew core:fatJar
```

### Output
The runnable JAR will be created at:
```
core/build/libs/defenders-of-solara-1.0.0-all.jar
```

### Running the Game

**Windows:**
```bash
java -jar core/build/libs/defenders-of-solara-1.0.0-all.jar
```

**Linux/Mac:**
```bash
java -jar core/build/libs/defenders-of-solara-1.0.0-all.jar
```

### Alternative: Build Regular JAR
If you prefer a regular JAR (without bundled dependencies):
```bash
gradlew.bat core:jar
```

Note: The regular JAR requires all dependencies to be in the classpath.

## Recent Fixes

### Save/Load System
- Battle state is now saved automatically when pausing
- Enemy HP, mana, and stats are preserved exactly as saved
- Resume functionality works correctly

### Known Issues Fixed
- Enemy state restoration now uses saved data directly (no re-scaling)
- Battle state is saved in memory when pausing (even without clicking Save)
- Resume button appears on world selection when a saved battle exists

