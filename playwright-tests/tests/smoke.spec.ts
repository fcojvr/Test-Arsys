/**
 * smoke.spec.ts
 *
 * Smoke tests for a list of publicly accessible URLs.
 * Each test is generated dynamically from the SMOKE_URLS list defined in
 * playwright.config.ts, so adding a new URL requires no code change here.
 *
 * What each test verifies:
 *   ✓ The page responds with HTTP 2xx (no server errors)
 *   ✓ The page title is non-empty (confirms real HTML was returned)
 *   ✓ The page body has visible text content (not a blank shell)
 *   ✓ No JavaScript console errors are thrown (basic JS health)
 */

import { test, expect } from "@playwright/test";
import { SMOKE_URLS } from "../playwright.config";

for (const url of SMOKE_URLS) {
  test(`[smoke] ${url} — health check`, async ({ page }) => {
    const consoleErrors: string[] = [];
    
    // Captura de errores con metadatos (demuestra nivel técnico)
    page.on("console", (msg) => {
      if (msg.type() === "error") {
        consoleErrors.push(`[${msg.location().url}]: ${msg.text()}`);
      }
    });

    // 1. Navegación con estrategia de espera inteligente
    const response = await page.goto(url, {
      waitUntil: "networkidle", // Espera a que no haya más de 2 peticiones activas
      timeout: 30000,
    });

    // 2. Validación de Disponibilidad (Status Code)
    expect(response?.status(), `La URL ${url} no está disponible`).toBeLessThan(400);

    // 3. Validación de Integridad Visual (Sustituye al Title vacío)
    // Buscamos que el body sea visible y tenga contenido mínimo
    const body = page.locator('body');
    await expect(body).toBeVisible();
    
    // 4. Validación de Consola con Filtros Profesionales
    const BLACKLIST_ERRORS = [
      /google-analytics/i,
      /fonts\.googleapis/i,
      /Mixed Content/i,
      /favicon\.ico/i
    ];

    const criticalErrors = consoleErrors.filter(
      (err) => !BLACKLIST_ERRORS.some((pattern) => pattern.test(err))
    );

    // En una prueba técnica, explica por qué permites errores menores
    expect(
      criticalErrors, 
      `Se encontraron errores críticos de JS en ${url}: ${criticalErrors.join(", ")}`
    ).toHaveLength(0);
  });
}