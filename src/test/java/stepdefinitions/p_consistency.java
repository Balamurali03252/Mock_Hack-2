package stepdefinitions;

import io.cucumber.java.en.*;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.junit.Assert;
import pages.Base;
import pages.P_ConsistencyPage;

import java.io.IOException;
import java.util.Arrays;

/**
 * p_consistency.java
 * Step definitions for product_consistency.feature
 *
 * OTP HANDLING EXPLAINED:
 * ───────────────────────
 * Myntra requires OTP login (phone/email). OTP cannot be automated without
 * access to the SMS API. This framework handles it as follows:
 *
 *   STEP 1 (One-time manual setup):
 *     - A tester opens the browser via initDriver(), navigates to Myntra,
 *       and completes the OTP login manually.
 *     - After login, Base.saveCookies() is called to write the session to:
 *         src/test/resources/cookies/myntra_session.json
 *
 *   STEP 2 (Every automated run):
 *     - The step "the user loads saved login cookies" triggers Base.loadCookies()
 *     - The driver navigates to Myntra and adds cookies to the browser session
 *     - A page refresh applies the cookies – Myntra recognises the session
 *
 *   Cookies last ~30 days. Re-run the manual save step if tests fail on login.
 */
public class p_consistency {

    private final P_ConsistencyPage page = new P_ConsistencyPage();

    // Cart value arrays shared across steps
    private double[] guestCartValues;
    private double[] loggedInCartValues;

    // ── Given ─────────────────────────────────────────────────────────────────

    @Given("the user opens Myntra product URL directly")
    @Step("Open Myntra product URL directly")
    public void openProductUrl() {
        Base.navigateToProduct();
        Allure.step("Opened: " + Base.PRODUCT_URL);
    }

    @Given("the user is on the Myntra product page as a guest")
    @Step("Open Myntra as guest user")
    public void openAsGuest() {
        Base.clearCookies();
        Base.navigateToProduct();
        Allure.step("Opened product page as guest (no cookies)");
    }

    // ── When – Product Interaction ────────────────────────────────────────────

    @When("the user selects an available size if required")
    @Step("Select available size")
    public void selectSize() {
        page.selectSize();
    }

    @When("the user clicks {string} button")
    @Step("Click button: {buttonLabel}")
    public void clickButton(String buttonLabel) {
        switch (buttonLabel.toLowerCase().trim()) {
            case "add to bag":   page.clickAddToBag();    break;
            case "place order":  page.clickPlaceOrder();  break;
            default: throw new IllegalArgumentException("Unknown button: " + buttonLabel);
        }
    }

    @When("the user navigates to the cart")
    @Step("Navigate to cart")
    public void navigateToCart() {
        page.goToCart();
    }

    // ── When – Price Capture ──────────────────────────────────────────────────

    @When("the user captures cart values as guest")
    @Step("Capture guest cart values")
    public void captureGuestCart() {
        guestCartValues = page.captureCartValues();
        Allure.step("Guest values: " + Arrays.toString(guestCartValues));
    }

    @When("the user captures cart values as logged-in user")
    @Step("Capture logged-in cart values")
    public void captureLoggedInCart() {
        loggedInCartValues = page.captureCartValues();
        Allure.step("Logged-in values: " + Arrays.toString(loggedInCartValues));
    }

    // ── When – Session Management ─────────────────────────────────────────────

    @When("the user clears cookies to remove guest session")
    @Step("Clear cookies – end guest session")
    public void clearGuestSession() {
        Base.clearCookies();
        Allure.step("Guest session cleared");
    }

    /**
     * Loads previously saved login cookies to restore a logged-in session.
     * See class-level Javadoc above for full OTP login setup instructions.
     */
    @When("the user loads saved login cookies")
    @Step("Load saved login cookies")
    public void loadLoginCookies() {
        try {
            Base.navigateToHome();      // Must be on Myntra domain before adding cookies
            Base.loadCookies();
            Base.getDriver().navigate().refresh();
            Allure.step("Logged-in session restored via cookies");
        } catch (IOException e) {
            String msg =
                "Cookie file not found. Perform manual OTP login on Myntra then call " +
                "Base.saveCookies(). Error: " + e.getMessage();
            Allure.step("❌ " + msg);
            Assert.fail(msg);
        }
    }

    // ── Then – Validations ────────────────────────────────────────────────────

    @Then("the login or signup page should appear")
    @Step("Assert login/signup page is displayed")
    public void assertLoginPageAppears() {
        boolean shown = page.isLoginPageDisplayed();
        Base.attachScreenshotToAllure("Login Page State");
        Assert.assertTrue(
            "Expected login/signup page for guest after Place Order – not found.", shown);
        Allure.step("✅ Login/Signup page verified for guest user");
    }

    @Then("the address selection page should appear for the logged-in user")
    @Step("Assert address page is displayed for logged-in user")
    public void assertAddressPageAppears() {
        boolean shown = page.isAddressPageDisplayed();
        Base.attachScreenshotToAllure("Address Page State");
        Assert.assertTrue(
            "Expected address page for logged-in user after Place Order – not found.", shown);
        Allure.step("✅ Address page verified for logged-in user");
    }

    @Then("the cart totals for guest and logged-in user should match")
    @Step("Assert cart totals match between guest and logged-in user")
    public void assertCartTotalsMatch() {
        Assert.assertNotNull("Guest cart values are null", guestCartValues);
        Assert.assertNotNull("Logged-in cart values are null", loggedInCartValues);

        boolean match = page.compareCartValues(guestCartValues, loggedInCartValues);
        Base.attachScreenshotToAllure("Cart Comparison Result");

        Assert.assertTrue(
            "Cart values differ between guest and logged-in user!\n" +
            "Guest    : " + Arrays.toString(guestCartValues) + "\n" +
            "LoggedIn : " + Arrays.toString(loggedInCartValues),
            match);

        Allure.step("✅ Cart totals match");
    }

    @Then("the subtotal should be greater than zero")
    @Step("Assert subtotal > 0")
    public void assertSubtotalGreaterThanZero() {
        double[] values = guestCartValues != null ? guestCartValues : loggedInCartValues;
        Assert.assertNotNull("No cart values captured", values);
        Assert.assertTrue("Subtotal must be > 0, got: " + values[0], values[0] > 0);
        Allure.step("✅ Subtotal = " + values[0]);
    }
}