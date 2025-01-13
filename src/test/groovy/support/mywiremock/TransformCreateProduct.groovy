package support.mywiremock

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformerV2
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.http.ResponseDefinition
import com.github.tomakehurst.wiremock.stubbing.ServeEvent

//local-scope, intended for stubbing specific endpoint(s) only
class TransformCreateProduct implements ResponseDefinitionTransformerV2 {
    static String createdProductResponseBody //populated by serve event listener in applyComplete stage by the POST create product call

    @Override
    ResponseDefinition transform(ServeEvent serveEvent) {
        //intercept to return created product
        if(serveEvent.request.method == RequestMethod.GET && createdProductResponseBody) {
            def responseDefinitionBuilder = ResponseDefinitionBuilder.like(serveEvent.responseDefinition)
            responseDefinitionBuilder.withBody("[${createdProductResponseBody}]".toString())
            return responseDefinitionBuilder.build()
        }

        serveEvent.responseDefinition //regular proceed
    }

    @Override
    boolean applyGlobally() {
        false //local scope
    }

    @Override
    String getName() {
        'transform-create-product-template' //to be used, either in mapping or using.withTransformers() method
    }
}