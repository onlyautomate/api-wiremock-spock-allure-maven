package support.myrestassured

import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import support.logs.CustomLog

class CustomFilter implements Filter {

    @Override
    Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
        Response response = ctx.next(requestSpec, responseSpec)
        logRequestDetails(requestSpec, response)
        response
    }

    private static Object printRequestBody(FilterableRequestSpecification requestSpec) {
        try {
            return JsonOutput.prettyPrint(requestSpec.body.toString())
        } catch (Exception ignored) {
            return requestSpec.body
        }
    }

    private static void logRequestDetails(FilterableRequestSpecification requestSpec, Response response) {
        def sb = new StringBuilder()
        sb.append(System.lineSeparator())
        sb.append("=======================================================").append(System.lineSeparator())
        sb.append("CALLED ENDPOINT: ${response.statusCode} ${requestSpec.method} ${requestSpec.URI}").append(System.lineSeparator())
        sb.append("Status line: ${response.statusLine}").append(System.lineSeparator())
        sb.append("Request body: ${printRequestBody(requestSpec)}").append(System.lineSeparator())
        sb.append("Request headers: ${new JsonBuilder(requestSpec.headers).toPrettyString()}").append(System.lineSeparator())
        sb.append("Response body: ${response.body.asPrettyString()}").append(System.lineSeparator())
        sb.append("Response headers: ${new JsonBuilder(response.headers).toPrettyString()}").append(System.lineSeparator())
        sb.append("=======================================================").append(System.lineSeparator())
        CustomLog.info(sb)
    }
}