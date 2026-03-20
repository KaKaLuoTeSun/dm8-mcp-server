package com.ch.dm8mcp.tools;

import com.ch.dm8mcp.service.Dm8QueryService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class Dm8Tools {

    private final Dm8QueryService queryService;

    public Dm8Tools(Dm8QueryService queryService) {
        this.queryService = queryService;
    }

    @Tool(description = "List all user-defined schemas in the DM8 database")
    public Map<String, Object> listSchemas() {
        try {
            List<String> schemas = queryService.listSchemas();
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("schemas", schemas);
            return result;
        } catch (DataAccessException e) {
            throw new RuntimeException("database error: " + e.getMessage(), e);
        }
    }

    @Tool(description = "List all table names in the specified DM8 schema. schemaName is case-insensitive.")
    public Map<String, Object> listTables(
            @ToolParam(description = "Schema name (case-insensitive)") String schemaName) {
        if (schemaName == null || schemaName.isBlank()) {
            throw new IllegalArgumentException("schemaName must not be empty");
        }
        String schema = schemaName.trim().toUpperCase();
        try {
            List<String> tables = queryService.listTables(schema);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("schema", schema);
            result.put("tables", tables);
            if (tables.isEmpty()) {
                result.put("note", "no tables found");
            }
            return result;
        } catch (DataAccessException e) {
            throw new RuntimeException("database error: " + e.getMessage(), e);
        }
    }

    @Tool(description = "Get the DDL (CREATE TABLE statement) of a table in DM8. Both schemaName and tableName are case-insensitive.")
    public Map<String, Object> getTableDdl(
            @ToolParam(description = "Schema name (case-insensitive)") String schemaName,
            @ToolParam(description = "Table name (case-insensitive)") String tableName) {
        if (schemaName == null || schemaName.isBlank()) {
            throw new IllegalArgumentException("schemaName must not be empty");
        }
        if (tableName == null || tableName.isBlank()) {
            throw new IllegalArgumentException("tableName must not be empty");
        }
        String schema = schemaName.trim().toUpperCase();
        String table  = tableName.trim().toUpperCase();
        try {
            String ddl = queryService.getTableDdl(schema, table);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("schema", schema);
            result.put("table", table);
            result.put("ddl", ddl);
            return result;
        } catch (DataAccessException e) {
            throw new RuntimeException("database error: " + e.getMessage(), e);
        }
    }

    @Tool(description = "Get the column definitions of a table in DM8, including name, type, " +
            "length, precision, scale, nullable, and comment. Both schemaName and tableName are case-insensitive.")
    public Map<String, Object> describeTable(
            @ToolParam(description = "Schema name (case-insensitive)") String schemaName,
            @ToolParam(description = "Table name (case-insensitive)") String tableName) {
        if (schemaName == null || schemaName.isBlank()) {
            throw new IllegalArgumentException("schemaName must not be empty");
        }
        if (tableName == null || tableName.isBlank()) {
            throw new IllegalArgumentException("tableName must not be empty");
        }
        String schema = schemaName.trim().toUpperCase();
        String table  = tableName.trim().toUpperCase();
        try {
            List<Map<String, Object>> columns = queryService.describeTable(schema, table);
            if (columns.isEmpty()) {
                throw new IllegalArgumentException(
                        "table '" + schema + "." + table + "' not found");
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("schema", schema);
            result.put("table", table);
            result.put("columns", columns);
            return result;
        } catch (DataAccessException e) {
            throw new RuntimeException("database error: " + e.getMessage(), e);
        }
    }
}
