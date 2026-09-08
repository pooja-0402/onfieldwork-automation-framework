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
import java.time.Duration;
import java.util.List;

public class InspectProjectCard {
    public static void main(String[] args) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu", "--window-size=1920,1080", "--start-maximized", "--headless=new", "--ignore-certificate-errors");
        options.setAcceptInsecureCerts(true);
        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            driver.get("https://devapp.marcoaiot.com/auth/login");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username"))).sendKeys("pooja.mali@marcoaiot.com");
            driver.findElement(By.id("password")).sendKeys("User@123");
            driver.findElement(By.xpath("//button[@type='submit']")).click();
            wait.until(d -> !d.getCurrentUrl().contains("auth/login"));
            Thread.sleep(2000);

            try {
                WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//p[contains(text(), 'Marco Secure Solutions Ltd.')]/parent::div//button")));
                js.executeScript("arguments[0].click();", btn);
                Thread.sleep(2000);
            } catch (Exception ignored) {}

            WebElement infraMenu = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[text()='Infra Projects'] | //span[contains(text(), 'Infra Projects')] | //div[contains(text(), 'Infra Projects')]")
            ));
            js.executeScript("arguments[0].click();", infraMenu);
            Thread.sleep(1500);

            WebElement projListMenu = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[contains(text(), 'Project List')] | //a[contains(@href, 'projects')]")
            ));
            js.executeScript("arguments[0].click();", projListMenu);
            Thread.sleep(2500);

            WebElement projectCard = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//main//h5[text()='ANP'] | //h5[text()='ANP'] | //h5[1]")
            ));
            js.executeScript("arguments[0].click();", projectCard);
            Thread.sleep(3000);

            System.out.println("URL on Details: " + driver.getCurrentUrl());
            WebElement profileCard = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[contains(text(), 'Project Profile')]")
            ));
            System.out.println("Profile Card verified: " + profileCard.getText());

            // Inspect all elements with text 'Infrastructure'
            List<WebElement> infraElements = driver.findElements(By.xpath("//*[text()='Infrastructure']"));
            System.out.println("Elements with exact text 'Infrastructure': " + infraElements.size());
            for (int i = 0; i < infraElements.size(); i++) {
                WebElement el = infraElements.get(i);
                System.out.println("Element " + i + ": Tag=" + el.getTagName() + ", Parent=" + el.findElement(By.xpath("..")).getTagName() + ", Displayed=" + el.isDisplayed());
                if (el.isDisplayed()) {
                    System.out.println("Clicking element " + i + " via JS...");
                    js.executeScript("arguments[0].click();", el);
                    Thread.sleep(2000);
                    takeScreenshot(driver, "screenshots/infra_tab_clicked_" + i + ".png");
                }
            }

            System.out.println("TEST COMPLETED SUCCESSFULLY!");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    public static void takeScreenshot(WebDriver driver, String path) {
        try {
            TakesScreenshot ts = (TakesScreenshot) driver;
            File src = ts.getScreenshotAs(OutputType.FILE);
            File dest = new File(path);
            dest.getParentFile().mkdirs();
            FileHandler.copy(src, dest);
            System.out.println("Saved screenshot: " + dest.getAbsolutePath());
        } catch (Exception ignored) {}
    }
}
