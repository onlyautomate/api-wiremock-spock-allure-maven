package config

import support.myspock.SpecAnalyzer
import java.util.concurrent.atomic.AtomicInteger

class TestExecutionConfig {
    static List<SpecAnalyzer.SpecLevelCountDetails> specLevelCountDetailsList = []
    public static AtomicInteger specCounter = new AtomicInteger(1)
    public static final Boolean LOCAL_RUN = !System.getProperty("skipTests")
}