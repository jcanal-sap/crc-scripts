import org.apache.commons.io.*
import org.apache.commons.lang.StringUtils
import groovy.json.JsonOutput
import groovy.json.JsonSlurper


def baseUrl = 'raw.githubusercontent.com/sap-onestrike/crc-scripts/main/scripts'

def includeScripts = []
def scriptsList = StringUtils.split(IOUtils.toString(new URL("https://${baseUrl}/list.txt?${System.currentTimeMillis()}"), 'utf-8'), '\n')
def js = new JsonSlurper()
def results = []

println "scripts: ${scriptsList.join(', ')}"

scriptsList*.trim().each { s ->
  if (s && (includeScripts.isEmpty() || s in includeScripts)) {
    def sc = remoteScriptsRepository.lookupScript('https', "${baseUrl}/${s}")
    def name = FilenameUtils.getBaseName(s)
    
    println "running '${name}'..."
    
    def res = scriptingLanguagesExecutor.executeScript(sc.engineName, sc.content, false)
    
    results << [ 'name': name, 'ok': !((res.stacktraceText?.trim()) as boolean), 'result': js.parseText(res.executionResult), 'error': res.stacktraceText ]
  }
}

JsonOutput.prettyPrint(JsonOutput.toJson(results))