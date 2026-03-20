package com.ch.dm8mcp;

import com.ch.dm8mcp.service.Dm8QueryService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("集成测试，需要真实 DM8 连接，不在 CI/打包阶段运行")
@SpringBootTest
class Dm8QueryServiceTest {

    @Autowired
    private Dm8QueryService service;

    @Test
    void listSchemas_realDb() {
        System.out.println(service.listSchemas());
    }
}