package support.mywiremock

import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ServeEventListener
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.stubbing.ServeEvent
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import support.logs.CustomLog

//global scope, by default
class CustomServeEventListener implements ServeEventListener {

    //will be called after completion of the response component of stubbing
    @Override
    void afterComplete(ServeEvent serveEvent, Parameters parameters) {
        def req = serveEvent.request
        def resp = serveEvent.response

        if(req.method == RequestMethod.POST && req.url.endsWith('/api/product') && !TransformCreateProduct.createdProductResponseBody) {
            TransformCreateProduct.createdProductResponseBody = resp.bodyAsString //special case, capture the details of created product to be used in subsequent endpoints
        }

        def sb = new StringBuilder()
        sb.append(System.lineSeparator())
        sb.append("=======================================================").append(System.lineSeparator())
        sb.append("STUBBED ENDPOINT: ${resp.status} ${req.method} ${req.url}").append(System.lineSeparator())
        sb.append("Request body: ${prettyPrintBody(req.bodyAsString)}").append(System.lineSeparator())
        sb.append("Request headers: ${new JsonBuilder(req.headers).toPrettyString()}").append(System.lineSeparator())
        sb.append("Response body: ${prettyPrintBody(resp.bodyAsString)}").append(System.lineSeparator())
        sb.append("Response headers: ${new JsonBuilder(resp.headers).toPrettyString()}").append(System.lineSeparator())
        sb.append("=======================================================").append(System.lineSeparator())

        def path = req.url.substring(req.url.indexOf('/')).replaceAll('/', '-')
        def suffix = "${resp.status}-${req.method}${path}"
        CustomLog.addWireMockLogs("wiremock-stub-${suffix}", sb) //keep track wiremock logs, independently for each stubbed endpoint, for allue report
    }

    @Override
    void beforeResponseSent(ServeEvent serveEvent, Parameters parameters) {
        super.beforeResponseSent(serveEvent, parameters)
    }

    @Override
    void beforeMatch(ServeEvent serveEvent, Parameters parameters) {
        super.beforeMatch(serveEvent, parameters)
    }

    @Override
    void afterMatch(ServeEvent serveEvent, Parameters parameters) {
        super.afterMatch(serveEvent, parameters)
    }

    @Override
    String getName() {
        'custom-wiremock-serve-event-listener'
    }

    private static Object prettyPrintBody(String body) {
        try {
            return JsonOutput.prettyPrint(body)
        } catch (Exception ignored) {
            return body
        }
    }
}