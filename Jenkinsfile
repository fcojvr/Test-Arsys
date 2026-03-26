pipeline {
    agent {
        docker {
            image 'mcr.microsoft.com/playwright:v1.44.0-jammy'
            args '-u root'
        }
    }

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
                sh "cd playwright-tests && npm ci --prefer-offline"
            }
        }

        stage("Install Playwright browsers") {
            steps {
                sh "cd playwright-tests && npx playwright install --with-deps chromium"
            }
        }

        stage("Run smoke tests") {
            steps {
                sh """
                    cd playwright-tests
                    export TARGET_URLS="${params.TARGET_URLS}"
                    npx playwright test --reporter=html --output=test-results
                """
            }
            post {
                always {
                    publishHTML(target: [
                        allowMissing         : true,
                        alwaysLinkToLastBuild: true,
                        keepAll              : true,
                        reportDir            : "playwright-tests/playwright-report",
                        reportFiles          : "index.html",
                        reportName           : "Playwright Report"
                    ])

                    archiveArtifacts(
                        artifacts: "playwright-tests/test-results/**",
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