import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class TestMarcoAIoTLogin {

    public static void testLogin() {
        WebDriver driver = new ChromeDriver();

        try {
            System.out.println("Navigating to https://stageapp.marcoaiot.com/...");
            driver.get("https://stageapp.marcoaiot.com/");
            driver.manage().window().maximize();

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            System.out.println("Entering username...");
            WebElement usernameField = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.id("username"))
            );
            usernameField.sendKeys("pooja.mali@marcoaiot.com");

            System.out.println("Entering password...");
            WebElement passwordField = driver.findElement(By.id("password"));
            passwordField.sendKeys("User@123");

            System.out.println("Clicking 'Sign In' button...");
            WebElement loginButton = driver.findElement(By.cssSelector("button.btn-primary"));
            loginButton.click();

            System.out.println("Waiting for login to complete and dashboard to load...");
            WebDriverWait dashboardWait = new WebDriverWait(driver, Duration.ofSeconds(15));
            dashboardWait.until(
                    ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), 'Projects')]"))
            );

            System.out.println("Login Test Passed: Dashboard loaded successfully.");

        } catch (Exception e) {
            System.out.println("Login Test Failed: " + e.getMessage());
        } finally {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {}
            driver.quit();
        }
    }

    public static void main(String[] args) {
        testLogin();
    }
}
