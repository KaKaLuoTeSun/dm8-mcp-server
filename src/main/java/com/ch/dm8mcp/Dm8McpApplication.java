package com.ch.dm8mcp;

import com.ch.dm8mcp.tools.Dm8Tools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Dm8McpApplication {

    public static void main(String[] args) {
        SpringApplication.run(Dm8McpApplication.class, args);
    }

    /**
     * Register all @Tool-annotated methods in Dm8Tools with the MCP server.
     * Spring AI scans this provider at startup and exposes each tool to MCP clients.
     */
    @Bean
    public ToolCallbackProvider dm8ToolCallbackProvider(Dm8Tools dm8Tools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(dm8Tools)
                .build();
    }
}
