package base

import com.github.tomakehurst.wiremock.WireMockServer
import config.TestExecutionConfig
import spock.lang.*
import support.logs.CustomLog

@Stepwise
@Unroll
abstract class BaseSpecification extends Specification {

    /*
    Thread-safe storage of wiremock server instances to be used in individual specifications
    Globalized for the purpose of this demo
    But if only some spock specifications need stubbing, you may choose to create wiremock server within the specification - create in setupSpec() & dispose off in cleanupSpec
    */

    @Shared private final ThreadLocal<WireMockServer> wireMockServerThreadLocal = new ThreadLocal<>()
    @Shared def currentSpec = this.specificationContext.currentSpec
    @Shared String specFullName = "${currentSpec.package}.${currentSpec.name}".toString()
    @Shared def specCounter = TestExecutionConfig.specCounter.getAndIncrement()
    @Shared def specsCount = TestExecutionConfig.specLevelCountDetailsList.size()

    def setupSpec() {
        CustomLog.info("${System.lineSeparator()}Started specification # ${specCounter} out of total ${specsCount} - ${specFullName}")
        def ws = BaseMockApi.createAndStartWireMockServer()
        CustomLog.info("WireMock server was started on port ${ws.port()}")
        wireMockServerThreadLocal.set(ws)
        CustomLog.attachLogsToAllure('global-setupSpec-logs') //attach to allure
    }

    def cleanupSpec() {
        CustomLog.info("${System.lineSeparator()}Ended specification # ${specCounter} out of total ${specsCount} - ${specFullName}")
        def ws = acquireWireMockServer()
        if(ws) {
            CustomLog.info("Stopping wireMock server running on port ${ws.port()}")
            ws.stop()
        }
        CustomLog.attachLogsToAllure('global-cleanupSpec-logs') //attach to allure
    }

    WireMockServer acquireWireMockServer() {
        return wireMockServerThreadLocal.get()
    }
}