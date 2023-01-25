import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PaymentMethodsTests extends BaseTest {
    String webSite = "http://zelektronika.store";
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

    By secureAuthorizeFrame = By.xpath(".//iframe[contains(@src, 'authorize-with-url-inner')]");
    By secondStepSecure = By.cssSelector(".AuthorizeWithUrlApp-content");
    By authorizeButton = By.cssSelector("button#test-source-authorize-3ds");
    By failButton = By.cssSelector("button#test-source-fail-3ds");

    By orderButton = By.cssSelector("#place_order");

    By paymentMethod = By.cssSelector("[for='payment_method_stripe']");

    @Test
    public void ordinarySuccessfulPaymentTest() {
        addProductAndViewCart("http://zelektronika.store/product/hoodie-with-logo/");
        driver.findElement(checkoutButton).click();

        fillOutCheckoutForm();
        fillOutCardData("378282246310005", "0226", "456");
        driver.findElement(orderButton).click();

        waitForOrderToComplete();

        int numberOfOrderReceivedMessages = driver.findElements(By.cssSelector(".woocommerce-thankyou-order-received")).size();
        int expectedNumberOfMessages = 1;
        assertTrue(expectedNumberOfMessages == numberOfOrderReceivedMessages, "Number of 'order received' messages is not 1. Was the payment successful?");
    }

    @Test
    public void secureSuccessfulPaymentTest() {
        addProductAndViewCart(webSite + "/product/hoodie-with-logo/");
        driver.findElement(checkoutButton).click();

        fillOutCheckoutForm();
        fillOutCardData("4000000000003220", "0226", "456");
        driver.findElement(orderButton).click();

        switchToFrame(secureAuthorizeFrame);
        switchToFrame(secondStepSecure);

        wait.until(ExpectedConditions.elementToBeClickable(authorizeButton)).submit();
        driver.switchTo().defaultContent();

        waitForOrderToComplete();

        int numberOfOrderReceivedMessages = driver.findElements(By.cssSelector(".woocommerce-thankyou-order-received")).size();
        int expectedNumberOfMessages = 1;
        assertTrue(expectedNumberOfMessages == numberOfOrderReceivedMessages, "Number of 'order received' messages is not 1. Was the payment successful?");
    }

    @Test
    public void secureUnsuccessfulPaymentTest() {
        addProductAndViewCart("http://zelektronika.store/product/hoodie-with-logo/");
        driver.findElement(checkoutButton).click();

        fillOutCheckoutForm();
        fillOutCardData("4000000000003220", "0226", "456");
        driver.findElement(orderButton).click();

        switchToFrame(secureAuthorizeFrame);
        switchToFrame(secondStepSecure);

        wait.until(ExpectedConditions.elementToBeClickable(failButton)).submit();
        driver.switchTo().defaultContent();

        String errorMessage = waitForErrorMessage();
        String unsuccessfulPaymentErrorMessage = "Nie można przetworzyć tej płatności, spróbuj ponownie lub użyj alternatywnej metody.";
        assertEquals(unsuccessfulPaymentErrorMessage, errorMessage, "Error message about unsuccesful payment has not been found.");
    }

    @Test
    public void cardDeclinedTest() {
        addProductAndViewCart("http://zelektronika.store/product/hoodie-with-logo/");
        driver.findElement(checkoutButton).click();
        fillOutCheckoutForm();

        fillOutCardData("4000008400001629", "0226", "456");
        driver.findElement(orderButton).click();

        switchToFrame(secureAuthorizeFrame);
        switchToFrame(secondStepSecure);
        wait.until(ExpectedConditions.elementToBeClickable(authorizeButton)).submit();
        driver.switchTo().defaultContent();

        String errorMessage = waitForErrorMessage();
        String cardDeclinedErrorMessage = "Karta została odrzucona.";
        assertEquals(cardDeclinedErrorMessage, errorMessage, "Error message about declined card has not been found. Was the card declined?");
    }

    @Test
    public void incorrectCardNumberValidationTest() {
        addProductAndViewCart("http://zelektronika.store/product/hoodie-with-logo/");
        driver.findElement(checkoutButton).click();
        fillOutCardData("4000000000003221", "0226", "456");
        driver.findElement(orderButton).click();
        String wrongCardNumberErrorMessage = waitForErrorMessage();
        String expectedErrorMessage = "Numer karty nie jest prawidłowym numerem karty kredytowej.";
        assertEquals(expectedErrorMessage, wrongCardNumberErrorMessage, "Error message about wrong card number has not been found.");
    }

    @Test
    public void incompleteCardNumberValidationTest() {
        addProductAndViewCart("http://zelektronika.store/product/hoodie-with-logo/");
        driver.findElement(checkoutButton).click();
        fillOutCardData("", "0226", "456");
        driver.findElement(orderButton).click();
        String incompleteCardNumberErrorMessage = waitForErrorMessage();
        String expectedErrorMessage = "Numer karty jest niekompletnyl.";
        assertEquals(expectedErrorMessage, incompleteCardNumberErrorMessage, "Error message about incomplete card number has not been found.");
    }

    @Test
    public void incompleteExpirationDateValidationTest() {
        addProductAndViewCart("http://zelektronika.store/product/hoodie-with-logo/");
        driver.findElement(checkoutButton).click();
        fillOutCardData("4000000000003220", "", "456");
        driver.findElement(orderButton).click();
        String incompleteExpirationDateErrorMessage = waitForErrorMessage();
        String expectedErrorMessage = "Data ważności karty jest niekompletna.";
        assertEquals(expectedErrorMessage, incompleteExpirationDateErrorMessage, "Error message about incomplete expiration date has not been found.");
    }

    @Test
    public void cardExpiredValidationTest() {
        addProductAndViewCart("http://zelektronika.store/product/hoodie-with-logo/");
        driver.findElement(checkoutButton).click();
        fillOutCardData("4000000000003220", "10/18", "456");
        driver.findElement(orderButton).click();
        String cardExpiredErrorMessage = waitForErrorMessage();
        String expectedErrorMessage = "Rok ważności karty upłynął w przeszłości";
        assertEquals(expectedErrorMessage, cardExpiredErrorMessage, "Error message about expired card has not been found.");
    }

    @Test
    public void incompleteCvcNumberValidationTest() {
        addProductAndViewCart("http://zelektronika.store/product/hoodie-with-logo/");
        driver.findElement(checkoutButton).click();
        fillOutCardData("4000000000003220", "10/30", "");
        driver.findElement(orderButton).click();
        String incompleteCvcNumberErrorMessage = waitForErrorMessage();
        String expectedErrorMessage = "Kod bezpieczeństwa karty jest niekompletny.";
        assertEquals(expectedErrorMessage, incompleteCvcNumberErrorMessage, "Error message about incomplete cvc number has not been found.");
    }

    private void switchToFrame(By frameLocator) {
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frameLocator));
        wait.until(d -> ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete"));
    }

    private void addProductAndViewCart(String productPageUrl) {
        addProductToCart(productPageUrl);
        viewCart();
    }

    private void viewCart() {
        wait.until(ExpectedConditions.elementToBeClickable(productPageViewCartButton)).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".shop_table")));
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

    private void fillOutCheckoutForm() {
        wait.until(ExpectedConditions.elementToBeClickable(firstNameField)).sendKeys("Helena");
        wait.until(ExpectedConditions.elementToBeClickable(lastNameField)).sendKeys("Mazur");
        wait.until(ExpectedConditions.elementToBeClickable(countryCodeArrow)).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("li[id*='-PL']"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(addressField)).click();
        wait.until(ExpectedConditions.elementToBeClickable(addressField)).sendKeys("Diamentowa 145");
        wait.until(ExpectedConditions.elementToBeClickable(postalCodeField)).sendKeys("71-232");
        wait.until(ExpectedConditions.elementToBeClickable(cityField)).sendKeys("Szczecin");
        wait.until(ExpectedConditions.elementToBeClickable(phoneField)).sendKeys("66 758 13 50");
        wait.until(ExpectedConditions.elementToBeClickable(emailField)).sendKeys("dmolewski@gmail.com");
    }

    private void waitForOrderToComplete() {
        WebDriverWait wait = new WebDriverWait(driver, 20);
        wait.until(ExpectedConditions.urlContains("/checkout/order-received/"));
    }

    private String waitForErrorMessage() {
        By errorList = By.cssSelector("ul.woocommerce-error");
        wait.until(d -> ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete"));
        return wait.until(ExpectedConditions.presenceOfElementLocated(errorList)).getText();
    }

    private void fillOutCardData(String cardNumber, String expirationDate, String cvc) {
        driver.findElement(paymentMethod).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(loadingIcon));

        switchToFrame(cardNumberFrame);
        WebElement cardNumberElement = wait.until(ExpectedConditions.elementToBeClickable(cardNumberField));

        slowType(cardNumberElement, cardNumber);
        driver.switchTo().defaultContent();
        switchToFrame(expirationDateFrame);
        WebElement expirationDateElement = wait.until(ExpectedConditions.elementToBeClickable(expirationDateField));
        slowType(expirationDateElement, expirationDate);
        driver.switchTo().defaultContent();
        switchToFrame(cvcFrame);
        WebElement cvcElement = wait.until(ExpectedConditions.elementToBeClickable(cvcField));
        slowType(cvcElement, cvc);
        driver.switchTo().defaultContent();

        driver.findElement(By.cssSelector("#terms")).click();
    }

    private void slowType(WebElement element, String text) {
        for (int i = 0; i < text.length(); i++) {
            element.sendKeys(Character.toString(text.charAt(i)));
        }
    }
}