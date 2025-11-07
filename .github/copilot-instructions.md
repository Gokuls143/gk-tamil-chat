## Copilot instructions — GK Tamil Chat (concise)

Purpose: help AI contributors be immediately productive in this Spring Boot monolith.

- Repository layout to inspect first:
  - `src/main/java/com/example/demo/` — main Java packages (controller/, service/, repository/, model/)
  - `src/main/resources/` — application.properties, `application-prod.properties`, and `static/` frontend assets (index.html, script.js, style.css)
  - `build/libs/` — produced JAR (e.g. `demo-0.0.1-SNAPSHOT.jar`)
  - `DEPLOYMENT-DETAILED.md` and `DEPLOYMENT.md` — deployment steps, environment variables and examples (useful for CI/CD and runtime env)
  - `Dockerfile`, `Procfile`, `gradlew.bat` — container and platform artifacts

- Big picture (what to know):
  - This is a single Spring Boot web application. HTTP requests hit controllers under `controller/`, business logic lives in `service/`, persistence is in `repository/` and entities are in `model/`.
  - Frontend is static files served from `src/main/resources/static/` — small JS/CSS + HTML (no separate SPA build pipeline).
  - Production DB is MySQL; `src/main/resources/application-prod.properties` reads `DATABASE_URL` and the project expects `SPRING_PROFILES_ACTIVE=prod` in production. See `DEPLOYMENT-DETAILED.md` for exact env var names and example values.

- Common developer workflows (copyable PowerShell examples):
  - Build: `.
    .\gradlew.bat clean build` (run from repo root in PowerShell)
  - Run locally: `java -jar build\libs\demo-0.0.1-SNAPSHOT.jar`
  - Run tests: `.
    .\gradlew.bat test` or include in build step above
  - Debug locally: run from IDE (Main application class under `com.example.demo`) or `java -jar build\libs\demo-0.0.1-SNAPSHOT.jar --logging.level.root=DEBUG`

- Environment & deployment notes (explicit):
  - Production profile file: `src/main/resources/application-prod.properties` — it uses `${DATABASE_URL}` and `${PORT:8080}`. When modifying DB startup behavior note `spring.jpa.hibernate.ddl-auto=update` is used (no DB migration tool detected).
  - Essential env vars: `SPRING_PROFILES_ACTIVE`, `DATABASE_URL`, `PORT`. Optional (mail): `SPRING_MAIL_*` variables shown in `DEPLOYMENT-DETAILED.md`.
  - Target JDK: the docs recommend Java 21 for deployment (see `DEPLOYMENT-DETAILED.md`).

- Patterns and conventions specific to this project:
  - Package-by-feature under `com.example.demo` with conventional subpackages (controller/service/repository/model). Follow existing naming and placement when adding new features.
  - Static assets are edited in `src/main/resources/static/`. Changes here do not require a separate frontend build step — rebuild the JAR to publish.
  - Tests include integration tests (see `build/reports/tests/test/` and `test-results/` for CI artifacts). When adding tests, follow the existing JUnit setup found under `test/`.

- Integration & external points to be careful with:
  - Database: MySQL connection string is provided via `DATABASE_URL` (Railway/Render-style `jdbc:mysql://host:port/db?user=...&password=...`) — ensure encoding is preserved when setting env vars.
  - Deploy artifacts: Dockerfile and Procfile exist; CI may build the JAR and run `java -jar` directly.

- Quick troubleshooting hints for AI to propose edits/PRs:
  - If runtime DB errors appear, check `application-prod.properties` for `DATABASE_URL` usage and `SPRING_PROFILES_ACTIVE` presence.
  - If static changes don't show up, confirm you rebuilt the JAR and restarted the service.
  - Avoid invasive schema changes — project uses `hibernate.ddl-auto=update` in prod (no migration system detected).

If anything here is unclear or you want more examples (e.g., sample controller + test snippet or CI steps), tell me which area to expand and I will iterate.
