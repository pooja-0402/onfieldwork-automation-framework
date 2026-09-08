import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class MasterTest {

    private static final Logger logger = Logger.getLogger(MasterTest.class.getName());
    private static FileHandler fh = null;

    public static void setupLogger() {
        try {
            fh = new FileHandler("selenium_run.log", true);
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
        } catch (IOException e) {
            System.err.println("Failed to initialize logger file handler: " + e.getMessage());
        }
    }

    public static void closeLogger() {
        if (fh != null) {
            try {
                fh.close();
                logger.removeHandler(fh);
            } catch (Exception ignored) {}
        }
    }

    public static void main(String[] args) {
        setupLogger();

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

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        JavascriptExecutor js = (JavascriptExecutor) driver;

        logger.info("--- STABLE TEST SESSION STARTED ---");

        try {
            // --- LOGIN ---
            driver.get("https://stageapp.marcoaiot.com/auth/login");
            
            WebElement emailInput = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//input[@placeholder='Enter your email' or @id='username' or @type='email']")
            ));
            emailInput.sendKeys("pooja.mali@marcoaiot.com");

            driver.findElement(By.xpath("//input[@type='password' or @id='password']")).sendKeys("User@123");
            driver.findElement(By.xpath("//button[contains(text(), 'Sign In') or @type='submit']")).click();

            // --- TENANT SELECTION ---
            String tenantXpath = "//p[contains(text(), 'Marco Secure Solutions Ltd.')]/parent::div//button | //button[contains(., 'Dashboard')]";
            WebElement dashboardBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(tenantXpath)));
            js.executeScript("arguments[0].click();", dashboardBtn);
            logger.info("Tenant selected.");

            // --- NAVIGATION ---
            WebElement financeMenu = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[text()='Finance & Expense'] | //span[contains(text(), 'Finance')]")));
            financeMenu.click();

            WebElement paymentRequestsMenu = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[text()='Payment Requests'] | //a[contains(., 'Payment Requests')]")));
            paymentRequestsMenu.click();

            WebElement newBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(., 'New')]")));
            js.executeScript("arguments[0].click();", newBtn);
            logger.info("Form opened.");

            // --- FORM FILLING ---
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='Select Project']"))).click();
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Aurus Tech Pvt Ltd')] | //*[contains(text(), 'Aurus Tech')]"))).click();

            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='Select Category']"))).click();
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[contains(text(), 'Vendor/Supplier Payments')] | //*[contains(text(), 'Vendor')]"))).click();

            driver.findElement(By.id("title")).sendKeys("payment request for vendor and supplier");

            // Radio button 'No'
            WebElement radioNo = driver.findElement(By.id("isVariableFalse"));
            js.executeScript("arguments[0].click();", radioNo);

            // Dynamic date input via JS (1 month in future)
            String targetDate = LocalDate.now().plusMonths(1).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            logger.info("Injecting dynamic date via JavaScript: " + targetDate);
            WebElement dateInput = driver.findElement(By.xpath("//input[@placeholder='DD-MM-YYYY']"));
            js.executeScript("arguments[0].value = '" + targetDate + "';", dateInput);
            js.executeScript("arguments[0].dispatchEvent(new Event('change'));", dateInput);

            // Amount & Payee
            driver.findElement(By.id("amount")).sendKeys("400");
            WebElement payee = driver.findElement(By.xpath("//label[contains(text(),'Payee Name')]/following-sibling::input | //input[@id='payeeName']"));
            payee.sendKeys("pooja mali");

            // Description
            driver.findElement(By.id("description")).sendKeys("payment request");

            // --- SAVE AS DRAFT ---
            WebElement saveDraftBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Save as Draft']")));
            js.executeScript("arguments[0].scrollIntoView();", saveDraftBtn);
            js.executeScript("arguments[0].click();", saveDraftBtn);

            logger.info("Form saved as draft successfully.");
            System.out.println("Success: Test completed without crashes!");

            Thread.sleep(3000);

        } catch (Exception e) {
            logger.severe("Test Failed: " + e.getMessage());
            try {
                File dir = new File("screenshots");
                if (!dir.exists()) dir.mkdirs();
                TakesScreenshot ts = (TakesScreenshot) driver;
                File source = ts.getScreenshotAs(OutputType.FILE);
                org.openqa.selenium.io.FileHandler.copy(source, new File(dir, "final_crash_report.png"));
            } catch (Exception ex) {
                logger.severe("Could not take screenshot: Session already closed.");
            }
        } finally {
            driver.quit();
            logger.info("Session closed.");
            closeLogger();
        }
    }
}
