# QuluList

A cross-platform todo app built with Kotlin Multiplatform + Compose Multiplatform — targeting desktop, mobile, and web from a single codebase.

> *qulu* — to do, to act (Zulu / isiZulu)

## Features

- Create, edit, complete, and delete todos
- Priority levels (Low, Medium, High, Urgent) with color badges
- Due date and time pickers
- Quick-add dialog in list view
- Filter by All / Active / Completed
- Search by title
- SQLDelight-powered local persistence
- Material 3 theming with dark/light mode

## Tech Stack

| Layer | Technology |
|---|---|
| UI | Compose Multiplatform |
| Architecture | Clean Architecture + MVVM |
| DI | Koin |
| Navigation | Voyager |
| Database | SQLDelight (SQLite) |
| HTTP | Ktor (planned) |
| Serialization | kotlinx-serialization |
| Date/Time | kotlinx-datetime |

## Prerequisites

- JDK 17+
- Gradle (wrapper included)

## Run

```bash
./gradlew :composeApp:run
```

## Build Distributions

```bash
./gradlew :composeApp:packageDmg       # macOS
./gradlew :composeApp:packageMsi       # Windows
./gradlew :composeApp:packageDeb       # Linux
```

## Roadmap

- [x] Todo CRUD with persistence
- [ ] Calendar / schedule view
- [ ] Pomodoro timer (floating mini-timer)
- [ ] Lock-in focus mode
- [ ] Dropbox API sync
- [ ] Google OAuth + Drive sync
- [ ] AES-256 encryption for cloud data
