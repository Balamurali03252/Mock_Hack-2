@checkout_parity @regression
Feature: Myntra Guest vs Logged-in Checkout Parity

  Background:
    Given the user opens Myntra product URL directly

  @smoke @guest_flow
  Scenario: Guest user adds product to cart and is redirected to login on checkout
    When  the user selects an available size if required
    And   the user clicks "Add to Bag" button
    And   the user navigates to the cart
    When  the user captures cart values as guest
    Then  the subtotal should be greater than zero
    When  the user clicks "Place Order" button
    Then  the login or signup page should appear

  @parity @logged_in_flow
  Scenario: Cart totals are consistent between guest and logged-in user for the same product

    # -- PHASE 1: Guest User Flow --
    When  the user selects an available size if required
    And   the user clicks "Add to Bag" button
    And   the user navigates to the cart
    When  the user captures cart values as guest
    When  the user clicks "Place Order" button
    Then  the login or signup page should appear
    When  the user clears cookies to remove guest session

    # -- PHASE 2: Logged-in User Flow --
    When  the user loads saved login cookies
    And   the user opens Myntra product URL directly
    And   the user selects an available size if required
    And   the user clicks "Add to Bag" button
    And   the user navigates to the cart
    When  the user captures cart values as logged-in user

    # -- Validation --
    Then  the cart totals for guest and logged-in user should match

  @logged_in_flow
  Scenario: Logged-in user proceeds to address page after clicking Place Order
    When  the user loads saved login cookies
    And   the user opens Myntra product URL directly
    And   the user selects an available size if required
    And   the user clicks "Add to Bag" button
    And   the user navigates to the cart
    When  the user clicks "Place Order" button
    Then  the address selection page should appear for the logged-in user