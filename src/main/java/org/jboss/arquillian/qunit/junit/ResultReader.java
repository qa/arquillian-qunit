package org.jboss.arquillian.qunit.junit;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.spi.annotations.Root;
import org.jboss.arquillian.graphene.spi.javascript.JavaScript;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

@RunWith(Arquillian.class)
@RunAsClient
public class ResultReader {

    private Logger log = Logger.getLogger(ResultReader.class.getSimpleName());

    public static TestSuite suite;
    public static RunNotifier notifier;
    private DoneFunctionIterator doneFunctionIterator;

    @Deployment
    public static WebArchive deployment() {
        return Packager.scan(false);
    }

    @ArquillianResource
    private URL contextPath;

    @Drone
    WebDriver driver;

    @ArquillianResource
    JavascriptExecutor executor;

    private By testLocator(int i) {
        return By.cssSelector("#qunit-tests > li:nth-child(" + i + ") ~ li");
    }

    private By byTestName = By.cssSelector("span.test-name");
    private By byModuleName = By.cssSelector("span.module-name");
    private By byFailed = By.cssSelector("#qunit-testresult .failed");

    private Integer totalTests = null;

    private JavaScript script = JavaScript.fromResource("read-results.js");

    private boolean finished(int testNumber) {
        if (driver.findElements(byFailed).isEmpty()) {
            return false;
        }

        if (totalTests == null) {
            totalTests = driver.findElements(By.cssSelector("#qunit-tests > li")).size();
        }

        return totalTests <= testNumber;
    }

    @org.junit.Test
    public void test() throws IOException {

        try {

            driver.get(contextPath.toExternalForm() + "test/index.html");

            final TestFile testFile = suite.getFiles().iterator().next();

            UniqueName uniqueTestName = new UniqueName();
            doneFunctionIterator = new DoneFunctionIterator(testFile);

            int testsFound = 0;

            while (!finished(testsFound)) {

                List<List<String>> result = (List<List<String>>) (executor.executeScript(script.getSourceCode(), testsFound));

                for (List<String> testResult : result) {

                    // collect test result data
                    String testName = testResult.get(0);
                    String moduleName = testResult.get(1);
                    boolean failed = testResult.get(2).contains("fail");

                    testsFound += 1;

                    // modification of test result data
                    testName = uniqueTestName.getName(moduleName, testName);
                    moduleName = moduleName == null ? "null" : moduleName;

                    TestModule module = testFile.getOrAddModule(moduleName);

                    if (module != null) {
                        TestFunction function = module.getFunction(testName);
                        if (function != null) {
                            function.markDone();
                            function.setFailed(failed);
                        } else {
                            // TODO
                            System.err.println("function with name " + testName + " not found");
                        }
                    } else {
                        // TODO
                        System.err.println("module with name " + moduleName + " not found");
                    }

                    reportDone();
                }

            }

            reportDone();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reportDone() {
        while (doneFunctionIterator.hasNext()) {

            TestFunction reportFunction = doneFunctionIterator.next();

            notifier.fireTestStarted(reportFunction.getDescription());

            if (reportFunction.isFailed()) {
                notifier.fireTestFailure(new Failure(reportFunction.getDescription(), new Exception("failed")));
            } else {
                notifier.fireTestFinished(reportFunction.getDescription());
            }

        }
    }

    private class Test {

        @Root
        private WebElement root;

        @FindBy(css = "span.test-name")
        private WebElement nameElement;

        @FindBy(css = "span.module-name")
        private WebElement moduleElement;

        public String getName() {
            return nameElement.getText();
        }

        public String getModule() {
            try {
                return moduleElement.getText();
            } catch (NoSuchElementException e) {
                return null;
            }
        }

        public boolean isFailed() {
            return root.getAttribute("class").equals("fail");
        }
    }
}
