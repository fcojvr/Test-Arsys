/**
 * Jenkinsfile (Declarative Pipeline)
 *
 * This file serves as a reference / portable definition of the smoke-tests
 * pipeline. The authoritative definition is seeded automatically via Job DSL
 * (jenkins/init.groovy.d/create-jobs.groovy) so no manual configuration is
 * needed. This file is checked in for documentation and portability.
 */

pipeline {
    agent any

    options {
        timestamps()
        ansiColor("xterm")
        timeout(time: 10, unit: "MINUTES")
        disableConcurrentBuilds()
    }

    parameters {
        string(
            name: "TARGET_URLS",
            defaultValue: "",
            description: "Comma-separated URL overrides. Leave blank to use playwright.config.ts defaults."
        )
    }

    stages {

        stage("Install dependencies") {
            steps {
                sh "cd /app && npm ci --prefer-offline"
            }
        }

        stage("Install Playwright browsers") {
            steps {
                sh "cd /app && npx playwright install --with-deps chromium"
            }
        }

        stage("Run smoke tests") {
            steps {
                sh """
                    cd /app
                    export TARGET_URLS="${params.TARGET_URLS}"
                    npx playwright test \
                        --reporter=html,junit \
                        --output=test-results
                """
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: "/app/test-results/junit*.xml"

                    publishHTML(target: [
                        allowMissing         : true,
                        alwaysLinkToLastBuild: true,
                        keepAll              : true,
                        reportDir            : "/app/playwright-report",
                        reportFiles          : "index.html",
                        reportName           : "Playwright Report",
                        reportTitles         : "Smoke Test Report"
                    ])

                    archiveArtifacts(
                        artifacts: "test-results/**",
                        allowEmptyArchive: true,
                        fingerprint: true
                    )
                }
            }
        }
    }

    post {
        success { echo "✅ All smoke tests passed." }
        failure { echo "❌ One or more smoke tests failed — check the Playwright report." }
    }
}
