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
        String schema = normalize(schemaName, "schemaName");
        try {
            List<String> tables = queryService.listTables(schema);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("schema", schema);
            result.put("tables", tables);
            if (tables.isEmpty()) {
                result.put("note", "该 Schema 下未找到表");
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
        String schema = normalize(schemaName, "schemaName");
        String table  = normalize(tableName,  "tableName");
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

    @Tool(name = "listTableIndexes", description = "查询达梦数据库（DM8）指定表的索引信息，包含索引名、是否唯一、索引类型、索引列及列顺序，Schema 和表名不区分大小写")
    public Map<String, Object> listTableIndexes(
            @ToolParam(description = "Schema 名称，不区分大小写") String schemaName,
            @ToolParam(description = "表名，不区分大小写") String tableName) {
        String schema = normalize(schemaName, "schemaName");
        String table  = normalize(tableName,  "tableName");
        try {
            List<Map<String, Object>> indexes = queryService.listTableIndexes(schema, table);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("schema",  schema);
            result.put("table",   table);
            result.put("indexes", indexes);
            return result;
        } catch (DataAccessException e) {
            throw new RuntimeException("database error: " + e.getMessage(), e);
        }
    }

    @Tool(name = "listTableConstraints", description = "查询达梦数据库（DM8）指定表的约束信息，包含约束名、约束类型（主键/唯一键/外键/检查约束）、约束列，Schema 和表名不区分大小写")
    public Map<String, Object> listTableConstraints(
            @ToolParam(description = "Schema 名称，不区分大小写") String schemaName,
            @ToolParam(description = "表名，不区分大小写") String tableName) {
        String schema = normalize(schemaName, "schemaName");
        String table  = normalize(tableName,  "tableName");
        try {
            List<Map<String, Object>> constraints = queryService.listTableConstraints(schema, table);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("schema",      schema);
            result.put("table",       table);
            result.put("constraints", constraints);
            return result;
        } catch (DataAccessException e) {
            throw new RuntimeException("database error: " + e.getMessage(), e);
        }
    }

    @Tool(name = "listViews", description = "列出达梦数据库（DM8）指定 Schema 下的所有视图及其定义，Schema 名称不区分大小写")
    public Map<String, Object> listViews(
            @ToolParam(description = "Schema 名称，不区分大小写") String schemaName) {
        String schema = normalize(schemaName, "schemaName");
        try {
            List<Map<String, Object>> views = queryService.listViews(schema);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("schema", schema);
            result.put("views",  views);
            if (views.isEmpty()) {
                result.put("note", "该 Schema 下未找到视图");
            }
            return result;
        } catch (DataAccessException e) {
            throw new RuntimeException("database error: " + e.getMessage(), e);
        }
    }

    @Tool(name = "describeTable", description = "查询达梦数据库（DM8）指定表的列信息，包含列名、数据类型、长度、精度、小数位、是否可空、注释，Schema 和表名不区分大小写")
    public Map<String, Object> describeTable(
            @ToolParam(description = "Schema 名称，不区分大小写") String schemaName,
            @ToolParam(description = "表名，不区分大小写") String tableName) {
        String schema = normalize(schemaName, "schemaName");
        String table  = normalize(tableName,  "tableName");
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

    @Tool(name = "executeQuery", description = "在达梦数据库（DM8）中执行只读 SQL 查询，仅允许 SELECT 语句，禁止任何写操作（INSERT/UPDATE/DELETE/DROP 等）")
    public Map<String, Object> executeQuery(
            @ToolParam(description = "只读 SQL 查询语句，必须以 SELECT 开头") String sql) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("sql must not be empty");
        }
        String trimmed = sql.trim();
        if (!trimmed.toUpperCase().startsWith("SELECT")) {
            throw new IllegalArgumentException("只允许执行 SELECT 查询语句");
        }
        String upper = trimmed.toUpperCase();
        for (String keyword : new String[]{"INSERT", "UPDATE", "DELETE", "DROP", "TRUNCATE", "ALTER", "CREATE", "GRANT", "REVOKE", "EXEC", "EXECUTE", "CALL"}) {
            if (upper.contains(keyword)) {
                throw new IllegalArgumentException("SQL 中包含禁止的关键字: " + keyword);
            }
        }
        try {
            List<Map<String, Object>> rows = queryService.executeQuery(trimmed);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("rowCount", rows.size());
            result.put("rows", rows);
            return result;
        } catch (DataAccessException e) {
            throw new RuntimeException("database error: " + e.getMessage(), e);
        }
    }

    private String normalize(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be empty");
        }
        return value.trim().toUpperCase();
    }
}
