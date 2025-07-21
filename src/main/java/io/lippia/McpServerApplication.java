package io.lippia;

import com.crowdar.core.actions.ActionManager;

import io.appium.java_client.MobileBy;
import io.lippia.models.Features;
import io.lippia.models.requests.PromptFeatureRequest;

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
import org.openqa.selenium.By;
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

        // browser management
        var syncOpenBrowserToolSpecification = getSyncOpenBrowserToolSpecification();
        var syncNavigateToToolSpecification = getSyncNavigateToToolSpecification();
        var syncCloseBrowserToolSpecification = getSyncCloseBrowserToolSpecification();

        // wait
        var syncWaitClickableToolSpecification = getSyncWaitClickableToolSpecification();
        var syncWaitVisibilityToolSpecification = getSyncWaitVisibilityToolSpecification();
        var syncWaitVisibilitiesToolSpecification = getSyncWaitVisibilitiesToolSpecification();
        var syncWaitInvisibilityToolSpecification = getSyncWaitInvisibilityToolSpecification();
        var syncWaitInvisibilitiesToolSpecification = getSyncWaitInvisibilitiesToolSpecification();
        var syncWaitPresenceToolSpecification = getSyncWaitPresenceToolSpecification();
        var syncWaitPresencesToolSpecification = getSyncWaitPresencesToolSpecification();

        // verification
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
                new McpSchema.Tool("create_feature", "Genera un prompt para crear feature files a partir una o más user stories", schema),
                (exchange, args) -> {
                    String userStory = (String) args.get("userStory");

                    String prompt = PromptBuilder.buildPromptForFeatures(
                            new PromptFeatureRequest(userStory)
                    );

                    McpSchema.GetPromptResult gpr = new McpSchema.GetPromptResult(
                            "Generar archivos .feature",
                            List.of(new McpSchema.PromptMessage(McpSchema.Role.USER, new McpSchema.TextContent(prompt)))
                    );

                    return new McpSchema.CallToolResult(String.valueOf(gpr), false);
                }
        );
    }

    private static McpServerFeatures.SyncToolSpecification getSyncCreateJavaTestToolSpecification() {
        String schema = Resources.load("schemas/test.json");

        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("create_test", "Genera los tests a partir de uno o más feature files", schema),
                (McpSyncServerExchange exchange, Map<String, Object> arguments) -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        String json = mapper.writeValueAsString(arguments);
                        Features features = mapper.readValue(json, Features.class);

                        final String prompt = PromptBuilder.buildPromptForSteps(features);

                        McpSchema.GetPromptResult gpr = new McpSchema.GetPromptResult(
                                "Generar el glue code de los features",
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
                new McpSchema.Tool("execute_test", "Ejecuta los tests por tag", schema),
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
                new McpSchema.Tool("get_report", "Retorna la ruta del reporte generado post ejecución", schema),
                (McpSyncServerExchange exchange, Map<String, Object> arguments) -> {
                    log.info("Getting report with arguments: {}", arguments);
                    String file = arguments.get("file").toString();
                    String command = String.format("""
                Please open the following file with chrome or firefox:

                ```bash
                <current_working_dir>/target/reports/%s"
                ```
                """, file);
                    McpSchema.Content content = new McpSchema.TextContent(command);
                    return new McpSchema.CallToolResult(List.of(content), false);
                }
        );
    }

    private static McpServerFeatures.SyncToolSpecification getSyncOpenBrowserToolSpecification() {
        String schema = Resources.load("schemas/open_browser.json");
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("open_browser", "Abre una nueva instancia de navegador Chrome usando Lippia.", schema),
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
                new McpSchema.Tool("navigate_to", "Navega a una URL usando el driver abierto.", schema),
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
                new McpSchema.Tool("close_browser", "Cierra la instancia del navegador abierta.", schema),
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
                new McpSchema.Tool("click", "Hace click en un elemento.", schema),
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
                new McpSchema.Tool("type", "Escribe texto en un campo de entrada.", schema),
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
                new McpSchema.Tool("get_text", "Obtiene el texto de un elemento.", schema),
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
                new McpSchema.Tool("screenshot", "Toma una captura de pantalla y la guarda en un archivo.", schema),
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
                new McpSchema.Tool("wait_clickable", "Espera a que un elemento sea clickable", schema),
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
                new McpSchema.Tool("wait_visibility", "Espera a que un elemento sea encontrado.", schema),
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
                new McpSchema.Tool("wait_visibilities", "Espera a que más de un elemento sean encontrados", schema),
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
                new McpSchema.Tool("wait_invisibility", "Espera a que un elemento no sea encontrado.", schema),
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
                new McpSchema.Tool("wait_invisibilities", "Espera a que más de un elemento no sean encontrados", schema),
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
                new McpSchema.Tool("wait_presence", "Espera a que un elemento esté presente", schema),
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
                new McpSchema.Tool("wait_presences", "Espera a que más de un elemento estén presentes", schema),
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
                new McpSchema.Tool("is_enabled", "Verifica una única vez si el elemento está habilitado", schema),
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
                new McpSchema.Tool("is_selected", "Verifica una única vez si el elemento está seleccionado", schema),
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
                new McpSchema.Tool("is_present", "Verifica una única vez si el elemento está presente", schema),
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
                new McpSchema.Tool("is_visible", "Verifica una única vez si el elemento es visible", schema),
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
                new McpSchema.Tool("get_page_title", "Obtiene el título de la página actual.", schema),
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
                new McpSchema.Tool("get_page_source", "Obtiene el código fuente HTML completo de la página actual.", schema),
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
