package com.crowdar;

import com.crowdar.models.Features;
import com.crowdar.models.requests.PromptFeatureRequest;

import com.crowdar.utils.PromptBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

import java.nio.charset.StandardCharsets;

import java.util.List;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class McpServerApplication {
    private static final Logger log = LoggerFactory.getLogger(McpServerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
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

    private static String loadSchemaFromResource(final String path) {
        try (InputStream input = McpServerApplication.class.getClassLoader().getResourceAsStream(path)) {
            if (input != null) {
                return new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }

            throw new IOException("Resource not found: " + path);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load JsonSchema: " + path, e);
        }
    }

    private static McpServerFeatures.SyncToolSpecification getSyncCreateFeatureToolSpecification() {
        String schema = loadSchemaFromResource("schemas/feature.json");

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
        String schema = loadSchemaFromResource("schemas/test.json");

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
        String schema = loadSchemaFromResource("schemas/execute.json");

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
        String schema = loadSchemaFromResource("schemas/reporting.json");

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
