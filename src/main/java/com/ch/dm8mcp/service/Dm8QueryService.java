package com.ch.dm8mcp.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class Dm8QueryService {

    private static final String SQL_LIST_SCHEMAS =
            """
select name from SYS.SYSOBJECTS where type$='SCH'
""";

    private static final String SQL_LIST_TABLES =
            "SELECT TABLE_NAME FROM ALL_TABLES WHERE OWNER = ? ORDER BY TABLE_NAME";

    private static final String SQL_DESCRIBE_TABLE =
            "SELECT c.COLUMN_NAME, c.DATA_TYPE, c.DATA_LENGTH, c.DATA_PRECISION, " +
            "       c.DATA_SCALE, c.NULLABLE, c.COLUMN_ID, m.COMMENT$ AS COMMENTS " +
            "FROM ALL_TAB_COLUMNS c " +
            "LEFT JOIN SYS.SYSCOLUMNCOMMENTS m " +
            "    ON m.SCHNAME = c.OWNER " +
            "   AND m.TVNAME  = c.TABLE_NAME " +
            "   AND m.COLNAME = c.COLUMN_NAME " +
            "WHERE c.OWNER = ? AND c.TABLE_NAME = ? " +
            "ORDER BY c.COLUMN_ID";

    private static final String SQL_GET_DDL =
            "SELECT DBMS_METADATA.GET_DDL('TABLE', ?, ?) AS DDL FROM DUAL";

    private static final String SQL_LIST_TABLE_INDEXES =
            "SELECT i.INDEX_NAME, i.UNIQUENESS, i.INDEX_TYPE, c.COLUMN_NAME, c.COLUMN_POSITION " +
            "FROM ALL_INDEXES i " +
            "JOIN ALL_IND_COLUMNS c ON i.INDEX_NAME = c.INDEX_NAME AND i.OWNER = c.INDEX_OWNER " +
            "WHERE i.OWNER = ? AND i.TABLE_NAME = ? " +
            "ORDER BY i.INDEX_NAME, c.COLUMN_POSITION";

    private static final String SQL_LIST_TABLE_CONSTRAINTS =
            "SELECT c.CONSTRAINT_NAME, c.CONSTRAINT_TYPE, c.STATUS, c.R_CONSTRAINT_NAME, " +
            "       col.COLUMN_NAME, col.POSITION " +
            "FROM ALL_CONSTRAINTS c " +
            "JOIN ALL_CONS_COLUMNS col ON c.CONSTRAINT_NAME = col.CONSTRAINT_NAME AND c.OWNER = col.OWNER " +
            "WHERE c.OWNER = ? AND c.TABLE_NAME = ? " +
            "ORDER BY c.CONSTRAINT_TYPE, c.CONSTRAINT_NAME, col.POSITION";

    private static final String SQL_LIST_VIEWS =
            "SELECT VIEW_NAME, TEXT " +
            "FROM ALL_VIEWS " +
            "WHERE OWNER = ? " +
            "ORDER BY VIEW_NAME";

    private final JdbcTemplate jdbcTemplate;

    public Dm8QueryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> executeQuery(String sql) {
        return jdbcTemplate.queryForList(sql);
    }

    public List<String> listSchemas() {
        return jdbcTemplate.queryForList(SQL_LIST_SCHEMAS, String.class);
    }

    public List<String> listTables(String schemaName) {
        return jdbcTemplate.queryForList(SQL_LIST_TABLES, String.class, schemaName);
    }

    public String getTableDdl(String schemaName, String tableName) {
        return jdbcTemplate.queryForObject(SQL_GET_DDL, String.class, tableName, schemaName);
    }

    public List<Map<String, Object>> listTableIndexes(String schemaName, String tableName) {
        return jdbcTemplate.query(SQL_LIST_TABLE_INDEXES, (rs, rowNum) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("indexName",      rs.getString("INDEX_NAME"));
            row.put("unique",         "UNIQUE".equals(rs.getString("UNIQUENESS")));
            row.put("indexType",      rs.getString("INDEX_TYPE"));
            row.put("columnName",     rs.getString("COLUMN_NAME"));
            row.put("columnPosition", rs.getInt("COLUMN_POSITION"));
            return row;
        }, schemaName, tableName);
    }

    public List<Map<String, Object>> listTableConstraints(String schemaName, String tableName) {
        return jdbcTemplate.query(SQL_LIST_TABLE_CONSTRAINTS, (rs, rowNum) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("constraintName",  rs.getString("CONSTRAINT_NAME"));
            row.put("constraintType",  resolveConstraintType(rs.getString("CONSTRAINT_TYPE")));
            row.put("status",          rs.getString("STATUS"));
            row.put("refConstraint",   rs.getString("R_CONSTRAINT_NAME"));
            row.put("columnName",      rs.getString("COLUMN_NAME"));
            row.put("columnPosition",  rs.getInt("POSITION"));
            return row;
        }, schemaName, tableName);
    }

    public List<Map<String, Object>> listViews(String schemaName) {
        return jdbcTemplate.query(SQL_LIST_VIEWS, (rs, rowNum) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("viewName", rs.getString("VIEW_NAME"));
            row.put("text",     rs.getString("TEXT"));
            return row;
        }, schemaName);
    }

    private String resolveConstraintType(String code) {
        return switch (code) {
            case "P" -> "PRIMARY KEY";
            case "U" -> "UNIQUE";
            case "R" -> "FOREIGN KEY";
            case "C" -> "CHECK";
            default  -> code;
        };
    }

    public List<Map<String, Object>> describeTable(String schemaName, String tableName) {
        return jdbcTemplate.query(SQL_DESCRIBE_TABLE, (rs, rowNum) -> {
            Map<String, Object> col = new LinkedHashMap<>();
            col.put("name",      rs.getString("COLUMN_NAME"));
            col.put("type",      rs.getString("DATA_TYPE"));
            col.put("length",    rs.getObject("DATA_LENGTH"));
            col.put("precision", rs.getObject("DATA_PRECISION"));
            col.put("scale",     rs.getObject("DATA_SCALE"));
            col.put("nullable",  rs.getString("NULLABLE"));
            col.put("comment",   rs.getString("COMMENTS"));
            return col;
        }, schemaName, tableName);
    }
}
