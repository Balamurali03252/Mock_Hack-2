package stepdefinitions;

import io.cucumber.java.en.*;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.junit.Assert;
import pages.Base;
import pages.SearchPage;

/**
 * Search.java
 * Step definitions for search.feature
 */
public class Search {

    private final SearchPage searchPage = new SearchPage();

    // ── Given ─────────────────────────────────────────────────────────────────

    @Given("the user is on the Myntra homepage")
    @Step("Open Myntra homepage")
    public void openHomepage() {
        Base.navigateToHome();
        Allure.step("Opened Myntra homepage");
    }

    // ── When ──────────────────────────────────────────────────────────────────

    @When("the user searches for {string}")
    @Step("Search for: {keyword}")
    public void searchFor(String keyword) {
        searchPage.searchFor(keyword);
        Allure.step("Searched for: " + keyword);
    }

    @When("the user enters {string} in the search bar")
    @Step("Enter in search bar: {term}")
    public void enterInSearchBar(String term) {
        searchPage.openSearchBar();
        searchPage.enterSearchQuery(term);
    }

    @When("the user submits the search")
    @Step("Submit search")
    public void submitSearch() {
        searchPage.submitSearch();
    }

    @When("the user clears the search input")
    @Step("Clear search input")
    public void clearSearchInput() {
        searchPage.clearSearch();
    }

    @When("the user clicks on the first search result")
    @Step("Click first search result")
    public void clickFirstResult() {
        searchPage.clickResultAtIndex(1);
    }

    // ── Then ──────────────────────────────────────────────────────────────────

    @Then("search results should be displayed")
    @Step("Assert search results are displayed")
    public void assertResultsDisplayed() {
        searchPage.waitForSearchResultsToLoad();
        int count = searchPage.getResultCount();
        Base.attachScreenshotToAllure("Search Results");
        Assert.assertTrue("Expected results but found 0.", count > 0);
        Allure.step("✅ " + count + " results displayed");
    }

    @Then("the results should contain products related to {string}")
    @Step("Assert results contain keyword: {keyword}")
    public void assertResultsContainKeyword(String keyword) {
        boolean found = searchPage.isKeywordPresentInResults(keyword);
        Base.attachScreenshotToAllure("Results for: " + keyword);
        Assert.assertTrue("No results contain keyword: " + keyword, found);
        Allure.step("✅ Keyword found: " + keyword);
    }

    @Then("at least {int} products should be visible")
    @Step("Assert at least {minCount} products visible")
    public void assertAtLeastProducts(int minCount) {
        int count = searchPage.getResultCount();
        Assert.assertTrue("Expected ≥ " + minCount + " results, got: " + count, count >= minCount);
        Allure.step("✅ " + count + " products visible");
    }

    @Then("a no results message should be displayed")
    @Step("Assert no-results message is shown")
    public void assertNoResultsShown() {
        boolean noResults = searchPage.isNoResultsDisplayed();
        Base.attachScreenshotToAllure("No Results State");
        Assert.assertTrue("Expected no-results message but it was not found.", noResults);
        Allure.step("✅ No-results message displayed");
    }

    @Then("the results count should be greater than {int}")
    @Step("Assert result count > {minCount}")
    public void assertResultCountGreaterThan(int minCount) {
        int count = searchPage.getResultCount();
        Assert.assertTrue("Expected count > " + minCount + ", got: " + count, count > minCount);
        Allure.step("✅ Count " + count + " > " + minCount);
    }

    @Then("the total results text should be visible")
    @Step("Assert total results text is visible")
    public void assertTotalResultsText() {
        String text = searchPage.getTotalResultsText();
        Assert.assertFalse("Total results text was empty.", text.isEmpty());
        Allure.step("✅ Total results text: " + text);
    }
}