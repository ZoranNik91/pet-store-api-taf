package zoran;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.*;

import static io.cucumber.junit.platform.engine.Constants.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameters({
        @ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "zoran.steps"),
        @ConfigurationParameter(key = PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, value = "true"),
        @ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, 
            value = "pretty, " +
                    "json:target/cucumber-reports/cucumber.json, " +
                    "html:target/cucumber-reports/cucumber.html, " +
                    "rerun:target/rerun.txt, " +
                    "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"),
        @ConfigurationParameter(key = JUNIT_PLATFORM_NAMING_STRATEGY_PROPERTY_NAME, value = "long"),
        @ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "not @wip")
})
public class FunctionTestRunner {
    // This class is used as a test suite for running Cucumber tests with JUnit 5
    // The @Suite annotation will take care of running the tests
    // No need for a main method when running with Maven or JUnit 5
}