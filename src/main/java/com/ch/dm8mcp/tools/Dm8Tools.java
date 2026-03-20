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

    @Tool(name = "listSchemas", description = "列出达梦数据库（DM8）中所有的 Schema")
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

    @Tool(name = "listTables", description = "列出指定 Schema 下的所有表名，Schema 名称不区分大小写")
    public Map<String, Object> listTables(
            @ToolParam(description = "Schema 名称，不区分大小写") String schemaName) {
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

    @Tool(name = "getTableDdl", description = "获取达梦数据库（DM8）中指定表的 DDL 建表语句，Schema 和表名不区分大小写")
    public Map<String, Object> getTableDdl(
            @ToolParam(description = "Schema 名称，不区分大小写") String schemaName,
            @ToolParam(description = "表名，不区分大小写") String tableName) {
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

    @Tool(name = "describeTable", description = "查询达梦数据库（DM8）指定表的列信息，包含列名、数据类型、长度、精度、小数位、是否可空、注释，Schema 和表名不区分大小写")
    public Map<String, Object> describeTable(
            @ToolParam(description = "Schema 名称，不区分大小写") String schemaName,
            @ToolParam(description = "表名，不区分大小写") String tableName) {
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
