package jdbcService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import sec.siis.jdbc.config.JdbcConfig;
import sec.siis.jdbc.config.JsonConfigLoader;
import sec.siis.jdbc.execution.JdbcExecutionResult;
import sec.siis.jdbc.execution.JdbcExecutor;
import sec.siis.jdbc.util.CommonJsonUtil;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JdbcExecutorTest {
    
    private DataSource ds;
    
    @BeforeEach
    void setUp() throws Exception {
        ds = new SimpleDataSource(
            "jdbc:oracle:thin:@localhost:1521:XE", 
            "C##siis", 
            "siis1234"
        );
    }
    
    @Test
    @Order(1)
    void deleteOnlyTestNoParam() throws Exception {
        // 1. YAML → JdbcConfig
        String yaml = Files.readString(
            Path.of("src/test/resources/DeleteOnlyNoParam.yaml")
        );
        JdbcConfig ifcfg = JsonConfigLoader.loadFromString(yaml);
        
        // 2. Input JSON
        String inputJson = Files.readString(
            Path.of("src/test/resources/DeleteOnlyNoParam.json")
        );
        
        // 3. Execute
        JdbcExecutor executor = new JdbcExecutor(ds,"junitMsgID");
        JdbcExecutionResult result=new JdbcExecutionResult();
        result = executor.executeJdbc(ifcfg, inputJson,result);
        
        // 4. Assert
        assertFalse(result.hasFailure());
        //assertEquals(1, result.getOperations().size());
    }

    @Test
    @Order(1)
    void deleteOnlyParamIn() throws Exception {
        // 1. YAML → JdbcConfig
        String yaml = Files.readString(
            Path.of("src/test/resources/DeleteOnlyParamIn.yaml")
        );
        JdbcConfig ifcfg = JsonConfigLoader.loadFromString(yaml);
        
        // 2. Input JSON
        String inputJson = Files.readString(
            Path.of("src/test/resources/DeleteOnlyParamIn.json")
        );
        
        // 3. Execute
        JdbcExecutor executor = new JdbcExecutor(ds,"junitMsgID");
        JdbcExecutionResult result=new JdbcExecutionResult();
        result = executor.executeJdbc(ifcfg, inputJson,result);
        
        // 4. Assert
        assertFalse(result.hasFailure());
        //assertEquals(1, result.getOperations().size());
    }
    
    @Test
    @Order(2)    
    void deleteInsert() throws Exception {
        // 1. YAML → JdbcConfig
        String yaml = Files.readString(
            Path.of("src/test/resources/DeleteInsert.yaml")
        );
        JdbcConfig ifcfg = JsonConfigLoader.loadFromString(yaml);
        
        // 2. Input JSON
        String inputJson = Files.readString(
            Path.of("src/test/resources/DeleteInsert.json")
        );
        
        // 3. Execute
        JdbcExecutor executor = new JdbcExecutor(ds,"junitMsgID");
        JdbcExecutionResult result=new JdbcExecutionResult();
        result = executor.executeJdbc(ifcfg, inputJson,result);
        
        // 4. Assert
        assertFalse(result.hasFailure());
        //assertEquals(1, result.getOperations().size());
    }    

    @Test
    @Order(3)    
    void deleteInsertProcedure() throws Exception {
        // 1. YAML → JdbcConfig
        String yaml = Files.readString(
            Path.of("src/test/resources/DeleteInsertProcedure.yaml")
        );
        JdbcConfig ifcfg = JsonConfigLoader.loadFromString(yaml);
        
        // 2. Input JSON
        String inputJson = Files.readString(
            Path.of("src/test/resources/DeleteInsertProcedure.json")
        );
        
        // 3. Execute
        JdbcExecutor executor = new JdbcExecutor(ds,"junitMsgID");
        JdbcExecutionResult result=new JdbcExecutionResult();
        result = executor.executeJdbc(ifcfg, inputJson,result);
        
        // 4. Assert
        assertFalse(result.hasFailure());
        //assertEquals(1, result.getOperations().size());
    } 
    
    @Test
    @Order(4)    
    void deleteMergeProcedure() throws Exception {
        // 1. YAML → JdbcConfig
        String yaml = Files.readString(
            Path.of("src/test/resources/DeleteMergeProcedure.yaml")
        );
        JdbcConfig ifcfg = JsonConfigLoader.loadFromString(yaml);
        
        // 2. Input JSON
        String inputJson = Files.readString(
            Path.of("src/test/resources/DeleteMergeProcedure.json")
        );
        
        // 3. Execute
        JdbcExecutor executor = new JdbcExecutor(ds,"junitMsgID");
        JdbcExecutionResult result=new JdbcExecutionResult();
        result = executor.executeJdbc(ifcfg, inputJson,result);
        
        // 4. Assert
        assertFalse(result.hasFailure());
        //assertEquals(1, result.getOperations().size());
    }
    @Test
    @Order(5)    
    void insertMasterDetail() throws Exception {
        // 1. YAML → JdbcConfig
        String yaml = Files.readString(
            Path.of("src/test/resources/InsertMasterDetail.yaml")
        );
        JdbcConfig ifcfg = JsonConfigLoader.loadFromString(yaml);
        
        // 2. Input JSON
        String inputJson = Files.readString(
            Path.of("src/test/resources/InsertMasterDetail.json")
        );
        
        // 3. Execute
        JdbcExecutor executor = new JdbcExecutor(ds,"junitMsgID");
        JdbcExecutionResult result=new JdbcExecutionResult();
        result = executor.executeJdbc(ifcfg, inputJson,result);
        
        // 4. Assert
        assertFalse(result.hasFailure());
        //assertEquals(1, result.getOperations().size());
    } 
    
    @Test
    @Order(6)    
    void insertMasterDetailRow() throws Exception {
        // 1. YAML → JdbcConfig
        String yaml = Files.readString(
            Path.of("src/test/resources/InsertMasterDetailRow.yaml")
        );
        JdbcConfig ifcfg = JsonConfigLoader.loadFromString(yaml);
        
        // 2. Input JSON
        String inputJson = Files.readString(
            Path.of("src/test/resources/InsertMasterDetailRow.json")
        );
        
        // 3. Execute
        JdbcExecutor executor = new JdbcExecutor(ds,"junitMsgID");
        JdbcExecutionResult result=new JdbcExecutionResult();
        result = executor.executeJdbc(ifcfg, inputJson,result);
        
        // 4. Assert
        assertFalse(result.hasFailure());
        //assertEquals(1, result.getOperations().size());
    }  
    
    @Test
    @Order(7)    
    void insertMasterDetailRowError() throws Exception {
        // 1. YAML → JdbcConfig
        String yaml = Files.readString(
            Path.of("src/test/resources/InsertMasterDetailRow.yaml")
        );
        JdbcConfig ifcfg = JsonConfigLoader.loadFromString(yaml);
        
        // 2. Input JSON
        String inputJson = Files.readString(
            Path.of("src/test/resources/InsertMasterDetailRowError.json")
        );
        
        // 3. Execute
        JdbcExecutor executor = new JdbcExecutor(ds,"junitMsgID");
        JdbcExecutionResult result=new JdbcExecutionResult();
        result = executor.executeJdbc(ifcfg, inputJson,result);
        
        System.out.println(CommonJsonUtil.toJson(result));
        
        // 4. Assert
        assertFalse(result.hasFailure());
        //assertEquals(1, result.getOperations().size());
    }  
    
    @Test
    @Order(8)    
    void deleteInsertRow() throws Exception {
        // 1. YAML → JdbcConfig
        String yaml = Files.readString(
            Path.of("src/test/resources/DeleteInsertRow.yaml")
        );
        JdbcConfig ifcfg = JsonConfigLoader.loadFromString(yaml);
        
        // 2. Input JSON
        String inputJson = Files.readString(
            Path.of("src/test/resources/DeleteInsertRowError.json")
        );
        
        // 3. Execute
        JdbcExecutor executor = new JdbcExecutor(ds,"junitMsgID");
        JdbcExecutionResult result=new JdbcExecutionResult();
        result = executor.executeJdbc(ifcfg, inputJson,result);
        
        System.out.println(CommonJsonUtil.toJson(result));
        
        // 4. Assert
        assertFalse(result.hasFailure());
        //assertEquals(1, result.getOperations().size());
    }
    
    @Test
    @Order(9)    
    void selectParam() throws Exception {
        // 1. YAML → JdbcConfig
        String yaml = Files.readString(
            Path.of("src/test/resources/SelectParam.yaml")
        );
        JdbcConfig ifcfg = JsonConfigLoader.loadFromString(yaml);
        
        // 2. Input JSON
        String inputJson = Files.readString(
            Path.of("src/test/resources/SelectParam.json")
        );
        
        
        // 3. Execute
        JdbcExecutor executor = new JdbcExecutor(ds,"junitMsgID");
        JdbcExecutionResult result=new JdbcExecutionResult();
        result = executor.executeJdbc(ifcfg, inputJson,result);
        
        System.out.println(CommonJsonUtil.toJson(result));
        
        // 4. Assert
        assertFalse(result.hasFailure());
        //assertEquals(1, result.getOperations().size());
    }        
    /**
     * 순수 JDBC 기반 Simple DataSource 구현
     */
    private static class SimpleDataSource implements DataSource {
        
        private final String url;
        private final String username;
        private final String password;
        
        public SimpleDataSource(String url, String username, String password) {
            this.url = url;
            this.username = username;
            this.password = password;
        }
        
        @Override
        public Connection getConnection() throws SQLException {
            return DriverManager.getConnection(url, username, password);
        }
        
        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            return DriverManager.getConnection(url, username, password);
        }
        
        @Override
        public PrintWriter getLogWriter() throws SQLException {
            return null;
        }
        
        @Override
        public void setLogWriter(PrintWriter out) throws SQLException {
        }
        
        @Override
        public void setLoginTimeout(int seconds) throws SQLException {
        }
        
        @Override
        public int getLoginTimeout() throws SQLException {
            return 0;
        }
        
        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException();
        }
        
        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            throw new SQLException("Not supported");
        }
        
        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false;
        }
    }
}