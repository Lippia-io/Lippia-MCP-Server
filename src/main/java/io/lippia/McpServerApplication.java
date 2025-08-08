package io.lippia;

import com.crowdar.core.actions.ActionManager;

import io.lippia.models.Features;
import io.lippia.models.requests.PromptFeatureRequest;

import io.lippia.reporting.Notifier;
import io.lippia.reporting.NotifierServiceFactory;

import io.lippia.utils.PromptBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.lippia.utils.Resources;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.File;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.Map;

import com.crowdar.driver.DriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.OutputType;
import org.apache.commons.io.FileUtils;

public class McpServerApplication {
    private static final Logger log = LoggerFactory.getLogger(McpServerApplication.class);

    public static void main(String[] args) {
        var transportProvider = new StdioServerTransportProvider();

        /* Code Building Tools */
        var syncToolCreateFeatureSpecification = getSyncCreateFeatureToolSpecification();
        var syncToolSpecification = getSyncCreateJavaTestToolSpecification();
        var syncExecutionToolSpecification = getSyncExecutionToolSpecification();
        var syncReportingToolSpecification = getSyncReportingToolSpecification();

        /* Navigation Tools */

        // browser managements
        var syncOpenBrowserToolSpecification = getSyncOpenBrowserToolSpecification();
        var syncNavigateToToolSpecification = getSyncNavigateToToolSpecification();
        var syncCloseBrowserToolSpecification = getSyncCloseBrowserToolSpecification();

        // waits
        var syncWaitClickableToolSpecification = getSyncWaitClickableToolSpecification();
        var syncWaitVisibilityToolSpecification = getSyncWaitVisibilityToolSpecification();
        var syncWaitVisibilitiesToolSpecification = getSyncWaitVisibilitiesToolSpecification();
        var syncWaitInvisibilityToolSpecification = getSyncWaitInvisibilityToolSpecification();
        var syncWaitInvisibilitiesToolSpecification = getSyncWaitInvisibilitiesToolSpecification();
        var syncWaitPresenceToolSpecification = getSyncWaitPresenceToolSpecification();
        var syncWaitPresencesToolSpecification = getSyncWaitPresencesToolSpecification();

        // verifications
        var syncIsEnabledToolSpecification = getSyncIsEnabledToolSpecification();
        var syncIsSelectedToolSpecification = getSyncIsSelectedToolSpecification();
        var syncIsPresentToolSpecification = getSyncIsPresentToolSpecification();
        var syncIsVisibleToolSpecification = getSyncIsVisibleToolSpecification();

        // actions
        var syncClickToolSpecification = getSyncClickToolSpecification();
        var syncTypeToolSpecification = getSyncTypeToolSpecification();
        var syncGetTextToolSpecification = getSyncGetTextToolSpecification();
        var syncScreenshotToolSpecification = getSyncScreenshotToolSpecification();
        var syncGetPageTitleToolSpecification = getSyncGetPageTitleToolSpecification();
        var syncGetPageSourceToolSpecification = getSyncGetPageSourceToolSpecification();

        McpServer.sync(transportProvider)
                .serverInfo("lippia-mcp-server", "1.0")
                .capabilities(McpSchema.ServerCapabilities.builder()
                        .tools(true)
                        .prompts(true)
                        .logging()
                        .build())
                .tools(
                        syncToolCreateFeatureSpecification,
                        syncToolSpecification,
                        syncExecutionToolSpecification,
                        syncReportingToolSpecification,
                        syncOpenBrowserToolSpecification,
                        syncNavigateToToolSpecification,
                        syncCloseBrowserToolSpecification,
                        syncWaitClickableToolSpecification,
                        syncWaitVisibilityToolSpecification,
                        syncWaitVisibilitiesToolSpecification,
                        syncWaitInvisibilityToolSpecification,
                        syncWaitInvisibilitiesToolSpecification,
                        syncWaitPresenceToolSpecification,
                        syncWaitPresencesToolSpecification,
                        syncIsEnabledToolSpecification,
                        syncIsSelectedToolSpecification,
                        syncIsPresentToolSpecification,
                        syncIsVisibleToolSpecification,
                        syncClickToolSpecification,
                        syncTypeToolSpecification,
                        syncGetTextToolSpecification,
                        syncScreenshotToolSpecification,
                        syncGetPageTitleToolSpecification,
                        syncGetPageSourceToolSpecification)
                .build();

        log.info("Lippia MCP Server initialized!");
    }

