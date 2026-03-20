# dm8-mcp-server

基于 [Spring AI MCP](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html) 实现的达梦数据库（DM8）MCP Server，支持通过 AI 助手（如 Claude）查询 DM8 数据库的 Schema、表结构等信息。

## 功能

| 工具 | 描述 |
|------|------|
| `listSchemas` | 列出数据库中所有 Schema |
| `listTables` | 列出指定 Schema 下的所有表名 |
| `describeTable` | 查询指定表的列信息（列名、类型、长度、是否可空、注释） |
| `getTableDdl` | 获取指定表的 DDL 建表语句 |
| `listTableIndexes` | 查询指定表的索引信息（索引名、是否唯一、索引类型、索引列） |
| `listTableConstraints` | 查询指定表的约束信息（主键/唯一键/外键/检查约束） |
| `listViews` | 列出指定 Schema 下的所有视图及其定义 |
| `executeQuery` | 执行只读 SELECT 查询（禁止任何写操作） |

## 环境要求

- Java 17+
- Maven 3.6+
- 达梦数据库 DM8
- DM8 JDBC 驱动（`DmJdbcDriver18.jar`）

## 构建步骤

### 1. 克隆项目

```bash
git clone https://github.com/KaKaLuoTeSun/dm8-mcp-server.git
cd dm8-mcp-server
```

### 2. 放入 DM8 驱动

将 `DmJdbcDriver18.jar` 复制到项目 `lib/` 目录下：

```
dm8-mcp-server/
└── lib/
    └── DmJdbcDriver18.jar   ← 放这里
```

> 驱动文件可从达梦数据库安装目录的 `drivers/jdbc/` 下找到。

### 3. 打包

```bash
mvn clean package -DskipTests
```

打包成功后，在 `target/` 目录下生成 `dm8-mcp-1.0.0.jar`。

## 配置 Claude Desktop

编辑 Claude Desktop 配置文件：

- **Windows**：`%APPDATA%\Claude\claude_desktop_config.json`
- **macOS**：`~/Library/Application Support/Claude/claude_desktop_config.json`

在 `mcpServers` 中添加以下配置：

```json
{
  "mcpServers": {
    "dm8": {
      "command": "java",
      "args": ["-jar", "C:\\path\\to\\dm8-mcp-1.0.0.jar"],
      "env": {
        "DM8_URL": "jdbc:dm://127.0.0.1:5236",
        "DM8_USERNAME": "SYSDBA",
        "DM8_PASSWORD": "your_password"
      }
    }
  }
}
```

> 将 `args` 中的路径替换为实际的 jar 文件路径，将 `env` 中的连接信息替换为实际的数据库连接信息。

配置完成后，**重启 Claude Desktop** 使配置生效。

## 使用示例

配置成功后，可以在 Claude 对话中直接提问：

- "列出数据库中所有的 schema"
- "查看 DMHR 下有哪些表"
- "描述一下 DMHR.CITY 表的结构"
- "获取 DMHR.CITY 表的 DDL"
- "查看 DMHR.DEPARTMENT 表有哪些索引"
- "查看 DMHR.EMPLOYEE 表的约束信息"
- "列出 DMHR 下所有视图"
- "执行 SELECT * FROM DMHR.CITY WHERE ROWNUM <= 10"

## 技术栈

- Spring Boot 3.3.5
- Spring AI 1.1.0（MCP Server Starter）
- Spring JDBC
- 达梦 DM8 JDBC Driver
