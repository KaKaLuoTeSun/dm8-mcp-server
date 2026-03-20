package com.ch.dm8mcp;

import com.ch.dm8mcp.service.Dm8QueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class Dm8QueryServiceTest {

    @Autowired
    private Dm8QueryService service;

    @Test
    void listSchemas_realDb() {
        System.out.println(service.listSchemas());
    }
}