    private static McpServerFeatures.SyncToolSpecification getSyncCreateFeatureToolSpecification() {
        String schema = Resources.load("schemas/feature.json");

        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("create_feature", "Generates a prompt to create feature files from one or more user stories", schema),
                (exchange, args) -> {
                    String userStory = (String) args.get("userStory");

                    String prompt = PromptBuilder.buildPromptForFeatures(
                            new PromptFeatureRequest(userStory)
                    );

                    McpSchema.GetPromptResult gpr = new McpSchema.GetPromptResult(
                            "Generate .feature files",
                            List.of(new McpSchema.PromptMessage(McpSchema.Role.USER, new McpSchema.TextContent(prompt)))
                    );

                    return new McpSchema.CallToolResult(String.valueOf(gpr), false);
                }
        );
    }

    private static McpServerFeatures.SyncToolSpecification getSyncCreateJavaTestToolSpecification() {
        String schema = Resources.load("schemas/test.json");

        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("create_test", "Generates tests from one or more feature files", schema),
                (McpSyncServerExchange exchange, Map<String, Object> arguments) -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        String json = mapper.writeValueAsString(arguments);
                        Features features = mapper.readValue(json, Features.class);

                        final String prompt = PromptBuilder.buildPromptForSteps(features);

                        McpSchema.GetPromptResult gpr = new McpSchema.GetPromptResult(
                                "Generate the glue code for the features",
                                List.of(new McpSchema.PromptMessage(McpSchema.Role.USER, new McpSchema.TextContent(prompt)))
                        );

                        return new McpSchema.CallToolResult(String.valueOf(gpr), false);
                    } catch (IOException e) {
                        throw new RuntimeException("Error deserializing arguments", e);
                    }
                }
        );
    }

    private static McpServerFeatures.SyncToolSpecification getSyncExecutionToolSpecification() {
        String schema = Resources.load("schemas/execute.json");

        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("execute_test", "Executes tests by tag", schema),
                (McpSyncServerExchange exchange, Map<String, Object> arguments) -> {
                    log.info("Executing tests with arguments: {}", arguments);
                    String tag = arguments.get("tag").toString();
                    String command = String.format("""
                Please run the following command in the terminal:

                ```bash
                mvn clean test -D"cucumber.tags=@%s"
                ```
                """, tag);
                    McpSchema.Content content = new McpSchema.TextContent(command);
                    return new McpSchema.CallToolResult(List.of(content), false);
                }
        );
    }

    private static McpServerFeatures.SyncToolSpecification getSyncReportingToolSpecification() {
        String schema = Resources.load("schemas/reporting.json");

        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool(
                        "send_report",
                        "Sends a report of work completed so far",
                        schema
                ),
                (McpSyncServerExchange exchange, Map<String, Object> arguments) -> {
                    log.info("Sending report with arguments: {}", arguments);

                    Object reportChannel = arguments.get("channel");
                    Object reportObj = arguments.get("message");

                    String channel = reportChannel != null ? reportChannel.toString() : null;
                    String message = reportObj != null ? reportObj.toString() : null;

                    if (message != null && !message.isBlank()) {
                        try {
                            Notifier service = NotifierServiceFactory.find(channel);
                            if (service == null) {
                                return new McpSchema.CallToolResult(
                                        List.of(new McpSchema.TextContent(
                                                String.format("Error sending report, channel %s does not exist: ", channel))),
                                        true
                                );
                            }

                            service.sendMessage(message);
                            log.info("Report sent to {}.", channel);
                        } catch (Exception e) {
                            log.error("Error sending report to {}", channel);
                            return new McpSchema.CallToolResult(
                                    List.of(new McpSchema.TextContent("Error sending report: " + e.getMessage())),
                                    true
                            );
                        }
                    } else {
                        log.warn("Report is empty or null.");
                        return new McpSchema.CallToolResult(
                                List.of(new McpSchema.TextContent("Report content is empty.")),
                                true
                        );
                    }

                    return new McpSchema.CallToolResult(
                            List.of(new McpSchema.TextContent("Report sent successfully to " + channel)),
                            false
                    );
                }
        );
    }

    private static McpServerFeatures.SyncToolSpecification getSyncOpenBrowserToolSpecification() {
        String schema = Resources.load("schemas/open_browser.json");
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("open_browser", "Opens a new Chrome browser instance using Lippia.", schema),
                (exchange, args) -> {
                    try {
                        DriverManager.getDriverInstance();
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Browser opened successfully.")), false);
                    } catch (Exception e) {
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Error opening browser: " + e.getMessage())), true);
                    }
                }
        );
    }

    private static McpServerFeatures.SyncToolSpecification getSyncNavigateToToolSpecification() {
        String schema = Resources.load("schemas/navigate_to.json");
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("navigate_to", "Navigates to a URL using the open driver.", schema),
                (exchange, args) -> {
                    try {
                        String url = (String) args.get("url");
                        DriverManager.getDriverInstance().get(url);
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Navigated to URL: " + url)), false);
                    } catch (Exception e) {
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Error navigating to URL: " + e.getMessage())), true);
                    }
                }
        );
    }

    private static McpServerFeatures.SyncToolSpecification getSyncCloseBrowserToolSpecification() {
        String schema = Resources.load("schemas/close_browser.json");
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("close_browser", "Closes the open browser instance.", schema),
                (exchange, args) -> {
                    try {
                        DriverManager.getDriverInstance().quit();
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Browser closed successfully.")), false);
                    } catch (Exception e) {
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Error closing browser: " + e.getMessage())), true);
                    }
                }
        );
    }

    private static McpServerFeatures.SyncToolSpecification getSyncClickToolSpecification() {
        String schema = Resources.load("schemas/click.json");
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("click", "Clicks on an element.", schema),
                (exchange, args) -> {
                    try {
                        String selector = (String) args.get("selector");
                        String by = (String) args.get("by");

                        ActionManager.click(by + ":" + selector);
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Clicked on element: " + by + ":" + selector)), false);
                    } catch (Exception e) {
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Error clicking element: " + e.getMessage())), true);
                    }
                }
        );
    }

    private static McpServerFeatures.SyncToolSpecification getSyncTypeToolSpecification() {
        String schema = Resources.load("schemas/type.json");
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("type", "Types text into an input field.", schema),
                (exchange, args) -> {
                    try {
                        String selector = (String) args.get("selector");
                        String by = (String) args.get("by");
                        String text = (String) args.get("text");

                        ActionManager.setInput(by + ":" + selector, text, true);
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Typed text '" + text + "' into element: " + by + ":" + selector)), false);
                    } catch (Exception e) {
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Error typing text: " + e.getMessage())), true);
                    }
                }
        );
    }

    private static McpServerFeatures.SyncToolSpecification getSyncGetTextToolSpecification() {
        String schema = Resources.load("schemas/get_text.json");
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("get_text", "Gets the text from an element.", schema),
                (exchange, args) -> {
                    try {
                        String selector = (String) args.get("selector");
                        String by = (String) args.get("by");
                        String text = ActionManager.getText(by + ":" + selector);
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Text from element '" + by + ":" + selector + "': " + text)), false);
                    } catch (Exception e) {
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Error getting text: " + e.getMessage())), true);
                    }
                }
        );
    }

    private static McpServerFeatures.SyncToolSpecification getSyncScreenshotToolSpecification() {
        String schema = Resources.load("schemas/screenshot.json");
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("screenshot", "Takes a screenshot and saves it to a file.", schema),
                (exchange, args) -> {
                    try {
                        String filename = (String) args.get("filename");
                        if (filename == null || filename.isEmpty()) {
                            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                            filename = "screenshot_" + timestamp + ".png";
                        }
                        
                        WebDriver driver = DriverManager.getDriverInstance();
                        TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
                        File sourceFile = takesScreenshot.getScreenshotAs(OutputType.FILE);
                        File destFile = new File(filename);
                        FileUtils.copyFile(sourceFile, destFile);
                        
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Screenshot saved as: " + destFile.getAbsolutePath())), false);
                    } catch (Exception e) {
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Error taking screenshot: " + e.getMessage())), true);
                    }
                }
        );
    }

    // ******************* Waits *******************
    private static McpServerFeatures.SyncToolSpecification getSyncWaitClickableToolSpecification() {
        String schema = Resources.load("schemas/navigation/wait/wait_clickable.json");
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("wait_clickable", "Waits for an element to be clickable", schema),
                (exchange, args) -> {
                    try {
                        String by = (String) args.get("by");
                        String selector = (String) args.get("selector");

                        WebElement we = ActionManager.waitClickable(by + ":" + selector);
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Element is clickable: " + we)), false);
                    } catch (Exception e) {
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Element is not clickable: " + e.getMessage())), true);
                    }
                }
        );
    }

    private static McpServerFeatures.SyncToolSpecification getSyncWaitVisibilityToolSpecification() {
        String schema = Resources.load("schemas/navigation/wait/wait_visibility.json");
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("wait_visibility", "Waits for an element to be found.", schema),
                (exchange, args) -> {
                    try {
                        String by = (String) args.get("by");
                        String selector = (String) args.get("selector");

                        WebElement we = ActionManager.waitVisibility(by + ":" + selector);
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Element was found: " + we)), false);
                    } catch (Exception e) {
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Element was not found: " + e.getMessage())), true);
                    }
                }
        );
    }

    private static McpServerFeatures.SyncToolSpecification getSyncWaitVisibilitiesToolSpecification() {
        String schema = Resources.load("schemas/navigation/wait/wait_visibilities.json");
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("wait_visibilities", "Waits for multiple elements to be found", schema),
                (exchange, args) -> {
                    try {
                        String by = (String) args.get("by");
                        String selector = (String) args.get("selector");
                        List<WebElement> we = ActionManager.waitVisibilities(by + ":" + selector);
                        if (!we.isEmpty()) {
                            return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Elements was found: " + we)), false);
                        }

                        throw new Exception();
                    } catch (Exception e) {
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Element was not found: " + e.getMessage())), true);
                    }
                }
        );
    }

    private static McpServerFeatures.SyncToolSpecification getSyncWaitInvisibilityToolSpecification() {
        String schema = Resources.load("schemas/navigation/wait/wait_invisibility.json");
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("wait_invisibility", "Waits for an element to not be found.", schema),
                (exchange, args) -> {
                    try {
                        String by = (String) args.get("by");
                        String selector = (String) args.get("selector");

                        WebElement we = ActionManager.waitVisibility(by + ":" + selector);
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Element was not found: " + we)), false);
                    } catch (Exception e) {
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Element was found: " + e.getMessage())), true);
                    }
                }
        );
    }

    private static McpServerFeatures.SyncToolSpecification getSyncWaitInvisibilitiesToolSpecification() {
        String schema = Resources.load("schemas/navigation/wait/wait_invisibilities.json");
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("wait_invisibilities", "Waits for multiple elements to not be found", schema),
                (exchange, args) -> {
                    try {
                        String by = (String) args.get("by");
                        String selector = (String) args.get("selector");
                        List<WebElement> we = ActionManager.waitVisibilities(by + ":" + selector);
                        if (we.isEmpty()) {
                            return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Elements was not found: " + we)), false);
                        }

                        throw new Exception();
                    } catch (Exception e) {
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Element was found: " + e.getMessage())), true);
                    }
                }
        );
    }

    private static McpServerFeatures.SyncToolSpecification getSyncWaitPresenceToolSpecification() {
        String schema = Resources.load("schemas/navigation/wait/wait_presence.json");
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("wait_presence", "Waits for an element to be present", schema),
                (exchange, args) -> {
                    try {
                        String by = (String) args.get("by");
                        String selector = (String) args.get("selector");

                        WebElement we = ActionManager.waitPresence(by + ":" + selector);
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Element was present: " + we)), false);
                    } catch (Exception e) {
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Element was not present: " + e.getMessage())), true);
                    }
                }
        );
    }

    private static McpServerFeatures.SyncToolSpecification getSyncWaitPresencesToolSpecification() {
        String schema = Resources.load("schemas/navigation/wait/wait_presences.json");
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("wait_presences", "Waits for multiple elements to be present", schema),
                (exchange, args) -> {
                    try {
                        String by = (String) args.get("by");
                        String selector = (String) args.get("selector");
                        List<WebElement> we = ActionManager.waitPresences(by + ":" + selector);
                        if (!we.isEmpty()) {
                            return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Elements was present: " + we)), false);
                        }

                        throw new Exception();
                    } catch (Exception e) {
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Element was not present: " + e.getMessage())), true);
                    }
                }
        );
    }

    // ******************* Verify *******************
    private static McpServerFeatures.SyncToolSpecification getSyncIsEnabledToolSpecification() {
        String schema = Resources.load("schemas/navigation/verification/is_enabled.json");
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("is_enabled", "Verifies once if the element is enabled", schema),
                (exchange, args) -> {
                    try {
                        String by = (String) args.get("by");
                        String selector = (String) args.get("selector");

                        boolean isEnabled = ActionManager.isEnabled(by + ":" + selector);
                        if (isEnabled) {
                            return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Element is enabled")), false);
                        }

                        throw new Exception();
                    } catch (Exception e) {
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Element is not enabled" + e.getMessage())), true);
                    }
                }
        );
    }

    private static McpServerFeatures.SyncToolSpecification getSyncIsSelectedToolSpecification() {
        String schema = Resources.load("schemas/navigation/verification/is_selected.json");
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("is_selected", "Verifies once if the element is selected", schema),
                (exchange, args) -> {
                    try {
                        String by = (String) args.get("by");
                        String selector = (String) args.get("selector");

                        boolean isSelected = ActionManager.isSelected(by + ":" + selector);
                        if (isSelected) {
                            return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Element is selected")), false);
                        }

                        throw new Exception();
                    } catch (Exception e) {
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Element is not selected" + e.getMessage())), true);
                    }
                }
        );
    }

    private static McpServerFeatures.SyncToolSpecification getSyncIsPresentToolSpecification() {
        String schema = Resources.load("schemas/navigation/verification/is_present.json");
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("is_present", "Verifies once if the element is present", schema),
                (exchange, args) -> {
                    try {
                        String by = (String) args.get("by");
                        String selector = (String) args.get("selector");

                        boolean isPresent = ActionManager.isPresent(by + ":" + selector);
                        if (isPresent) {
                            return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Element is present")), false);
                        }

                        throw new Exception();
                    } catch (Exception e) {
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Element is not present" + e.getMessage())), true);
                    }
                }
        );
    }

    private static McpServerFeatures.SyncToolSpecification getSyncIsVisibleToolSpecification() {
        String schema = Resources.load("schemas/navigation/verification/is_visible.json");
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("is_visible", "Verifies once if the element is visible", schema),
                (exchange, args) -> {
                    try {
                        String by = (String) args.get("by");
                        String selector = (String) args.get("selector");

                        boolean isPresent = ActionManager.isPresent(by + ":" + selector);
                        if (isPresent) {
                            return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Element is visible")), false);
                        }

                        throw new Exception();
                    } catch (Exception e) {
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Element is not visible" + e.getMessage())), true);
                    }
                }
        );
    }

    // ******************* Actions *******************

    private static McpServerFeatures.SyncToolSpecification getSyncGetPageTitleToolSpecification() {
        String schema = Resources.load("schemas/get_page_title.json");
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("get_page_title", "Gets the title of the current page.", schema),
                (exchange, args) -> {
                    try {
                        WebDriver driver = DriverManager.getDriverInstance();
                        String title = driver.getTitle();
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Page title: " + title)), false);
                    } catch (Exception e) {
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Error getting page title: " + e.getMessage())), true);
                    }
                }
        );
    }

    private static McpServerFeatures.SyncToolSpecification getSyncGetPageSourceToolSpecification() {
        String schema = Resources.load("schemas/get_page_source.json");
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("get_page_source", "Gets the complete HTML source code of the current page.", schema),
                (exchange, args) -> {
                    try {
                        WebDriver driver = DriverManager.getDriverInstance();
                        String pageSource = driver.getPageSource();
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Page HTML source:\n\n" + pageSource)), false);
                    } catch (Exception e) {
                        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Error getting page source: " + e.getMessage())), true);
                    }
                }
        );
    }
}
