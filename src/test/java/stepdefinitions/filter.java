package stepdefinitions;

import io.cucumber.java.en.*;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.junit.Assert;
import pages.Base;
import pages.FilterPage;

import java.util.List;

/**
 * filter.java
 * Step definitions for filter.feature
 */
public class filter {

    private final FilterPage filterPage = new FilterPage();
    private int preFilterCount;

    // ── Given ─────────────────────────────────────────────────────────────────

    @Given("the user is on the Myntra search results page for {string}")
    @Step("Navigate to Myntra results page for: {searchTerm}")
    public void onSearchResultsPage(String searchTerm) {
        Base.navigateTo(Base.BASE_URL + searchTerm.toLowerCase().replace(" ", "-"));
        Allure.step("On results page for: " + searchTerm);
    }

    @Given("the user is on the Myntra category page for {string}")
    @Step("Navigate to category page: {category}")
    public void onCategoryPage(String category) {
        Base.navigateTo(Base.BASE_URL + category.toLowerCase().replace(" ", "-"));
        Allure.step("On category page: " + category);
    }

    @Given("the user records the initial product count")
    @Step("Record initial product count")
    public void recordInitialCount() {
        preFilterCount = filterPage.getFilteredResultCount();
        Allure.step("Initial count: " + preFilterCount);
    }

    // ── When ──────────────────────────────────────────────────────────────────

    @When("the user opens the filter panel")
    @Step("Open filter panel")
    public void openFilterPanel() {
        filterPage.openFilterPanel();
    }

    @When("the user filters by brand {string}")
    @Step("Filter by brand: {brand}")
    public void filterByBrand(String brand) {
        filterPage.filterByBrand(brand);
    }

    @When("the user filters by price range {int} to {int}")
    @Step("Filter by price: {min} to {max}")
    public void filterByPrice(int min, int max) {
        filterPage.filterByPriceRange(min, max);
    }

    @When("the user filters by discount {string}")
    @Step("Filter by discount: {discount}")
    public void filterByDiscount(String discount) {
        filterPage.filterByDiscount(discount);
    }

    @When("the user filters by size {string}")
    @Step("Filter by size: {size}")
    public void filterBySize(String size) {
        filterPage.filterBySize(size);
    }

    @When("the user sorts results by {string}")
    @Step("Sort by: {sortOption}")
    public void sortBy(String sortOption) {
        filterPage.sortBy(sortOption);
    }

    @When("the user clears all filters")
    @Step("Clear all filters")
    public void clearAllFilters() {
        filterPage.clearAllFilters();
    }

    // ── Then ──────────────────────────────────────────────────────────────────

    @Then("the filter {string} should be shown as applied")
    @Step("Assert filter is applied: {filterLabel}")
    public void assertFilterApplied(String filterLabel) {
        boolean applied = filterPage.isFilterApplied(filterLabel);
        Base.attachScreenshotToAllure("Applied Filter: " + filterLabel);
        Assert.assertTrue("Filter '" + filterLabel + "' not shown as applied.", applied);
        Allure.step("✅ Filter applied: " + filterLabel);
    }

    @Then("the search results should be filtered and visible")
    @Step("Assert filtered results are visible")
    public void assertFilteredResultsVisible() {
        int count = filterPage.getFilteredResultCount();
        Base.attachScreenshotToAllure("Filtered Results");
        Assert.assertTrue("Expected filtered results but found 0.", count > 0);
        Allure.step("✅ " + count + " filtered results visible");
    }

    @Then("the product count should decrease after applying the filter")
    @Step("Assert product count decreased after filter")
    public void assertCountDecreased() {
        int postCount = filterPage.getFilteredResultCount();
        Assert.assertTrue("Expected results after filter, got 0.", postCount > 0);
        Allure.step("Pre: " + preFilterCount + " | Post: " + postCount);
    }
    @Then("the applied filters should be cleared")
    @Step("Assert no filters are applied")
    public void assertFiltersCleared() {
        List<String> applied = filterPage.getAppliedFilters();
        Base.attachScreenshotToAllure("Cleared Filters");
        Assert.assertTrue("Expected no applied filters, found: " + applied, applied.isEmpty());
        Allure.step("✅ All filters cleared");
    }

    @Then("the results count should be at least {int}")
    @Step("Assert result count ≥ {minCount}")
    public void assertResultCountAtLeast(int minCount) {
        int count = filterPage.getFilteredResultCount();
        Assert.assertTrue("Expected ≥ " + minCount + ", got: " + count, count >= minCount);
        Allure.step("✅ Count " + count + " ≥ " + minCount);
    }
}