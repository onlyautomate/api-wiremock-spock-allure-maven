package apitests.demostorespecs

import apis.StubbedDemoStoreApi
import base.BaseSpecification
import com.github.tomakehurst.wiremock.WireMockServer
import groovy.json.JsonBuilder
import spock.lang.Shared

class CreateProductSpec extends BaseSpecification {

    @Shared WireMockServer wireMockServer
    @Shared def authToken
    @Shared def categoryId = 823
    @Shared def createdProduct

    def "authenticate"() {
        when: 'acquire token with relevant credentials'
            wireMockServer = acquireWireMockServer() //from BaseSpecification
            authToken = StubbedDemoStoreApi.acquireAuthenticationToken(wireMockServer)

        then: 'token acquired'
            authToken
    }

    def "get products from category without any product"() {
        given: 'category with no existing product'
            def stubbedDemoStoreApi = new StubbedDemoStoreApi(StubbedDemoStoreApi.ApiEndpoint.GETGetAllProducts, wireMockServer).tap {
                requestHeaders = [Authorization: "Bearer ${authToken}"]
                this
            }
            def response = stubbedDemoStoreApi.makeCall([category: categoryId])

        expect: 'returns an empty list of products'
            response.statusCode() == 200
            response.jsonPath().get() == []
    }

    def "add new product to category"() {
        when: 'new product is added to the said category'
            def stubbedDemoStoreApi = new StubbedDemoStoreApi(StubbedDemoStoreApi.ApiEndpoint.POSTCreateProduct, wireMockServer).tap {
                requestHeaders = [Authorization: "Bearer ${authToken}"]
                this
            }

            //modify suitable for new product creation
            def productMap = StubbedDemoStoreApi.acquireFakeProduct(true).tap {
                it.remove('id')
                it.remove('createdAt')
                it.remove('updatedAt')
                it
            }

            productMap.categoryId = categoryId
            createdProduct = stubbedDemoStoreApi.makeCall(new JsonBuilder(productMap).toString()).then().statusCode(200).extract().jsonPath().get()
        then: 'product gets added'
            createdProduct
    }

    def "category returns newly created product"() {
        when: 'get products again from the said category'
            /*
            Flow being attempted in this spec is: get products from a category with no existing products -> add product to category -> get products from category
            By setting specific scenario state, as specified in the stub mapping trasnform-create-product.json, desired stubbing is enforced
            */
            wireMockServer.setScenarioState('Category-Without-Product', 'Product-Added')
            def stubbedDemoStoreApi = new StubbedDemoStoreApi(StubbedDemoStoreApi.ApiEndpoint.GETGetAllProducts, wireMockServer).tap {
                requestHeaders = [Authorization: "Bearer ${authToken}"]
                this
            }
            def jsonPath = stubbedDemoStoreApi.makeCall([category: categoryId]).then().statusCode(200).extract().jsonPath()

        then: 'list showing created product is returned'
            jsonPath.get() == [createdProduct]
            jsonPath.get('[0].categoryId') == categoryId.toString()
    }
}