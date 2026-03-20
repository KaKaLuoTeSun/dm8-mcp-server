package com.ch.dm8mcp.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Dm8QueryServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private Dm8QueryService service;

    @Test
    void listSchemas_returnsSchemaNames() {
        when(jdbcTemplate.queryForList(anyString(), eq(String.class)))
                .thenReturn(List.of("APP", "TEST"));

        assertThat(service.listSchemas()).containsExactly("APP", "TEST");
    }

    @Test
    void listSchemas_returnsEmptyList_whenNoUserSchemas() {
        when(jdbcTemplate.queryForList(anyString(), eq(String.class)))
                .thenReturn(List.of());

        assertThat(service.listSchemas()).isEmpty();
    }

    @Test
    void listTables_returnsTableNamesForSchema() {
        when(jdbcTemplate.queryForList(anyString(), eq(String.class), eq("APP")))
                .thenReturn(List.of("USER", "ORDER"));

        assertThat(service.listTables("APP")).containsExactly("USER", "ORDER");
    }

    @Test
    void listTables_returnsEmptyList_whenSchemaHasNoTables() {
        when(jdbcTemplate.queryForList(anyString(), eq(String.class), eq("EMPTY_SCHEMA")))
                .thenReturn(List.of());

        assertThat(service.listTables("EMPTY_SCHEMA")).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void describeTable_returnsColumnMaps() {
        Map<String, Object> col = Map.of(
                "name", "ID", "type", "NUMBER", "length", 22,
                "precision", 10, "scale", 0, "nullable", "N", "comment", "主键"
        );
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq("APP"), eq("USER")))
                .thenReturn(List.of(col));

        List<Map<String, Object>> result = service.describeTable("APP", "USER");

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsEntry("name", "ID");
    }

    @Test
    @SuppressWarnings("unchecked")
    void describeTable_returnsEmptyList_whenTableDoesNotExist() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq("APP"), eq("GHOST")))
                .thenReturn(List.of());

        assertThat(service.describeTable("APP", "GHOST")).isEmpty();
    }
}
