### Lippia MCP Server
This is a custom MCP (Model Context Protocol) server for Lippia, designed to work with any Autonomous Agent.  
It allows you to run Lippia as a MCP server, exposing tools like
* `lippia:create_test`
* `lippia:execute_test`
* `lippia:get_report`

## Getting Started
__Cursor MCPs Configuration__

```json
{
    "mcpServers": {
        "selenium": {
            "command": "npx",
            "args": [ "-y", "@angiejones/mcp-selenium" ]
        },
        "lippia": {
            "command": "java",
            "args": ["-jar", "<path-to-your-repository>/target/mcp-server-1.0-SNAPSHOT.jar"],
            "env": {
                "OPENAI_API_KEY": "<your-openai-api-key>"
            }
        }
    }
}
```

__Minimalist Architecture__  
__to be completed__