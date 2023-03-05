import Helpers.TestStatus;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class BaseTest {
    protected WebDriver driver;
    protected WebDriverWait wait;

    @RegisterExtension
    TestStatus status = new TestStatus();

    @BeforeEach
    public void testSetUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        wait = new WebDriverWait(driver, 7);

        //driver.manage().window().setSize(new Dimension(1400, 800));
        driver.manage().window().maximize();
        //driver.manage().window().setPosition(new Point(0, 0));

        driver.navigate().to("http://zelektronika.store");
        driver.findElement(By.cssSelector(".woocommerce-store-notice__dismiss-link")).click();
    }

    @AfterEach
    public void closeDriver(TestInfo info) {
        if (status.isFailed) {
            String path = takeScreenshot(info);
            System.out.println("Screenshot of the failed test is available at: " + path);
            addScreenshotToReport(path);
        }
        driver.quit();
    }

    private String takeScreenshot(TestInfo info) {
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        LocalDateTime timeNow = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH-mm-ss");
        File screenshotsDirectory = new File("screenshots/");

        if (!screenshotsDirectory.exists()) {
            screenshotsDirectory.mkdirs();
        }

        String path = "screenshots/" + info.getDisplayName() + " - " + formatter.format(timeNow) + ".png";
        try {
            FileHandler.copy(screenshot, new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    private void addScreenshotToReport(String path) {
        InputStream stream = null;
        try {
            stream = Files.newInputStream(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Allure.addAttachment("Screenshot", stream);
    }
}