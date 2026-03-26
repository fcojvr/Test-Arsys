/**
 * api-smoke.spec.ts
 *
 * Lightweight API-level smoke tests using Playwright's built-in request context.
 * These run without launching a browser — very fast (<1 s each).
 *
 * Useful for JSON endpoints (REST APIs, health checks) where rendering a full
 * browser page would be overkill.
 */

import { test, expect } from "@playwright/test";

const API_ENDPOINTS: Array<{
  label: string;
  url: string;
  /** Optional: key that must exist at the top level of the JSON body */
  expectedKey?: string;
  /** Optional: expected HTTP status code (default 200) */
  expectedStatus?: number;
}> = [
  {
    label: "JSONPlaceholder — single todo",
    url: "https://jsonplaceholder.typicode.com/todos/1",
    expectedKey: "title",
  },
  {
    label: "JSONPlaceholder — posts list",
    url: "https://jsonplaceholder.typicode.com/posts",
    // Response is an array — we'll handle this specially below
  },
  {
    label: "httpbin — GET echo",
    url: "https://httpbin.org/get",
    expectedKey: "url",
  },
  {
    label: "httpbin — status 200",
    url: "https://httpbin.org/status/200",
    expectedStatus: 400,
  },
];

for (const endpoint of API_ENDPOINTS) {
  test(`[api-smoke] ${endpoint.label}`, async ({ request }) => {
    const response = await request.get(endpoint.url);

    // ── Status code ─────────────────────────────────────────────────────────
    const expectedStatus = endpoint.expectedStatus ?? 200;
    expect(
      response.status(),
      `Expected ${expectedStatus} for ${endpoint.url}`
    ).toBe(expectedStatus);

    // ── Optional JSON shape check ────────────────────────────────────────────
    if (endpoint.expectedKey) {
      const body = await response.json();
      expect(
        body,
        `Expected key "${endpoint.expectedKey}" in response`
      ).toHaveProperty(endpoint.expectedKey);
    }

    // ── Array response check ─────────────────────────────────────────────────
    if (endpoint.url.includes("/posts")) {
      const body = await response.json();
      expect(Array.isArray(body), "Posts response should be an array").toBe(
        true
      );
      expect(
        (body as unknown[]).length,
        "Posts array should not be empty"
      ).toBeGreaterThan(0);
    }
  });
}
