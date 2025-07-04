### Lippia MCP Server
This is a custom MCP (Model Context Protocol) server for Lippia, designed to work with any Autonomous Agent that supports the protocol.  
It allows you to run Lippia as a MCP server, exposing tools like
* `lippia:create_feature`
* `lippia:create_test`
* `lippia:execute_test`
* `lippia:get_report`

## System Requirements: 
- **Git** https://gitforwindows.org/
- **JDK 17+** https://www.oracle.com/java/technologies/downloads/
- **Maven** https://maven.apache.org/download.cgi

## Getting Started

__Repository__
```bash
$ git clone https://gitlab.crowdaronline.com/lippia/products/lippia-framework/mcp-server/lippia-mcp-server.git && cd "$(basename "$_" .git)"
```

__Packaging__
```bash
$ mvn clean package
```

### Agent Configuration
__Cursor__

```json
{
    "mcpServers": {
        "selenium": {
            "command": "npx",
            "args": [ "-y", "@angiejones/mcp-selenium" ]
        },
        "lippia": {
            "command": "java",
            "args": ["-jar", "<path-to-your-repository>/target/mcp-server-1.0-SNAPSHOT.jar"]
        }
    }
}
```

## __[GitLab MCP Server Pages](https://lippia-mcp-server-657e5f.pages.crowdaronline.com/)__