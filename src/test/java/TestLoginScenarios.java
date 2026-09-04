

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

public class TestLoginScenarios {

    private static final String URL = "https://devapp.marcoaiot.com/auth/login";

    // Test Data
    private static final String VALID_EMAIL = "pooja.mali@marcoaiot.com";
    private static final String VALID_PASS = "User@123";

    private static final String INVALID_EMAIL = "invalid.user@marcoaiot.com";
    private static final String INVALID_PASS = "WrongPassword@999";

    /**
     * Configures Chrome options and returns the driver instance.
     */
    public static WebDriver setupDriver() {
        System.out.println("Initializing Chrome WebDriver...");
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--start-maximized");

        return new ChromeDriver(options);
    }

    /**
     * Captures a screenshot and saves it with the given filename.
     */
    public static void takeScreenshot(WebDriver driver, String fileName) {
        try {
            TakesScreenshot ts = (TakesScreenshot) driver;
            File source = ts.getScreenshotAs(OutputType.FILE);
            File destination = new File(fileName);
            FileHandler.copy(source, destination);
            System.out.println("Screenshot saved to: " + destination.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save screenshot: " + e.getMessage());
        }
    }

    /**
     * Helper to perform login form submission.
     */
    public static void runLogin(WebDriver driver, String email, String password, String scenarioName) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println(">>> Scenario: " + scenarioName);
        System.out.println("=".repeat(50));
        System.out.println("Navigating to: " + URL);
        driver.get(URL);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // 1. Enter email/username
        System.out.println("Entering Email: " + email);
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        emailField.clear();
        emailField.sendKeys(email);

        // 2. Enter password
        System.out.println("Entering Password: " + password);
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.clear();
        passwordField.sendKeys(password);

        // 3. Click Sign In button
        System.out.println("Clicking 'Sign In' button...");
        WebElement signInBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@type='submit' and contains(., 'Sign In')]")
        ));
        signInBtn.click();
    }

    /**
     * SCENARIO 1: Valid Username and Valid Password
     */
    public static void testScenario1_ValidCredentials() {
        WebDriver driver = setupDriver();
        try {
            runLogin(driver, VALID_EMAIL, VALID_PASS, "1. Valid Username + Valid Password");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            System.out.println("Waiting for dashboard/redirection...");

            try {
                wait.until(d -> !d.getCurrentUrl().contains("auth/login"));
                System.out.println("Redirected successfully! Current URL: " + driver.getCurrentUrl());
            } catch (Exception e) {
                System.out.println("URL check timed out, inspecting page state...");
            }

            Thread.sleep(4000);
            takeScreenshot(driver, "scenario1_valid_login.png");

            String currentUrl = driver.getCurrentUrl();
            if (!currentUrl.contains("auth/login") || currentUrl.contains("tenant") || currentUrl.contains("dashboard")) {
                System.out.println("[PASS] Scenario 1 Passed: Successfully logged in!");
            } else {
                System.out.println("[FAIL] Scenario 1 Failed: User remained on login page.");
            }

        } catch (Exception e) {
            System.err.println("Scenario 1 Error: " + e.getMessage());
            takeScreenshot(driver, "scenario1_error.png");
        } finally {
            driver.quit();
        }
    }

    /**
     * Helper to verify negative test scenarios (expecting login failure)
     */
    private static void verifyNegativeScenario(WebDriver driver, String scenarioName, String screenshotName) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(8));
        System.out.println("Verifying login failure and checking for error message...");

        try {
            WebElement errorElement = wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//*[contains(@class, 'toast') or contains(@class, 'alert') or contains(text(), 'Invalid') or contains(text(), 'incorrect') or contains(text(), 'failed') or contains(text(), 'User not found')]")
                    )
            );
            System.out.println("Error message detected on page: '" + errorElement.getText().trim() + "'");
        } catch (Exception e) {
            System.out.println("No error toast element detected, checking URL state...");
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {}

        takeScreenshot(driver, screenshotName);

        // Negative test is considered PASSED if user is kept on login page (blocked from entering)
        if (driver.getCurrentUrl().contains("auth/login")) {
            System.out.println("[PASS] " + scenarioName + " Passed: Access was correctly blocked.");
        } else {
            System.out.println("[FAIL] " + scenarioName + " Failed: User unexpectedly redirected!");
        }
    }

    /**
     * SCENARIO 2: Valid Username and Invalid Password
     */
    public static void testScenario2_ValidUserInvalidPassword() {
        WebDriver driver = setupDriver();
        try {
            runLogin(driver, VALID_EMAIL, INVALID_PASS, "2. Valid Username + Invalid Password");
            verifyNegativeScenario(driver, "Scenario 2", "scenario2_invalid_password.png");
        } catch (Exception e) {
            System.err.println("Scenario 2 Error: " + e.getMessage());
            takeScreenshot(driver, "scenario2_error.png");
        } finally {
            driver.quit();
        }
    }

    /**
     * SCENARIO 3: Invalid Username and Valid Password
     */
    public static void testScenario3_InvalidUserValidPassword() {
        WebDriver driver = setupDriver();
        try {
            runLogin(driver, INVALID_EMAIL, VALID_PASS, "3. Invalid Username + Valid Password");
            verifyNegativeScenario(driver, "Scenario 3", "scenario3_invalid_username.png");
        } catch (Exception e) {
            System.err.println("Scenario 3 Error: " + e.getMessage());
            takeScreenshot(driver, "scenario3_error.png");
        } finally {
            driver.quit();
        }
    }

    /**
     * SCENARIO 4: Invalid Username and Invalid Password
     */
    public static void testScenario4_InvalidUserInvalidPassword() {
        WebDriver driver = setupDriver();
        try {
            runLogin(driver, INVALID_EMAIL, INVALID_PASS, "4. Invalid Username + Invalid Password");
            verifyNegativeScenario(driver, "Scenario 4", "scenario4_invalid_user_and_pass.png");
        } catch (Exception e) {
            System.err.println("Scenario 4 Error: " + e.getMessage());
            takeScreenshot(driver, "scenario4_error.png");
        } finally {
            driver.quit();
        }
    }

    public static void main(String[] args) {
        System.out.println("\n*******************************************************");
        System.out.println("  STARTING 4-SCENARIO LOGIN AUTOMATION SUITE (JAVA)   ");
        System.out.println("*******************************************************");

        // 1. Valid Username + Valid Password
        testScenario1_ValidCredentials();

        // 2. Valid Username + Invalid Password
        testScenario2_ValidUserInvalidPassword();

        // 3. Invalid Username + Valid Password
        testScenario3_InvalidUserValidPassword();

        // 4. Invalid Username + Invalid Password
        testScenario4_InvalidUserInvalidPassword();

        System.out.println("\n*******************************************************");
        System.out.println("  ALL 4 LOGIN SCENARIOS COMPLETED                      ");
        System.out.println("*******************************************************\n");
    }
}
