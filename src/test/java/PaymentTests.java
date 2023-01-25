import Helpers.TestStatus;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentTests {
    WebDriver driver;
    WebDriverWait wait;
    By checkoutButton = By.cssSelector(".checkout-button");
    By productPageAddToCartButton = By.cssSelector("button[name='add-to-cart']");
    By productPageViewCartButton = By.cssSelector(".woocommerce-message>.button");
    By countryCodeArrow = By.cssSelector(".select2-selection__arrow");
    By firstNameField = By.cssSelector("#billing_first_name");
    By lastNameField = By.cssSelector("#billing_last_name");
    By addressField = By.cssSelector("#billing_address_1");
    By postalCodeField = By.cssSelector("#billing_postcode");
    By cityField = By.cssSelector("#billing_city");
    By phoneField = By.cssSelector("#billing_phone");
    By emailField = By.cssSelector("#billing_email");
    By loadingIcon = By.cssSelector(".blockOverlay");
    By cardNumberFrame = By.cssSelector("#stripe-card-element iframe");
    By cardNumberField = By.cssSelector("[name='cardnumber']");
    By expirationDateFrame = By.cssSelector("#stripe-exp-element iframe");
    By expirationDateField = By.cssSelector("[name='exp-date']");
    By cvcFrame = By.cssSelector("#stripe-cvc-element iframe");
    By cvcField = By.cssSelector("[name='cvc']");
    By createNewAccountCheckbox = By.cssSelector("#createaccount");
    By password = By.cssSelector("#account_password");
    By summaryDate = By.cssSelector(".date>strong");
    By summaryPrice = By.cssSelector(".total .amount");
    By summaryPaymentMethod = By.cssSelector(".method>strong");
    By summaryProductRows = By.cssSelector("tbody>tr");
    By summaryProductQuantity = By.cssSelector(".product-quantity");
    By summaryProductName = By.cssSelector(".product-name>a");
    By summaryOrderNumber = By.cssSelector(".order>strong");

    By orderButton = By.cssSelector("#place_order");
    By paymentMethod = By.cssSelector("[for='payment_method_stripe']");

    @RegisterExtension
    TestStatus status = new TestStatus();


    @BeforeEach
    public void testSetUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        wait = new WebDriverWait(driver, 5);

        driver.manage().window().setSize(new Dimension(1920, 900));
        driver.manage().window().setPosition(new Point(8, 30));

        driver.navigate().to("http://zelektronika.store");
        driver.findElement(By.cssSelector(".woocommerce-store-notice__dismiss-link")).click();
    }

    @Test
    public void buyWithoutAccountTest() {
        addProductAndViewCart("http://zelektronika.store/product/hoodie-with-logo/");
        driver.findElement(checkoutButton).click();
        fillOutCheckoutForm("dmolewski@gmail.com");
        fillOutCardData(true);
        orderAndWaitToComplete();
        int numberOfOrderReceivedMessages = driver.findElements(By.cssSelector(".woocommerce-thankyou-order-received")).size();
        int expectedNumberOfMessages = 1;
        assertTrue(expectedNumberOfMessages == numberOfOrderReceivedMessages,
                "Number of 'order received' messages is not 1. Was the payment successful?");
    }

    @Test
    public void buyWithNewAccountTest() {
        addProductAndViewCart("http://zelektronika.store/product/hoodie-with-logo/");
        driver.findElement(checkoutButton).click();
        fillOutCheckoutForm("test@testelka.pl");
        fillOutCardData(true);
        driver.findElement(createNewAccountCheckbox).click();
        wait.until(ExpectedConditions.elementToBeClickable(password)).sendKeys("zupełnieprzypadkowehasł0");
        orderAndWaitToComplete();
        goToMyAccountOrders();
        int actualNumberOfOrders = driver.findElements(By.cssSelector("tr.order")).size();
        int expectedNumberOfOrders = 1;

        assertEquals(expectedNumberOfOrders, actualNumberOfOrders,
                "Number of orders in my account is not correct. Expected: " + expectedNumberOfOrders +
                        " but was: " + actualNumberOfOrders);

        //deleting user after test
        driver.findElement(By.cssSelector(".woocommerce-MyAccount-navigation-link--")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".delete-me"))).click();
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();
    }

    @Test
    public void orderSummaryTest() {
        addProductAndViewCart("http://zelektronika.store/product/hoodie-with-logo/");
        driver.findElement(checkoutButton).click();

        fillOutCheckoutForm("dmolewski+2@gmail.com");
        fillOutCardData(true);

        int orderNumber = Integer.parseInt(orderAndWaitToComplete());

        String dateFromSummary = driver.findElement(summaryDate).getText();
        String currentDate = getCurrentDate();
        String actualPrice = driver.findElement(summaryPrice).getText();
        String expectedPrice = "45,00 zł";
        String actualPaymentMethod = driver.findElement(summaryPaymentMethod).getText();
        String expectedPaymentMethod = "Karta płatnicza (Stripe)";
        int actualNumberOfProducts = driver.findElements(summaryProductRows).size();
        int expectedNumberOfProducts = 1;
        String actualProductQuantity = driver.findElement(summaryProductQuantity).getText();
        String expectedProductQuantity = "× 1";
        String actualProductName = driver.findElement(summaryProductName).getText();
        String expectedProductName = "Bluza";

        assertAll(
                () -> assertTrue(orderNumber > 0, "Order number is not bigger than 0"),
                () -> assertEquals(currentDate, dateFromSummary,
                        "Date on the summary is not correct. Expected: " +
                                currentDate + " but was: " + dateFromSummary),
                () -> assertEquals(expectedPrice, actualPrice,
                        "Price in summary is not correct. Expected: " + expectedPrice +
                                " but was: " + actualPrice),
                () -> assertEquals(expectedPaymentMethod, actualPaymentMethod,
                        "Payment method in summary is not correct. Expected: " + expectedPaymentMethod +
                                " but was: " + actualPaymentMethod),
                () -> assertEquals(expectedNumberOfProducts, actualNumberOfProducts,
                        "Number of products in summary is not correct. Expected: " + expectedNumberOfProducts +
                                " but was: " + actualNumberOfProducts),
                () -> assertEquals(expectedProductQuantity, actualProductQuantity,
                        "Product quantity in summary is not correct. Expected: " + expectedProductQuantity +
                                " but was: " + actualProductQuantity),
                () -> assertEquals(expectedProductName, actualProductName,
                        "Product name is not correct. Expected: " + expectedProductName +
                                " but was: " + actualProductName)
        );
    }

    @Test
    public void buyWithExistingAccountTest() {
        By wrappedLoginView = By.cssSelector(".login[style='display: none;']");
        By usernameField = By.cssSelector("#username");
        By passwordField = By.cssSelector("#password");
        By loginButton = By.cssSelector("[name='login']");
        String username = "dmolewski+konto@gmail.com";
        String password = "hasłodokonta";
        By expandLoginForm = By.cssSelector(".showlogin");

        addProductAndViewCart("http://zelektronika.store/product/hoodie-with-logo/");
        driver.findElement(checkoutButton).click();

        wait.until(ExpectedConditions.elementToBeClickable(expandLoginForm)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(wrappedLoginView));
        wait.until(ExpectedConditions.elementToBeClickable(usernameField)).sendKeys(username);
        wait.until(ExpectedConditions.elementToBeClickable(passwordField)).sendKeys(password);
        driver.findElement(loginButton).click();

        fillOutCardData(true);
        String orderNumber = orderAndWaitToComplete();
        goToMyAccountOrders();

        int numberOfOrdersWithGivenNumber = driver.findElements(By.xpath("//a[contains(text(), '#" + orderNumber + "')]")).size();
        assertTrue(numberOfOrdersWithGivenNumber == 1,
                "Expected one order with a given number (" + orderNumber + ") but found " + numberOfOrdersWithGivenNumber + " orders.");
    }

    @Test
    public void obligatoryFieldsValidationMessageTest() {
        addProductAndViewCart("http://zelektronika.store/product/hoodie-with-logo/");
        driver.findElement(checkoutButton).click();
        fillOutCardData(false);
        String errorMessage = orderAndWaitForErrorMessage();
        assertAll(
                () -> assertTrue(errorMessage.contains("Imię płatnika jest wymaganym polem."),
                        "Error message doesn't contain lack of first name error."),
                () -> assertTrue(errorMessage.contains("Nazwisko płatnika jest wymaganym polem."),
                        "Error message doesn't contain lack of last name error."),
                () -> assertTrue(errorMessage.contains("Ulica płatnika jest wymaganym polem."),
                        "Error message doesn't contain lack of street name error."),
                () -> assertTrue(errorMessage.contains("Miasto płatnika jest wymaganym polem."),
                        "Error message doesn't contain lack of city name error."),
                () -> assertTrue(errorMessage.contains("Telefon płatnika jest wymaganym polem."),
                        "Error message doesn't contain lack of phone number error."),
                () -> assertTrue(errorMessage.contains("Adres email płatnika jest wymaganym polem."),
                        "Error message doesn't contain lack of email address error."),
                () -> assertTrue(errorMessage.contains("Kod pocztowy płatnika nie jest prawidłowym kodem pocztowym."),
                        "Error message doesn't contain lack of postal code error."),
                () -> assertTrue(errorMessage.contains("Proszę przeczytać i zaakceptować regulamin sklepu aby móc sfinalizować zamówienie"),
                        "Error message doesn't contain lack of terms acceptance error.")
        );
    }

    @Test
    public void phoneWrongFormatTest() {
        addProductAndViewCart("http://zelektronika.store/product/hoodie-with-logo/");
        driver.findElement(checkoutButton).click();
        fillOutCheckoutForm("dmolewski+2@gmail.com", "phone number");
        fillOutCardData(true);
        String errorMessage = orderAndWaitForErrorMessage();
        String expectedErrorMessage = "Numer telefonu płatnika nie jest poprawnym numerem telefonu..";
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", errorMessage);
        assertEquals(expectedErrorMessage, errorMessage, "Error message was not correct. Expected: " + expectedErrorMessage + " but was: " + errorMessage);


    }

    @AfterEach
    public void closeDriver(TestInfo info) throws IOException {
        if (status.isFailed) {
            System.out.println("Test screenshot is available at: " + takeScreenshot(info));
        }
        driver.quit();
    }

    private String takeScreenshot(TestInfo info) throws IOException {
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        LocalDateTime timeNow = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH-mm-ss");
        String path = "src/main/resources/screenshots/" + info.getDisplayName() + " " + formatter.format(timeNow) + ".png";
        FileHandler.copy(screenshot, new File(path));
        return path;
    }

    private void addProductAndViewCart(String productPageUrl) {
        addProductToCart(productPageUrl);
        viewCart();
    }

    private void addProductToCart() {
        WebElement addToCartButton = driver.findElement(productPageAddToCartButton);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", addToCartButton);
        addToCartButton.click();
        wait.until(ExpectedConditions.elementToBeClickable(productPageViewCartButton));
    }

    private void addProductToCart(String productPageUrl) {
        driver.navigate().to(productPageUrl);
        addProductToCart();
    }

    private void viewCart() {
        wait.until(ExpectedConditions.elementToBeClickable(productPageViewCartButton)).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".shop_table")));
    }

    private void slowType(WebElement element, String text) {
        for (int i = 0; i < text.length(); i++) {
            element.sendKeys(Character.toString(text.charAt(i)));
        }
    }

    private WebElement findElementInFrame(By frameLocator, By elementLocator) {
        driver.switchTo().defaultContent();
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frameLocator));
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(elementLocator));
        return element;
    }

    private String getCurrentDate() {
        Calendar date = Calendar.getInstance();
        String fullDate = getPolishMonth(date.get(Calendar.MONTH)) + " " +
                date.get(Calendar.DAY_OF_MONTH) + ", " + date.get(Calendar.YEAR);
        return fullDate;
    }

    private String orderAndWaitToComplete() {
        driver.findElement(orderButton).click();
        WebDriverWait wait = new WebDriverWait(driver, 20);
        wait.until(ExpectedConditions.urlContains("/checkout/order-received/"));
        return wait.until(ExpectedConditions.presenceOfElementLocated(summaryOrderNumber)).getText();
    }

    private String orderAndWaitForErrorMessage() {
        driver.findElement(orderButton).click();
        By errorList = By.cssSelector("ul.woocommerce-error");
        return wait.until(ExpectedConditions.presenceOfElementLocated(errorList)).getText();
    }

    private String getPolishMonth(int numberOfMonth) {
        String[] monthNames = {"Styczeń", "Luty", "Marzec", "Kwiecień", "Maj", "Czerwiec",
                "Lipiec", "Sierpień", "Wrzesień", "Październik", "Listopad", "Grudzień"};
        return monthNames[numberOfMonth];
    }

    private void goToMyAccountOrders() {
        driver.findElement(By.cssSelector("#menu-menu>.my-account")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".woocommerce-MyAccount-navigation-link--orders"))).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".woocommerce-MyAccount-orders")));
    }

    private void fillOutCheckoutForm(String email) {
        fillOutCheckoutForm(email, "578811371");
    }

    private void fillOutCheckoutForm(String email, String phone) {
        wait.until(ExpectedConditions.elementToBeClickable(firstNameField)).sendKeys("Helena");
        wait.until(ExpectedConditions.elementToBeClickable(lastNameField)).sendKeys("Mazur");
        wait.until(ExpectedConditions.elementToBeClickable(countryCodeArrow)).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("li[id*='-PL']"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(addressField)).click();
        wait.until(ExpectedConditions.elementToBeClickable(addressField)).sendKeys("Diamentowa 145");
        wait.until(ExpectedConditions.elementToBeClickable(postalCodeField)).sendKeys("71-232");
        wait.until(ExpectedConditions.elementToBeClickable(cityField)).sendKeys("Szczecin");
        wait.until(ExpectedConditions.elementToBeClickable(phoneField)).sendKeys(phone);
        wait.until(ExpectedConditions.elementToBeClickable(emailField)).sendKeys(email);
    }

    private void fillOutCardData(boolean acceptTerms) {
        driver.findElement(paymentMethod).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(loadingIcon));
        WebElement cardNumberElement = findElementInFrame(cardNumberFrame, cardNumberField);

        slowType(cardNumberElement, "4242424242424242");
        WebElement expirationDateElement = findElementInFrame(expirationDateFrame, expirationDateField);
        slowType(expirationDateElement, "0530");
        WebElement cvcElement = findElementInFrame(cvcFrame, cvcField);
        slowType(cvcElement, "456");
        driver.switchTo().defaultContent();
        if (acceptTerms) {
            driver.findElement(By.cssSelector("#terms")).click();
        }
    }
}
