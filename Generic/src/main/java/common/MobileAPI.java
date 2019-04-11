package common;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.LogStatus;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.remote.MobilePlatform;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.touch.TouchActions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.*;
import reporting.ExtentManager;
import reporting.ExtentTestManager;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MobileAPI {
    public static AppiumDriver ad = null;
    /**************** Reporting ****************/
    public static ExtentReports extent;
    public String OS = null;
    public String deviceName = null;
    public String deviceType = null;
    public String appType = null;
    public String version = null;
    public File appDirectory = null;
    public File findApp = null;
    public DesiredCapabilities cap = null;

    public static void scrollKeys(AppiumDriver driver, String[] list, String parent) {
        System.out.println("Starting the process");
        for (int i = 0; i < list.length; i++) {
            MobileElement we = (MobileElement) driver.findElementByXPath(parent + "/UIAPickerWheel[" + (i + 1) + "]");
            we.sendKeys(list[i]);
        }
        System.out.println("Ending Process");
    }

    //screenshot
    public static void captureScreenshot(WebDriver driver, String screenshotName) {

        DateFormat df = new SimpleDateFormat("(MM.dd.yyyy-HH:mma)");
        Date date = new Date();
        df.format(date);

        File file = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(file, new File(System.getProperty("user.dir") + "/screenshots/" + screenshotName + " " + df.format(date) + ".png"));
            System.out.println("Screenshot captured");
        } catch (Exception e) {
            System.out.println("Exception while taking screenshot " + e.getMessage());
        }
    }

    public static String convertToString(String st) {
        String splitString = "";
        //splitString = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(st), ' ');
        return splitString;
    }

    /**
     * This method will swipe either up, Down, left or Right according to the
     * direction specified. This method takes the size of the screen and uses
     * the swipe function present in the Appium driver to swipe on the screen
     * with a particular timeout. There is one more method to implement swipe
     * using touch actions, which is not put up here.
     *
     * @param Direction The direction we need to swipe in.
     * @param swipeTime The swipe time, ie the time for which the driver is supposed
     *                  to swipe.
     * @param Offset    The offset for the driver, eg. If you want to swipe 'up', then
     *                  the offset is the number of pixels you want to leave from the
     *                  bottom of the screen t start the swipe.
     * @Author - Zann
     * @Modified By -
     */

    public static void functionSwipe(String Direction, int swipeTime, int Offset) {
        Dimension size;
        size = (ad).manage().window().getSize();
        int starty = (int) (size.height * 0.80);
        int endy = (int) (size.height * 0.20);
        int startx = size.width / 2;
        if (Direction.equalsIgnoreCase("Up")) {
            ((AppiumDriver<WebElement>) (ad)).swipe(startx / 2, starty - Offset, startx / 2, endy, swipeTime);
        } else if (Direction.equalsIgnoreCase("Down")) {
            ((AppiumDriver<WebElement>) (ad)).swipe(startx / 2, endy + Offset, startx / 2, starty, swipeTime);
        } else if (Direction.equalsIgnoreCase("Right")) {
            starty = size.height / 2;
            endy = size.height / 2;
            startx = (int) (size.width * 0.10);
            int endx = (int) (size.width * 0.90);
            ((AppiumDriver<WebElement>) (ad)).swipe(startx + Offset, starty, endx, endy, swipeTime);
        } else if (Direction.equalsIgnoreCase("Left")) {
            starty = size.height / 2;
            endy = size.height / 2;
            startx = (int) (size.width * 0.90);
            int endx = (int) (size.width * 0.10);
            ((AppiumDriver<WebElement>) (ad)).swipe(startx - Offset, starty, endx, endy, swipeTime);
        }
    }

    @BeforeSuite
    public void extentSetup(ITestContext context) {
        ExtentManager.setOutputDirectory(context);
        extent = ExtentManager.getInstance();
    }

    @BeforeMethod
    public void startExtent(Method method) {
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName().toLowerCase();
        ExtentTestManager.startTest(method.getName());
        ExtentTestManager.getTest().assignCategory(className);
    }

    protected String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

    @AfterMethod
    public void afterEachTestMethod(ITestResult result) {
        ExtentTestManager.getTest().getTest().setStartedTime(getTime(result.getStartMillis()));
        ExtentTestManager.getTest().getTest().setEndedTime(getTime(result.getEndMillis()));

        for (String group : result.getMethod().getGroups()) {
            ExtentTestManager.getTest().assignCategory(group);
        }

        if (result.getStatus() == 1) {
            ExtentTestManager.getTest().log(LogStatus.PASS, "Test Passed");
        } else if (result.getStatus() == 2) {
            ExtentTestManager.getTest().log(LogStatus.FAIL, getStackTrace(result.getThrowable()));
        } else if (result.getStatus() == 3) {
            ExtentTestManager.getTest().log(LogStatus.SKIP, "Test Skipped");
        }
        ExtentTestManager.endTest();
        extent.flush();
        if (result.getStatus() == ITestResult.FAILURE) {
            captureScreenshot(ad, result.getName());
        }
//        driver.quit();
    }

    @AfterSuite
    public void generateReport() {
        extent.close();
    }

    private Date getTime(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.getTime();
    }

    /*************** Reporting *****************/

    @Parameters({"OS", "appType", "deviceType", "deviceName", "version", "file", "fileName"})
    @BeforeMethod
    public void setUp(@Optional("Android") String OS, @Optional("Phone") String appType, @Optional("Emulator") String deviceType,
                      @Optional("Galaxy s6 edge") String deviceName,
                      @Optional("7") String version, @Optional("src/app") String file, @Optional("Cricbuzz.apk") String fileName) throws IOException {

        if (OS.equalsIgnoreCase("ios")) {
            if (appType.contains("iPhone")) {
                appDirectory = new File("/Users/jewal/IntelliJ/MobileAutomationJanuary2019/UICatalog/src/app/UICatalog6.1.app.zip");
                findApp = new File(appDirectory, "UICatalog6.1.app.zip");
                if (deviceType.equalsIgnoreCase("RealDevice")) {
                    cap = new DesiredCapabilities();
                    cap.setCapability(MobileCapabilityType.DEVICE_NAME, deviceName);
                    cap.setCapability(MobileCapabilityType.PLATFORM_NAME, MobilePlatform.IOS);
                    cap.setCapability(MobileCapabilityType.PLATFORM_VERSION, version);
                    cap.setCapability(MobileCapabilityType.AUTOMATION_NAME, "XCUITest");
                    cap.setCapability(MobileCapabilityType.APP, findApp.getAbsolutePath());
                    ad = new IOSDriver(new URL("http://10.11.11.169:1122/wd/hub"), cap);
                    ad.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

                } else if (deviceType.equalsIgnoreCase("Simulator")) {
                    cap = new DesiredCapabilities();
                    cap.setCapability(MobileCapabilityType.DEVICE_NAME, deviceName);
                    cap.setCapability(MobileCapabilityType.PLATFORM_NAME, MobilePlatform.IOS);
                    cap.setCapability(MobileCapabilityType.PLATFORM_VERSION, version);
                    cap.setCapability(MobileCapabilityType.AUTOMATION_NAME, "XCUITest");
                    cap.setCapability(MobileCapabilityType.APP, appDirectory);//findApp.getAbsolutePath()
                    ad = new IOSDriver(new URL("http://10.11.11.169:1122/wd/hub"), cap);
                    ad.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
                }

            } else if (appType.equalsIgnoreCase("iPad 2")) {
                appDirectory = new File("IOS/src/app");
                findApp = new File(appDirectory, "UICatalog6.1.app.zip");
                if (deviceType.contains("RealDevice")) {
                    cap = new DesiredCapabilities();
                    cap.setCapability(MobileCapabilityType.DEVICE_NAME, deviceName);
                    cap.setCapability(MobileCapabilityType.PLATFORM_NAME, MobilePlatform.IOS);
                    cap.setCapability(MobileCapabilityType.PLATFORM_VERSION, version);
                    cap.setCapability(MobileCapabilityType.AUTOMATION_NAME, "XCUITest");
                    cap.setCapability(MobileCapabilityType.APP, findApp.getAbsolutePath());
                    ad = new IOSDriver(new URL("http://10.11.11.169:1122/wd/hub"), cap);
                    ad.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

                } else if (deviceType.equalsIgnoreCase("Simulator")) {
                    cap = new DesiredCapabilities();
                    cap.setCapability(MobileCapabilityType.DEVICE_NAME, deviceName);
                    cap.setCapability(MobileCapabilityType.PLATFORM_NAME, MobilePlatform.IOS);
                    cap.setCapability(MobileCapabilityType.PLATFORM_VERSION, version);
                    cap.setCapability(MobileCapabilityType.AUTOMATION_NAME, "XCUITest");
                    cap.setCapability(MobileCapabilityType.APP, findApp.getAbsolutePath());
                    ad = new IOSDriver(new URL("http://10.11.11.169:1122/wd/hub"), cap);
                    ad.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
                }
            }
        } else if (OS.contains("Android")) {
            if (appType.contains("Phone")) {
                appDirectory = new File(file);
                findApp = new File(appDirectory, fileName);
                if (deviceType.equalsIgnoreCase("RealDevice")) {
                    cap = new DesiredCapabilities();
                    cap.setCapability(MobileCapabilityType.UDID,"ce07171779230cc30c7e");
                    cap.setCapability(MobileCapabilityType.DEVICE_NAME, deviceName);
                    cap.setCapability(MobileCapabilityType.PLATFORM_NAME, MobilePlatform.ANDROID);
                    cap.setCapability(MobileCapabilityType.PLATFORM_VERSION, version);
                    cap.setCapability(MobileCapabilityType.APP, findApp.getAbsolutePath());
                    //cap.setCapability(MobileCapabilityType.AUTOMATION_NAME, "UiAutomator2");
                    ad = new AndroidDriver(new URL("http://10.11.11.169:1122/wd/hub"), cap);
                    ad.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

                } else if (deviceType.equalsIgnoreCase("Emulator")) {
                    cap = new DesiredCapabilities();
                    cap.setCapability(MobileCapabilityType.DEVICE_NAME, deviceName);
                    cap.setCapability(MobileCapabilityType.PLATFORM_NAME, MobilePlatform.ANDROID);
                    cap.setCapability(MobileCapabilityType.PLATFORM_VERSION, version);
                    cap.setCapability(MobileCapabilityType.APP, findApp.getAbsolutePath());
                    cap.setCapability(MobileCapabilityType.AUTOMATION_NAME, "UiAutomator2");
                    ad = new AndroidDriver(new URL("http://10.11.11.169:1122"), cap);
                    ad.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
                    // ad.findElement(By.xpath("/hierarchy/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.FrameLayout/android.widget.FrameLayout/android.widget.FrameLayout/android.view.ViewGroup/android.widget.Button")).click();
                }

            } else if (OS.equalsIgnoreCase("Tablets")) {
                if (deviceType.equalsIgnoreCase("RealDevice")) {
                    cap = new DesiredCapabilities();
                    cap.setCapability(MobileCapabilityType.DEVICE_NAME, deviceName);
                    cap.setCapability(MobileCapabilityType.PLATFORM_NAME, MobilePlatform.ANDROID);
                    cap.setCapability(MobileCapabilityType.PLATFORM_VERSION, version);
                    cap.setCapability(MobileCapabilityType.APP, findApp.getAbsolutePath());
                    ad = new AndroidDriver(new URL("http://10.11.11.169:1122/wd/hub"), cap);
                    cap.setCapability(MobileCapabilityType.AUTOMATION_NAME, "UiAutomator2");
                    ad.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

                } else if (deviceType.equalsIgnoreCase("Emulator")) {
                    cap = new DesiredCapabilities();
                    cap.setCapability(MobileCapabilityType.DEVICE_NAME, deviceName);
                    cap.setCapability(MobileCapabilityType.PLATFORM_NAME, MobilePlatform.ANDROID);
                    cap.setCapability(MobileCapabilityType.PLATFORM_VERSION, version);
                    cap.setCapability(MobileCapabilityType.APP, findApp.getAbsolutePath());
                    ad = new AndroidDriver(new URL("http://10.11.11.169:1122/wd/hub"), cap);
                    ad.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
                }
            }
        }
    }

    @AfterMethod
    public void cleanUpApp() {
        ad.quit();
    }

    public void clickByXpath(String locator) {
        ad.findElement(By.xpath(locator)).click();
    }

    public void clickByXpath(MobileElement locator) {
        locator.click();
    }


    public void clickByXpathWebElement(WebElement locator) {
        locator.click();
    }

    public void sleep(int sec) throws InterruptedException {
        Thread.sleep(1000 * sec);
    }


    public void typeByXpath(String locator, String value) {
        ad.findElement(By.xpath(locator)).sendKeys(value);
    }

    public List<String> getTexts(List<WebElement> elements) {
        List<String> text = new ArrayList<String>();

        for (WebElement element : elements) {
            text.add(element.getText());
        }

        return text;
    }

    public void scrollToElement(AppiumDriver driver, String element) {
        MobileElement we = (MobileElement) driver.findElementByXPath(element);
        driver.scrollTo(we.getText());
    }

    public void alertAccept(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, 5);
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (Exception e) {
            System.err.println("No alert visible in 5 seconds");
        }
    }

    public void scrollAndClickByName(String locator) {
        ad.scrollTo(locator).click();
    }

    public static void swipeFromOneToAnother(MobileElement element1, MobileElement element2) {
        try {
            TouchActions actions = new TouchActions(ad);
            ad.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
            Point location1 = element1.getLocation();
            int X1 = location1.getX();
            int Y1 = location1.getY();
            Point location2 = element2.getLocation();
            int X2 = location2.getX();
            int Y2 = location2.getY();

            actions.scroll(X1, Y1).move(X2, Y2).release().perform();
        } catch (Exception e) {
        }
    }
}

