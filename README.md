# Playwright Smoke CI

A **one-command local CI environment** using Docker Compose, Jenkins (configured via JCasC + Job DSL), and Playwright (TypeScript).

---

## Quick Start

```bash
# 1. Clone and enter the repository
git clone <repo-url> playwright-jenkins-ci
cd playwright-jenkins-ci

# 2. Start everything
docker compose up -d --build

# 3. Open Jenkins
open http://localhost:8080          # macOS
xdg-open http://localhost:8080      # Linux
# Credentials → admin / admin
```

> First boot takes ~3–5 minutes while Jenkins downloads plugins and browsers are installed on first job run.

---

## Prerequisites

| Tool | Minimum version |
|------|----------------|
| Docker Engine | 24+ |
| Docker Compose plugin | v2.x (`docker compose`) |
| Free ports | 8080, 50000 |

---

## How to Start / Reset the Environment

### Start

```bash
docker compose up -d --build
```

### Stop (keep data)

```bash
docker compose down
# or
make down
```

### Full reset — destroy all data and rebuild from scratch

```bash
docker compose down -v --remove-orphans
docker compose up -d --build
# or
make reset
```

> `--remove-orphans` cleans up stale containers. `-v` deletes the named volumes (Jenkins home, Playwright results).

---

## How to Run the Smoke Job

### Via Jenkins UI

1. Navigate to **http://localhost:8080**
2. Log in with `admin` / `admin`
3. Click **🔥 Playwright Smoke Tests**
4. Click **Build Now** (or **Build with Parameters** to override URLs)
5. Watch the build — results appear in the **Playwright Report** tab and the JUnit badge

### Via CLI (curl)

```bash
# Trigger with default URLs
curl -X POST http://admin:admin@localhost:8080/job/smoke-tests/build

# Trigger with URL overrides
curl -X POST \
  "http://admin:admin@localhost:8080/job/smoke-tests/buildWithParameters" \
  --data-urlencode "TARGET_URLS=https://example.com,https://httpbin.org/get"
```

### Directly on the Playwright container (no Jenkins)

```bash
docker compose exec playwright npx playwright test
# View the HTML report
docker compose exec playwright npx playwright show-report
```

---

## How to Change the Target URLs

### Option 1 — Permanent (recommended): edit `playwright.config.ts`

```typescript
// playwright-tests/playwright.config.ts
const DEFAULT_URLS: string[] = [
  "https://www.wikipedia.org",
  "https://your-new-url.example.com",   // ← add / remove here
  // …
];
```

After editing, rebuild the Playwright image:

```bash
docker compose build playwright
docker compose up -d playwright
```

### Option 2 — At runtime (no rebuild needed): Jenkins parameter

When triggering **Build with Parameters**, fill in the **TARGET_URLS** field:

```
https://example.com,https://httpbin.org/get,https://jsonplaceholder.typicode.com/todos/1
```

The config file value is ignored when this parameter is non-empty.

### Option 3 — Environment variable

```bash
TARGET_URLS="https://example.com,https://httpbin.org/get" \
  docker compose exec playwright npx playwright test
```

---

## Artifacts & Debugging

Each Jenkins build produces:

| Artifact | Location in Jenkins | Contents |
|----------|--------------------|-----------------------|
| Playwright HTML Report | **Playwright Report** tab | Full test run with timings, retries |
| JUnit XML | **Test Results** badge | Pass/fail counts, trends |
| Screenshots | **test-results/** archive | Only captured on failure |
| Traces | **test-results/** archive | `.zip` openable with `playwright show-trace` |

To open a trace locally:

```bash
# Download the .zip from Jenkins artifacts, then:
npx playwright show-trace path/to/trace.zip
```

---

## Project Structure

```
playwright-jenkins-ci/
├── docker-compose.yml          # Single-command bootstrap
├── Jenkinsfile                 # Pipeline reference (also seeded via Job DSL)
├── Makefile                    # Convenience wrappers
│
├── jenkins/
│   ├── Dockerfile              # Jenkins LTS + plugins + Docker CLI
│   ├── plugins.txt             # Pinned plugin list for jenkins-plugin-cli
│   ├── casc/
│   │   └── jenkins.yaml        # JCasC — full Jenkins config (no click-ops)
│   └── init.groovy.d/
│       └── create-jobs.groovy  # Job DSL seed — creates smoke-tests pipeline
│
└── playwright-tests/
    ├── Dockerfile              # mcr.microsoft.com/playwright base image
    ├── package.json
    ├── tsconfig.json
    ├── playwright.config.ts    # Config + URL list + reporters
    └── tests/
        ├── smoke.spec.ts       # Browser smoke tests (6 public URLs)
        └── api-smoke.spec.ts   # API-level smoke tests (no browser, fast)
```

---

## Key Design Decisions & Tradeoffs

### 1. Separate `playwright` container

**Decision:** Playwright runs in its own container (`mcr.microsoft.com/playwright`) rather than inside Jenkins.

**Why:** The official Playwright image bundles all browser dependencies (libnss, libgbm, etc.) in the right versions. Installing browsers inside a Jenkins JDK image is brittle and bloats it. Keeping concerns separate also makes it easy to update Playwright independently.

**Tradeoff:** Jenkins executes pipeline steps via `docker exec` into the container rather than using a Jenkins agent. This is simpler for a single-node setup but would need to be replaced with a proper Jenkins agent or Kubernetes pod for multi-node CI.

---

### 2. JCasC + Job DSL (zero click-ops)

**Decision:** Jenkins configuration is expressed entirely in `jenkins.yaml` (JCasC) and `create-jobs.groovy` (Job DSL). No manual configuration steps are needed.

**Why:** Manual click-through configuration is not reproducible or reviewable. JCasC/Job DSL make the setup auditable in git and idempotent on restart.

**Tradeoff:** The Groovy Job DSL seed script runs at startup; on the very first boot Jenkins may not have fully loaded all plugins yet. A `sleep 5000` guard in the init script handles the race condition pragmatically. A production setup would use a dedicated seed job or the Configuration as Code plugin's `jobs` block instead.

---

### 3. Dynamic URL list via environment variable

**Decision:** `TARGET_URLS` is an env var / Jenkins parameter that overrides the hardcoded defaults in `playwright.config.ts`.

**Why:** Lets operators run ad-hoc checks against staging or a new domain without touching code or redeploying the image.

**Tradeoff:** The override mechanism is a single flat comma-separated string — simple but not strongly typed. A proper solution would use a JSON file or a dedicated config service.

---

### 4. No retries, no parallelism across browsers

**Decision:** `retries: 0`, Chromium-only, `workers: 4`.

**Why:** Smoke tests should be reliable enough to pass on the first run. Retries mask flakiness. Adding Firefox/WebKit would triple run time with little benefit for "is the site up?" checks.

**Tradeoff:** A genuine flaky network or DNS issue will cause a failure. Accept this — it's the right signal for a smoke suite.

---

### 5. Artifacts: HTML report + traces/screenshots on failure only

**Decision:** Videos are off; screenshots and traces are captured only on failure.

**Why:** Videos are large and rarely needed for smoke failures. Traces + screenshots give enough context (full DOM snapshot, network log, console errors) without bloating every build's artifacts.

---

## Credentials

| Service | Username | Password |
|---------|----------|----------|
| Jenkins | `admin` | `admin` |

> These are intentionally simple for local development. Change them in `jenkins/casc/jenkins.yaml` before exposing Jenkins on a network.
