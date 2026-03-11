package stepdefinitions;

import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import pages.Base;

/**
 * Hooks.java
 * Cucumber lifecycle hooks – runs around every scenario.
 */
public class Hooks {

    @Before(order = 1)
    public void setUp(Scenario scenario) {
        System.out.println("\n========================================");
        System.out.println("▶ SCENARIO: " + scenario.getName());
        System.out.println("   Tags    : " + scenario.getSourceTagNames());
        System.out.println("========================================");
        Base.initDriver();
        Allure.step("🚀 Starting scenario: " + scenario.getName());
    }

    @After(order = 1)
    public void tearDown(Scenario scenario) {
        if (scenario.isFailed()) {
            System.out.println("[Hooks] ❌ FAILED: " + scenario.getName());

            // Attach to Allure
            Base.attachScreenshotToAllure("Failure Screenshot – " + scenario.getName());

            // Embed into Cucumber report
            try {
                byte[] screenshot = ((TakesScreenshot) Base.getDriver())
                    .getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "Failure Screenshot");
            } catch (Exception e) {
                System.out.println("[Hooks] Could not embed screenshot: " + e.getMessage());
            }

            // Attach page source
            try {
                byte[] src = Base.getDriver().getPageSource().getBytes();
                Allure.getLifecycle()
                      .addAttachment("Page Source", "text/html", "html", src);
            } catch (Exception e) {
                System.out.println("[Hooks] Could not attach page source: " + e.getMessage());
            }

        } else {
            System.out.println("[Hooks] ✅ PASSED: " + scenario.getName());
            Allure.step("✅ Scenario completed successfully");
        }

        Base.quitDriver();
        System.out.println("========================================\n");
    }

    @AfterStep
    public void afterEachStep(Scenario scenario) {
        if (scenario.isFailed() && Base.getDriver() != null) {
            Base.attachScreenshotToAllure("Step Failure – " + scenario.getName());
        }
    }
}