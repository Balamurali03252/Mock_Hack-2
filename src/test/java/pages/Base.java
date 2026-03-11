package pages;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;

/**
 * Base.java
 * Central driver management class.
 *
 * OTP LOGIN STRATEGY:
 * -------------------
 * Myntra uses OTP-based login. This cannot be automated without SMS/email API access.
 * Solution:
 *   1. Tester performs OTP login MANUALLY once in the opened browser.
 *   2. Call Base.saveCookies() after successful login to persist the session.
 *   3. All subsequent automated runs call Base.loadCookies() to restore the session.
 *   4. Cookie file is saved at: src/test/resources/cookies/myntra_session.json
 *   5. Cookies typically last 30 days. Re-save if session expires.
 */
public class Base {

    protected static WebDriver driver;
    protected static WebDriverWait wait;

    public static final String BASE_URL    = "https://www.myntra.com/";
    // Replace with a real stable product URL before execution
    public static final String PRODUCT_URL =
        "https://www.myntra.com/tshirts/hm/hm-men-regular-fit-t-shirt/1234567/buy";

    private static final long   EXPLICIT_WAIT_SEC = 20;
    private static final String COOKIE_FILE_PATH  =
        "src/test/resources/cookies/myntra_session.json";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // ── Driver Init / Quit ───────────────────────────────────────────────────

    @Step("Initialise Chrome Browser")
    public static void initDriver() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--remote-allow-origins=*");
        // Uncomment for headless CI:
        // options.addArguments("--headless=new");
        // options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
        wait   = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT_SEC));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));

        Allure.step("ChromeDriver initialised successfully");
    }

    @Step("Quit Browser")
    public static void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
            wait   = null;
        }
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    @Step("Navigate to URL: {url}")
    public static void navigateTo(String url) {
        driver.get(url);
        Allure.step("Navigated to: " + url);
    }

    public static void navigateToHome() {
        navigateTo(BASE_URL);
    }

    public static void navigateToProduct() {
        navigateTo(PRODUCT_URL);
    }

    // ── Explicit Wait Helpers ────────────────────────────────────────────────

    public static WebElement waitForVisibility(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement waitForClickability(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static WebElement waitForPresence(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public static List<WebElement> waitForAllVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    public static void waitForUrlContains(String fragment) {
        wait.until(ExpectedConditions.urlContains(fragment));
    }

    public static void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) driver)
            .executeScript("arguments[0].scrollIntoView({block:'center'});", element);
    }

    public static void jsClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    public static void safeClick(By locator) {
        WebElement el = waitForClickability(locator);
        scrollIntoView(el);
        el.click();
    }

    public static String getText(By locator) {
        return waitForVisibility(locator).getText().trim();
    }

    // ── Cookie Management ────────────────────────────────────────────────────

    /**
     * Saves current browser cookies to JSON.
     * Call ONCE after manual OTP login on Myntra.
     */
    @Step("Save session cookies to file")
    public static void saveCookies() throws IOException {
        Set<Cookie> cookies = driver.manage().getCookies();
        List<Map<String, Object>> cookieList = new ArrayList<>();

        for (Cookie c : cookies) {
            Map<String, Object> map = new HashMap<>();
            map.put("name",     c.getName());
            map.put("value",    c.getValue());
            map.put("domain",   c.getDomain()   != null ? c.getDomain()   : "");
            map.put("path",     c.getPath()     != null ? c.getPath()     : "/");
            map.put("secure",   c.isSecure());
            map.put("httpOnly", c.isHttpOnly());
            cookieList.add(map);
        }

        Files.createDirectories(Paths.get("src/test/resources/cookies"));
        objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(COOKIE_FILE_PATH), cookieList);

        System.out.println("[Base] Saved " + cookieList.size() + " cookies → " + COOKIE_FILE_PATH);
        Allure.step("Saved " + cookieList.size() + " cookies to file");
    }

    /**
     * Loads cookies from JSON into the current session.
     * Driver must already be on the Myntra domain before calling this.
     */
    @Step("Load session cookies from file")
    public static void loadCookies() throws IOException {
        File cookieFile = new File(COOKIE_FILE_PATH);
        if (!cookieFile.exists()) {
            throw new FileNotFoundException(
                "[Base] Cookie file not found: " + COOKIE_FILE_PATH +
                "\nPlease perform a manual OTP login and call Base.saveCookies() first.");
        }

        List<Map<String, Object>> cookieList =
            objectMapper.readValue(cookieFile,
                new TypeReference<List<Map<String, Object>>>() {});

        driver.manage().deleteAllCookies();

        for (Map<String, Object> map : cookieList) {
            try {
                Cookie cookie = new Cookie.Builder(
                        (String) map.get("name"),
                        (String) map.get("value"))
                    .domain((String) map.get("domain"))
                    .path((String) map.get("path"))
                    .isSecure((Boolean)  map.get("secure"))
                    .isHttpOnly((Boolean) map.get("httpOnly"))
                    .build();
                driver.manage().addCookie(cookie);
            } catch (Exception e) {
                System.out.println("[Base] Skipped cookie: " + map.get("name") + " → " + e.getMessage());
            }
        }

        System.out.println("[Base] Loaded " + cookieList.size() + " cookies");
        Allure.step("Loaded " + cookieList.size() + " cookies from file");
    }

    @Step("Clear all browser cookies")
    public static void clearCookies() {
        driver.manage().deleteAllCookies();
        Allure.step("All cookies cleared");
    }

    // ── Screenshot / Allure ──────────────────────────────────────────────────

    @Step("Capture screenshot")
    public static void attachScreenshotToAllure(String label) {
        if (driver == null) return;
        try {
            byte[] screenshot = ((TakesScreenshot) driver)
                .getScreenshotAs(OutputType.BYTES);
            Allure.getLifecycle().addAttachment(label, "image/png", "png", screenshot);
        } catch (Exception e) {
            System.out.println("[Base] Screenshot capture failed: " + e.getMessage());
        }
    }

    // ── Utility ──────────────────────────────────────────────────────────────

    public static double parsePrice(String priceText) {
        if (priceText == null || priceText.trim().isEmpty()) return 0.0;
        String cleaned = priceText.replaceAll("[^0-9.]", "");
        if (cleaned.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public static void hardWait(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }

    public static WebDriver getDriver() { return driver; }
    public static WebDriverWait getWait() { return wait; }
}