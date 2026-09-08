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
import java.time.Duration;

public class TestMarcoAIoTLogin {

    private static final String APP_URL = "https://stageapp.marcoaiot.com/auth/login";

    public static WebDriver setupDriver() {
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

    public static void testLogin() {
        WebDriver driver = setupDriver();

        try {
            System.out.println("Navigating to " + APP_URL + "...");
            driver.get(APP_URL);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            System.out.println("Entering username...");
            WebElement usernameField = wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//input[@id='username' or @placeholder='Enter your email' or @type='email']")
                    )
            );
            usernameField.clear();
            usernameField.sendKeys("pooja.mali@marcoaiot.com");

            System.out.println("Entering password...");
            WebElement passwordField = wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//input[@id='password' or @type='password']")
                    )
            );
            passwordField.clear();
            passwordField.sendKeys("User@123");

            System.out.println("Clicking 'Sign In' button...");
            WebElement loginButton = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[@type='submit' or contains(., 'Sign In') or contains(@class, 'btn-primary')]")
                    )
            );
            loginButton.click();

            System.out.println("Waiting for login to complete and dashboard/tenant to load...");
            wait.until(d -> !d.getCurrentUrl().contains("auth/login"));

            System.out.println("Login Test Passed: Successfully authenticated. Current URL: " + driver.getCurrentUrl());

        } catch (Exception e) {
            System.err.println("Login Test Failed: " + e.getMessage());
            try {
                File folder = new File("screenshots");
                if (!folder.exists()) folder.mkdirs();
                File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                FileHandler.copy(src, new File(folder, "stageapp_login_error.png"));
            } catch (Exception ignored) {}
        } finally {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {}
            driver.quit();
        }
    }

    public static void main(String[] args) {
        testLogin();
    }
}
