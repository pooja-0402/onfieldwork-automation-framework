import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.util.List;

public class InspectErrorPage {
    public static void main(String[] args) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu", "--ignore-certificate-errors");
        options.setAcceptInsecureCerts(true);
        WebDriver driver = new ChromeDriver(options);
        try {
            driver.get("https://devapp.marcoaiot.com/server-error");
            Thread.sleep(2000);
            System.out.println("Page Title: " + driver.getTitle());
            System.out.println("Page URL: " + driver.getCurrentUrl());
            List<WebElement> allButtons = driver.findElements(By.xpath("//button | //a"));
            for (WebElement b : allButtons) {
                System.out.println("Tag: " + b.getTagName() + ", Text: '" + b.getText() + "', href: '" + b.getAttribute("href") + "', class: " + b.getAttribute("class"));
            }
            WebElement goDash = driver.findElement(By.xpath("//button[contains(., 'Go to dashboard')]"));
            System.out.println("Clicking 'Go to dashboard'...");
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", goDash);
            Thread.sleep(3000);
            System.out.println("URL after clicking Go to dashboard: " + driver.getCurrentUrl());
            System.out.println("Page Title after clicking: " + driver.getTitle());
            List<WebElement> h5s = driver.findElements(By.xpath("//h5"));
            System.out.println("H5 elements found: " + h5s.size());
            List<WebElement> infra = driver.findElements(By.xpath("//*[contains(text(), 'Infra Projects')]"));
            System.out.println("Infra Projects found: " + infra.size());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}
