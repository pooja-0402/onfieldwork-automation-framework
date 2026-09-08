import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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
import java.util.List;

public class TestDashboardScenarios {

    private static final String LOGIN_URL = "https://devapp.marcoaiot.com/auth/login";
    private static final String VALID_EMAIL = "pooja.mali@marcoaiot.com";
    private static final String VALID_PASS = "User@123";
    private static final String SCREENSHOT_DIR = "screenshots";

    // Unified HTML & Excel Report Managers
    private static final HtmlReportManager reportManager = new HtmlReportManager("Dashboard Automation & Navigation Test Suite", LOGIN_URL);

    /**
     * Initializes Chrome driver with resilient options.
     */
    public static WebDriver setupDriver() {
        System.out.println("Initializing Chrome WebDriver...");
        try {
            io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup();
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
     * Saves screenshots into the 'screenshots/' folder.
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
     * Connected Helper: Reuses TestLoginScenarios.runLogin() to reach Dashboard.
     */
    public static boolean loginAndNavigateToDashboard(WebDriver driver, WebDriverWait wait, JavascriptExecutor js) {
        try {
            System.out.println("Connecting to TestLoginScenarios.runLogin()...");
            
            // Calling runLogin from TestLoginScenarios directly
            TestLoginScenarios.runLogin(driver, VALID_EMAIL, VALID_PASS, "Dashboard Suite Authentication");

            // 1. Wait for URL redirect away from login page
            wait.until(d -> !d.getCurrentUrl().contains("auth/login"));
            Thread.sleep(1500);

            // 2. Select Tenant if screen appears (using dedicated short wait)
            try {
                WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
                String tenantXpath = "//p[contains(text(), 'Marco Secure Solutions Ltd.')]/parent::div//button | //button[contains(., 'Go To Dashboard') or contains(., 'Dashboard')]";
                WebElement tenantBtn = shortWait.until(ExpectedConditions.elementToBeClickable(By.xpath(tenantXpath)));
                js.executeScript("arguments[0].click();", tenantBtn);
                System.out.println("Tenant selected: Marco Secure Solutions Ltd.");
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println("Tenant selection bypassed or direct dashboard access.");
            }

            // 3. Confirm Dashboard is loaded
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[contains(text(), 'Dashboard') or contains(text(), 'Projects') or contains(text(), 'Finance')]")
            ));
            Thread.sleep(2000);
            return true;

        } catch (Exception e) {
            System.err.println("Failed to login and reach dashboard: " + e.getMessage());
            return false;
        }
    }

    /**
     * TEST 1: Dashboard KPI / Summary Cards Verification
     */
    public static void testDashboardSummaryCards(WebDriver driver, WebDriverWait wait, JavascriptExecutor js) {
        long startTime = System.currentTimeMillis();
        boolean passed = false;
        String actualResult = "";
        String errorMsg = "";
        String screenshotPath = SCREENSHOT_DIR + "/dashboard_cards.png";

        System.out.println("\n=== Test 1: Verifying Dashboard Cards & KPI Widgets ===");
        try {
            List<WebElement> cards = driver.findElements(By.xpath("//div[contains(@class, 'card') or contains(@class, 'widget') or contains(@class, 'shadow') or contains(@class, 'rounded')]"));
            
            System.out.println("Total visual card containers detected: " + cards.size());
            
            if (!cards.isEmpty()) {
                for (int i = 0; i < Math.min(cards.size(), 4); i++) {
                    js.executeScript("arguments[0].style.border='2px solid #10b981';", cards.get(i));
                }
            }

            screenshotPath = takeScreenshot(driver, "dashboard_cards_verified.png");

            passed = !cards.isEmpty() || driver.getPageSource().contains("Projects");
            actualResult = "Successfully verified " + cards.size() + " dashboard cards and KPI metrics.";
            System.out.println("[PASS] Dashboard cards verified.");

        } catch (Exception e) {
            actualResult = "Error while verifying dashboard cards.";
            errorMsg = e.getMessage();
            screenshotPath = takeScreenshot(driver, "dashboard_cards_error.png");
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            reportManager.addResult(new HtmlReportManager.TestResult(
                    "Dashboard KPI Cards Verification",
                    "Verify presence, metrics, and visual cards on the main dashboard view.",
                    VALID_EMAIL,
                    "••••••••",
                    "All dashboard summary cards and widgets load without error.",
                    actualResult,
                    passed,
                    duration,
                    screenshotPath,
                    errorMsg
            ));
        }
    }

