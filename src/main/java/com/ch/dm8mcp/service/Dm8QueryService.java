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
            "       c.DATA_SCALE, c.NULLABLE, c.COLUMN_ID, m.COMMENTS " +
            "FROM ALL_TAB_COLUMNS c " +
            "LEFT JOIN ALL_COL_COMMENTS m " +
            "    ON m.OWNER       = c.OWNER " +
            "   AND m.TABLE_NAME  = c.TABLE_NAME " +
            "   AND m.COLUMN_NAME = c.COLUMN_NAME " +
            "WHERE c.OWNER = ? AND c.TABLE_NAME = ? " +
            "ORDER BY c.COLUMN_ID";

    private static final String SQL_GET_DDL =
            "SELECT DBMS_METADATA.GET_DDL('TABLE', ?, ?) AS DDL FROM DUAL";

    private final JdbcTemplate jdbcTemplate;

    public Dm8QueryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
