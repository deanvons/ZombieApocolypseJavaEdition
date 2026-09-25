# Zombie Apocalypse – Java Edition

A small zombie apocalypse class library: survivors with attributes, skills, gear and actions. It's also the starting point for the course's flagship full stack app.

The plan is to grow it into a **Survivor Manager**. Each player joins the camp as one survivor, equips them, and sees how well they perform actions.

| Layer | Tech |
|---|---|
| API | Spring Boot (controller → service → repository) |
| Database | PostgreSQL with Hibernate / Spring Data JPA |
| Auth | Keycloak (OAuth2 / JWT) |
| Frontend | React (Vite) |

## Current status

**Pre-Spring Boot.** This is still a plain Java + Gradle class library with JUnit 5 tests. There's no web API or database yet.

| Phase | What | Status |
|---|---|---|
| Prep | Clean up the class library (custom exceptions, tests passing) | Completed |
| 0 | Add Spring Boot, health endpoint | Completed |
| 1 | Service layer (in memory) | In Progress |
| 2 | REST controllers + DTOs | In Progress |
| 3 | PostgreSQL + JPA entities | |
| 4 | Keycloak security, `Player` profile | |
| 5 | React frontend | |

## Prerequisites

- **JDK 21**
- Nothing else: the Gradle wrapper (`gradlew`) downloads the right Gradle version for you.

## Build, run and test

Run these from the project root. On Windows PowerShell use `.\gradlew`, and on macOS, Linux or Git Bash use `./gradlew`.

| Command | What it does |
|---|---|
| `./gradlew build` | Compiles everything and runs all tests |
| `./gradlew run` | Runs `no.loopacademy.Main` |
| `./gradlew test` | Runs the tests and prints `PASSED` / `FAILED` for each one |
| `./gradlew test --rerun` | Forces the tests to run again, even if nothing changed |

**Why doesn't `./gradlew test` always run anything?** If nothing has changed since the last run, Gradle marks the task `UP-TO-DATE` and skips it. The previous results still stand. Use `--rerun` to run the tests anyway.

**Test report:** after a test run, open `build/reports/tests/test/index.html` in a browser for the full results.

**From the IDE:** click the green ▶ next to `main` in `Main.java`, or next to any test class or method.

Once Spring Boot is added (Phase 0), the app will start with `./gradlew bootRun` instead of `./gradlew run`.

## Project structure

```
src/main/java/no/loopacademy/
├── Main.java
├── exceptions/     CarryWeightExceededException
└── models/
    ├── actions/    Action, ActionType
    ├── attributes/ GeneralAttributes, SurvivorAttributes, AttributeWeights
    ├── items/      Item, Weapon, Tool, WeaponCategory
    ├── skills/     Skill (enum)
    └── survivors/  Survivor (abstract), CareGiver, TestSurvivor
src/test/java/      ActionTests, ItemTests, SurvivorTests
```

## How the domain works

- A **survivor** has 7 attributes: strength, endurance, agility, courage, intelligence, leadership and trustworthiness. They also have a list of **skills** and their **gear**.
- **Carry limit:** a survivor can carry up to `10 + strength × 3` kg. Loading more throws `CarryWeightExceededException`.
- An **action** has a weight for each attribute. A survivor's score for an action is `sum(attribute × weight) × 10`.

## Design decisions

These are the two decisions the course is built around. The full reasoning is in the roadmap.

1. **Behaviour stays on the entity when it only uses the entity's own state.** `performAction()` and `load()` stay on `Survivor`. Services handle loading data, security and transactions.
   *Principle:* an entity must never reach out to the database, other services or the current user.
2. **Keycloak owns identity, and our database owns the game.** A `Player` profile is linked to Keycloak by the `sub` claim, and each player has exactly one survivor. Anyone can view a survivor, but only its owner can change it.


