import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.Allure;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;

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

    @BeforeTest
    public void testSetUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        wait = new WebDriverWait(driver, 7);

        //driver.manage().window().setSize(new Dimension(1400, 800));
        driver.manage().window().maximize();
        //driver.manage().window().setPosition(new Point(0, 0));
    }

    @BeforeMethod
    public void clearCacheAndDismissNotice() {
        driver.manage().deleteAllCookies();
        driver.navigate().to("http://zelektronika.store");

        driver.findElement(By.cssSelector(".woocommerce-store-notice__dismiss-link")).click();
    }

    @AfterClass
    public void closeBrowser() {
        driver.quit();
    }

    public String takeScreenshot(WebDriver driver, String testMethodName) {
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        LocalDateTime timeNow = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH-mm-ss");
        File screenshotsDirectory = new File("screenshots/");
        if (!screenshotsDirectory.exists()) {
            screenshotsDirectory.mkdirs();
        }
        String path = "screenshots/" + testMethodName + " " + formatter.format(timeNow) + ".png";
        try {
            FileHandler.copy(screenshot, new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        addScreenshotToReport(path);
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

    public static void saveLink(WebDriver driver) {
        Allure.addAttachment("Link do strony: ", driver.getCurrentUrl());
    }
}