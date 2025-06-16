package zoran;

import io.cucumber.junit.platform.engine.Cucumber;
import org.junit.platform.suite.api.*;

import static io.cucumber.junit.platform.engine.Constants.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameters({
        @ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "zoran.steps"),
        @ConfigurationParameter(key = PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, value = "true"),
        @ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, json:target/cucumber-reports/cucumber.json, html:target/cucumber-reports/cucumber.html, rerun:target/rerun.txt"),
        @ConfigurationParameter(key = JUNIT_PLATFORM_NAMING_STRATEGY_PROPERTY_NAME, value = "long"),
        @ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@petManagement")
})
public class FunctionTestRunner {
    
    // Main method to allow running from IntelliJ
    public static void main(String[] args) {
        // This will be called when running from IntelliJ
        // The actual test execution is handled by JUnit 5 and Cucumber
        // The @Suite annotation will take care of running the tests
    }
}