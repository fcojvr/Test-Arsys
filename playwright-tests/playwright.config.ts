import { defineConfig, devices } from "@playwright/test";

/**
 * Target URLs for the smoke suite.
 *
 * How to change them:
 *   1. Edit the DEFAULT_URLS array below and rebuild/restart.
 *   2. At runtime, set the TARGET_URLS environment variable as a
 *      comma-separated list — the Jenkins job exposes this as a parameter.
 *
 * Example override:
 *   TARGET_URLS="https://example.com,https://httpbin.org/get" npx playwright test
 */
const DEFAULT_URLS: string[] = [
  "https://demoqa.com/",
  "https://the-internet.herokuapp.com/",
  "https://automationexercise.com/",
  "https://playwright.dev",
  "https://www.typescriptlang.org/",
  "https://nodejs.org",
];

function resolveUrls(): string[] {
  const env = process.env.TARGET_URLS;
  if (env && env.trim().length > 0) {
    return env
      .split(",")
      .map((u) => u.trim())
      .filter(Boolean);
  }
  return DEFAULT_URLS;
}

// Expose resolved URLs so tests can import them
export const SMOKE_URLS = resolveUrls();

export default defineConfig({
  testDir: "./tests",

  /* Run each test file in parallel, tests within a file serially */
  fullyParallel: false,
  workers: 4,

  /* Fail the build on any test failure */
  forbidOnly: !!process.env.CI,

  /* No retries for smoke tests — failures should be investigated, not masked */
  retries: 0,

  /* Timeouts */
  timeout: 30_000,
  expect: { timeout: 10_000 },

  reporter: [
    ["html", { outputFolder: "playwright-report", open: "never" }],
    ["junit", { outputFile: "test-results/junit-results.xml" }],
    ["list"],
  ],

  use: {
    /* Capture a screenshot and trace on every failure for easy debugging */
    screenshot: "only-on-failure",
    trace: "retain-on-failure",
    video: "off", // Keep artifacts lightweight; enable if needed

    /* Use Chromium in headless mode — fastest and most stable in CI */
    ...devices["Desktop Chrome"],
    headless: !!process.env.CI || false,

    /* Extra navigation timeout */
    navigationTimeout: 20_000,
  },

  outputDir: "test-results",
});
