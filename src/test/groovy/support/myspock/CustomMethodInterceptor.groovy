package support.myspock

import io.qameta.allure.Allure
import io.qameta.allure.model.Label
import io.qameta.allure.model.TestResult
import org.spockframework.runtime.extension.AbstractMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import support.logs.CustomLog

class CustomMethodInterceptor extends AbstractMethodInterceptor {

    @Override
    void interceptCleanupMethod(IMethodInvocation invocation) throws Throwable {
        CustomLog.attachWireMockLogsToAllure() //attach wiremock logs into allure report
        CustomLog.attachLogsToAllure('feature-logs') //attach spock spec logs into allure report
        invocation.proceed() //continue
    }

    @Override
    void interceptIterationExecution(IMethodInvocation invocation) throws Throwable {
        def expSpecParentSuiteName = invocation.spec.package
        Allure.lifecycle.updateTestCase { TestResult testResult ->
            def parentSuiteUpdateNeeded = testResult.getLabels().removeIf { it.name == 'parentSuite' && it.value != expSpecParentSuiteName }
            if(parentSuiteUpdateNeeded) {
                testResult.labels.add(new Label().setName('parentSuite').setValue(expSpecParentSuiteName)) //meaningful parent suite name instead of BaseSpecification
            }
            testResult
        }
        invocation.proceed() //continue
    }
}