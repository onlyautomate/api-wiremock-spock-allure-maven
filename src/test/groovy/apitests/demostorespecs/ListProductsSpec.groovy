package apitests.demostorespecs

import apis.StubbedDemoStoreApi
import base.BaseSpecification
import com.github.tomakehurst.wiremock.WireMockServer
import spock.lang.Shared

class ListProductsSpec extends BaseSpecification {

    @Shared WireMockServer wireMockServer
    @Shared def authToken

    def "acquire token"() {
        given: 'acquire token with relevant credentials'
            wireMockServer = acquireWireMockServer()
            authToken = StubbedDemoStoreApi.acquireAuthenticationToken(wireMockServer)
        expect: 'token acquired'
            authToken
    }

    def "get products for category: #useCase"() {
        given: 'authenticated or unauthenticated user gets product from category'
            def stubbedDemoStoreApi = new StubbedDemoStoreApi(StubbedDemoStoreApi.ApiEndpoint.GETGetAllProducts, wireMockServer).tap {
                if(authenticated) requestHeaders = [Authorization: "Bearer ${authToken}"]
                this
            }
            def products = stubbedDemoStoreApi.makeCall([category: categoryId]).then().statusCode(200).extract().jsonPath().get()

        expect: 'successful call returns list of products, if they exist'
            products instanceof List
            ((products as List).size() > 0) == nonZeroProducts

        where:
            categoryId | authenticated | nonZeroProducts | useCase
            17         | false         | true            | 'unauthenticated, success, existing category, products returned'
            98         | false         | false           | 'unauthenticated, success, non-existing category, zero products returned'
            17         | true          | true            | 'authenticated, success, existing category, products returned'
            99         | true          | false           | 'authenticated, success, existing category, products returned'
    }
}