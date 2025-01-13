package base

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.MappingBuilder
import groovy.json.JsonBuilder
import io.restassured.RestAssured
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import support.myrestassured.CustomFilter
import support.mywiremock.TransformCreateProduct
import support.mywiremock.CustomServeEventListener
import support.mywiremock.CustomStubFilter
import support.mywiremock.TransformProductList
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo
import static com.github.tomakehurst.wiremock.client.WireMock.matching
import static com.github.tomakehurst.wiremock.client.WireMock.request
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig

abstract class BaseMockApi {

    String baseUri
    String basePath = '/'
    ContentType requestContentType = ContentType.JSON
    ContentType responseContentType
    Method method
    Map requestHeaders
    String basicAuthUserName
    String basicAuthPassword
    private Map defaultRequestHeaders = [:]

    //stubbing related
    Map<String,Object> stubResponseBodyTransformationMap
    String stubResponseBodyFileName
    String stubResponseBodyString
    Boolean useStubMapping = false
    String urlPattern
    Boolean authNeeded = false

    @SuppressWarnings('unused')
    Response makeCall() {
        makeCall(null, null, null)
    }

    Response makeCall(List pathParams) {
        makeCall(pathParams, null, null)
    }

    Response makeCall(Map<String, Object> queryParams) {
        makeCall(null, queryParams, null)
    }

    @SuppressWarnings('unused')
    Response makeCall(List pathParams, Map<String, Object> queryParams) {
        makeCall(pathParams, queryParams, null)
    }

    @SuppressWarnings('unused')
    Response makeCall(List pathParams, String requestBody) {
        makeCall(pathParams, null, requestBody)
    }

    Response makeCall(String requestBody) {
        makeCall(null, null, requestBody)
    }

    Response makeCall(List pathParams, Map<String, Object> queryParams, String requestBody) {
        RequestSpecBuilder requestSpecBuilder = new RequestSpecBuilder()
            .setBaseUri(this.baseUri)
            .addHeaders(this.defaultRequestHeaders)
            .setContentType(this.requestContentType)

        if(this.responseContentType) {
            requestSpecBuilder.setAccept(responseContentType)
        }

        if(this.requestHeaders) {
            requestSpecBuilder.addHeaders(requestHeaders)
        }

        if(queryParams) {
            requestSpecBuilder.addQueryParams(queryParams)
        }

        if(requestBody) {
            requestSpecBuilder.setBody(requestBody)
        }

        RequestSpecification requestSpecification = requestSpecBuilder.build()
        if(this.basicAuthUserName && this.basicAuthPassword) {
            requestSpecification.auth().basic(this.basicAuthUserName, this.basicAuthPassword)
        }

        if(pathParams) {
            RestAssured.given(requestSpecification)
                .filter(new CustomFilter()) //customize execution of api call
                .request(this.method, this.basePath, pathParams.toArray()) //unnamed api path parameters
        } else {
            requestSpecification.basePath(this.basePath)
            RestAssured.given(requestSpecification)
                .filter(new CustomFilter()) //customize execution of api call
                .request(this.method)
        }
    }

    //programmatic stubbing of an endpoint based on provided configuration
    void stubEndpoint(WireMockServer wireMockServer, Boolean authNeeded) {
        //response definition builder
        def respDefBuilder = aResponse().withStatus(200).withTransformers('response-template')

        if(this.stubResponseBodyString) {
            respDefBuilder.withBody(this.stubResponseBodyString) //json string
        } else if(this.stubResponseBodyFileName) {
            respDefBuilder.withBodyFile(this.stubResponseBodyFileName) //json file name
            if(this.stubResponseBodyTransformationMap) {
                respDefBuilder.withTransformerParameters(this.stubResponseBodyTransformationMap) //transformers map, if provided
            }
        }

        if(authNeeded) {
            wireMockServer.stubFor(
                    getRequestBuilder()
                            .willReturn(
                                    aResponse()
                                            .withStatus(401)
                                            .withBody('Unauthorized access')
                            )
            )

            //stubbing for case with present auth header
            wireMockServer.stubFor(
                    getRequestBuilder()
                            .withHeader('Authorization', matching('Bearer ecvt.*'))
                            .willReturn(respDefBuilder)
            )
        } else {
            wireMockServer.stubFor(
                    getRequestBuilder()
                            .willReturn(respDefBuilder)
            )
        }
    }

    //request builder needed for stubbing
    private MappingBuilder getRequestBuilder() {
        def requestBuilder = request(this.method.toString(), urlMatching(this.urlPattern ?: this.basePath))
                .withHeader("Content-Type", equalTo(this.requestContentType.toString()))
                .withHeader("Accept", equalTo('*/*'))

        if (this.requestHeaders) {
            this.requestHeaders.entrySet().each {
                requestBuilder.withHeader(it.key, equalTo(it.value))
            }
        }

        if(this.basicAuthUserName && this.basicAuthPassword) {
            def reqBody = new JsonBuilder([username: this.basicAuthUserName, password: this.basicAuthPassword]).toString()
            requestBuilder.withRequestBody(equalTo(reqBody))
        }

        requestBuilder
    }

    /*
    Create and start wiremock server, expected to be created & started at the beginning of spec and disposed off at the end of the specification
    Custom wiremock extensions, with either global or local scope, are loaded here
    If local-scoped, extension must be explicitely specified, with given name, as transformer either in mapping or using.withTransformers() method
    CustomStubFilter, global, used to filter and modify requests before they are matched against stubs
    CustomServeEventListener, global, used to listen to the events of a request-response lifecycle, after the request has been processed and a response has been served
    TransformProductList, local, transform product list for GETGetAllProducts api endpoint
    TransformCreateProduct, local, trasnform created product for POSTCreateProduct api endpoint
    */
    static WireMockServer createAndStartWireMockServer() {
        def wireMockServer = new WireMockServer(wireMockConfig()
                .dynamicPort()
                .dynamicHttpsPort()
                .extensions(new TransformProductList(), new TransformCreateProduct(), new CustomStubFilter(), new CustomServeEventListener())
        )
        wireMockServer.start()
        wireMockServer
    }
}