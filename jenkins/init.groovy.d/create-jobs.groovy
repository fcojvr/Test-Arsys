// init.groovy.d/create-jobs.groovy
// Runs once at Jenkins startup and seeds the smoke-tests pipeline job.
// Job DSL is used so zero click-ops are needed.

import jenkins.model.Jenkins
import hudson.model.*
import javaposse.jobdsl.plugin.*
import javaposse.jobdsl.dsl.*

def jobDslScript = '''
pipelineJob("smoke-tests") {
    displayName("🔥 Playwright Smoke Tests")
    description("Runs Playwright smoke tests against well-known public URLs. Artifacts: HTML report + traces/screenshots.")

    properties {
        disableConcurrentBuilds()
    }

    triggers {
        // Optionally enable a periodic trigger — commented out by default.
        // cron("H/30 * * * *")
    }

    definition {
        cps {
            script("""
pipeline {
    agent any

    options {
        timestamps()
        ansiColor("xterm")
        timeout(time: 10, unit: "MINUTES")
    }

    parameters {
        // Comma-separated list so users can override URLs at runtime without
        // touching code. Default set lives in playwright.config.ts.
        string(
            name: "TARGET_URLS",
            defaultValue: "",
            description: "Optional: comma-separated URL overrides. Leave blank to use the defaults in playwright.config.ts."
        )
    }

    stages {

        stage("Install dependencies") {
            steps {
                sh """
                    cd /app
                    npm ci --prefer-offline
                """
            }
        }

        stage("Install Playwright browsers") {
            steps {
                sh """
                    cd /app
                    npx playwright install --with-deps chromium
                """
            }
        }

        stage("Run smoke tests") {
            steps {
                sh """
                    cd /app
                    export TARGET_URLS="\${params.TARGET_URLS}"
                    npx playwright test \\\\
                        --reporter=html,junit \\\\
                        --output=test-results
                """
            }
            post {
                always {
                    // JUnit results for the build badge
                    junit allowEmptyResults: true, testResults: "/app/test-results/junit*.xml"

                    // HTML report as a browsable Jenkins artifact
                    publishHTML(target: [
                        allowMissing         : true,
                        alwaysLinkToLastBuild: true,
                        keepAll              : true,
                        reportDir            : "/app/playwright-report",
                        reportFiles          : "index.html",
                        reportName           : "Playwright Report",
                        reportTitles         : "Smoke Test Report"
                    ])

                    // Raw artifacts for deeper debugging (traces, screenshots)
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
        success { echo "\\u2705 All smoke tests passed." }
        failure { echo "\\u274C One or more smoke tests failed — check the Playwright report." }
    }
}
""".stripIndent())
            sandbox(true)
        }
    }
}
'''

// Seed the job using the Job DSL executor
Thread.start {
    sleep 5000  // Give Jenkins a moment to finish booting

    def jenkins = Jenkins.get()
    def jobDslFacade = new DslScriptLoader(new JenkinsJobManagement(System.out, [:], new File(".")))

    try {
        jobDslFacade.runScript(jobDslScript)
        println "[init.groovy] ✅ smoke-tests job created/updated via Job DSL"
    } catch (Exception e) {
        println "[init.groovy] ❌ Failed to seed job: ${e.message}"
        e.printStackTrace()
    }
}
