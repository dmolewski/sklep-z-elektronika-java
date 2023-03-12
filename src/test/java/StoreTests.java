import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Listeners({TestListener.class})
public class StoreTests extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(TestListener.class);

    SoftAssert softAssert;

    private static final String storeURL = "http://zelektronika.store/";
    private static final String username = "dmolewski+sklep";
    private static final String password = "testowekontoztestowymhaslem";
    private static final String userFullName = "dmolewskisklep";

    public static int randomNumber = (int) (Math.random() * 100) + 1;

    String productId = "192";
    By productPageAddToCartButton = By.cssSelector("button[name='add-to-cart']");
    By removeProductButton = By.cssSelector("a[data-product_id='" + productId + "']");
    By productPageViewCartButton = By.cssSelector(".woocommerce-message>.button");
    By shopTable = By.cssSelector(".shop_table");

    By checkoutButton = By.cssSelector(".checkout-button");

    By firstNameField = By.cssSelector("#billing_first_name");
    By lastNameField = By.cssSelector("#billing_last_name");
    By countryCodeArrow = By.cssSelector(".select2-selection__arrow");
    By addressField = By.cssSelector("#billing_address_1");
    By postalCodeField = By.cssSelector("#billing_postcode");
    By cityField = By.cssSelector("#billing_city");
    By phoneField = By.cssSelector("#billing_phone");
    By emailField = By.cssSelector("#billing_email");
    By paymentMethod = By.cssSelector("label[for='payment_method_stripe']");
    By shippingMethod = By.cssSelector("label[for='shipping_method_0_flat_rate2']");
    By loadingIcon = By.cssSelector(".blockOverlay");
    By cardNumberFrame = By.cssSelector("#stripe-card-element iframe");
    By cardNumberField = By.cssSelector("[name='cardnumber']");
    By expirationDateFrame = By.cssSelector("#stripe-exp-element iframe");
    By expirationDateField = By.cssSelector("[name='exp-date']");
    By cvcFrame = By.cssSelector("#stripe-cvc-element iframe");
    By cvcField = By.cssSelector("[name='cvc']");
    By orderButton = By.cssSelector("#place_order");

    By summaryDate = By.cssSelector(".date>strong");
    By summaryPrice = By.cssSelector(".total .amount");
    By summaryPaymentMethod = By.cssSelector(".method>strong");
    By summaryProductRows = By.cssSelector("tbody>tr");
    By summaryProductQuantity = By.cssSelector(".product-quantity");
    By summaryProductName = By.cssSelector(".product-name>a");

    @BeforeTest
    public void prepareTests() {
        softAssert = new SoftAssert();
    }

    @Test
    public void registerWithEmailAndPassword() {
        String email = username + randomNumber + "@gmail.com";
        register(email, password);

        String myAccountContent = getAccountMessage();
        String expectedName = userFullName + randomNumber;
        String errorMessage = String.format("Strona nie zawiera spodziewanej nazwy użytkownika: \"%s\", znaleziono: \"%s\"", expectedName, myAccountContent);
        assertTrue(myAccountContent.contains(expectedName), errorMessage);
        log.info(String.format("Oczekiwana nazwa użytkownika: \"%s\", strona zawiera nazwę użytkownika: \"%s\"", expectedName, myAccountContent));

        String ordersSelector = ".woocommerce-MyAccount-navigation-link--orders";
        String editAddressSelector = ".woocommerce-MyAccount-navigation-link--edit-address";
        String paymentMethodsSelector = ".woocommerce-MyAccount-navigation-link--payment-methods";
        assertTrue(driver.findElement(By.cssSelector(ordersSelector)).getText().contains("Zamówienia"), "Strona nie zawiera spodziewanego przycisku: Zamówienia");
        assertTrue(driver.findElement(By.cssSelector(editAddressSelector)).getText().contains("Adresy"), "Strona nie zawiera spodziewanego przycisku: Adresy");
        assertTrue(driver.findElement(By.cssSelector(paymentMethodsSelector)).getText().contains("Metody płatności"), "Strona nie zawiera spodziewanego przycisku: Metody płatności");

        goToMyAccountSubpage(editAddressSelector, "adresy");
        goToMyAccountSubpage(paymentMethodsSelector, "metod");

        deleteAccount();
    }

    @Test
    public void searchInStoreAndSortResults() {
        String productName = "komputer";
        searchForProduct(productName);
        verifySearchResults(productName);

        sortByPriceLowToHigh();
        List<Double> productPrices = getProductPrices();

        log.info("Na stronie wyników są widoczne (" + productPrices.size() + ") ceny produktów");
        log.info("Ceny produktów: " + productPrices);

        List<Double> sortedPrices = new ArrayList<>(productPrices);
        Collections.sort(sortedPrices);
        assertEquals(sortedPrices, productPrices, "Produkty nie są posortowane od najniższej ceny");
        log.info("Posortowane ceny produktów: " + sortedPrices);
    }

    @Test
    public void addToShoppingCart() {
        String[] productPages = {"/drukarka/", "/glosnik/", "/komputer/",
                //"/komputer-przenosny/", "/monitor/", "/mysz-komputerowa/", "/sluchawki/", "/tablet/"
        };

        double totalPrice = 0.00;

        for (String productPage : productPages) {
            addProductToCart("http://zelektronika.store/product" + productPage);
            totalPrice += addProductPrice();
            log.info("Aktualna wartość koszyka: " + totalPrice);
        }
        viewCart();
        double cartTotalPrice = getCartTotalPrice();
        assertEquals(cartTotalPrice, totalPrice, 0.02, "Cena produktów w koszyku (" + getCartTotalPrice() + ") nie jest równa obliczonej w teście: " + totalPrice);

        int numberOfItems = driver.findElements(By.cssSelector(".cart_item")).size();
        assertEquals(productPages.length, numberOfItems, "Ilość produktów w koszyku jest nieprawidłowa. Wymagane: " + productPages.length + ", w ramach testu obliczono: " + numberOfItems);

        addCoupon();

        double cartTotalPriceAfterCoupon = getCartTotalPriceWithCoupon();
        log.info("Cena po rabacie: " + cartTotalPriceAfterCoupon);

        double expectedDiscountedPrice = cartTotalPrice * 0.8;
        assertEquals(cartTotalPriceAfterCoupon, expectedDiscountedPrice, 0.02, String.format("Cena po uwzględnieniu rabatu nie jest równa oczekiwanej: %s =/= %s", cartTotalPriceAfterCoupon, expectedDiscountedPrice));

        removeProductFromCart();
        numberOfItems = driver.findElements(By.cssSelector(".cart_item")).size();
        assertEquals(productPages.length - 1, numberOfItems, "Ilość produktów w koszyku jest nieprawidłowa. Wymagane: " + productPages.length + ", w ramach testu obliczono: " + numberOfItems);
    }

    @Test
    public void checkoutTest() {
        addProductToCart("http://zelektronika.store/product/komputer");
        viewCart();
        driver.findElement(checkoutButton).click();
        String email = username + randomNumber + "@gmail.com";
        fillOutCheckoutForm(email, "123456789");
        fillOutCardData("4242424242424242", "0226", "456");

        checkConfirmationBox();

        int orderNumber = Integer.parseInt(orderAndWaitToComplete());

        int numberOfOrderReceivedMessages = driver.findElements(By.cssSelector(".woocommerce-thankyou-order-received")).size();
        int expectedNumberOfMessages = 1;
        assertEquals(numberOfOrderReceivedMessages, expectedNumberOfMessages, "Nieprawidłowy komunikat o otrzymaniu zamówienia, czy płatność została poprawnie przetworzona?");

        String dateFromSummary = driver.findElement(summaryDate).getText();
        String currentDate = getCurrentDate();
        String actualPrice = driver.findElement(summaryPrice).getText();
        String expectedPrice = "2008,99 zł";
        String actualPaymentMethod = driver.findElement(summaryPaymentMethod).getText();
        String expectedPaymentMethod = "Karta płatnicza (Stripe)";
        int actualNumberOfProducts = driver.findElements(summaryProductRows).size();
        int expectedNumberOfProducts = 1;
        String actualProductQuantity = driver.findElement(summaryProductQuantity).getText();
        String expectedProductQuantity = "× 1";
        String actualProductName = driver.findElement(summaryProductName).getText();
        String expectedProductName = "Komputer";

        assertAll(
                () -> assertTrue(orderNumber > 0, "Numer zamówienia nie jest większy niż 0"),
                () -> assertEquals(currentDate, dateFromSummary, "Data w podsumowaniu nieprawidłowa. Oczekiwana: " + currentDate + ", w podsumowaniu: " + dateFromSummary),
                () -> assertEquals(expectedPrice, actualPrice, "Cena w podsumowaniu nieprawidłowa. Oczekiwana: " + expectedPrice + ", w podsumowaniu: " + actualPrice),
                () -> assertEquals(expectedPaymentMethod, actualPaymentMethod, "Metoda płatności w podsumowaniu nieprawidłowa. Oczekiwana: " + expectedPaymentMethod + " w podsumowaniu: " + actualPaymentMethod),
                () -> assertEquals(expectedNumberOfProducts, actualNumberOfProducts, "Produkty w podsumowaniu nieprawidłowe. Oczekiwane: " + expectedNumberOfProducts + " w podsumowaniu: " + actualNumberOfProducts),
                () -> assertEquals(expectedProductQuantity, actualProductQuantity, "Liczba produktów w podsumowaniu nieprawidłowa. Oczekiwana: " + expectedProductQuantity + " w podsumowaniu: " + actualProductQuantity),
                () -> assertEquals(expectedProductName, actualProductName, "Nazwa produktu w podsumowaniu nieprawidłowa. Oczekiwana: " + expectedProductName + " w podsumowaniu: " + actualProductName),
                () -> log.info("Dane w podsumowaniu zamówienia są poprawne"));
    }

    @Test
    public void paymentTest() {
        addProductToCart("http://zelektronika.store/product/komputer");
        viewCart();
        driver.findElement(checkoutButton).click();
        String email = username + randomNumber + "@gmail.com";
        fillOutCheckoutForm(email, "123456789");

        fillOutCardData("4000000000000002", "0123", "456");
        checkConfirmationBox();
        driver.findElement(orderButton).click();

        String actualErrorMessage = waitForErrorMessage();
        log.info("Wyświetlony błąd: " + actualErrorMessage);
        softAssert.assertTrue(actualErrorMessage.contains("Data ważności karty już minęła."), "Błąd daty ważności karty nie został wyświetlony");

        fillOutCardData("4000000000000002", "0227", "");
        driver.findElement(orderButton).click();
        actualErrorMessage = waitForErrorMessage();
        log.info("Wyświetlony błąd: " + actualErrorMessage);
        softAssert.assertTrue(actualErrorMessage.contains("Kod bezpieczeństwa karty jest niekompletny."), "Błąd kodu CVC karty nie został wyświetlony");

        fillOutCardData("4000000000000002", "0227", "456");
        driver.findElement(orderButton).click();
        actualErrorMessage = waitForErrorMessage();
        log.info("Wyświetlony błąd: " + actualErrorMessage);
        softAssert.assertTrue(actualErrorMessage.contains("Karta została odrzucona."), "Błąd o odrzuceniu płatności nie został wyświetlony");

        fillOutCardData("4242424242424242", "0227", "456");
        orderAndWaitToComplete();

        int numberOfOrderReceivedMessages = driver.findElements(By.cssSelector(".woocommerce-thankyou-order-received")).size();
        assertEquals(numberOfOrderReceivedMessages, 1, "Nieprawidłowy komunikat o otrzymaniu zamówienia, czy płatność została poprawnie przetworzona?");

        softAssert.assertAll();
    }

    @Test
    public void orderHistoryTest() {
        addProductToCart("http://zelektronika.store/product/komputer");
        viewCart();
        driver.findElement(checkoutButton).click();

        logInDuringCheckout("dmolewskisklep", "testowekontoztestowymhaslem");
        fillOutCardData("4242424242424242", "0226", "456");

        checkConfirmationBox();
        int orderNumber = Integer.parseInt(orderAndWaitToComplete());

        goToMyAccountOrders();

        int numberOfOrdersWithGivenNumber = driver.findElements(By.xpath("//a[contains(text(), '#" + orderNumber + "')]")).size();

        assertEquals(1, numberOfOrdersWithGivenNumber, "Expected one order with a given number (" + orderNumber + ") but found " + numberOfOrdersWithGivenNumber + " orders.");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table.shop_table")));

        List<WebElement> orders = driver.findElements(By.cssSelector("table.shop_table tr.order"));
        log.info("W tabeli wyświetlono (" + orders.size() + ") ostatnich zamówień");

        for (WebElement order : orders) {
            String orderNumberTable = order.findElement(By.cssSelector("td.woocommerce-orders-table__cell.woocommerce-orders-table__cell-order-number")).getText();
            String orderDate = order.findElement(By.cssSelector("td.woocommerce-orders-table__cell.woocommerce-orders-table__cell-order-date")).getText();
            String orderTotal = order.findElement(By.cssSelector("td.woocommerce-orders-table__cell.woocommerce-orders-table__cell-order-total")).getText();

                        softAssert.assertNotNull(orderNumberTable, "Pole nr zamówienia jest puste dla zamówienia z datą: " + orderDate);
            softAssert.assertNotNull(orderDate, "Pole data jest puste dla zamówienia: " + orderNumber);
            softAssert.assertNotNull(orderTotal, "Pole kwota jest puste dla zamówienia: " + orderNumber);
        }
        log.info("Wszystkie zamówienia posiadają niepuste pole numer");
        log.info("Wszystkie zamówienia posiadają niepuste pole data");
        log.info("Wszystkie zamówienia posiadają niepuste pole z kwotą zamówienia");

        WebElement viewButton = driver.findElement(By.cssSelector("td.woocommerce-orders-table__cell.woocommerce-orders-table__cell-order-actions a.woocommerce-button.wp-element-button.button.view"));
        String href = viewButton.getAttribute("href");
        viewButton.click();

        log.info("Podsumowanie zamówienia nr " + orderNumber);
        log.info("Link do podsumowania ostatniego zamówienia: " + href);

        String[] hrefParts = href.split("/");
        String actualOrderNumber = hrefParts[hrefParts.length - 1];
        int actualOrderNumberInt = Integer.parseInt(actualOrderNumber);
        assertEquals(actualOrderNumberInt, orderNumber, "Numer zamówienia z linku z tabeli nie jest równy temu ze złożonego w tescie zamówienia");

        assertEquals(getAccountMessage(), "Zamówienie nr " + orderNumber + " złożone " + getCurrentDate() + " jest obecnie W trakcie realizacji.", "Numer zamówienia z linku z tabeli nie jest równy temu ze złożonego w tescie zamówienia");

        checkOrderDetailsSection();

        softAssert.assertAll();
    }

    private void checkOrderDetailsSection() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("section.woocommerce-order-details")));
        WebElement orderDetailsSection = driver.findElement(By.cssSelector("section.woocommerce-order-details"));

        List<WebElement> orderDetailsLabels = orderDetailsSection.findElements(By.cssSelector("th"));
        List<WebElement> orderDetailsValues = orderDetailsSection.findElements(By.cssSelector("td"));

        for (int i = 0; i < orderDetailsLabels.size(); i++) {
            String label = orderDetailsLabels.get(i).getText().trim();

            String value = orderDetailsValues.get(i).getText().trim();
            log.info(label + " " + value);
            softAssert.assertNotNull(value, "Wartość pola: \"" + label + "\" jest pusta");
        }

        WebElement billingAddressSection = driver.findElement(By.cssSelector("div.woocommerce-column--billing-address"));
        WebElement billingAddressElement = billingAddressSection.findElement(By.cssSelector("address"));
        softAssert.assertNotNull(billingAddressElement, "Adres rozliczeniowy jest pusty");
        log.info(billingAddressSection.getText());

        WebElement shippingAddressSection = driver.findElement(By.cssSelector("div.woocommerce-column--shipping-address"));
        WebElement shippingAddressElement = shippingAddressSection.findElement(By.cssSelector("address"));
        log.info(shippingAddressSection.getText());
        softAssert.assertNotNull(shippingAddressElement, "Adres do wysyłki jest pusty");

        softAssert.assertAll();
    }

    private static String getCurrentDate() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy"));
    }

    private String waitForErrorMessage() {
        By errorList = By.cssSelector("ul.woocommerce-error");
        wait.until(d -> ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete"));
        return wait.until(ExpectedConditions.presenceOfElementLocated(errorList)).getText();
    }

    private String waitForMessage() {
        wait.until(d -> ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete"));
        return wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".woocommerce-message"))).getText();
    }

    private void checkConfirmationBox() {
        driver.switchTo().defaultContent();
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector(".blockUI"), 0));
        WebElement confirmationBox = driver.findElement(By.cssSelector("input#terms"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", confirmationBox);
        confirmationBox.click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".blockOverlay")));
    }

    private void logIn(String userName, String password) {
        driver.get(storeURL);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("li[id='menu-item-19']"))).click();
        driver.findElement(By.cssSelector("input[id='username']")).sendKeys(userName);
        driver.findElement(By.cssSelector("input[id='password']")).sendKeys(password);
        driver.findElement(By.cssSelector("button[name='login']")).click();
    }

    private void logInDuringCheckout(String userName, String password) {
        By expandLoginForm = By.cssSelector(".showlogin");
        By wrappedLoginView = By.cssSelector(".login[style='display: none;']");

        By usernameField = By.cssSelector("#username");
        By passwordField = By.cssSelector("#password");
        By loginButton = By.cssSelector("[name='login']");

        wait.until(ExpectedConditions.elementToBeClickable(expandLoginForm)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(wrappedLoginView));
        wait.until(ExpectedConditions.elementToBeClickable(usernameField)).sendKeys(userName);
        wait.until(ExpectedConditions.elementToBeClickable(passwordField)).sendKeys(password);
        driver.findElement(loginButton).click();
        log.info("Zalogowano z nazwą użytkownika: " + userName);
    }

    private void register(String email, String password) {
        driver.get(storeURL);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("li[id='menu-item-19']"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[id='reg_email']"))).sendKeys(email);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[id='reg_password']"))).sendKeys(password);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[name='register']"))).click();
    }

    private String getAccountMessage() {
        return driver.findElement(By.cssSelector("div[class='woocommerce-MyAccount-content']>p")).getText();
    }

    private void goToMyAccountOrders() {
        driver.findElement(By.cssSelector("li[id='menu-item-19']")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".woocommerce-MyAccount-navigation-link--orders"))).click();
    }

    private void deleteAccount() {
        driver.findElement(By.cssSelector("a[href='http://zelektronika.store/my-account/wpf-delete-account/']")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div[class='wpfda-submit'] button[type='submit']"))).click();
    }

    private String getErrorMessage() {
        return driver.findElement(By.cssSelector("ul[class='woocommerce-error']")).getText();
    }

    private void goToMyAccountSubpage(String selector, String expectedText) {
        clickAndWait(By.cssSelector("li[id='menu-item-19']"));
        clickAndWait(By.cssSelector(selector));
        String subpageContent = driver.findElement(By.cssSelector("div[class='woocommerce-MyAccount-content']>p")).getText();
        log.info("Znaleziony tekst: \"" + subpageContent + "\", oczekiwany: \"" + expectedText + "\"");
        String errorMessage = String.format("Strona nie zawiera spodziewanego fragmentu tekstu. Spodziewany fragment: \"%s\", znaleziony tekst: \"%s\"", expectedText, subpageContent);
        assertTrue(subpageContent.contains(expectedText), errorMessage);
    }

    private void clickAndWait(By selector) {
        wait.until(ExpectedConditions.elementToBeClickable(selector)).click();
    }

    private void searchForProduct(String productName) {
        WebElement searchBox = driver.findElement(By.cssSelector(".woocommerce-product-search input"));
        searchBox.sendKeys(productName);

        Actions actions = new Actions(driver);
        actions.moveToElement(searchBox);
        actions.click();
        actions.sendKeys(Keys.ENTER).perform();

        //searchBox.sendKeys(productName, Keys.RETURN); //tylko dla Chrome, Firefox wymusza użycie klasy Actions w przypadku użycia klawisza ENTER/RETURN
    }

    private void sortByPriceLowToHigh() {
        WebElement sortByDropdown = driver.findElement(By.cssSelector(".woocommerce-ordering select"));
        sortByDropdown.click();

        Select select = new Select(sortByDropdown);
        select.selectByVisibleText("Sortuj po cenie od najniższej");
    }

    private List<Double> getProductPrices() {
        List<WebElement> productPriceElements = driver.findElements(By.cssSelector(".price"));
        List<Double> productPrices = new ArrayList<>();
        for (WebElement productPriceElement : productPriceElements) {
            String productPriceText = productPriceElement.getText().replace(",", ".");
            double productPrice = Double.parseDouble(productPriceText.replace("zł", ""));
            productPrices.add(productPrice);
        }
        return productPrices;
    }

    private void verifySearchResults(String searchTerm) {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.urlContains(searchTerm));
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".product")));

        List<WebElement> searchResultElements = driver.findElements(By.cssSelector(".product"));
        int numSearchResults = searchResultElements.size();

        assertTrue(numSearchResults > 0, "Brak wyników wyszukiwania dla zadanego zapytania");

        log.info("Liczba wyników wyszukiwania: " + numSearchResults);

        for (WebElement searchResultElement : searchResultElements) {
            String searchResultText = searchResultElement.getText();
            assertTrue(searchResultText.toLowerCase().contains(searchTerm), "Wynik wyszukiwania nie zawiera frazy: \"" + searchTerm + "\"");
        }
        log.info("Wszystkie wyniki wyszukiwania zawierają frazę \"" + searchTerm + "\"");
    }

    private void viewCart() {
        wait.until(ExpectedConditions.elementToBeClickable(productPageViewCartButton)).click();
        wait.until(presenceOfElementLocated(shopTable));
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
        addToCartCheck();
    }

    public void removeProductFromCart() {
        driver.findElement(removeProductButton).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".blockOverlay")));
    }

    private double addProductPrice() {
        String productPriceText = driver.findElement(By.cssSelector("div[class='summary entry-summary'] bdi:nth-child(1)")).getText();
        double productPrice = Double.parseDouble(productPriceText.replaceAll("[^0-9.,]+", "").replace(",", "."));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div[id='content'] p[class='woocommerce-mini-cart__total total'] bdi:nth-child(1)")));
        return productPrice;
    }

    private double getCartTotalPrice() {
        String cartTotalPriceText;
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector(".blockUI"), 0));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("tr[class='cart-subtotal'] bdi:nth-child(1)")));
        cartTotalPriceText = driver.findElement(By.cssSelector("tr[class='cart-subtotal'] bdi:nth-child(1)")).getText().replace(",", ".");
        return Double.parseDouble(cartTotalPriceText.replace("zł", ""));
    }

    private double getCartTotalPriceWithCoupon() {
        String cartTotalPriceText;
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector(".blockUI"), 0));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("tr[class='order-total'] bdi:nth-child(1)")));
        cartTotalPriceText = driver.findElement(By.cssSelector("tr[class='order-total'] bdi:nth-child(1)")).getText().replace(",", ".");
        return Double.parseDouble(cartTotalPriceText.replace("zł", ""));
    }

    private void addCoupon() {
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#coupon_code"))).sendKeys("rabatwsti");
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[value='Wykorzystaj kupon']"))).click();

        String couponMessage = waitForMessage();

        String expectedCouponMessage = "Kupon został pomyślnie użyty.";
        assertEquals(expectedCouponMessage, couponMessage, "Kupon nie został dodany do koszyka");

        log.info("Dodano kupon rabatowy");
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector(".blockUI"), 0));

        WebElement freeShippingAvailablility = driver.findElement(By.cssSelector("label[for='shipping_method_0_free_shipping1']"));
        assertNotNull(freeShippingAvailablility, "Darmowa dostawa jest nie dostępna");
    }

    private void addToCartCheck() {
        String productName = driver.findElement(By.cssSelector(".product_title.entry-title")).getText();

        assertTrue(waitForMessage().contains("„" + productName + "” został dodany do koszyka."), "Produkt „" + productName + "” nie został dodany do koszyka");
        log.info("Dodano nowy produkt do koszyka: „" + productName + "”");
    }

    private void fillOutCheckoutForm(String email, String phone) {
        wait.until(ExpectedConditions.elementToBeClickable(firstNameField)).sendKeys("Helena");
        wait.until(ExpectedConditions.elementToBeClickable(lastNameField)).sendKeys("Mazur");
        wait.until(ExpectedConditions.elementToBeClickable(countryCodeArrow)).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("li[id*='-PL']"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(addressField)).click();
        wait.until(ExpectedConditions.elementToBeClickable(addressField)).clear();
        wait.until(ExpectedConditions.elementToBeClickable(addressField)).sendKeys("Diamentowa 145");
        wait.until(ExpectedConditions.elementToBeClickable(postalCodeField)).click();
        wait.until(ExpectedConditions.elementToBeClickable(postalCodeField)).clear();
        wait.until(ExpectedConditions.elementToBeClickable(postalCodeField)).sendKeys("71-232");
        wait.until(ExpectedConditions.elementToBeClickable(cityField)).sendKeys("Szczecin");
        wait.until(ExpectedConditions.elementToBeClickable(phoneField)).sendKeys(phone);
        wait.until(ExpectedConditions.elementToBeClickable(emailField)).sendKeys(email);
    }

    private void fillOutCardData(String cardNumber, String expirationDate, String cvc) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".blockOverlay")));

        //zamiast zwykłego findElement dla większej stabilności testów w FireFox
        WebElement element = driver.findElement(shippingMethod);
        JavascriptExecutor executor = (JavascriptExecutor)driver;
        executor.executeScript("arguments[0].click();", element);

        //driver.findElement(shippingMethod).click();

        wait.until(ExpectedConditions.invisibilityOfElementLocated(loadingIcon));

        driver.findElement(paymentMethod).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(loadingIcon));

        switchToFrame(cardNumberFrame);
        WebElement cardNumberElement = wait.until(ExpectedConditions.elementToBeClickable(cardNumberField));
        cardNumberElement.clear();
        slowType(cardNumberElement, cardNumber);
        driver.switchTo().defaultContent();

        switchToFrame(expirationDateFrame);
        WebElement expirationDateElement = wait.until(ExpectedConditions.elementToBeClickable(expirationDateField));
        expirationDateElement.clear();
        slowType(expirationDateElement, expirationDate);
        driver.switchTo().defaultContent();

        switchToFrame(cvcFrame);
        WebElement cvcElement = wait.until(ExpectedConditions.elementToBeClickable(cvcField));
        cvcElement.clear();
        slowType(cvcElement, cvc);
        driver.switchTo().defaultContent();
    }

    private String orderAndWaitToComplete() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".blockOverlay")));
        driver.findElement(orderButton).click();

        WebDriverWait wait = new WebDriverWait(driver, 20);
        wait.until(ExpectedConditions.urlContains("/checkout/zamowienie-otrzymane/"));
        log.info("Zamówienie złożone poprawnie - nr zamówienia: " + wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".order>strong"))).getText());
        return wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".order>strong"))).getText();
    }

    private void slowType(WebElement element, String text) {
        for (int i = 0; i < text.length(); i++) {
            element.sendKeys(Character.toString(text.charAt(i)));
        }
    }

    private void switchToFrame(By frameLocator) {
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frameLocator));
        wait.until(d -> ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete"));
    }

    private void checkErrors() {
        String errorMessage = getErrorMessage();
        assertAll(() -> assertTrue(errorMessage.contains("Imię płatnika jest wymaganym polem."), "Błąd o braku imienia:"), () -> assertTrue(errorMessage.contains("Nazwisko płatnika jest wymaganym polem."), "Błąd o braku nazwiska:"), () -> assertTrue(errorMessage.contains("Ulica płatnika jest wymaganym polem."), "Błąd o braku ulicy:"), () -> assertTrue(errorMessage.contains("Miasto płatnika jest wymaganym polem."), "Błąd o braku miasta:"), () -> assertTrue(errorMessage.contains("Telefon płatnika jest wymaganym polem."), "Błąd o braku telefonu:"), () -> assertTrue(errorMessage.contains("Adres email płatnika jest wymaganym polem."), "Błąd o braku adresu e-mail:"), () -> assertTrue(errorMessage.contains("Kod pocztowy płatnika nie jest prawidłowym kodem pocztowym."), "Błąd o braku braku kodu pocztowego:"), () -> log.info("Brak błędów walidacyjnych"));

        String alert = "Proszę przeczytać i zaakceptować regulamin, aby kontynuować składanie zamówienia.";
        assertFalse(driver.findElement(By.cssSelector("ul[role='alert'] li")).getText().contains(alert), "Strona zawiera komunikat o nie zaznaczeniu regulaminu");
        log.info("Znaleziono komunikat: " + alert);
    }
}