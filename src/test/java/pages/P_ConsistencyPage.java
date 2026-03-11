package pages;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * P_ConsistencyPage.java
 * Page Object for Guest vs Logged-in Checkout Parity.
 *
 * Covers:
 *  - Product Detail Page  : size selection, Add to Bag
 *  - Cart Page            : price capture (subtotal, shipping, platform fee, total)
 *  - Checkout             : Place Order, login redirect check, address page check
 */
public class P_ConsistencyPage extends Base {

    // ── Product Detail Page ──────────────────────────────────────────────────

    private static final By SIZE_CONTAINER =
        By.xpath("//div[contains(@class,'size-buttons-container') or contains(@class,'sizeContainer')]");

    private static final By SIZE_BUTTONS =
        By.xpath("//div[contains(@class,'size-buttons-unified-size') " +
                 "and not(contains(@class,'size-buttons-size-out-of-stock'))]");

    private static final By ADD_TO_BAG_BUTTON =
        By.xpath("//div[contains(@class,'pdp-add-to-bag') and not(contains(@class,'disabled'))] | " +
                 "//button[contains(text(),'ADD TO BAG')] | " +
                 "//div[@class='btn-add-to-bag']");

    // ── Cart Page ────────────────────────────────────────────────────────────

    private static final By CART_ICON =
        By.xpath("//a[@href='/checkout/cart'] | " +
                 "//div[contains(@class,'myntraweb-sprite desktop-iconBag')]//parent::a");

    private static final By CART_ITEMS_WRAPPER =
        By.xpath("//div[contains(@class,'itemContainer') or contains(@class,'cartItem')]");

    private static final By PRICE_SECTION =
        By.xpath("//div[contains(@class,'priceHeader') or contains(@class,'summaryContainer')]");

    private static final By SUBTOTAL_VALUE =
        By.xpath("//span[contains(text(),'Total MRP') or contains(text(),'Subtotal')]" +
                 "/following-sibling::span[1] | " +
                 "//div[contains(@class,'totalMRP')]//span[last()]");

    private static final By SHIPPING_VALUE =
        By.xpath("//span[contains(text(),'Delivery Charge') or contains(text(),'Shipping')]" +
                 "/following-sibling::span[1] | " +
                 "//div[contains(@class,'delivery')]//span[last()]");

    private static final By PLATFORM_FEE_VALUE =
        By.xpath("//span[contains(text(),'Platform Fee') or contains(text(),'Convenience')]" +
                 "/following-sibling::span[1] | " +
                 "//div[contains(@class,'platformFee')]//span[last()]");

    private static final By TOTAL_AMOUNT_VALUE =
        By.xpath("//span[contains(text(),'Total Amount') or contains(text(),'Grand Total')]" +
                 "/following-sibling::span[1] | " +
                 "//div[contains(@class,'totalAmount')]//span[last()] | " +
                 "//span[@class='totalAmount']");

    // ── Checkout ─────────────────────────────────────────────────────────────

    private static final By PLACE_ORDER_BUTTON =
        By.xpath("//div[contains(@class,'action-btn')]//button[contains(text(),'PLACE ORDER')] | " +
                 "//button[contains(text(),'Place Order')] | " +
                 "//div[contains(@class,'placeOrder')]//button");

    private static final By LOGIN_PAGE_INDICATOR =
        By.xpath("//div[contains(@class,'login-modal') or contains(@class,'loginModal')] | " +
                 "//h1[contains(text(),'Login') or contains(text(),'Sign In')] | " +
                 "//div[contains(@class,'login-container')]" +
                 "//input[@placeholder='Enter Email/Mobile number']");

    private static final By ADDRESS_PAGE_INDICATOR =
        By.xpath("//div[contains(@class,'address-container') or contains(@class,'addressContainer')] | " +
                 "//h2[contains(text(),'Select Delivery Address') or contains(text(),'Delivery Address')] | " +
                 "//div[@class='checkoutAddresses']");

    // ── Product Actions ──────────────────────────────────────────────────────

