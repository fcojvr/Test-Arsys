import jenkins.model.Jenkins
import hudson.model.*
import javaposse.jobdsl.plugin.*
import javaposse.jobdsl.dsl.*
import net.sf.json.JSONObject

System.setProperty("hudson.model.DirectoryBrowserSupport.CSP", "")

def jobDslScript = '''
pipelineJob("smoke-tests") {
    displayName("🔥 Playwright Smoke Tests")
    description("Runs Playwright smoke tests against well-known public URLs. Artifacts: HTML report + traces/screenshots.")

    properties {
        disableConcurrentBuilds()
    }

    // Configuración para que el Job clone tu repositorio automáticamente
    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url('https://github.com/fcojvr/Test-Arsys.git')
                    }
                    branch('main')
                }
            }
            scriptPath('Jenkinsfile')
        }
    }
}
'''

// Seed the job using the Job DSL executor
Thread.start {
    sleep 5000  // Esperar a que Jenkins termine de arrancar

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