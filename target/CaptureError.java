import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.io.FileHandler;
import java.io.File;

public class CaptureError {
    public static void main(String[] args) {
        ChromeOptions opt = new ChromeOptions();
        opt.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage", "--ignore-certificate-errors");
        opt.setAcceptInsecureCerts(true);
        WebDriver driver = new ChromeDriver(opt);
        try {
            driver.get("https://devapp.marcoaiot.com/auth/login");
            Thread.sleep(3000);
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileHandler.copy(src, new File("screenshots/infra_testing_error.png"));
            System.out.println("Page Title: " + driver.getTitle());
            System.out.println("Current URL: " + driver.getCurrentUrl());
            System.out.println("Page Source snippet: " + driver.getPageSource().substring(0, Math.min(300, driver.getPageSource().length())));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}
