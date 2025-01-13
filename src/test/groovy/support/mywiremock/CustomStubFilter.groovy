package support.mywiremock

import com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilterAction
import com.github.tomakehurst.wiremock.extension.requestfilter.StubRequestFilterV2
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.stubbing.ServeEvent

//global scope, by default
class CustomStubFilter implements StubRequestFilterV2 {

    @Override
    RequestFilterAction filter(Request request, ServeEvent serveEvent) {
        //override, if needed (e.g. fail a non-authorized request with RequestFilterAction.stopWith())  else continue
        RequestFilterAction.continueWith(request)
    }

    @Override
    String getName() {
        'custom-wiremock-filter'
    }
}