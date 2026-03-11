@search @regression
Feature: Myntra Search Functionality

  Background:
    Given the user is on the Myntra homepage

  @smoke
  Scenario: Search for a valid keyword shows relevant products
    When  the user searches for "men's t-shirt"
    Then  search results should be displayed
    And   the results should contain products related to "t-shirt"
    And   at least 10 products should be visible

  @smoke
  Scenario: Search for a brand name shows that brand's products
    When  the user searches for "Nike"
    Then  search results should be displayed
    And   the results should contain products related to "Nike"
    And   the results count should be greater than 5

  Scenario: Searching for a product category returns results
    When  the user searches for "running shoes"
    Then  search results should be displayed
    And   the total results text should be visible
    And   at least 5 products should be visible

  Scenario: Searching for a gibberish term shows no results message
    When  the user searches for "xyzqwerty12345nonexistent"
    Then  a no results message should be displayed

  Scenario: Multi-word search returns relevant results
    When  the user searches for "blue denim jacket"
    Then  search results should be displayed
    And   at least 1 products should be visible

  Scenario: User can click on a search result and view the product
    When  the user searches for "kurta"
    And   search results should be displayed
    And   the user clicks on the first search result
    Then  search results should be displayed