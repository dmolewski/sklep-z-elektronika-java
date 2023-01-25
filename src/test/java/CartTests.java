import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import static org.junit.jupiter.api.Assertions.*;

public class CartTests extends BaseTest {

    String productId = "12";
    By productPageAddToCartButton = By.cssSelector("button[name='add-to-cart']");
    By categoryPageAddToCartButton = By.cssSelector(".post-" + productId + ">.add_to_cart_button");
    By removeProductButton = By.cssSelector("a[data-product_id='" + productId + "']");
    By productPageViewCartButton = By.cssSelector(".woocommerce-message>.button");
    By cartQuantityField = By.cssSelector("input.qty");
    By updateCartButton = By.cssSelector("[name='update_cart']");
    By shopTable = By.cssSelector(".shop_table");
    String[] productPages = {"/hoodie-with-logo/", "/hoodie-with-logo/"};

    @Test
    public void addToCartFromProductPageTest() {
        addProductAndViewCart("http://zelektronika.store/product/hoodie-with-logo/");
        assertTrue(driver.findElements(removeProductButton).size() == 1, "Remove button was not found for a product with id=12 (Hoodie with logo). " + "Was the product added to cart?");
    }

    @Test
    public void addToCartFromCategoryPageTest() {
        driver.navigate().to("http://zelektronika.store/product-category/clothing/");
        driver.findElement(categoryPageAddToCartButton).click();
        By viewCartButton = By.cssSelector(".added_to_cart");
        wait.until(ExpectedConditions.elementToBeClickable(viewCartButton));
        driver.findElement(viewCartButton).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(shopTable));
        assertTrue(driver.findElements(removeProductButton).size() == 1, "Remove button was not found for a product with id=12 (Hoodie with logo). " + "Was the product added to cart?");
    }

    @Test
    public void addOneProductTenTimesTest() {
        addProductAndViewCart("http://zelektronika.store/product/hoodie-with-logo/", "10");
        String quantityString = driver.findElement(By.cssSelector("div.quantity>input")).getAttribute("value");
        int quantity = Integer.parseInt(quantityString);
        assertEquals(10, quantity, "Quantity of the product is not what expected. Expected: 10, but was " + quantity);
    }

    @Test
    public void addTenProductsToCartTest() {
        for (String productPage : productPages) {
            addProductToCart("http://zelektronika.store/product" + productPage);
        }
        viewCart();
        int numberOfItems = driver.findElements(By.cssSelector(".cart_item")).size();
        assertEquals(10, numberOfItems, "Number of items in the cart is not correct. Expected: 10, but was: " + numberOfItems);
    }

    @Test
    public void changeNumberOfProductsTest() {
        addProductAndViewCart("http://zelektronika.store/product/hoodie-with-logo/");
        WebElement quantityField = driver.findElement(cartQuantityField);
        quantityField.clear();
        quantityField.sendKeys("8");
        WebElement updateButton = driver.findElement(updateCartButton);
        wait.until(ExpectedConditions.elementToBeClickable(updateButton));
        updateButton.click();
        String quantityString = driver.findElement(By.cssSelector("div.quantity>input")).getAttribute("value");
        int quantity = Integer.parseInt(quantityString);
        assertEquals(8, quantity, "Quantity of the product is not what expected. Expected: 2, but was " + quantity);
    }

    @Test
    public void removePositionFromCartTest() {
        addProductAndViewCart("http://zelektronika.store/product/hoodie-with-logo/");
        driver.findElement(removeProductButton).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".blockOverlay")));
        int numberOfEmptyCartMessages = driver.findElements(By.cssSelector("p.cart-empty")).size();
        assertEquals(1, numberOfEmptyCartMessages, "One message about empty cart was expected, but found " + numberOfEmptyCartMessages);
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

    private void addProductToCart(String productPageUrl, String quantity) {
        driver.navigate().to(productPageUrl);
        WebElement quantityField = driver.findElement(By.cssSelector("input.qty"));
        quantityField.clear();
        quantityField.sendKeys(quantity);
        addProductToCart();
    }

    private void viewCart() {
        wait.until(ExpectedConditions.elementToBeClickable(productPageViewCartButton)).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(shopTable));
    }

    private void addProductAndViewCart(String productPageUrl) {
        addProductToCart(productPageUrl);
        viewCart();
    }

    private void addProductAndViewCart(String productPageUrl, String quantity) {
        addProductToCart(productPageUrl, quantity);
        viewCart();
    }
}