import jenkins.model.Jenkins
import hudson.model.*
import javaposse.jobdsl.plugin.ExecuteDslScripts
import javaposse.jobdsl.dsl.*
import net.sf.json.JSONObject

System.setProperty("hudson.model.DirectoryBrowserSupport.CSP", "")

def jobDslCode = '''
pipelineJob('smoke-tests') {
    displayName('🔥 Playwright Smoke Tests')
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

// 3. Ejecución forzada inmediata
def jobManagement = new javaposse.jobdsl.plugin.JenkinsJobManagement(System.out, [:], new File('.'))
def dslScriptLoader = new javaposse.jobdsl.dsl.DslScriptLoader(jobManagement)
dslScriptLoader.runScript(jobDslCode)

println "[init.groovy] ✅ TODO LISTO: CSP desactivado y Job 'smoke-tests' creado."