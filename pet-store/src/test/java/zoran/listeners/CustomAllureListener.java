package zoran.listeners;

import io.qameta.allure.Allure;
import io.qameta.allure.listener.TestLifecycleListener;
import io.qameta.allure.model.TestResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;

public class CustomAllureListener implements TestLifecycleListener {
    @Override
    public void beforeTestStart(TestResult result) {
        String className = "";
        String methodName = "";
        
        // Get test source information
        if (result.getLinks().stream().anyMatch(link -> "test".equals(link.getType()))) {
            // For Cucumber tests
            String name = result.getName();
            String[] parts = name.split("\\.");
            if (parts.length >= 2) {
                className = parts[0];
                methodName = parts[1];
            }
        } else {
            // For regular JUnit tests
            className = result.getFullName();
            methodName = result.getName();
        }
        
        // Add labels to the test
        Allure.label("feature", className);
        Allure.label("story", methodName);
        Allure.label("layer", "api");
        Allure.label("thread", String.valueOf(Thread.currentThread().getId()));
    }
}