

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
    private static final String SCREENSHOT_DIR = "screenshots";

    // Test Credentials
    private static final String VALID_EMAIL = "pooja.mali@marcoaiot.com";
    private static final String VALID_PASS = "User@123";

    private static final String INVALID_EMAIL = "invalid.user@marcoaiot.com";
    private static final String INVALID_PASS = "WrongPassword@999";

    // Unified HTML Report Manager
    private static final HtmlReportManager reportManager = new HtmlReportManager("Login Automation Test Suite", URL);

    /**
     * Configures Chrome options and returns the driver instance.
     */
    public static WebDriver setupDriver() {
        System.out.println("Initializing Chrome WebDriver...");
        try {
            WebDriverManager.chromedriver().setup();
        } catch (Exception e) {
            System.out.println("WebDriverManager setup fallback: " + e.getMessage());
        }

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--start-maximized");
        options.addArguments("--ignore-certificate-errors");
        options.setAcceptInsecureCerts(true);

        return new ChromeDriver(options);
    }

    /**
     * Captures a screenshot and saves it inside the 'screenshots/' folder.
     */
    public static String takeScreenshot(WebDriver driver, String fileName) {
        try {
            File folder = new File(SCREENSHOT_DIR);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File destination = new File(folder, fileName);
            TakesScreenshot ts = (TakesScreenshot) driver;
            File source = ts.getScreenshotAs(OutputType.FILE);
            FileHandler.copy(source, destination);

            String relativePath = SCREENSHOT_DIR + "/" + fileName;
            System.out.println("Screenshot saved: " + destination.getAbsolutePath());
            return relativePath;
        } catch (IOException e) {
            System.err.println("Failed to save screenshot: " + e.getMessage());
            return fileName;
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

        // 1. Enter Email / Username
        System.out.println("Entering Email: " + email);
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//input[@id='username' or @placeholder='Enter your email' or @type='email' or @name='username']")
        ));
        emailField.clear();
        emailField.sendKeys(email);

        // 2. Enter Password
        System.out.println("Entering Password: " + password);
        WebElement passwordField = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//input[@id='password' or @type='password' or @name='password']")
        ));
        passwordField.clear();
        passwordField.sendKeys(password);

        // 3. Click Sign In button
        System.out.println("Clicking 'Sign In' button...");
        WebElement signInBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@type='submit' or contains(., 'Sign In')]")
        ));
        signInBtn.click();
    }

    /**
     * SCENARIO 1: Valid Username and Valid Password
     */
    public static void testScenario1_ValidCredentials() {
        long startTime = System.currentTimeMillis();
        boolean passed = false;
        String actualResult = "";
        String errorMsg = "";
        String screenshotPath = SCREENSHOT_DIR + "/scenario1_valid_login.png";

        WebDriver driver = setupDriver();
        try {
            runLogin(driver, VALID_EMAIL, VALID_PASS, "1. Valid Username + Valid Password");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            try {
                wait.until(d -> !d.getCurrentUrl().contains("auth/login"));
                System.out.println("Redirected! Current URL: " + driver.getCurrentUrl());
            } catch (Exception e) {
                System.out.println("URL check timed out, inspecting page state...");
            }

            Thread.sleep(3000);
            screenshotPath = takeScreenshot(driver, "scenario1_valid_login.png");

            String currentUrl = driver.getCurrentUrl();
            if (!currentUrl.contains("auth/login") || currentUrl.contains("tenant") || currentUrl.contains("dashboard") || currentUrl.contains("projects")) {
                passed = true;
                actualResult = "User successfully authenticated and redirected to " + currentUrl;
                System.out.println("[PASS] Scenario 1 Passed: Successfully logged in!");
            } else {
                actualResult = "User stayed on login page despite valid credentials.";
                System.out.println("[FAIL] Scenario 1 Failed: User remained on login page.");
            }

        } catch (Exception e) {
            actualResult = "Exception during login execution";
            errorMsg = e.getMessage();
            screenshotPath = takeScreenshot(driver, "scenario1_error.png");
            System.err.println("[FAIL] " + e.getMessage());
        } finally {
            driver.quit();
            long duration = System.currentTimeMillis() - startTime;
            reportManager.addResult(new HtmlReportManager.TestResult(
                    "Valid Username + Valid Password",
                    "Verify user can successfully log in with valid credentials.",
                    VALID_EMAIL,
                    VALID_PASS,
                    "Redirect away from login page to dashboard/tenant selection.",
                    actualResult,
                    passed,
                    duration,
                    screenshotPath,
                    errorMsg
            ));
        }
    }

    /**
     * Helper to execute and verify negative test scenarios (expecting login rejection).
     */
    private static void executeNegativeScenario(String scenarioTitle, String desc, String email, String password, String screenshotName) {
        long startTime = System.currentTimeMillis();
        boolean passed = false;
        String actualResult = "";
        String errorMsg = "";
        String screenshotPath = SCREENSHOT_DIR + "/" + screenshotName;

        WebDriver driver = setupDriver();
        try {
            runLogin(driver, email, password, scenarioTitle);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(8));
            System.out.println("Verifying login failure and checking for error feedback...");

            String detectedToast = "";
            try {
                WebElement errorElement = wait.until(
                        ExpectedConditions.presenceOfElementLocated(
                                By.xpath("//*[contains(@class, 'toast') or contains(@class, 'alert') or contains(text(), 'Invalid') or contains(text(), 'incorrect') or contains(text(), 'failed') or contains(text(), 'User not found')]")
                        )
                );
                detectedToast = errorElement.getText().trim();
                System.out.println("Error message detected on page: '" + detectedToast + "'");
            } catch (Exception e) {
                System.out.println("No error toast element detected, checking URL state...");
            }

            Thread.sleep(2500);
            screenshotPath = takeScreenshot(driver, screenshotName);

            if (driver.getCurrentUrl().contains("auth/login")) {
                passed = true;
                actualResult = "Access blocked correctly. Stays on login page." + (detectedToast.isEmpty() ? "" : " Error: " + detectedToast);
                System.out.println("[PASS] " + scenarioTitle + " Passed: Access was correctly blocked.");
            } else {
                actualResult = "User unexpectedly logged in with invalid credentials.";
                System.out.println("[FAIL] " + scenarioTitle + " Failed: User unexpectedly redirected!");
            }

        } catch (Exception e) {
            actualResult = "Exception during test execution";
            errorMsg = e.getMessage();
            screenshotPath = takeScreenshot(driver, "error_" + screenshotName);
            System.err.println("[FAIL] " + e.getMessage());
        } finally {
            driver.quit();
            long duration = System.currentTimeMillis() - startTime;
            reportManager.addResult(new HtmlReportManager.TestResult(
                    scenarioTitle,
                    desc,
                    email,
                    password,
                    "Login denied, remain on login page with error feedback.",
                    actualResult,
                    passed,
                    duration,
                    screenshotPath,
                    errorMsg
            ));
        }
    }

    /**
     * SCENARIO 2: Valid Username and Invalid Password
     */
    public static void testScenario2_ValidUserInvalidPassword() {
        executeNegativeScenario(
                "Valid Username + Invalid Password",
                "Verify authentication is rejected when entering a wrong password.",
                VALID_EMAIL,
                INVALID_PASS,
                "scenario2_invalid_password.png"
        );
    }

    /**
     * SCENARIO 3: Invalid Username and Valid Password
     */
    public static void testScenario3_InvalidUserValidPassword() {
        executeNegativeScenario(
                "Invalid Username + Valid Password",
                "Verify authentication is rejected when username is non-existent.",
                INVALID_EMAIL,
                VALID_PASS,
                "scenario3_invalid_username.png"
        );
    }

    /**
     * SCENARIO 4: Invalid Username and Invalid Password
     */
    public static void testScenario4_InvalidUserInvalidPassword() {
        executeNegativeScenario(
                "Invalid Username + Invalid Password",
                "Verify authentication is rejected when both username and password are invalid.",
                INVALID_EMAIL,
                INVALID_PASS,
                "scenario4_invalid_user_and_pass.png"
        );
    }

    public static void main(String[] args) {
        System.out.println("\n*******************************************************");
        System.out.println("  STARTING 4-SCENARIO LOGIN AUTOMATION SUITE (JAVA)   ");
        System.out.println("*******************************************************");

        // 1. Execute all 4 scenarios
        testScenario1_ValidCredentials();
        testScenario2_ValidUserInvalidPassword();
        testScenario3_InvalidUserValidPassword();
        testScenario4_InvalidUserInvalidPassword();

        // 2. Automatically generate / update HTML Report
        reportManager.generateReport("test_report.html");

        // 3. Automatically append results to Excel (test_report.csv)
        ExcelReportManager.appendResultsToExcel(reportManager.getResults());

        System.out.println("\n*******************************************************");
        System.out.println("  ALL 4 SCENARIOS COMPLETED: HTML & EXCEL REPORTS READY");
        System.out.println("*******************************************************\n");
    }
}
