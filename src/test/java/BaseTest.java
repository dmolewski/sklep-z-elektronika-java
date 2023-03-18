import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.Allure;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;

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

    @BeforeClass
    public void testSetUp() {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        driver = new FirefoxDriver(options);
        //options.addArguments("--headless");
        //options.addArguments("--disable-extensions");
        options.addPreference("extensions.webextensions.enabled", false);

        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        wait = new WebDriverWait(driver, 10);

        //driver.manage().window().setSize(new Dimension(1400, 800));
        driver.manage().window().maximize();
        //driver.manage().window().setPosition(new Point(0, 0));
        driver.navigate().to("http://zelektronika.store");
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