import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener extends BaseTest implements ITestListener {

    private static final Logger log = LoggerFactory.getLogger(TestListener.class);

    private static String getTestMethodName(ITestResult result) {
        return result.getMethod().getConstructorOrMethod().getName();
    }

    @Override
    public void onTestStart(ITestResult result) {
    }

    @Override
    public void onTestSuccess(ITestResult result) {
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testMethodName = getTestMethodName(result);
        Object testClass = result.getInstance();
        WebDriver driver = ((BaseTest) testClass).driver;
        if (driver != null) {
            saveLink(driver);
            log.error("Screenshot of the failed test is available at: " + takeScreenshot(driver, testMethodName));
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    }

    @Override
    public void onStart(ITestContext result) {
    }

    @Override
    public void onFinish(ITestContext result) {
    }
}
