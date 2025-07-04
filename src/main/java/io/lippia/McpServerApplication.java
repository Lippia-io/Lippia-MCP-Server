package io.lippia;

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

import java.util.List;
import java.util.Map;

public class McpServerApplication {
    private static final Logger log = LoggerFactory.getLogger(McpServerApplication.class);

    public static void main(String[] args) {
        // Inicialización MCP Server
        var transportProvider = new StdioServerTransportProvider();
        var syncToolCreateFeatureSpecification = getSyncCreateFeatureToolSpecification();
        var syncToolSpecification = getSyncCreateJavaTestToolSpecification();
        var syncExecutionToolSpecification = getSyncExecutionToolSpecification();
        var syncReportingToolSpecification = getSyncReportingToolSpecification();

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
                        syncReportingToolSpecification)
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
}