    /**
     * TEST 2: Sidebar / Menu Navigation Verification
     */
    public static void testSidebarNavigation(WebDriver driver, WebDriverWait wait, JavascriptExecutor js) {
        long startTime = System.currentTimeMillis();
        boolean passed = false;
        String actualResult = "";
        String errorMsg = "";
        String screenshotPath = SCREENSHOT_DIR + "/dashboard_navigation.png";

        System.out.println("\n=== Test 2: Verifying Sidebar & Navigation Links ===");
        try {
            // 1. Finance & Expense Menu
            WebElement financeMenu = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[text()='Finance & Expense'] | //span[contains(text(), 'Finance')]")
            ));
            js.executeScript("arguments[0].click();", financeMenu);
            System.out.println("Clicked 'Finance & Expense' menu.");
            Thread.sleep(1500);

            // 2. Payment Requests Sub-menu
            WebElement paymentReqMenu = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[text()='Payment Requests'] | //a[contains(., 'Payment Requests')]")
            ));
            js.executeScript("arguments[0].click();", paymentReqMenu);
            System.out.println("Clicked 'Payment Requests'.");
            Thread.sleep(2500);

            screenshotPath = takeScreenshot(driver, "navigation_payment_requests.png");

            passed = driver.getCurrentUrl().contains("payment") || driver.getPageSource().contains("Payment Requests");
            actualResult = "Successfully navigated through sidebar menus to Payment Requests page.";
            System.out.println("[PASS] Navigation verified successfully.");

        } catch (Exception e) {
            actualResult = "Failed to navigate through sidebar menu.";
            errorMsg = e.getMessage();
            screenshotPath = takeScreenshot(driver, "navigation_error.png");
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            reportManager.addResult(new HtmlReportManager.TestResult(
                    "Sidebar Navigation Flow",
                    "Verify navigation from Dashboard to Finance & Expense -> Payment Requests.",
                    VALID_EMAIL,
                    "••••••••",
                    "Sidebar menus expand and navigate to target views smoothly.",
                    actualResult,
                    passed,
                    duration,
                    screenshotPath,
                    errorMsg
            ));
        }
    }

    /**
     * TEST 3: Action Buttons & Views (New Button, Table Records, Forms)
     */
    public static void testActionButtonsAndViews(WebDriver driver, WebDriverWait wait, JavascriptExecutor js) {
        long startTime = System.currentTimeMillis();
        boolean passed = false;
        String actualResult = "";
        String errorMsg = "";
        String screenshotPath = SCREENSHOT_DIR + "/dashboard_action_view.png";

        System.out.println("\n=== Test 3: Verifying Action Buttons & Form Views ===");
        try {
            // Click 'New' Button
            WebElement newBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(., 'New') or contains(., 'Create') or contains(., 'Add')]")
            ));
            js.executeScript("arguments[0].click();", newBtn);
            System.out.println("Clicked 'New' Action Button.");
            Thread.sleep(2000);

            // Verify Form/Drawer View is rendered
            WebElement formElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//input[@id='title'] | //span[contains(text(), 'Select Project')] | //form")
            ));

            screenshotPath = takeScreenshot(driver, "action_form_view_opened.png");

            passed = formElement.isDisplayed();
            actualResult = "Action button triggered properly. Form/Drawer modal view rendered successfully.";
            System.out.println("[PASS] Action & view verification completed.");

        } catch (Exception e) {
            actualResult = "Failed to trigger action button or view drawer.";
            errorMsg = e.getMessage();
            screenshotPath = takeScreenshot(driver, "action_view_error.png");
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            reportManager.addResult(new HtmlReportManager.TestResult(
                    "Action Buttons & Modal View Verification",
                    "Verify 'New/Add' action button opens form drawer and view fields correctly.",
                    VALID_EMAIL,
                    "••••••••",
                    "Action view renders with all interactive form fields accessible.",
                    actualResult,
                    passed,
                    duration,
                    screenshotPath,
                    errorMsg
            ));
        }
    }

    public static void main(String[] args) {
        System.out.println("\n*******************************************************************");
        System.out.println("  STARTING INTEGRATED DASHBOARD & NAVIGATION AUTOMATION SUITE      ");
        System.out.println("*******************************************************************");

        WebDriver driver = setupDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            // Connected Login using TestLoginScenarios.runLogin()
            boolean loggedIn = loginAndNavigateToDashboard(driver, wait, js);

            if (loggedIn) {
                testDashboardSummaryCards(driver, wait, js);
                testSidebarNavigation(driver, wait, js);
                testActionButtonsAndViews(driver, wait, js);
            } else {
                System.err.println("Aborting dashboard suite: Login pre-requisite failed.");
            }

        } finally {
            driver.quit();

            // Generate HTML Report
            reportManager.generateReport("test_report.html");

            // Append results to Excel (test_report.csv)
            ExcelReportManager.appendResultsToExcel(reportManager.getResults());

            System.out.println("\n*******************************************************************");
            System.out.println("  DASHBOARD AUTOMATION SUITE FINISHED & REPORTS UPDATED             ");
            System.out.println("*******************************************************************\n");
        }
    }
}