    @Step("Select available product size")
    public void selectSize() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(SIZE_CONTAINER));
            List<WebElement> sizes = driver.findElements(SIZE_BUTTONS);
            if (sizes.isEmpty()) {
                Allure.step("No available sizes – one-size or OOS product");
                return;
            }
            WebElement firstSize = sizes.get(0);
            scrollIntoView(firstSize);
            firstSize.click();
            Allure.step("Selected size: " + firstSize.getText().trim());
        } catch (TimeoutException e) {
            Allure.step("No size selector present – skipping");
        }
    }

    @Step("Click Add to Bag")
    public void clickAddToBag() {
        WebElement btn = waitForClickability(ADD_TO_BAG_BUTTON);
        scrollIntoView(btn);
        btn.click();
        Allure.step("Clicked 'Add to Bag'");
    }

    // ── Cart Navigation ───────────────────────────────────────────────────────

    @Step("Navigate to Cart page")
    public void goToCart() {
        try {
            waitForClickability(CART_ICON).click();
        } catch (Exception e) {
            driver.get("https://www.myntra.com/checkout/cart");
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(CART_ITEMS_WRAPPER));
        Allure.step("On Cart page");
    }

    // ── Price Capture ─────────────────────────────────────────────────────────

    /**
     * Captures cart price components into a double array.
     * Index: [0]=Subtotal  [1]=Shipping  [2]=PlatformFee  [3]=Total
     */
    @Step("Capture cart price values")
    public double[] captureCartValues() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(PRICE_SECTION));

        double subtotal    = extractPriceOrDefault(SUBTOTAL_VALUE,     0.0);
        double shipping    = extractPriceOrDefault(SHIPPING_VALUE,      0.0);
        double platformFee = extractPriceOrDefault(PLATFORM_FEE_VALUE, 0.0);
        double total       = extractPriceOrDefault(TOTAL_AMOUNT_VALUE,  0.0);

        double[] values = { subtotal, shipping, platformFee, total };

        Allure.step(String.format(
            "Cart → Subtotal=%.2f | Shipping=%.2f | PlatformFee=%.2f | Total=%.2f",
            subtotal, shipping, platformFee, total));

        return values;
    }

    private double extractPriceOrDefault(By locator, double defaultVal) {
        try {
            List<WebElement> els = driver.findElements(locator);
            for (int i = els.size() - 1; i >= 0; i--) {
                String text = els.get(i).getText().trim();
                if (!text.isEmpty()) return parsePrice(text);
            }
        } catch (Exception ignored) {}
        return defaultVal;
    }

    // ── Checkout Actions ──────────────────────────────────────────────────────

    @Step("Click Place Order")
    public void clickPlaceOrder() {
        WebElement btn = waitForClickability(PLACE_ORDER_BUTTON);
        scrollIntoView(btn);
        btn.click();
        Allure.step("Clicked 'Place Order'");
    }

    @Step("Verify login/signup page appears for guest")
    public boolean isLoginPageDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_PAGE_INDICATOR));
            Allure.step("✅ Login/Signup page detected");
            return true;
        } catch (TimeoutException e) {
            String url = driver.getCurrentUrl();
            boolean byUrl = url.contains("login") || url.contains("signup");
            Allure.step(byUrl ? "✅ Login redirect via URL: " + url
                              : "❌ Login page NOT detected. URL: " + url);
            return byUrl;
        }
    }

    @Step("Verify address page appears for logged-in user")
    public boolean isAddressPageDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(ADDRESS_PAGE_INDICATOR));
            Allure.step("✅ Address page detected");
            return true;
        } catch (TimeoutException e) {
            String url = driver.getCurrentUrl();
            boolean byUrl = url.contains("address") || url.contains("checkout");
            Allure.step(byUrl ? "✅ Address page via URL: " + url
                              : "❌ Address page NOT detected. URL: " + url);
            return byUrl;
        }
    }

    // ── Cart Comparison ───────────────────────────────────────────────────────

    /**
     * Compares guest vs logged-in double[] arrays with ±0.01 tolerance.
     */
    @Step("Compare guest and logged-in cart totals")
    public boolean compareCartValues(double[] guest, double[] loggedIn) {
        final double TOLERANCE = 0.01;
        boolean allMatch = true;
        String[] labels = { "Subtotal", "Shipping", "Platform Fee", "Total" };

        for (int i = 0; i < Math.min(guest.length, loggedIn.length); i++) {
            double diff  = Math.abs(guest[i] - loggedIn[i]);
            boolean match = diff <= TOLERANCE;
            String msg = String.format("%s | %s → Guest=%.2f | LoggedIn=%.2f | Diff=%.2f",
                match ? "✅" : "❌", labels[i], guest[i], loggedIn[i], diff);
            System.out.println("[P_ConsistencyPage] " + msg);
            Allure.step(msg);
            if (!match) allMatch = false;
        }
        return allMatch;
    }
}