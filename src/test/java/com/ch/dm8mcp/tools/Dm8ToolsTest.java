package com.ch.dm8mcp.tools;

import com.ch.dm8mcp.service.Dm8QueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataRetrievalFailureException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Dm8ToolsTest {

    @Mock
    private Dm8QueryService queryService;

    private Dm8Tools tools;

    @BeforeEach
    void setUp() {
        tools = new Dm8Tools(queryService);
    }

    // --- listSchemas ---

    @Test
    void listSchemas_returnsMapWithSchemaList() {
        when(queryService.listSchemas()).thenReturn(List.of("APP", "TEST"));

        Map<String, Object> result = tools.listSchemas();

        assertThat(result.get("schemas")).isEqualTo(List.of("APP", "TEST"));
    }

    @Test
    void listSchemas_propagatesDbException() {
        when(queryService.listSchemas())
                .thenThrow(new DataRetrievalFailureException("connection refused"));

        assertThatThrownBy(() -> tools.listSchemas())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("database error:");
    }

    // --- listTables ---

    @Test
    void listTables_normalizesInputToUpperCase() {
        when(queryService.listTables("APP")).thenReturn(List.of("USER", "ORDER"));

        Map<String, Object> result = tools.listTables("app");

        assertThat(result.get("schema")).isEqualTo("APP");
        assertThat(result.get("tables")).isEqualTo(List.of("USER", "ORDER"));
    }

    @Test
    void listTables_throwsException_whenSchemaNameIsNull() {
        assertThatThrownBy(() -> tools.listTables(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("schemaName must not be empty");
    }

    @Test
    void listTables_throwsException_whenSchemaNameIsBlank() {
        assertThatThrownBy(() -> tools.listTables("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("schemaName must not be empty");
    }

    @Test
    void listTables_includesNoteField_whenNoTablesFound() {
        when(queryService.listTables("EMPTY")).thenReturn(List.of());

        Map<String, Object> result = tools.listTables("EMPTY");

        assertThat((List<?>) result.get("tables")).isEmpty();
        assertThat(result.get("note")).isNotNull();
    }

    // --- describeTable ---

    @Test
    void describeTable_normalizesInputAndReturnsColumns() {
        Map<String, Object> col = new LinkedHashMap<>();
        col.put("name", "ID");
        col.put("type", "NUMBER");
        when(queryService.describeTable("APP", "USER")).thenReturn(List.of(col));

        Map<String, Object> result = tools.describeTable("app", "user");

        assertThat(result.get("schema")).isEqualTo("APP");
        assertThat(result.get("table")).isEqualTo("USER");
        assertThat((List<?>) result.get("columns")).hasSize(1);
    }

    @Test
    void describeTable_throwsException_whenSchemaNameIsNull() {
        assertThatThrownBy(() -> tools.describeTable(null, "USER"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("schemaName must not be empty");
    }

    @Test
    void describeTable_throwsException_whenTableNameIsNull() {
        assertThatThrownBy(() -> tools.describeTable("APP", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("tableName must not be empty");
    }

    @Test
    void describeTable_throwsException_whenTableNameIsBlank() {
        assertThatThrownBy(() -> tools.describeTable("APP", "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("tableName must not be empty");
    }

    @Test
    void describeTable_throwsException_whenTableNotFound() {
        when(queryService.describeTable("APP", "GHOST")).thenReturn(List.of());

        assertThatThrownBy(() -> tools.describeTable("APP", "GHOST"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("APP.GHOST");
    }

    @Test
    void describeTable_propagatesDbException() {
        when(queryService.describeTable("APP", "USER"))
                .thenThrow(new DataRetrievalFailureException("timeout"));

        assertThatThrownBy(() -> tools.describeTable("APP", "USER"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("database error:");
    }
}
