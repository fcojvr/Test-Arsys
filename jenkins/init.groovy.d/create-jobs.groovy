import jenkins.model.Jenkins
import javaposse.jobdsl.plugin.JenkinsJobManagement
import javaposse.jobdsl.dsl.DslScriptLoader

def jobDslCode = '''
pipelineJob('smoke-tests') {
    displayName('🔥 Playwright Smoke Tests')
    description('Smoke tests automáticos con Playwright. Artefactos: HTML report + traces/screenshots.')

    properties {
        disableConcurrentBuilds()
    }

    parameters {
        stringParam(
            'TARGET_URLS',
            '',
            'Comma-separated URL overrides. Leave blank to use playwright.config.ts defaults.'
        )
    }

    definition {
        cpsScm {
            scm {
                git {
                    remote { url('https://github.com/fcojvr/Test-Arsys.git') }
                    branch('main')
                }
            }
            scriptPath('Jenkinsfile')
        }
    }
}
'''

Thread.start {
    def maxAttempts = 20
    def attempt = 0
    def created = false

    while (attempt < maxAttempts && !created) {
        attempt++
        try {
            def jenkins = Jenkins.get()

            Class.forName('javaposse.jobdsl.plugin.JenkinsJobManagement')

            def jobDslPlugin = jenkins.pluginManager.getPlugin('job-dsl')
            if (jobDslPlugin == null || !jobDslPlugin.isActive()) {
                println "[init.groovy] Attempt ${attempt}/${maxAttempts}: job-dsl plugin not active yet, waiting 5s..."
                Thread.sleep(5000)
                continue
            }

            def jobManagement = new JenkinsJobManagement(System.out, [:], new File('.'))
            def loader = new DslScriptLoader(jobManagement)
            loader.runScript(jobDslCode)

            jenkins.reload()

            println "[init.groovy] ✅ Job 'smoke-tests' created on attempt ${attempt}."
            created = true

        } catch (ClassNotFoundException e) {
            println "[init.groovy] Attempt ${attempt}/${maxAttempts}: Job DSL classes not loaded yet. Waiting 5s..."
            Thread.sleep(5000)
        } catch (Exception e) {
            println "[init.groovy] Attempt ${attempt}/${maxAttempts}: ${e.message}. Waiting 5s..."
            Thread.sleep(5000)
        }
    }

    if (!created) {
        println "[init.groovy] ❌ Failed to create job after ${maxAttempts} attempts."
    }
}