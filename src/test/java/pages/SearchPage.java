package pages;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.ArrayList;
import java.util.List;

/**
 * SearchPage.java
 * Page Object for Myntra Search functionality.
 */
public class SearchPage extends Base {

    private static final By SEARCH_ICON =
        By.xpath("//div[contains(@class,'desktop-iconSearch')] | " +
                 "//span[@class='myntraweb-sprite desktop-iconSearch sprites-search']");

    private static final By SEARCH_INPUT =
        By.xpath("//input[@placeholder='Search for products, brands and more'] | " +
                 "//div[contains(@class,'search-bar')]//input[@type='text']");

    private static final By RESULT_PRODUCT_CARDS =
        By.xpath("//li[contains(@class,'product-base')]");

    private static final By BRAND_NAME_IN_CARD =
        By.xpath(".//h3[contains(@class,'product-brand')]");

    private static final By PRODUCT_NAME_IN_CARD =
        By.xpath(".//p[contains(@class,'product-product')]");

    private static final By NO_RESULTS_TEXT =
        By.xpath("//div[contains(@class,'no-results') or contains(@class,'noResults')] | " +
                 "//h1[contains(text(),'Sorry')]");

    private static final By RESULTS_COUNT =
        By.xpath("//span[contains(@class,'title-count') or contains(@class,'productsCount')] | " +
                 "//h1[contains(@class,'results-base')]//span");

    private static final By SEARCH_CLEAR_BUTTON =
        By.xpath("//div[contains(@class,'search-bar')]" +
                 "//span[contains(@class,'clear') or @title='Clear']");

    // ── Actions ──────────────────────────────────────────────────────────────

    @Step("Open search bar")
    public void openSearchBar() {
        try {
            waitForClickability(SEARCH_ICON).click();
        } catch (TimeoutException e) {
            System.out.println("[SearchPage] Search icon not found – bar may already be open");
        }
        Allure.step("Search bar opened");
    }

    @Step("Enter search query: {query}")
    public void enterSearchQuery(String query) {
        WebElement input = waitForClickability(SEARCH_INPUT);
        input.clear();
        input.sendKeys(query);
        Allure.step("Typed: " + query);
    }

    @Step("Submit search")
    public void submitSearch() {
        waitForVisibility(SEARCH_INPUT).sendKeys(Keys.ENTER);
        waitForSearchResultsToLoad();
        Allure.step("Search submitted");
    }

    @Step("Search for: {query}")
    public void searchFor(String query) {
        openSearchBar();
        enterSearchQuery(query);
        submitSearch();
    }

    @Step("Clear search input")
    public void clearSearch() {
        try {
            safeClick(SEARCH_CLEAR_BUTTON);
        } catch (Exception e) {
            waitForVisibility(SEARCH_INPUT).clear();
        }
        Allure.step("Search cleared");
    }

    // ── Results ───────────────────────────────────────────────────────────────

    @Step("Wait for search results to load")
    public void waitForSearchResultsToLoad() {
        wait.until(ExpectedConditions.or(
            ExpectedConditions.visibilityOfElementLocated(RESULT_PRODUCT_CARDS),
            ExpectedConditions.visibilityOfElementLocated(NO_RESULTS_TEXT)
        ));
        Allure.step("Search results loaded");
    }

    @Step("Get result cards")
    public List<WebElement> getResultCards() {
        return waitForAllVisible(RESULT_PRODUCT_CARDS);
    }

    @Step("Get result count")
    public int getResultCount() {
        int count = driver.findElements(RESULT_PRODUCT_CARDS).size();
        Allure.step("Result count: " + count);
        return count;
    }

    @Step("Get total results text")
    public String getTotalResultsText() {
        try { return getText(RESULTS_COUNT); }
        catch (Exception e) { return ""; }
    }

    @Step("Extract product titles")
    public List<String> getResultTitles() {
        List<WebElement> cards = getResultCards();
        List<String> titles = new ArrayList<>();
        for (WebElement card : cards) {
            try {
                String brand   = card.findElements(BRAND_NAME_IN_CARD).isEmpty()   ? "" :
                                 card.findElements(BRAND_NAME_IN_CARD).get(0).getText().trim();
                String product = card.findElements(PRODUCT_NAME_IN_CARD).isEmpty() ? "" :
                                 card.findElements(PRODUCT_NAME_IN_CARD).get(0).getText().trim();
                titles.add((brand + " " + product).trim());
            } catch (StaleElementReferenceException ignored) {}
        }
        return titles;
    }

    @Step("Check keyword in results: {keyword}")
    public boolean isKeywordPresentInResults(String keyword) {
        boolean found = getResultTitles().stream()
            .anyMatch(t -> t.toLowerCase().contains(keyword.toLowerCase()));
        Allure.step(found ? "✅ Keyword found: " + keyword : "❌ Keyword missing: " + keyword);
        return found;
    }

    @Step("Check no-results state")
    public boolean isNoResultsDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(NO_RESULTS_TEXT));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    @Step("Click result at index {index}")
    public void clickResultAtIndex(int index) {
        List<WebElement> cards = getResultCards();
        if (index < 1 || index > cards.size())
            throw new IndexOutOfBoundsException("Index " + index + " out of range (" + cards.size() + ")");
        WebElement card = cards.get(index - 1);
        scrollIntoView(card);
        card.click();
        Allure.step("Clicked result #" + index);
    }
}