package apis

import base.BaseMockApi
import com.github.javafaker.Faker
import com.github.tomakehurst.wiremock.WireMockServer
import groovy.json.JsonBuilder
import io.restassured.http.Method
import org.apache.commons.lang3.RandomStringUtils
import org.apache.groovy.datetime.extensions.DateTimeExtensions
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class StubbedDemoStoreApi extends BaseMockApi {

    enum ApiEndpoint {
        GETGetProduct,
        PUTUpdateProduct,
        GETGetAllProducts,
        POSTCreateProduct,
        POSTGetAuthToken
    }

    StubbedDemoStoreApi(ApiEndpoint apiEndpoint, WireMockServer wireMockServer) {
        this.baseUri = "http://localhost:${wireMockServer.port()}" //wiremock server runs on localhost on a random port
        switch (apiEndpoint) {
            case ApiEndpoint.GETGetProduct:
                this.method = Method.GET
                this.basePath = '/api/product/{id}'
                this.urlPattern = '/api/product/([1-9][0-9]*)' //pattern to match for wiremock
                this.authNeeded = true
                this.stubResponseBodyFileName = 'get-product-details.json' //template which wiremock will transform based on below map
                this.stubResponseBodyTransformationMap = acquireFakeProduct(false).tap {
                    it.remove('productId')
                    it
                }
                break
            case ApiEndpoint.PUTUpdateProduct:
                this.method = Method.PUT
                this.basePath = '/api/product/{id}'
                this.urlPattern = '/api/product/([1-9][0-9]*)'
                this.authNeeded = true
                this.stubResponseBodyFileName = 'updated-product-details.json'
                break
            case ApiEndpoint.GETGetAllProducts:
                this.method = Method.GET
                this.basePath = '/api/product'
                this.useStubMapping = true //wiremock will use matching stub mapping from src/test/resources/mappings i.e. transform-product-list.json  in this case
                break
            case ApiEndpoint.POSTCreateProduct:
                this.method = Method.POST
                this.basePath = '/api/product'
                this.authNeeded = true
                this.useStubMapping = true //wiremock will use matching stub mapping from src/test/resources/mappings i.e. transform-create-product.json in this case
                break
            case ApiEndpoint.POSTGetAuthToken:
                this.method = Method.POST
                this.basePath = '/api/authenticate'
                this.basicAuthUserName = 'admin'
                this.basicAuthPassword = 'admin'
                this.stubResponseBodyString = new JsonBuilder([token: "ecvt${RandomStringUtils.randomAlphanumeric(24)}"]).toString()
                break
        }

        if(!this.useStubMapping) {
            stubEndpoint(wireMockServer, this.authNeeded) //programmatic stubbing instead of mappings json
        }
    }

    static String acquireAuthenticationToken(WireMockServer wireMockServer) {
        def demoStoreApi = new StubbedDemoStoreApi(ApiEndpoint.POSTGetAuthToken, wireMockServer)
        def reqBody = new JsonBuilder([username: demoStoreApi.basicAuthUserName, password: demoStoreApi.basicAuthPassword]).toString()
        def response = demoStoreApi.makeCall(reqBody)
        def jsonPath = response.then().statusCode(200).extract().jsonPath()
        jsonPath.get('token')
    }

    static Map acquireFakeProduct(Boolean newProduct) {
        def faker = new Faker(Locale.US)
        def format = "yyyy-MM-dd'T'HH:mm:ss"
        def createdAtDate
        def updatedAtDate
        if(newProduct) {
            createdAtDate = LocalDateTime.now()
            updatedAtDate = createdAtDate
        } else {
            createdAtDate = faker.date().past(faker.number().numberBetween(100, 365), TimeUnit.DAYS).toLocalDateTime()
            updatedAtDate = faker.date().past(faker.number().numberBetween(1, 99), TimeUnit.DAYS).toLocalDateTime()
        }

        [
                id: faker.number().numberBetween(100, 1000).toString(),
                name: faker.commerce().productName(),
                description: faker.commerce().material(),
                image: faker.internet().image(),
                price: faker.commerce().price(9.99, 99.99).toString(),
                categoryId: faker.number().numberBetween(1, 99).toString(),
                createdAt: DateTimeExtensions.format(createdAtDate, format),
                updatedAt: DateTimeExtensions.format(updatedAtDate, format)
        ]
    }
}