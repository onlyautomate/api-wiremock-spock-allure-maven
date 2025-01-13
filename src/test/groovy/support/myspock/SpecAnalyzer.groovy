package support.myspock

import config.TestExecutionConfig
import org.spockframework.runtime.model.SpecInfo

class SpecAnalyzer {

    class SpecLevelCountDetails {
        String specFullName
        int featureCount

        SpecLevelCountDetails(String specFullName, int featureCount) {
            this.specFullName = specFullName
            this.featureCount = featureCount
        }
    }

    //total count of specifications and features
    static Boolean fillSpecLevelCountDetails(SpecInfo specInfo) {
        def nowAcquired = false
        def specFullName = "${specInfo.package}.${specInfo.name}"
        def matchedSpec = TestExecutionConfig.specLevelCountDetailsList.find { it.specFullName == specFullName }
        if(!matchedSpec) {
            TestExecutionConfig.specLevelCountDetailsList.add(new SpecLevelCountDetails(null, specFullName, specInfo.features.size()))
            nowAcquired = true
        }
        nowAcquired
    }
}