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
                // Quitamos el cd /app, Jenkins ya está en la carpeta del código
                sh "npm ci --prefer-offline"
            }
        }

        stage("Install Playwright browsers") {
            steps {
                sh "npx playwright install --with-deps chromium"
            }
        }

        stage("Run smoke tests") {
            steps {
                sh """
                    export TARGET_URLS="${params.TARGET_URLS}"
                    npx playwright test \
                        --reporter=html,junit \
                        --output=test-results
                """
            }
            post {
                always {
                    // Corregimos las rutas de los reportes para que apunten a la raíz del workspace
                    junit allowEmptyResults: true,
                          testResults: "test-results/*.xml"

                    publishHTML(target: [
                        allowMissing         : true,
                        alwaysLinkToLastBuild: true,
                        keepAll               : true,
                        reportDir            : "playwright-report",
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
