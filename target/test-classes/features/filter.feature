@filter @regression
Feature: Myntra Filter and Sort Functionality

  @smoke
  Scenario: User filters products by brand name
    Given the user is on the Myntra search results page for "t-shirts"
    And   the user records the initial product count
    When  the user filters by brand "H&M"
    Then  the filter "H&M" should be shown as applied
    And   the search results should be filtered and visible

  @smoke
  Scenario: User filters products by price range
    Given the user is on the Myntra search results page for "jeans"
    And   the user records the initial product count
    When  the user filters by price range 500 to 1500
    Then  the search results should be filtered and visible
    And   the results count should be at least 1

  Scenario: User filters products by discount percentage
    Given the user is on the Myntra search results page for "dresses"
    When  the user filters by discount "50% and above"
    Then  the search results should be filtered and visible

  Scenario: User filters products by clothing size
    Given the user is on the Myntra search results page for "men shirts"
    When  the user filters by size "M"
    Then  the search results should be filtered and visible
    And   the results count should be at least 1

  @smoke
  Scenario: User sorts products by price from low to high
    Given the user is on the Myntra search results page for "sneakers"
    When  the user sorts results by "Price: Low to High"
    Then  the search results should be filtered and visible
    And   the results count should be at least 5

  Scenario: User sorts products by popularity
    Given the user is on the Myntra search results page for "handbags"
    When  the user sorts results by "Popularity"
    Then  the search results should be filtered and visible

  Scenario: User applies size and price filters together
    Given the user is on the Myntra search results page for "kurta"
    And   the user records the initial product count
    When  the user filters by price range 300 to 1000
    And   the user filters by size "S"
    Then  the search results should be filtered and visible

  Scenario: Clearing all filters resets search results
    Given the user is on the Myntra search results page for "t-shirts"
    When  the user filters by brand "H&M"
    And   the search results should be filtered and visible
    And   the user clears all filters
    Then  the applied filters should be cleared
    And   the search results should be filtered and visible