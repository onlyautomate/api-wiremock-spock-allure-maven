package support.logs

import config.TestExecutionConfig
import io.qameta.allure.Allure
import java.util.concurrent.ConcurrentHashMap

class CustomLog {

    private static final ConcurrentHashMap<String, StringBuilder> wireMockLogsMap = new ConcurrentHashMap<>() //for thread running wiremock
    private static final ThreadLocal<List<String>> threadLogBuffer = ThreadLocal.withInitial { [] } //for thread running specification

    static void info(Object message) {
        threadLogBuffer.get().add(message.toString()) //keep consolidating log messages until they are cleared
    }

    static void addWireMockLogs(String key, StringBuilder sb) {
        wireMockLogsMap.computeIfAbsent(key, k -> new StringBuilder()).append(sb.toString())
    }


    static void attachLogsToAllure(String attachmentName) {
        List<String> threadLogs = threadLogBuffer.get()
        if (!threadLogs.isEmpty()) {
            def content = threadLogs.join(System.lineSeparator())
            Allure.addAttachment(attachmentName, 'text/plain', content, '.txt')
            def localPrint = TestExecutionConfig.LOCAL_RUN || attachmentName.contains('global')
            if(localPrint) {
                println content //if local run, print to the console
            }
            threadLogBuffer.get().clear() //reset
        }
    }

    //must be called in the main thread which is running the specification
    static void attachWireMockLogsToAllure() {
        wireMockLogsMap.entrySet().each {
            Allure.addAttachment(it.key, 'text/plain', wireMockLogsMap.get(it.key).toString(), '.txt')
            if(TestExecutionConfig.LOCAL_RUN) {
                println wireMockLogsMap.get(it.key)
            }
            wireMockLogsMap.remove(it.key) //reset
        }
    }
}