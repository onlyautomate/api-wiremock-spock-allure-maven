package apitests.demostorespecs

import apis.StubbedDemoStoreApi
import base.BaseSpecification
import com.github.tomakehurst.wiremock.WireMockServer
import groovy.json.JsonBuilder
import io.restassured.path.json.JsonPath
import org.apache.commons.lang3.RandomStringUtils
import spock.lang.Shared

class UpdateProductSpec extends BaseSpecification {

    @Shared WireMockServer wireMockServer
    @Shared String authToken

    def "must need authorization to get product details"() {
        given: 'unauthenticated user tried to get product details'
            wireMockServer = acquireWireMockServer()
            def response = new StubbedDemoStoreApi(StubbedDemoStoreApi.ApiEndpoint.GETGetProduct, wireMockServer).makeCall([33])
        expect: 'not allowed to do so'
            response.statusCode == 401
            response.body().asString() == 'Unauthorized access'
    }

    def "acquire authorization token"() {
        given: 'acquire auth token with given credentials'
            authToken = StubbedDemoStoreApi.acquireAuthenticationToken(wireMockServer)
        expect: 'token acquired'
            authToken
        true
    }

    def "update existing product id '#productId' for '#propertyToUpdate'"() {
        given: 'get existing product by id as authenticated user'
            def response = new StubbedDemoStoreApi(StubbedDemoStoreApi.ApiEndpoint.GETGetProduct, wireMockServer).tap {
                requestHeaders = [Authorization: "Bearer ${authToken}"]
            }.makeCall([productId])

        expect: 'product acquired'
            response.statusCode() == 200

        when: "update product for given property"
            def jsonPath = response.jsonPath()
            Map updatedMap = [
                    name: jsonPath.get('name'),
                    description: jsonPath.get('description'),
                    image: jsonPath.get('image'),
                    price: jsonPath.get('price'),
                    categoryId: jsonPath.get('categoryId'),
                    createdAt: jsonPath.get('createdAt')
            ]
            updatedMap[propertyToUpdate] += RandomStringUtils.randomAlphanumeric(8)

            def updatedReqBody = new JsonBuilder(updatedMap).toString()
            def stubbedDemoStoreApi = new StubbedDemoStoreApi(StubbedDemoStoreApi.ApiEndpoint.PUTUpdateProduct, wireMockServer)
            def updatedResponse = stubbedDemoStoreApi.tap {
                requestHeaders = [Authorization: "Bearer ${authToken}"]
            }.makeCall([productId], updatedReqBody)

        then: "product updated"
            updatedResponse.statusCode() == 200
            with(updatedResponse.jsonPath()) { JsonPath jp ->
                jp.get(propertyToUpdate) == updatedMap[propertyToUpdate]
            }

        where: "vary by product id and property to update"
            productId | propertyToUpdate
            33        | 'name'
            34        | 'description'
    }
}