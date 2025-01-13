package support.mywiremock

import apis.StubbedDemoStoreApi
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformerV2
import com.github.tomakehurst.wiremock.http.ResponseDefinition
import com.github.tomakehurst.wiremock.stubbing.ServeEvent

//local scope
class TransformProductList implements ResponseDefinitionTransformerV2 {

    @Override
    ResponseDefinition transform(ServeEvent serveEvent) {
        if(!serveEvent.request.queryParameter('category').containsValue('17')) {
            def responseDefinitionBuilder = ResponseDefinitionBuilder.like(serveEvent.responseDefinition)
            responseDefinitionBuilder.withStatus(200)
            responseDefinitionBuilder.withBody("[]")
            return responseDefinitionBuilder.build()
        }

        def productList = []
        3.times {
            productList.add(StubbedDemoStoreApi.acquireFakeProduct(false))
        }
        def transformerMap = [products: productList]
        def responseDefinition = serveEvent.responseDefinition
        if(responseDefinition.transformerParameters) {
            return responseDefinition.transformerParameters.putAll(transformerMap)
        }

        def responseDefinitionBuilder = ResponseDefinitionBuilder.like(serveEvent.responseDefinition)
        responseDefinitionBuilder.withTransformerParameters(transformerMap)
        responseDefinitionBuilder.build()
    }

    @Override
    boolean applyGlobally() {
        false //local scope
    }

    @Override
    String getName() {
        'transform-product-list-template' //to be used, either in mapping or using.withTransformers() method
    }
}
