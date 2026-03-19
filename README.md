# Air Traffic Surveillance System (ATSS)

A multi-threaded console application that simulates a radar system tracking multiple aircraft, updating their positions in real-time, and detecting potential collisions.

Built in Java 21 following defense aerospace coding standards with strict naming conventions, full Javadoc documentation, and thread-safe concurrent design.

## Architecture

```
com.atss
├── model/
│   └── Aircraft.java            # Domain entity with position, speed, heading
├── engine/
│   └── RadarScanner.java        # Scheduled background thread for radar sweeps
├── data/
│   └── AircraftRegistry.java    # Thread-safe concurrent data store
├── detection/
│   └── CollisionDetector.java   # Pairwise proximity alert system
└── Main.java                    # Entry point and console interface
```

## Key Concepts

| Phase | Topic | Java Feature |
|-------|-------|--------------|
| 1 | Domain Modeling | Encapsulation, private fields, getters/setters |
| 2 | Radar Engine | `ScheduledExecutorService`, Java Memory Model |
| 3 | Thread-Safe Data | `ConcurrentHashMap`, shared mutable state |
| 4 | Collision Detection | `java.lang.Math`, O(n^2) optimization |

## Build & Run

```bash
# Compile
javac -d out src/main/java/com/atss/**/*.java src/main/java/com/atss/Main.java

# Run
java -cp out com.atss.Main
```

## Requirements

- Java 21 (LTS)
