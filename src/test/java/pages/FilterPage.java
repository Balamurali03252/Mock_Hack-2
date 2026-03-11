package pages;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;
import java.util.stream.Collectors;

/**
 * FilterPage.java
 * Page Object for Myntra Filter and Sort functionality.
 */
public class FilterPage extends Base {

    private static final By FILTER_BUTTON =
        By.xpath("//div[contains(@class,'filter-filter') or contains(@class,'filterContainer')]" +
                 "//span[contains(text(),'FILTERS') or contains(text(),'Filter')]");

    private static final By FILTER_PANEL =
        By.xpath("//div[contains(@class,'filter-base') or contains(@class,'filterPanel')] | " +
                 "//div[@class='filter-base-container']");

    private static final By BRAND_SEARCH_INPUT =
        By.xpath("//input[@placeholder='Search Brand' or @placeholder='Search brand']");

    private static final By PRICE_MIN_INPUT =
        By.xpath("//input[@placeholder='Min' or @aria-label='Min']");

    private static final By PRICE_MAX_INPUT =
        By.xpath("//input[@placeholder='Max' or @aria-label='Max']");

    private static final By APPLIED_FILTER_TAGS =
        By.xpath("//div[contains(@class,'filterApplied') or contains(@class,'applied-filter')]" +
                 "//span[contains(@class,'filterTag') or contains(@class,'filter-tag-text')]");

    private static final By CLEAR_ALL_BUTTON =
        By.xpath("//div[contains(text(),'CLEAR ALL') or contains(text(),'Clear All')] | " +
                 "//span[contains(@class,'clearAll')]");

    private static final By SORT_BUTTON =
        By.xpath("//div[contains(@class,'sort-filter') or contains(@class,'sortContainer')]" +
                 "//span[contains(text(),'SORT BY') or contains(text(),'Sort By')]");

    private static final By RESULT_ITEMS =
        By.xpath("//li[contains(@class,'product-base')]");

    // ── Filter Panel ──────────────────────────────────────────────────────────

    @Step("Open filter panel")
    public void openFilterPanel() {
        try {
            driver.findElement(FILTER_PANEL);
            Allure.step("Filter panel already open");
        } catch (NoSuchElementException e) {
            safeClick(FILTER_BUTTON);
            wait.until(ExpectedConditions.visibilityOfElementLocated(FILTER_PANEL));
            Allure.step("Filter panel opened");
        }
    }

    // ── Brand ─────────────────────────────────────────────────────────────────

    @Step("Filter by brand: {brandName}")
    public void filterByBrand(String brandName) {
        openFilterPanel();
        try {
            WebElement brandSearch = waitForVisibility(BRAND_SEARCH_INPUT);
            brandSearch.clear();
            brandSearch.sendKeys(brandName);
        } catch (Exception ignored) {}

        By brandLabel = By.xpath(
            "//label[contains(translate(normalize-space(.)," +
            "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" +
            brandName.toLowerCase() + "')]");

        WebElement el = waitForClickability(brandLabel);
        scrollIntoView(el);
        el.click();
        Allure.step("Applied brand filter: " + brandName);
        waitForResultsToRefresh();
    }

    // ── Price Range ───────────────────────────────────────────────────────────

    @Step("Filter by price range: {min} to {max}")
    public void filterByPriceRange(int min, int max) {
        openFilterPanel();
        if (min > 0) {
            try {
                WebElement minEl = waitForVisibility(PRICE_MIN_INPUT);
                minEl.clear(); minEl.sendKeys(String.valueOf(min)); minEl.sendKeys(Keys.TAB);
            } catch (Exception ignored) {}
        }
        if (max > 0) {
            try {
                WebElement maxEl = waitForVisibility(PRICE_MAX_INPUT);
                maxEl.clear(); maxEl.sendKeys(String.valueOf(max)); maxEl.sendKeys(Keys.ENTER);
            } catch (Exception ignored) {}
        }
        Allure.step("Applied price range: ₹" + min + " – ₹" + max);
        waitForResultsToRefresh();
    }

    // ── Discount ──────────────────────────────────────────────────────────────

    @Step("Filter by discount: {discountText}")
    public void filterByDiscount(String discountText) {
        openFilterPanel();
        By option = By.xpath(
            "//div[contains(@class,'filterSection')]" +
            "//label[contains(normalize-space(.),'" + discountText + "')]");
        WebElement el = waitForClickability(option);
        scrollIntoView(el);
        el.click();
        Allure.step("Applied discount filter: " + discountText);
        waitForResultsToRefresh();
    }

    // ── Size ──────────────────────────────────────────────────────────────────

    @Step("Filter by size: {size}")
    public void filterBySize(String size) {
        openFilterPanel();
        By sizeOption = By.xpath(
            "//div[contains(@class,'filterSection') or contains(@class,'sizeFilter')]" +
            "//label[normalize-space(.)='" + size + "'] | " +
            "//span[normalize-space(.)='" + size + "']/ancestor::label");
        WebElement el = waitForClickability(sizeOption);
        scrollIntoView(el);
        el.click();
        Allure.step("Applied size filter: " + size);
        waitForResultsToRefresh();
    }

    // ── Sort ──────────────────────────────────────────────────────────────────

    @Step("Sort by: {sortOption}")
    public void sortBy(String sortOption) {
        try { safeClick(SORT_BUTTON); } catch (Exception ignored) {}
        By sortItem = By.xpath(
            "//li[contains(normalize-space(.),'" + sortOption + "')] | " +
            "//div[contains(@class,'sort')]//span[contains(normalize-space(.),'" + sortOption + "')]");
        waitForClickability(sortItem).click();
        Allure.step("Sorted by: " + sortOption);
        waitForResultsToRefresh();
    }

    // ── Applied Filters ───────────────────────────────────────────────────────

    @Step("Get applied filter tags")
    public List<String> getAppliedFilters() {
        try {
            return waitForAllVisible(APPLIED_FILTER_TAGS).stream()
                .map(el -> el.getText().trim())
                .filter(t -> !t.isEmpty())
                .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    @Step("Check if filter '{filterLabel}' is applied")
    public boolean isFilterApplied(String filterLabel) {
        boolean applied = getAppliedFilters().stream()
            .anyMatch(f -> f.equalsIgnoreCase(filterLabel));
        Allure.step(applied ? "✅ Filter applied: " + filterLabel
                            : "❌ Filter NOT applied: " + filterLabel);
        return applied;
    }

    @Step("Clear all filters")
    public void clearAllFilters() {
        try {
            safeClick(CLEAR_ALL_BUTTON);
            waitForResultsToRefresh();
            Allure.step("All filters cleared");
        } catch (Exception e) {
            System.out.println("[FilterPage] No CLEAR ALL button found");
        }
    }

    @Step("Get filtered result count")
    public int getFilteredResultCount() {
        try {
            int count = driver.findElements(RESULT_ITEMS).size();
            Allure.step("Filtered result count: " + count);
            return count;
        } catch (Exception e) {
            return 0;
        }
    }

    private void waitForResultsToRefresh() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(RESULT_ITEMS));
        } catch (TimeoutException ignored) {}
    }
}